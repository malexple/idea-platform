-- Добавление тестового руководителя
-- Пароль: manager123 (BCrypt хеш)

INSERT INTO users (email, password, display_name, role, active, created_at, updated_at)
VALUES (
    'manager@company.com',
    '$2a$10$kFVZyd.USrsN/oQh/Rnaz.ok6ZXxzd/GstWm1r/9jZmU9uY4qJKWS',
    'Иван Руководитель',
    'MANAGER',
    true,
    NOW(),
    NOW()
)
ON CONFLICT (email) DO NOTHING;
