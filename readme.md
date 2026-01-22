# 1. Запустить PostgreSQL
docker-compose up -d postgres

# 2. Собрать и запустить приложение
./gradlew bootRun

# Или полностью в Docker
docker-compose up --build

# Тестовые пользователи:

| Email                | Пароль      | Роль     |
| -------------------- | ----------- | -------- |
| admin@company.com    | admin123    | ADMIN    |
| reviewer@company.com | reviewer123 | REVIEWER |
| ivan@company.com     | user123     | USER     |
| maria@company.com    | user123     | USER     |
| alex@company.com     | user123     | USER     |