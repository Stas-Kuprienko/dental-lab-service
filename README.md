# Dental Lab Service

---
> ## О проекте

Веб-сервис для учёта заказов зубных техников. Позволяет вести каталог работ, управлять заказами, хранить фотографии выполненных работ, отслеживать статусы выполнения и формировать отчёты за выбранный период.

Проект был создан для помощи в документации реального рабочего процесса зубных техников.

### Основные возможности

- Ведение персонального каталога зуботехнических работ с указанием стоимости.
- Создание и управление заказами (клиника, пациент, работы, даты, комментарии, статус).
- Загрузка и хранение фотографий выполненных работ.
- Расчёт дохода по месяцам с учётом статуса заказов.
- Экспорт отчётов в формате XLSX.
- Сортировка заказов по месяцам.
- Привязка Telegram-аккаунта к профилю пользователя и доступ к сервису через Telegram бот.
- Рассылка уведомлений о заказах на следующий день через Telegram или Email.
- Аутентификация и авторизация пользователей через Keycloak.

---
> ## Версии проекта

Репозиторий содержит две основные ветки:

#### 1. `simple-release`

Упрощённая версия системы, предназначенная для развёртывания на VDS с ограниченными ресурсами.

**Особенности:**

- отсутствие observability
- отсутствие брокера сообщений;
- отсутствие cloud-инфраструктуры;
- упрощённая схема взаимодействия сервисов;
- оптимизированное потребление памяти и вычислительных ресурсов.

Эта версия приложения развёрнута на VDS и доступна через домен [dental-lab-service](https://dental-lab-service.ru) с SSL-сертификатом и прокси Nginx.
###### [перейти к версии 👈](https://github.com/Stas-Kuprienko/dental-lab-service/tree/simple-release)

---
#### 2. `main`

Демонстрационная версия проекта, предназначенная для отображения архитектурных и инфраструктурных решений.

**Дополнительно включает:**

- экосистему Spring Cloud;
- брокер сообщений RabbitMQ;
- инструменты мониторинга и трассировки;
- элементы отказоустойчивости приложения.

Данная ветка демонстрирует подход к построению распределённых приложений на базе Spring Boot и Spring Cloud.

---
> ## Технологический стек

- Java 21
- Spring Boot 3.5
- Spring Cloud 2025
- Spring Security
- API Gateway
- Resilience4J
- OpenFeign
- Apache POI
- Keycloak
- RabbitMQ

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
**Observability**

- Micrometer
- Prometheus
- Loki
- Tempo
- Grafana

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
Бизнес-логика сосредоточена в `Dental-Lab-Service`, а взаимодействие между компонентами осуществляется через `API Gateway` и `RabbitMQ`.
Аутентификация и авторизация осуществляется через сервис `Keycloak`.

<img src="./docs/diagram.png">

---

[Файл диаграммы](docs/diagram.md)

---
> ## Технические решения

### RabbitMQ вместо Apache Kafka

Для асинхронного взаимодействия между сервисами выбран RabbitMQ.

Причины выбора:

- небольшой объём сообщений;
- отсутствие необходимости в event streaming;
- простота эксплуатации и меньшие требования к ресурсам;
- поддержка маршрутизации сообщений и Dead Letter Queue.

Для данного проекта использование Apache Kafka являлось бы избыточным решением.

---
### Keycloak вместо собственной реализации авторизации

Для аутентификации и авторизации используется Keycloak.

Причины выбора:

- поддержка OAuth2 и OpenID Connect;
- централизованное управление пользователями;
- поддержка ролей и политик доступа;
- готовая интеграция со Spring Security.

Это позволяет сосредоточиться на бизнес-логике приложения, не реализуя собственный Identity Provider.

---
### Dead Letter Queue

Для обработки ошибок доставки сообщений используется отдельная DLQ очередь.

Сценарий:

1. Dental Lab Service отправляет сообщение.
2. Telegram Bot обрабатывает сообщение.
3. При ошибке сообщение попадает в DLQ.
4. Dental Lab Service анализирует причину ошибки и выполняет альтернативные действия.

Например:

- отправка Email уведомления;
- запись метрики;
- логирование инцидента.

---
### Stateful Telegram Bot

Для поддержки многошаговых сценариев взаимодействия реализован механизм ChatSession.
```
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

```
    private SendMessage input(ChatSession session, Locale locale, String messageText, int messageId) {
        NewDentalWork newDentalWork = ... // парсинг сообщения в данные для объекта нового заказа
        ... // создание ответа со списком видов работ... 
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.NEW_DENTAL_WORK, Steps.SELECT_PRODUCT_TYPE.ordinal());
        session.addAttribute(Attributes.NEW_DENTAL_WORK.name(), newDentalWorkAsString(newDentalWork));
        session.setCommand(BotCommands.NEW_DENTAL_WORK);
        session.setStep(Steps.SELECT_PRODUCT_TYPE.ordinal());
        chatSessionService.save(session);
        ... // удаление предыдущего сообщения 
        return createSendMessage(session.getChatId(), text, keyboardMarkup);
    }
```
При новом запросе будет работать следующий шаг с использованием сохранённых в сессии данных.

---
### Использование Redis

Redis применяется в нескольких сервисах:

- кэширование часто запрашиваемых данных в dental-lab-service;
- хранение ChatSession Telegram пользователей;
- реализация Rate Limiting в API Gateway.

По факту Redis используется не только как кэш, но и как быстрое in-memory хранилище.

---
> ## Observability

В проекте реализован полный цикл наблюдаемости приложения.

### Метрики

**Prometheus + Micrometer**

Собираются:

- HTTP latency
- количество запросов
- ошибки
- Resilience4J
- бизнес-метрики

### Логи

**Loki + Promtail**

Централизованно собираются структурированные JSON логи.

### Трассировка

**OpenTelemetry + Tempo**

Поддерживается сквозная трассировка запросов между сервисами по Trace ID.

### Визуализация

**Grafana Dashboard**

---
> ## Скриншоты


---
> ## Запуск проекта

**Требования**

**Локальный запуск**

---
> ## Автор
