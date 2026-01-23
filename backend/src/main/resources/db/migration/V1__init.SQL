-- Habilita a extensão de UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. TABELA DE CATEGORIAS (Multi-tenant)
CREATE TABLE categories (
                            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            user_id UUID, -- Se NULL, é global
                            name VARCHAR(100) NOT NULL,
                            icon VARCHAR(50),
                            type VARCHAR(20) NOT NULL,
                            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, -- ADICIONADO AQUI

                            UNIQUE (user_id, name)
);

-- 2. TABELA DE CENÁRIOS (Digital Twin)
CREATE TABLE scenarios (
                           id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           user_id UUID NOT NULL,
                           name VARCHAR(100) NOT NULL,
                           description TEXT,
                           is_active BOOLEAN DEFAULT TRUE,
                           created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP -- ADICIONADO AQUI
);

-- 3. TABELA DE TRANSAÇÕES (Core)
CREATE TABLE transactions (
                              id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                              user_id UUID NOT NULL,
                              category_id UUID REFERENCES categories(id),
                              scenario_id UUID REFERENCES scenarios(id),

                              description VARCHAR(255) NOT NULL,
                              original_description TEXT,
                              amount DECIMAL(19, 2) NOT NULL,
                              date DATE NOT NULL,
                              type VARCHAR(20) NOT NULL,

                              transaction_hash VARCHAR(64),
                              source VARCHAR(50) NOT NULL,
                              raw_payload JSONB,

                              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                              UNIQUE (user_id, transaction_hash)
);

-- 4. METADADOS DE INTELIGÊNCIA
CREATE TABLE transaction_metadata (
                                      transaction_id UUID PRIMARY KEY REFERENCES transactions(id) ON DELETE CASCADE,
                                      sentiment_score DECIMAL(3, 2),
                                      regret_index DECIMAL(3, 2),
                                      ai_tags TEXT[],
                                      ai_analysis TEXT
);

-- 5. REGRAS DE CLASSIFICAÇÃO
CREATE TABLE classification_rules (
                                      id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                      user_id UUID NOT NULL,
                                      keyword VARCHAR(100) NOT NULL,
                                      target_category_id UUID NOT NULL REFERENCES categories(id),

                                      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP -- ADICIONADO AQUI
);

-- ÍNDICES
CREATE INDEX idx_transactions_user_date ON transactions(user_id, date);
CREATE INDEX idx_transactions_scenario ON transactions(scenario_id);