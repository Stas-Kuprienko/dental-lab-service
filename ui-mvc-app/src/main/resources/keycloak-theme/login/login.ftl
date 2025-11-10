<#-- login.ftl — кастомный логин для Keycloak -->
<!DOCTYPE html>
<html lang="ru">
<head>
  <meta charset="UTF-8">
  <title>DENTAL LAB SERVICE</title>
  <link rel="icon" href="data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 100 100%22><text y=%22.9em%22 font-size=%2290%22>🦷</text></svg>">
  <link rel="stylesheet" href="${url.resourcesPath}/css/style.css" />
</head>

<body>
<nav class="menu">
  <header><strong>🦷 DENTAL LAB SERVICE 🦷</strong></header>
</nav>

<section style="margin-top: 96px; text-align:center;">
    <#if infoMessage??>
        <div class="alert">${infoMessage}</div>
    </#if>
  <h2>Войти:</h2>

  <#-- Форма входа -->
  <form id="kc-form-login" class="form" action="${url.loginAction}" method="post">
    <label for="username">email:</label><br>
    <input type="text" id="username" name="username" autofocus required /><br>

    <label for="password">пароль:</label><br>
    <input type="password" id="password" name="password" required /><br><br>

    <button type="submit" style="font-size:18px;width:90px;">LOG IN</button>
  </form>
    <br>
    <a action="${properties.resetPassword}" method="get" style="color:white">
        Забыли пароль?
    </a>
  <br><br>

  <form action="${properties.registrationUrl}" method="get">
    <button type="submit" style="font-size:14px;">SIGN UP</button>
  </form>

  <#-- Сообщение об ошибке -->
  <#if message?has_content>
    <div class="alert" style="margin-top: 16px; color: red;">
      ${message.summary}
    </div>
  </#if>
</section>
</body>
</html>
