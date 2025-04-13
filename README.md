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
*   Spring Boot 3.x (Web, Data JPA, Thymeleaf, Validation)
*   PostgreSQL
*   Maven
*   Lombok
*   HTML / CSS (Thymeleaf)

## Как запустить локально

1.  **Клонировать репозиторий:**
    ```bash
    git clone https://github.com/llRauch/todo-app.git
    cd todo-app
    ```
2.  **Настроить базу данных PostgreSQL:**
    *   Убедитесь, что PostgreSQL установлен и запущен.
    *   Создайте базу данных с именем `todo_db`.
    *   Убедитесь, что пользователь `postgres` с паролем `admin` существует и имеет права на эту БД (или измените данные в `src/main/resources/application.properties`).
3.  **Запустить приложение:**
    *   Через Maven:
        ```bash
        mvn spring-boot:run
        ```
    *   Или запустите класс `TodoAppApplication.java` из вашей IDE.
4.  **Открыть в браузере:**
    Перейдите по адресу [http://localhost:8080/]
