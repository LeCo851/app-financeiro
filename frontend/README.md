# Especificações do Frontend - App Financeiro

Este documento descreve a estrutura, serviços e modelos do frontend do App Financeiro.

## Visão Geral

O frontend é desenvolvido em Angular e consome a API REST do backend. Ele utiliza Supabase para autenticação e gerencia o estado da aplicação através de serviços injetáveis.

## Estrutura do Projeto

O código fonte está localizado em `src/app`.

### Pacotes Principais

*   `core`: Componentes e serviços essenciais (guards, interceptors).
*   `pages`: Componentes de página (Login, Dashboard, Transações, Investimentos).
*   `models`: Interfaces TypeScript que espelham os DTOs do backend.
*   `services`: Serviços para comunicação com a API e lógica de negócios.

## Serviços (`src/app/services`)

### AuthService (`auth.service.ts`)
Gerencia a autenticação com Supabase.
*   `user$`: Observable do usuário atual.
*   `isAuthenticated`: Getter booleano.
*   `getSessionToken()`: Retorna o token JWT da sessão.
*   `signUp(email, password, fullName)`: Cria novo usuário.
*   `signIn(email, password)`: Realiza login.
*   `signOut()`: Realiza logout.

### TransactionService (`transaction.service.ts`)
Gerencia transações financeiras.
*   `findAll(year?, month?)`: Busca transações, opcionalmente filtradas por mês/ano.
    *   Retorna: `Observable<Transaction[]>`
*   `getSummary()`: Busca o resumo financeiro (saldos).
    *   Retorna: `Observable<DashboardSummary>`
*   `create(transaction)`: Cria uma nova transação.
    *   Retorna: `Observable<Transaction>`

### InvestmentService (`investment.service.ts`)
Gerencia investimentos.
*   `findAll()`: Busca todos os investimentos do usuário.
    *   Retorna: `Observable<Investment[]>`
*   `sync(itemId)`: Solicita sincronização de investimentos via Pluggy.
    *   Retorna: `Observable<void>`

### OpenFinanceService (`open-finance.service.ts`)
Gerencia a integração com Open Finance (Pluggy).
*   `getConnectToken()`: Obtém token para inicializar o widget da Pluggy.
    *   Retorna: `Observable<ConnectTokenResponse>`
*   `syncConnection(itemId)`: Solicita sincronização de dados após conexão bem-sucedida.
    *   Retorna: `Observable<void>`

## Modelos (Interfaces)

### Transaction
Interface para objetos de transação.
*   `id`: string (opcional)
*   `description`: string
*   `amount`: number
*   `date`: string | Date
*   `type`: 'INCOME' | 'EXPENSE' | 'INVESTMENT'
*   `source`: string
*   `categoryName`: string (opcional)
*   `categoryColor`: string (opcional)
*   `categoryIcon`: string (opcional)
*   `merchantName`: string (opcional)
*   `totalInstallments`: number (opcional)
*   `currentInstallment`: number (opcional)

### DashboardSummary
Interface para o resumo do dashboard.
*   `totalIncome`: number (opcional)
*   `totalExpense`: number (opcional)
*   `balance`: number (opcional)
*   `currentBalance`: number (opcional)

### Investment
Interface para objetos de investimento (definida em `models/investment.model.ts`, mas referenciada no serviço).
*   (Estrutura inferida do backend DTO): `id`, `pluggyInvestmentId`, `name`, `code`, `isin`, `type`, `subType`, `balance`, `amountInvested`, `quantity`, `annualRate`, `rateType`, `last12mRate`, `dueDate`, `status`.

### ConnectTokenResponse
Interface para resposta do token de conexão.
*   `accessToken`: string

## Configuração de Ambiente

As configurações de API e chaves do Supabase estão localizadas em `src/environment/environment.ts`.
*   `apiUrl`: URL base do backend (ex: `http://localhost:8080/api`).
*   `supabaseUrl`: URL do projeto Supabase.
*   `supabaseKey`: Chave pública (anon key) do Supabase.
