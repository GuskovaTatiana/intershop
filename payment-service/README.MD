# Приложение эмулятор платежной системы

Этот проект представляет собой простой эмулятор платежной системы, для эмуляции оплаты, пополнения и получения баланса

## Особенности

**Получение текущего баланса** 

**Пополнение баланса**

**Списание средств с баланса**

## Технологии

**База данных:** Redis


## Предварительные требования

**Установить сторонние технологии:**
1. JAVA 21
2. Redis

## Настройка перед запуском
**Настройте базу данных:**
1. Обновите файл конфигурации application.yml с учетными данными установленного Redis.

**Настройте порт:**
1. Обновите файл конфигурации application.yml. укажите свободный порт в поле server.port.
2. Обновите файл конфигурации api-spec.yaml. укажите свободный порт в поле servers:- url:

**Создайте образ проекта:**
1. В терминале запустите сборку проекта ./gradlew :payment-service:bootJar

## Запуск
    Через терминал запустите файл payment-service-0.3.jar


## Эндпоинты
### HTTP Получение баланса
    GET /api/payments/balance HTTP/1.1
    Host: localhost:8009

### HTTP Пополнение баланса
    POST /api/payments/balance HTTP/1.1
    Host: localhost:8009
    Content-Type: application/json
    Content-Length: 30
    
    {
    "depositAmount": 20000
    }

### HTTP Списание средств с баланса
    POST /api/payments/process HTTP/1.1
    Host: localhost:8009
    Content-Type: application/json
    Content-Length: 21
    
    {
    "amount": 100
    }

