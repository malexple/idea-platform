-- Добавление поля фактического эффекта
ALTER TABLE ideas ADD COLUMN IF NOT EXISTS actual_effect TEXT;
