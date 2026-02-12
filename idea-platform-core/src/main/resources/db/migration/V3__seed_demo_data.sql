-- src/main/resources/db/migration/V3__seed_demo_data.sql

-- Demo Division
INSERT INTO divisions (name, description, active) VALUES
('Технологии', 'Технологический дивизион', true),
('Продукт', 'Продуктовый дивизион', true),
('Операции', 'Операционный дивизион', true);

-- Demo Tribes
INSERT INTO tribes (name, description, division_id, active) VALUES
('Core Platform', 'Ядро платформы', 1, true),
('Mobile', 'Мобильная разработка', 1, true),
('Data', 'Данные и аналитика', 1, true),
('Growth', 'Рост и привлечение', 2, true),
('Retention', 'Удержание', 2, true),
('Support', 'Поддержка клиентов', 3, true);

-- Demo Teams
INSERT INTO teams (name, description, tribe_id, active) VALUES
('Backend', 'Бэкенд разработка', 1, true),
('Frontend', 'Фронтенд разработка', 1, true),
('DevOps', 'DevOps и инфраструктура', 1, true),
('iOS', 'iOS разработка', 2, true),
('Android', 'Android разработка', 2, true),
('Analytics', 'Команда аналитики', 3, true),
('ML', 'Machine Learning', 3, true),
('Marketing Tech', 'Маркетинг технологии', 4, true),
('Product Growth', 'Продуктовый рост', 4, true),
('CRM', 'CRM команда', 5, true),
('L1 Support', 'Первая линия поддержки', 6, true),
('L2 Support', 'Вторая линия поддержки', 6, true);

-- Demo Admin User (password: admin123)
INSERT INTO users (email, password, display_name, role, team_id, active) VALUES
('admin@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6p9S2', 'Администратор', 'ADMIN', 1, true);

-- Demo Reviewer User (password: reviewer123)
INSERT INTO users (email, password, display_name, role, team_id, active) VALUES
('reviewer@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6p9S2', 'Ревьюер Петров', 'REVIEWER', 1, true);

-- Demo Regular Users (password: user123)
INSERT INTO users (email, password, display_name, role, team_id, active) VALUES
('ivan@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6p9S2', 'Иван Сидоров', 'USER', 1, true),
('maria@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6p9S2', 'Мария Иванова', 'USER', 2, true),
('alex@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6p9S2', 'Алексей Козлов', 'USER', 4, true);
