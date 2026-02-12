-- src/main/resources/db/migration/V5__fix_passwords_correct.sql

-- Пароль: admin123
UPDATE users SET password = '$2a$10$zAAobvbJ7iJ4x5V4lSMGxOlMnHtGTb09Se.EuRJj293Jt1IbLP9l.'
WHERE email = 'admin@company.com';

-- Пароль: reviewer123
UPDATE users SET password = '$2a$10$81piePkDvNKlUz4odH.kku8eCp1/6VzYyDhO9QcRKUCG9D.Tdo86K'
WHERE email = 'reviewer@company.com';

-- Пароль: user123
UPDATE users SET password = '$2a$10$FaxryIIyhLd2qgR6NHlNUuAJ3Wx4YBjfsok7eiWab6sVSX12fZIf6'
WHERE email = 'ivan@company.com';

UPDATE users SET password = '$2a$10$FaxryIIyhLd2qgR6NHlNUuAJ3Wx4YBjfsok7eiWab6sVSX12fZIf6'
WHERE email = 'maria@company.com';

UPDATE users SET password = '$2a$10$FaxryIIyhLd2qgR6NHlNUuAJ3Wx4YBjfsok7eiWab6sVSX12fZIf6'
WHERE email = 'alex@company.com';
