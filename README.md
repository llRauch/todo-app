# Todo App - Pet Project

Простое веб-приложение для управления списком задач, созданное в качестве пет-проекта для изучения Spring Boot и Thymeleaf.

## Функционал

*   Просмотр списка задач
*   Добавление новых задач
*   Редактирование описания существующих задач
*   Отметка задач как выполненных/невыполненных
*   Удаление задач

## Технологии

*   Java 17
*   Spring Boot 3.x (Web, Data JPA, Thymeleaf)
*   PostgreSQL
*   Maven
*   Docker
*   Docker Compose 
*   Lombok
*   Thymeleaf

## Как запустить локально

### Способ 1: С использованием Docker

1. **Клонировать репозиторий:**
    ```bash
    git clone https://github.com/llRauch/todo-app.git
    cd todo-app
    ```
3.  **Запустить приложение и базу данных с помощью Docker Compose:**
    (Находясь в корневой папке проекта)
    ```bash
    docker-compose up --build 
    ```
    *(При первом запуске или после изменений в коде используйте `--build`. Для последующих запусков достаточно `docker-compose up`)*
4.  **Открыть в браузере:**
    Приложение будет доступно по адресу [http://localhost:8080/](http://localhost:8080/)

### Способ 2: Запуск без Docker

1. **Клонировать репозиторий:**
    ```bash
    git clone https://github.com/llRauch/todo-app.git
    cd todo-app
    ```
2. **Установить:** JDK 17, Maven, PostgreSQL.
3. **Настроить базу данных PostgreSQL:**
    *   Создайте базу данных с именем `todo_db`.
    *   Убедитесь, что пользователь `postgres` с паролем `admin` существует и имеет права на эту БД (или измените данные в `src/main/resources/application.properties`).
3. **Запустить приложение:**
    *   Через Maven:
        ```bash
        mvn spring-boot:run
        ```
4. **Открыть в браузере:**
    Перейдите по адресу [http://localhost:8080/]
