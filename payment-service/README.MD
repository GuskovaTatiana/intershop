# Приложение эмулятор платежной системы

Этот проект представляет собой простой эмулятор платежной системы, для эмуляции оплаты, пополнения и получения баланса

## Особенности

**Получение своего текущего баланса авторизованным пользователями** 

**Пополнение своего баланса авторизованным пользователями**

**Списание средств со своего баланса авторизованным пользователями**

## Технологии

**База данных:** Redis

**Контейнер:** Docker

**Aвторизационный сервер:** Keycloak


## Предварительные требования

**Установить сторонние технологии:**
1. JAVA 21
2. Redis
3. Keycloak
4. Docker

## Настройка перед запуском
**Настройте базу данных:**
1. Обновите файл конфигурации application.yml с учетными данными установленного Redis.

**Настройте порт:**
1. Обновите файл конфигурации application.yml. укажите свободный порт в поле server.port.
2. Обновите файл конфигурации api-spec.yaml. укажите свободный порт в поле servers:- url:


**Настройте Keycloak, если еще не настроили:**

1. В терминале запустите процесс генерации контейнера в Docker
   ```
   docker run -d \
      --name keycloak \
      -p 8007:8080 \
      -e KC_BOOTSTRAP_ADMIN_USERNAME=admin \
      -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
      quay.io/keycloak/keycloak:latest \
      start-dev
``
2. Наберите в браузере http://localhost:8007/
3. Авторизуйтесь с указанными выше логином и паролем
4. На вкладке manage Realms нажмите кнопку Create realm
5. В поле Realm name введите "ecommerce-realm" (без кавычек) и нажмите кнопку Create
6. Перейдите во вкладку Clients и нажмите кнопку Create client
7. Внесите в поле client ID значение **shop-to-payment** (взято из файл конфигурации application.yml)
8. На 2 вкладке включите настройки:
   8.1. Client authentication
   8.2. Direct access grants
   8.3. Standard flow
   8.4. Service accounts roles
9. На 3 вкалдке ничего не меняем и сохраняем клиента
10. На странице клиента переходим во вкладку Credentials и копируем Client secret
11. Вставляем скопированный ключ в файл конфигурации application-test.yml по пути payment-service.oauth2.client-secret
    
**Создайте образ проекта:**
1. В терминале запустите сборку проекта ./gradlew :payment-service:bootJar

## Запуск
    Через терминал запустите файл payment-service-0.3.jar


## Эндпоинты
### HTTP Получение баланса
    GET /api/payments/{userId}/balance HTTP/1.1
      Host: localhost:8009
      Authorization: Bearer <tocken>

### HTTP Пополнение баланса
    POST /api/payments/{userId}/balance HTTP/1.1
    Host: localhost:8009
    Authorization: Bearer <tocken>
    Content-Type: application/json
    Content-Length: 30
    
    {
    "depositAmount": 20000
    }

### HTTP Списание средств с баланса
    POST /api/payments/{userId}/process HTTP/1.1
    Authorization: Bearer <tocken>
    Host: localhost:8009
    Content-Type: application/json
    Content-Length: 21
    
    {
    "amount": 100
    }

### HTTP Получение токена для теста
```
POST /realms/ecommerce-realm/protocol/openid-connect/token HTTP/1.1
Host: localhost:8007
Content-Type: application/x-www-form-urlencoded
Content-Length: 102
grant_type=client_credentials&client_id=shop-to-payment&client_secret=h8TCTNMIyhtWU43FkCk1pnofyxCnOMnP
```

