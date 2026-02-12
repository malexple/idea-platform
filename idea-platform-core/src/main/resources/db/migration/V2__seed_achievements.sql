-- src/main/resources/db/migration/V2__seed_achievements.sql

-- OWL (Проблемы)
INSERT INTO achievements (code, name, description, hero_type, level, threshold, sort_order, icon_path) VALUES
('OWL_NOVICE', 'Совёнок', 'Первая зафиксированная проблема', 'OWL', 'NOVICE', 1, 1, '/images/achievements/owl_novice.png'),
('OWL_EXPLORER', 'Зоркая Сова', '5 зафиксированных проблем', 'OWL', 'EXPLORER', 5, 2, '/images/achievements/owl_explorer.png'),
('OWL_MASTER', 'Мудрая Сова', '15 зафиксированных проблем', 'OWL', 'MASTER', 15, 3, '/images/achievements/owl_master.png'),
('OWL_EXPERT', 'Сова-Страж', '30 зафиксированных проблем', 'OWL', 'EXPERT', 30, 4, '/images/achievements/owl_expert.png'),
('OWL_LEGEND', 'Сова-Оракул', '50 зафиксированных проблем — вы видите то, чего не замечают другие', 'OWL', 'LEGEND', 50, 5, '/images/achievements/owl_legend.png');

-- BEE (Автоматизации)
INSERT INTO achievements (code, name, description, hero_type, level, threshold, sort_order, icon_path) VALUES
('BEE_NOVICE', 'Пчёлка-Помощник', 'Первая идея автоматизации', 'BEE', 'NOVICE', 1, 10, '/images/achievements/bee_novice.png'),
('BEE_EXPLORER', 'Трудолюбивая Пчела', '5 идей автоматизации', 'BEE', 'EXPLORER', 5, 11, '/images/achievements/bee_explorer.png'),
('BEE_MASTER', 'Пчела-Инженер', '15 идей автоматизации', 'BEE', 'MASTER', 15, 12, '/images/achievements/bee_master.png'),
('BEE_EXPERT', 'Пчела-Архитектор', '30 идей автоматизации', 'BEE', 'EXPERT', 30, 13, '/images/achievements/bee_expert.png'),
('BEE_LEGEND', 'Королева Улья', '50 идей автоматизации — вы превращаете рутину в код', 'BEE', 'LEGEND', 50, 14, '/images/achievements/bee_legend.png');

-- EAGLE (Идеи)
INSERT INTO achievements (code, name, description, hero_type, level, threshold, sort_order, icon_path) VALUES
('EAGLE_NOVICE', 'Орлёнок', 'Первая поданная идея', 'EAGLE', 'NOVICE', 1, 20, '/images/achievements/eagle_novice.png'),
('EAGLE_EXPLORER', 'Парящий Орёл', '5 поданных идей', 'EAGLE', 'EXPLORER', 5, 21, '/images/achievements/eagle_explorer.png'),
('EAGLE_MASTER', 'Орёл-Первопроходец', '15 поданных идей', 'EAGLE', 'MASTER', 15, 22, '/images/achievements/eagle_master.png'),
('EAGLE_EXPERT', 'Орёл-Визионер', '30 поданных идей', 'EAGLE', 'EXPERT', 30, 23, '/images/achievements/eagle_expert.png'),
('EAGLE_LEGEND', 'Легендарный Орёл', '50 поданных идей — ваш взгляд охватывает горизонты', 'EAGLE', 'LEGEND', 50, 24, '/images/achievements/eagle_legend.png');

-- PHOENIX (Внедрения)
INSERT INTO achievements (code, name, description, hero_type, level, threshold, sort_order, icon_path) VALUES
('PHOENIX_NOVICE', 'Искра Феникса', 'Первое внедрение вашей идеи', 'PHOENIX', 'NOVICE', 1, 30, '/images/achievements/phoenix_novice.png'),
('PHOENIX_EXPLORER', 'Крыло Феникса', '3 внедрённых идеи', 'PHOENIX', 'EXPLORER', 3, 31, '/images/achievements/phoenix_explorer.png'),
('PHOENIX_MASTER', 'Пламя Феникса', '5 внедрённых идей', 'PHOENIX', 'MASTER', 5, 32, '/images/achievements/phoenix_master.png'),
('PHOENIX_EXPERT', 'Феникс Возрождения', '10 внедрённых идей', 'PHOENIX', 'EXPERT', 10, 33, '/images/achievements/phoenix_expert.png'),
('PHOENIX_LEGEND', 'Бессмертный Феникс', '20 внедрённых идей — вы меняете реальность', 'PHOENIX', 'LEGEND', 20, 34, '/images/achievements/phoenix_legend.png');

-- DOLPHIN (Сообщество - голосование)
INSERT INTO achievements (code, name, description, hero_type, level, threshold, sort_order, icon_path) VALUES
('DOLPHIN_VOTE_NOVICE', 'Дельфинёнок', 'Первый голос за чужую идею', 'DOLPHIN', 'NOVICE', 1, 40, '/images/achievements/dolphin_vote_novice.png'),
('DOLPHIN_VOTE_EXPLORER', 'Дружелюбный Дельфин', '20 голосов за идеи коллег', 'DOLPHIN', 'EXPLORER', 20, 41, '/images/achievements/dolphin_vote_explorer.png'),
('DOLPHIN_VOTE_MASTER', 'Дельфин-Наставник', '50 голосов за идеи коллег', 'DOLPHIN', 'MASTER', 50, 42, '/images/achievements/dolphin_vote_master.png'),
('DOLPHIN_VOTE_EXPERT', 'Дельфин-Амбассадор', '100 голосов за идеи коллег', 'DOLPHIN', 'EXPERT', 100, 43, '/images/achievements/dolphin_vote_expert.png'),
('DOLPHIN_VOTE_LEGEND', 'Голос Океана', '200 голосов — вы формируете волну изменений', 'DOLPHIN', 'LEGEND', 200, 44, '/images/achievements/dolphin_vote_legend.png');

-- DOLPHIN (Сообщество - комментарии)
INSERT INTO achievements (code, name, description, hero_type, level, threshold, sort_order, icon_path) VALUES
('DOLPHIN_COMMENT_EXPLORER', 'Общительный Дельфин', '10 комментариев к идеям', 'DOLPHIN', 'EXPLORER', 10, 45, '/images/achievements/dolphin_comment_explorer.png'),
('DOLPHIN_COMMENT_MASTER', 'Дельфин-Советник', '30 комментариев к идеям', 'DOLPHIN', 'MASTER', 30, 46, '/images/achievements/dolphin_comment_master.png'),
('DOLPHIN_COMMENT_EXPERT', 'Дельфин-Мудрец', '50 конструктивных комментариев', 'DOLPHIN', 'EXPERT', 50, 47, '/images/achievements/dolphin_comment_expert.png');

-- DRAGON (Мастерство)
INSERT INTO achievements (code, name, description, hero_type, level, threshold, sort_order, icon_path) VALUES
('DRAGON_MULTI_HERO', 'Дракон Многоликий', 'Получено по одному достижению каждого типа героя', 'DRAGON', 'MASTER', 5, 50, '/images/achievements/dragon_multi.png'),
('DRAGON_STREAK', 'Дракон Неустанный', '5 идей внедрено за квартал', 'DRAGON', 'EXPERT', 5, 51, '/images/achievements/dragon_streak.png'),
('DRAGON_ULTIMATE', 'Легендарный Дракон', 'Все достижения уровня Legend получены', 'DRAGON', 'LEGEND', 5, 52, '/images/achievements/dragon_ultimate.png');
