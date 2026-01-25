-- Habilita extensão de UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================================
-- 1. TABELA DE USUÁRIOS (Perfil Completo)
-- ==========================================
CREATE TABLE users (
                       id UUID PRIMARY KEY, -- ID vindo do Supabase/Auth
                       email VARCHAR(255) NOT NULL UNIQUE,
                       full_name VARCHAR(255),

    -- Dados de Identidade (Vindos da Pluggy /identity)
                       tax_number VARCHAR(20),       -- CPF/CNPJ
                       job_title VARCHAR(100),       -- "Comercial"
                       company_name VARCHAR(100),    -- "Pluggy LTDA"
                       investor_profile VARCHAR(50), -- "Moderate", "Conservative"

    -- Perfil Financeiro (Input Manual)
                       gross_salary DECIMAL(19, 2),        -- Salário Bruto
                       net_salary_estimate DECIMAL(19, 2), -- Líquido Estimado
                       receives_plr BOOLEAN DEFAULT FALSE,
                       plr_estimate DECIMAL(19, 2),
                       savings_goal DECIMAL(19, 2),

    -- Perfil Comportamental (Calculado pela IA)
                       impulsivity_level VARCHAR(20) DEFAULT 'UNKNOWN',

                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 2. TABELA DE CONTAS (Bancárias e Cartões)
-- ==========================================
CREATE TABLE accounts (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          user_id UUID NOT NULL REFERENCES users(id),
                          pluggy_account_id VARCHAR(255) NOT NULL UNIQUE, -- Vínculo com a API externa

                          name VARCHAR(100) NOT NULL,    -- "Conta Corrente", "Mastercard Black"
                          type VARCHAR(50) NOT NULL,     -- "BANK", "CREDIT"
                          subtype VARCHAR(50),           -- "CHECKING_ACCOUNT", "CREDIT_CARD"
                          currency_code VARCHAR(3) DEFAULT 'BRL',

                          balance DECIMAL(19, 2),        -- Saldo atual

    -- Campos Específicos de Cartão de Crédito
                          credit_limit DECIMAL(19, 2),           -- Limite Total
                          available_credit_limit DECIMAL(19, 2), -- Limite Disponível
                          balance_close_date DATE,               -- Dia do fechamento
                          balance_due_date DATE,                 -- Dia do vencimento

    -- Campos Bancários
                          agency VARCHAR(20),
                          account_number VARCHAR(50),

                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 3. TABELA DE CATEGORIAS
-- ==========================================
CREATE TABLE categories (
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
CREATE TABLE scenarios (
                           id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           user_id UUID NOT NULL REFERENCES users(id),
                           name VARCHAR(100) NOT NULL,
                           description TEXT,
                           is_active BOOLEAN DEFAULT FALSE,

                           created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 5. TABELA DE TRANSAÇÕES (O Coração)
-- ==========================================
CREATE TABLE transactions (
                              id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                              user_id UUID NOT NULL REFERENCES users(id),
                              account_id UUID REFERENCES accounts(id), -- Saber de qual conta saiu o dinheiro
                              category_id UUID REFERENCES categories(id),
                              scenario_id UUID REFERENCES scenarios(id),

    -- Dados Básicos
                              description VARCHAR(255) NOT NULL,
                              original_description TEXT,
                              amount DECIMAL(19, 2) NOT NULL,
                              date TIMESTAMP WITH TIME ZONE NOT NULL,
                              type VARCHAR(20) NOT NULL, -- "CREDIT", "DEBIT"
                              status VARCHAR(20) DEFAULT 'POSTED', -- "PENDING", "POSTED"
                              currency_code VARCHAR(3) DEFAULT 'BRL',

    -- Identificadores Externos
                              transaction_hash VARCHAR(255), -- Hash único para evitar duplicidade
                              pluggy_transaction_id VARCHAR(255), -- ID original da Pluggy
                              source VARCHAR(50) NOT NULL, -- "PLUGGY", "MANUAL", "CSV"

    -- Dados Ricos: Comerciante (Merchant)
                              merchant_name VARCHAR(255),
                              merchant_cnpj VARCHAR(20),
                              merchant_category VARCHAR(100),

    -- Dados Ricos: Pagamento (Boleto/Pix)
                              payment_method VARCHAR(50), -- "BOLETO", "PIX"
                              payer_doc_number VARCHAR(20),
                              receiver_name VARCHAR(255),
                              receiver_doc_number VARCHAR(20),

    -- Dados Ricos: Boleto
                              boleto_barcode VARCHAR(255),

    -- Dados Ricos: Parcelamento (Cartão)
                              installment_number INTEGER,     -- Parcela atual (ex: 2)
                              total_installments INTEGER,     -- Total parcelas (ex: 12)

                              raw_payload JSONB, -- Backup completo do JSON original

                              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                              UNIQUE (user_id, pluggy_transaction_id) -- Garante que não duplica ID da Pluggy
);

-- ==========================================
-- 6. TABELA DE INVESTIMENTOS (Patrimônio)
-- ==========================================
CREATE TABLE investments (
                             id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                             user_id UUID NOT NULL REFERENCES users(id),
                             pluggy_investment_id VARCHAR(255) NOT NULL UNIQUE,

                             name VARCHAR(255) NOT NULL, -- "Fundo XP...", "PETR4"
                             code VARCHAR(50),           -- Ticker ou CNPJ
                             isin VARCHAR(50),           -- Código Internacional

                             type VARCHAR(50),    -- "MUTUAL_FUND", "EQUITY", "FIXED_INCOME"
                             subtype VARCHAR(50), -- "CDB", "LCI", "ETF"

                             balance DECIMAL(19, 2),        -- Saldo Atual
                             amount_invested DECIMAL(19, 2),-- Valor Aplicado
                             quantity DECIMAL(19, 8),       -- Quantidade de Cotas/Ações

    -- Rentabilidade
                             annual_rate DECIMAL(10, 4),      -- Taxa a.a (ex: 12.5)
                             rate_type VARCHAR(20),           -- "CDI", "IPCA", "PRE"
                             last_12m_rate DECIMAL(10, 4),    -- Rentabilidade últimos 12 meses

                             due_date DATE, -- Vencimento
                             status VARCHAR(20), -- "ACTIVE"

                             created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 7. TABELA DE EMPRÉSTIMOS (Dívidas)
-- ==========================================
CREATE TABLE loans (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       user_id UUID NOT NULL REFERENCES users(id),
                       pluggy_loan_id VARCHAR(255) NOT NULL UNIQUE,

                       product_name VARCHAR(255), -- "Crédito Consignado"
                       contract_number VARCHAR(100),

                       total_contract_amount DECIMAL(19, 2), -- Valor tomado
                       outstanding_balance DECIMAL(19, 2),   -- Saldo Devedor

                       date DATE,      -- Data contratação
                       due_date DATE,  -- Data fim contrato

                       interest_rate DECIMAL(10, 4), -- Taxa de Juros
                       cet_annual DECIMAL(10, 4),    -- Custo Efetivo Total

                       installments_paid INTEGER,
                       installments_total INTEGER,

                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 8. INTENÇÃO DE COMPRA (Rastro Digital)
-- ==========================================
CREATE TABLE purchase_intents (
                                  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  user_id UUID NOT NULL REFERENCES users(id),
                                  url VARCHAR(2048),
                                  product_name VARCHAR(255),
                                  product_price DECIMAL(19, 2),
                                  analysis_result JSONB,
                                  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Índices Estratégicos
CREATE INDEX idx_transactions_user_date ON transactions(user_id, date);
CREATE INDEX idx_transactions_merchant ON transactions(merchant_name); -- Busca rápida por loja
CREATE INDEX idx_accounts_user ON accounts(user_id);
CREATE INDEX idx_investments_user ON investments(user_id);