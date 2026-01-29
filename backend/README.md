# Especificações do Backend - App Financeiro

Este documento descreve a estrutura, endpoints e modelos de dados do backend do App Financeiro.

## Visão Geral

O backend é construído em Java com Spring Boot, utilizando PostgreSQL como banco de dados. Ele gerencia usuários, contas, transações, investimentos e integrações com Open Finance (Pluggy).

## Estrutura do Projeto

O código fonte está localizado em `src/main/java/br/com/leandrocoelho/backend`.

### Pacotes Principais

*   `controller`: Controladores REST que expõem os endpoints da API.
*   `dto`: Objetos de Transferência de Dados (Request e Response).
*   `model`: Entidades JPA que representam as tabelas do banco de dados.
*   `repository`: Interfaces Spring Data JPA para acesso ao banco de dados.
*   `service`: Lógica de negócios.
*   `integration`: Integrações externas (ex: Pluggy).
*   `security`: Configurações de segurança.
*   `config`: Configurações gerais da aplicação.

## Modelos de Dados (Entidades)

### User (`users`)
Representa o usuário do sistema.
*   `id`: UUID (Chave Primária)
*   `email`: String (Único)
*   `fullName`: String
*   `grossSalary`: BigDecimal
*   `netSalaryEstimate`: BigDecimal
*   `receivesPlr`: Boolean
*   `plrEstimate`: BigDecimal
*   `savingsGoal`: BigDecimal
*   `impulsivityLevel`: String
*   `createdAt`: ZonedDateTime
*   `updatedAt`: ZonedDateTime

### Account (`accounts`)
Representa uma conta bancária ou cartão de crédito.
*   `id`: UUID
*   `user`: User (FK)
*   `pluggyAccountId`: String (Único)
*   `name`: String
*   `type`: String
*   `subtype`: String
*   `balance`: BigDecimal
*   `creditLimit`: BigDecimal
*   `currencyCode`: String
*   `availableCreditLimit`: BigDecimal
*   `agency`: String
*   `accountNumber`: String

### Category (`categories`)
Categorias para classificar transações.
*   `id`: UUID
*   `user`: User (FK)
*   `name`: String
*   `icon`: String
*   `color`: String
*   `type`: TransactionType (INCOME, EXPENSE)

### Transaction (`transactions`)
Registra movimentações financeiras.
*   `id`: UUID
*   `user`: User (FK)
*   `account`: Account (FK)
*   `category`: Category (FK)
*   `scenario`: Scenario (FK)
*   `description`: String
*   `amount`: BigDecimal
*   `date`: ZonedDateTime
*   `type`: TransactionType (INCOME, EXPENSE, TRANSFER, INVESTMENT)
*   `status`: String (PENDING, POSTED)
*   `source`: TransactionSource (MANUAL, PLUGGY, etc.)
*   `merchantName`: String
*   `installmentNumber`: Integer
*   `totalInstallments`: Integer
*   `pluggyTransactionId`: String (Único)

### Investment (`investments`)
Registra investimentos do usuário.
*   `id`: UUID
*   `user`: User (FK)
*   `name`: String
*   `code`: String
*   `isin`: String
*   `balance`: BigDecimal
*   `type`: String
*   `subType`: String
*   `dueDate`: LocalDate
*   `annualRate`: BigDecimal
*   `rateType`: String
*   `last12mRate`: BigDecimal
*   `status`: String
*   `quantity`: BigDecimal
*   `amountInvested`: BigDecimal
*   `pluggyInvestmentId`: String

### Scenario (`scenarios`)
Cenários para simulações financeiras.
*   `id`: UUID
*   `userId`: UUID
*   `name`: String
*   `description`: String
*   `isActive`: Boolean

## Endpoints da API

### Investimentos (`/api/investments`)

*   **POST /**: Cria um novo investimento manual.
    *   Body: `InvestmentRequestDto`
*   **GET /**: Lista os investimentos do usuário autenticado.
    *   Response: `List<InvestmentResponseDto>`
*   **POST /sync**: Sincroniza investimentos via Open Finance (Pluggy).
    *   Body: `SyncRequestDto`

### Open Finance (`/api/open-finance`)

*   **GET /connect-token**: Gera um token de conexão para o widget da Pluggy.
    *   Response: `Map<String, String>` (accessToken)
*   **POST /sync**: Sincroniza dados de uma conexão Open Finance.
    *   Body: `SyncRequestDto`

### Transações (`/api/transactions`)

*   **POST /**: Cria uma nova transação manual.
    *   Body: `TransactionRequestDto`
*   **GET /**: Lista transações do usuário.
    *   Params: `year` (opcional), `month` (opcional)
    *   Response: `List<TransactionResponseDto>`
*   **GET /summary**: Obtém o resumo financeiro (saldo atual).
    *   Response: `DashboardSummaryDto`

## DTOs (Data Transfer Objects)

### Request DTOs

*   **InvestmentRequestDto**: `pluggyInvestmentId`, `name`, `code`, `isin`, `type`, `subType`, `balance`, `amountInvested`, `quantity`, `annualRate`, `rateType`, `dueDate`.
*   **SyncRequestDto**: `itemId`.
*   **TransactionRequestDto**: `description`, `amount`, `date`, `type`, `categoryId`, `scenarioId`, `merchantName`, `currentInstallment`, `totalInstallments`.

### Response DTOs

*   **InvestmentResponseDto**: Dados completos do investimento.
*   **TransactionResponseDto**: Dados completos da transação, incluindo nomes de categoria e cenário.
*   **DashboardSummaryDto**: `totalIncome`, `totalExpense`, `balance`, `currentBalance`.

## Integrações

### Pluggy (Open Finance)
O sistema integra com a API da Pluggy para sincronização automática de contas, transações e investimentos.
*   **PluggyService**: Gerencia a comunicação HTTP com a Pluggy.
*   **SyncService**: Orquestra a sincronização de dados e mapeamento para entidades locais.
*   **PluggyDataMapper**: Converte DTOs da Pluggy para entidades do sistema.

## Segurança
A aplicação utiliza Spring Security com OAuth2 Resource Server (JWT) para autenticação. O ID do usuário é extraído do token JWT (`sub`).
