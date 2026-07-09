# Dental Lab Service

---
> ## О проекте

Веб-сервис для учёта заказов зубных техников. Позволяет вести каталог работ, управлять заказами, хранить фотографии выполненных работ, отслеживать статусы выполнения и формировать отчёты за выбранный период.

Проект был создан для ежедневной документации реального рабочего процесса зубных техников.

### Основные возможности

- Ведение персонального каталога зуботехнических работ с указанием стоимости.
- Запись и управление заказами (клиника, пациент, работы, даты, комментарии, статус).
- Загрузка и хранение фотографий выполненных работ.
- Расчёт дохода по месяцам с учётом статуса заказов.
- Экспорт и импорт отчётов в формате XLSX.
- Сортировка заказов по месяцам.
- Привязка Telegram-аккаунта к профилю пользователя и доступ к сервису через Telegram бот.
- Рассылка уведомлений о заказах на следующий день через Telegram или Email.
- Аутентификация и авторизация пользователей через Keycloak.

---
> ## Simple-Release версия

Это ветка упрощённой версии системы, предназначенная для развёртывания на VDS с ограниченными ресурсами.

**Особенности:**

- отсутствие централизованной наблюдаемости
- отсутствие брокера сообщений;
- отсутствие cloud-инфраструктуры;
- упрощённая схема взаимодействия сервисов;
- оптимизированное потребление памяти и вычислительных ресурсов.

Эта версия приложения развёрнута на VDS и доступна через домен [dental-lab-service](https://dental-lab-service.ru) с SSL-сертификатом и прокси Nginx.

---
### [Open API](https://dental-lab-service.ru/docs/swagger-ui/index.html)

_Swagger UI поддерживает OAuth2 Authorization Code Flow (PKCE).
 Для авторизации достаточно выполнить вход через Keycloak, **client secret** пользователю не требуется_

---
#### [main версия 👈](https://github.com/Stas-Kuprienko/dental-lab-service)

---
> ## Технологический стек

- Java 21
- Spring Boot 3.5
- Spring Security
- Apache POI
- Keycloak

---
**Хранение данных**

- PostgreSQL
- Flyway
- Redis
- MinIO S3

---
**Интеграции**

- Telegram Bot API
- Email

---
**UI**

- Thymeleaf

---
**Инфраструктура**

- Docker
- Nginx

---
**Сборка**

- Gradle

---
> ## Архитектура

Система состоит из нескольких независимых компонентов:
пользовательского `веб-интерфейса`, `Telegram-бота`, `backend-сервиса` и `инфраструктурных сервисов`.

Бизнес-логика сосредоточена в `Dental-Lab-Service`, а взаимодействие между компонентами осуществляется по _HTTP_.
Аутентификация и авторизация осуществляется через сервис `Keycloak`.

<img src="./docs/diagram.png">

---

[Файл диаграммы](docs/diagram.md)

---
> ## Технические решения

### Динамическая модель данных таблиц

Каждый пользователь имеет собственный каталог изделий (ProductMap),
 который определяет набор доступных типов работ и их стоимость. На его основе динамически формируются:

* таблицы работ в Web UI (Thymeleaf);
* Excel-отчёты (XLSX Export);
* импорт данных из Excel (XLSX Import).

Благодаря этому разные пользователи могут работать с различающимися наборами изделий без изменения структуры базы данных и программного кода.

Импорт поддерживает загрузку Excel-файлов (по определённому шаблону), валидирует колонки и автоматически сопоставляет данные.

---
### Stateful Telegram Bot

Для поддержки многошаговых сценариев взаимодействия реализован механизм ChatSession.
```java
public class ChatSession {

    private Long chatId;
    private UUID userId;
    private Context context;

    public static class Context {

        private BotCommands command;
        private Map<String, String> attributes;
        private int step;
    }
}
```
При каждом запросе CommandHandler сохраняет в сессию текущую команду, шаг (у каждого CommandHandler свои шаги) и по необходимости атрибуты.

```java
    private SendMessage input(ChatSession session, Locale locale, String messageText, int messageId) {
    
        NewDentalWork newDentalWork = ... // парсинг сообщения в данные для объекта нового заказа
        InlineKeyboardMarkup keyboardMarkup = ... // создание ответа со списком видов работ
        
        //сохраняем в сессию введённые пользователем данные в формате JSON
        session.addAttribute(Attributes.NEW_DENTAL_WORK.name(), newDentalWorkAsString(newDentalWork));
        
        //устанавливаем текущую команду
        session.setCommand(BotCommands.NEW_DENTAL_WORK);
        
        //устанавливаем в сессию следующий шаг
        session.setStep(Steps.SELECT_PRODUCT_TYPE.ordinal());
        
        //сохраняем в Redis
        chatSessionService.save(session);
        
        return createSendMessage(session.getChatId(), text, keyboardMarkup);
    }
```
При новом запросе будет работать следующий шаг с использованием сохранённых в сессии данных.

---

### Отсутствие брокера сообщений

Из-за ограниченных ресурсов VDS пришлось отказаться от брокера сообщений.
Отправка событий реализована через обычные HTTP REST-вызовы (RestClient) и завёрнуто в CompletableFuture.
Добавлена логика retry, на случай сбоев.

```java
    public void send(EventMessage message) {
        CompletableFuture
                .supplyAsync(() -> postRequest(message), executorService
                ).thenAccept(v ->
                        log.info("The message '{}' was sent to Telegram-bot service successfully", message.getId())
                ).exceptionally(thr -> {
                    int delaySeconds = 1;
                    LockSupport.parkNanos(Duration.of(delaySeconds, ChronoUnit.SECONDS).toNanos());
                    retry(message, retries, delaySeconds, thr);
                    return null;
                });
    }

    private void retry(EventMessage message, int retryI, int delaySeconds, Throwable throwable) {
        if (retryI > 0) {
            log.warn("Failure to send the message '%s' to Telegram-bot service, left %d retries".formatted(message.getId(), retryI), throwable);
            try {
                postRequest(message);
                log.info("The message '{}' was sent to Telegram-bot service successfully", message.getId());
            } catch (Throwable thr) {
                delaySeconds++;
                LockSupport.parkNanos(Duration.of(delaySeconds, ChronoUnit.SECONDS).toNanos());
                retry(message, retryI - 1, delaySeconds, thr);
            }
        } else {
            log.error("Failure to send the message '%s' to Telegram-bot service".formatted(message.getId()), throwable);
        }
    }
```

---
### Keycloak вместо собственной реализации авторизации

Для аутентификации и авторизации используется Keycloak.

Причины выбора:

- поддержка OAuth2 и OpenID Connect;
- централизованное управление пользователями;
- поддержка ролей и политик доступа;
- готовая интеграция со Spring Security.

Это позволяет сосредоточиться на бизнес-логике приложения, не реализуя собственный Identity Provider.

> ## Скриншоты

| **Привязка Telegram к профилю сервиса**                              | --- | **Создание вида работы в каталог**                                   |
|:---------------------------------------------------------------------|-----|:---------------------------------------------------------------------|
| <img src="./docs/screen/video_2026-07-08_19-42-23.gif" height="720"> |     | <img src="./docs/screen/video_2026-07-08_19-42-31.gif" height="720"> |
---

**Динамическая таблица и карта работ**

<img src="./docs/screen/video_2026-07-08_19-42-39.gif" weight="480">

---

**Импорт excel файлов**

<img src="./docs/screen/video_2026-07-08_19-42-42 (1).gif" weight="480">

---

**Отображение статусов в таблице**

<img src="./docs/screen/video_2026-07-08_19-42-35.gif" weight="480">

---
> ## Запуск проекта

**Требования**

- RAM - 4+ GB
- CPU - 2+ cores
- Disk - 10+ GB
- Docker engine - version 26+
- Docker compose - V2+
- Java 21 (для запуска проекта через IDE)

#### Моменты, которые нужно учесть 

* При запуске приложения Dental-Lab-service будет включён режим `mock-mode` для рассылки.
 Чтобы включить реальную рассылку по Email, нужно настроить smtp и указать данные в переменных! (`application.yaml: project.mail.*`)
 И нужно выключить режим `mock-mode` (`project.mailing.mock-mode.email: false`)

* Для запуска Telegram-bot требуется создать собственного бота через [BotFather](https://t.me/BotFather) и указать bot-name и token в переменных! (`application.yaml: project.variables.telegram.*`).
 Для включения рассылки через Telegram, нужно отключить режим `mock-mode` в приложении `dental-service` (`project.mailing.mock-mode.telegram: false`).
* Также для запуска Telegram-bot может понадобиться настройка параметров прокси для доступа через VPN! (`application.yaml: project.telegram.proxy.*`)

* Но Telegram-bot можно не запускать — приложение полностью работоспособно без него.

**Локальный запуск**

* Скачать проект [в виде архива](https://github.com/Stas-Kuprienko/dental-lab-service/archive/refs/heads/simple-release.zip) или через `git clone https://github.com/Stas-Kuprienko/dental-lab-service.git`.

1. Если скачивали Zip архив: Извлечь из архива проект, перейти в директорию `dental-lab-service`.
2. Если клонировали через Git: Перейти в директорию `cd dental-lab-service`, затем переключить ветку `git checkout simple-release`

* Для запуска через IDE, запустите инфраструктурные компоненты через [docker-compose](docker-compose.local.yml)
 `docker compose -f docker-compose.local.yml -d up` с укороченной и упрощённой конфигурацией.
 Затем запустите `dental-lab-service`, `ui-mvc-application` и `telegram-bot` (если указали bot-name и токен) через IDE.

* Открыть [страницу UI](http://localhost:8081/) локального приложения или [open API](http://localhost:8082/docs/index.html).
 [Страница Keycloak](http://localhost:8080/) админ консоли.

**Запуск на VDS через домен с SSL сертификатом**

* Скачать на сервер проект [в виде архива](https://github.com/Stas-Kuprienko/dental-lab-service/archive/refs/heads/simple-release.zip) или через `git clone https://github.com/Stas-Kuprienko/dental-lab-service.git`.
* Подключить к серверу домен, получить SSL сертификат, переместить сертификат в директорию `dental-lab-service`.
* Указать домен в [файле Nginx](./config/nginx/conf.d/dental-lab-service.conf) в переменных `server.server_name:`.
* Получить данные для smtp и указать в `dental-lab-service:application.yaml:project.mail.*` или в `environment` переменных в [файле docker-compose](./docker-compose.yml).
* Также нужно проверить конфигурацию `mock-mode` для рассылок ([см. "Моменты, которые нужно учесть"](#моменты-которые-нужно-учесть-))
* Создать файл `.env` и указать переменные для инфраструктурных компонентов ([см. пример](./.env.example))
* Настроить Telegram-bot ([см. "Моменты, которые нужно учесть"](#моменты-которые-нужно-учесть-)) или обойтись запуском без этого приложения.
* Запустить проект `docker compose -d up`, если без Telegram-bot, то `docker compose -d up nginx ui-mvc-app` 

---
> ## Автор

### Станислав Куприенко

[GitHub](https://github.com/Stas-Kuprienko)

[Telegram](@Stas_Kuprienko) 