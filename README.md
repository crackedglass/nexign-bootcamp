# Nexign Bootcamp Baby Billing

Это проект биллинговой системы, разработанный с использованием микросервисной архитектуры.

## Как запустить с помощью Docker Compose

Для запуска всех сервисов проекта с использованием Docker Compose, выполните следующие шаги:

1.  Убедитесь, что у вас установлен Docker и Docker Compose.
2.  Склонируйте репозиторий (если вы этого еще не сделали):
    ```bash
    git clone https://github.com/crackedglass/nexign-bootcamp
    cd nexign-bootcamp
    ```
3.  Выполните команду:
    ```bash
    docker-compose up -d
    ```
    Эта команда соберет образы (если они еще не собраны) и запустит все сервисы в фоновом режиме.

    Доступные сервисы и их порты (по умолчанию):
    *   **RabbitMQ Management UI**: `http://localhost:15672` (логин/пароль: user/password)
    *   **cdr-generator**: `http://localhost:8081`
    *   **brt-service**: `http://localhost:8082`
    *   **hrs-service**: `http://localhost:8083`
    *   **crm-service**: `http://localhost:8084`
    *   **PostgreSQL**: порт `5432` (база данных: `billing_db`, пользователь: `admin`, пароль: `password`)

4.  Для остановки всех сервисов выполните:
    ```bash
    docker-compose down
    ```

## Технологический стек

Проект разработан с использованием следующих технологий:

*   Java 17
*   Spring Boot 3.4.5
*   Maven
*   PostgreSQL (для основных данных), H2 (для `cdr-generator`)
*   RabbitMQ
*   Docker, Docker Compose
*   Liquibase
*   OpenFeign
*   Junit, Mockito
