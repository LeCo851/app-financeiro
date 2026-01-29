-- ==========================================
-- 0. CONFIGURAÇÕES INICIAIS
-- ==========================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================================
-- 1. TABELA DE USUÁRIOS
-- ==========================================
CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY, -- Geralmente sincronizado com Auth do Supabase
                                     email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255),

    -- CAMPOS DE INTEGRAÇÃO (NOVO)
    pluggy_item_id VARCHAR(255), -- Armazena o ID da conexão para o "Sync Rápido"

-- Identidade Financeira
    tax_number VARCHAR(20),
    job_title VARCHAR(100),
    company_name VARCHAR(100),
    investor_profile VARCHAR(50),

    -- Perfil Financeiro (Manual)
    gross_salary DECIMAL(19, 2),
    net_salary_estimate DECIMAL(19, 2),
    receives_plr BOOLEAN DEFAULT FALSE,
    plr_estimate DECIMAL(19, 2),
    savings_goal DECIMAL(19, 2),

    -- IA / Comportamental
    impulsivity_level VARCHAR(20) DEFAULT 'UNKNOWN',

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                             );

-- ==========================================
-- 2. TABELA DE CONTAS
-- ==========================================
CREATE TABLE IF NOT EXISTS accounts (
                                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    pluggy_account_id VARCHAR(255) NOT NULL UNIQUE, -- Link único com a API

    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,     -- "BANK", "CREDIT"
    subtype VARCHAR(50),           -- "CHECKING_ACCOUNT", "SAVINGS_ACCOUNT"
    currency_code VARCHAR(3) DEFAULT 'BRL',

    balance DECIMAL(19, 2),

    -- Cartão de Crédito
    credit_limit DECIMAL(19, 2),
    available_credit_limit DECIMAL(19, 2),
    balance_close_date DATE,
    balance_due_date DATE,

    -- Bancário
    agency VARCHAR(20),
    account_number VARCHAR(50),

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                             );

-- ==========================================
-- 3. TABELA DE CATEGORIAS
-- ==========================================
CREATE TABLE IF NOT EXISTS categories (
                                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(50),
    type VARCHAR(20) NOT NULL, -- "INCOME", "EXPENSE"
    color VARCHAR(20),

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                             UNIQUE (user_id, name)
    );

-- ==========================================
-- 4. TABELA DE CENÁRIOS (Digital Twin)
-- ==========================================
CREATE TABLE IF NOT EXISTS scenarios (
                                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                             );

-- ==========================================
-- 5. TABELA DE TRANSAÇÕES (Blindada)
-- ==========================================
CREATE TABLE IF NOT EXISTS transactions (
                                            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    account_id UUID REFERENCES accounts(id),
    category_id UUID REFERENCES categories(id),
    scenario_id UUID REFERENCES scenarios(id),

    -- Dados Core
    description VARCHAR(255) NOT NULL,
    original_description TEXT,
    amount DECIMAL(19, 2) NOT NULL,
    date TIMESTAMP WITH TIME ZONE NOT NULL, -- Importante para o fuso horário
                       type VARCHAR(20) NOT NULL, -- "INCOME", "EXPENSE", "INVESTMENT"
    status VARCHAR(20) DEFAULT 'POSTED',
    currency_code VARCHAR(3) DEFAULT 'BRL',

    -- Identificadores e Proteção
    transaction_hash VARCHAR(255),
    pluggy_transaction_id VARCHAR(255), -- ID da Pluggy
    source VARCHAR(50) NOT NULL,

    -- Dados Ricos
    merchant_name VARCHAR(255),
    merchant_cnpj VARCHAR(20),
    merchant_category VARCHAR(100),

    payment_method VARCHAR(50),
    payer_doc_number VARCHAR(20),
    receiver_name VARCHAR(255),
    receiver_doc_number VARCHAR(20),
    boleto_barcode VARCHAR(255),

    installment_number INTEGER,
    total_installments INTEGER,
    raw_payload JSONB,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                       -- CONSTRAINT VITAL: Impede duplicidade de ID vindo da Pluggy para o mesmo usuário
                       CONSTRAINT unique_pluggy_transaction_per_user UNIQUE (user_id, pluggy_transaction_id)
    );

-- ==========================================
-- 6. TABELA DE INVESTIMENTOS (Patrimônio)
-- ==========================================
CREATE TABLE IF NOT EXISTS investments (
                                           id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    pluggy_investment_id VARCHAR(255) NOT NULL UNIQUE, -- ID único do ativo na Pluggy

    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    isin VARCHAR(50),

    type VARCHAR(50),    -- "MUTUAL_FUND", "SECURITY", "EQUITY"
    subtype VARCHAR(50), -- "CDB", "LCI", "ETF"

    balance DECIMAL(19, 2),        -- Saldo Atual (Foto)
    amount_invested DECIMAL(19, 2),
    quantity DECIMAL(19, 8),

    -- Rentabilidade
    annual_rate DECIMAL(10, 4),
    rate_type VARCHAR(20),
    last_12m_rate DECIMAL(10, 4),

    due_date DATE,
    status VARCHAR(20),

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                             );

-- ==========================================
-- 7. TABELA DE EMPRÉSTIMOS
-- ==========================================
CREATE TABLE IF NOT EXISTS loans (
                                     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    pluggy_loan_id VARCHAR(255) NOT NULL UNIQUE,

    product_name VARCHAR(255),
    contract_number VARCHAR(100),

    total_contract_amount DECIMAL(19, 2),
    outstanding_balance DECIMAL(19, 2),

    date DATE,
    due_date DATE,

    interest_rate DECIMAL(10, 4),
    cet_annual DECIMAL(10, 4),

    installments_paid INTEGER,
    installments_total INTEGER,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                             );

-- ==========================================
-- 8. INTENÇÃO DE COMPRA
-- ==========================================
CREATE TABLE IF NOT EXISTS purchase_intents (
                                                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    url VARCHAR(2048),
    product_name VARCHAR(255),
    product_price DECIMAL(19, 2),
    analysis_result JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                             );

-- ==========================================
-- 9. ÍNDICES DE PERFORMANCE
-- ==========================================
CREATE INDEX IF NOT EXISTS idx_transactions_user_date ON transactions(user_id, date);
CREATE INDEX IF NOT EXISTS idx_transactions_merchant ON transactions(merchant_name);
CREATE INDEX IF NOT EXISTS idx_accounts_user ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_investments_user ON investments(user_id);