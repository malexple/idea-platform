-- src/main/resources/db/migration/V4__fix_passwords.sql

-- Пароль: admin123
UPDATE users SET password = '$2a$10$rDkPvvAFV8kqwvKJzwlRv.FDXhIbTLAHd7bXEFbNJpM.3XxqhXqVe'
WHERE email = 'admin@company.com';

-- Пароль: reviewer123
UPDATE users SET password = '$2a$10$rDkPvvAFV8kqwvKJzwlRv.FDXhIbTLAHd7bXEFbNJpM.3XxqhXqVe'
WHERE email = 'reviewer@company.com';

-- Пароль: user123
UPDATE users SET password = '$2a$10$rDkPvvAFV8kqwvKJzwlRv.FDXhIbTLAHd7bXEFbNJpM.3XxqhXqVe'
WHERE email = 'ivan@company.com';

UPDATE users SET password = '$2a$10$rDkPvvAFV8kqwvKJzwlRv.FDXhIbTLAHd7bXEFbNJpM.3XxqhXqVe'
WHERE email = 'maria@company.com';

UPDATE users SET password = '$2a$10$rDkPvvAFV8kqwvKJzwlRv.FDXhIbTLAHd7bXEFbNJpM.3XxqhXqVe'
WHERE email = 'alex@company.com';
