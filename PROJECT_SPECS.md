# Especificações do Projeto App Financeiro

Este documento consolida as especificações técnicas, endpoints e modelos de dados do projeto App Financeiro, abrangendo tanto o Backend quanto o Frontend.

---

## 1. Backend (Java / Spring Boot)

### Visão Geral
O backend é construído em Java com Spring Boot, utilizando PostgreSQL como banco de dados. Ele gerencia usuários, contas, transações, investimentos e integrações com Open Finance (Pluggy).

### Estrutura de Pacotes (`src/main/java/br/com/leandrocoelho/backend`)
*   `controller`: Endpoints REST.
*   `dto`: Objetos de Transferência de Dados.
*   `model`: Entidades JPA (Banco de Dados).
*   `repository`: Acesso a dados (Spring Data JPA).
*   `service`: Regras de negócio.
*   `integration`: Clientes externos (Pluggy).

### Endpoints da API

#### Investimentos (`/api/investments`)
*   `POST /`
    *   **Descrição**: Cria um novo investimento manual.
    *   **Body**: `InvestmentRequestDto`
*   `GET /`
    *   **Descrição**: Lista os investimentos do usuário autenticado.
    *   **Response**: `List<InvestmentResponseDto>`
*   `POST /sync`
    *   **Descrição**: Sincroniza investimentos via Open Finance (Pluggy).
    *   **Body**: `SyncRequestDto` (itemId)

#### Open Finance (`/api/open-finance`)
*   `GET /connect-token`
    *   **Descrição**: Gera um token de conexão para o widget da Pluggy.
    *   **Response**: `Map<String, String>` (accessToken)
*   `POST /sync`
    *   **Descrição**: Dispara a sincronização de dados (contas, transações) após conexão.
    *   **Body**: `SyncRequestDto` (itemId)

#### Transações (`/api/transactions`)
*   `POST /`
    *   **Descrição**: Cria uma nova transação manual.
    *   **Body**: `TransactionRequestDto`
*   `GET /`
    *   **Descrição**: Lista transações do usuário.
    *   **Params**: `year` (opcional), `month` (opcional).
    *   **Response**: `List<TransactionResponseDto>`
*   `GET /summary`
    *   **Descrição**: Obtém o resumo financeiro (saldos, receitas, despesas).
    *   **Response**: `DashboardSummaryDto`

### Modelos de Dados (Entidades Principais)

#### User
*   `id`: UUID
*   `email`: String
*   `fullName`: String
*   `grossSalary`: BigDecimal
*   `netSalaryEstimate`: BigDecimal
*   `savingsGoal`: BigDecimal

#### Account
*   `id`: UUID
*   `pluggyAccountId`: String
*   `name`: String
*   `type`: String (CHECKING, CREDIT, etc.)
*   `balance`: BigDecimal
*   `creditLimit`: BigDecimal

#### Transaction
*   `id`: UUID
*   `description`: String
*   `amount`: BigDecimal
*   `date`: ZonedDateTime
*   `type`: TransactionType (INCOME, EXPENSE, TRANSFER, INVESTMENT)
*   `source`: TransactionSource (MANUAL, PLUGGY)
*   `status`: String (PENDING, POSTED)
*   `merchantName`: String
*   `installmentNumber`: Integer
*   `totalInstallments`: Integer
*   `category`: Category (Relacionamento)
*   `scenario`: Scenario (Relacionamento)

#### Investment
*   `id`: UUID
*   `name`: String
*   `code`: String (Ticker)
*   `type`: String (Renda Fixa, Ações, etc.)
*   `balance`: BigDecimal
*   `amountInvested`: BigDecimal
*   `annualRate`: BigDecimal
*   `dueDate`: LocalDate

### DTOs Relevantes

*   **TransactionRequestDto**: `description`, `amount`, `date`, `type`, `categoryId`, `scenarioId`, `merchantName`, `currentInstallment`, `totalInstallments`.
*   **InvestmentRequestDto**: `pluggyInvestmentId`, `name`, `code`, `isin`, `type`, `subType`, `balance`, `amountInvested`, `quantity`, `annualRate`, `rateType`, `dueDate`.
*   **DashboardSummaryDto**: `totalIncome`, `totalExpense`, `balance`, `currentBalance`.

---

## 2. Frontend (Angular)

### Visão Geral
O frontend é desenvolvido em Angular e consome a API REST do backend. Utiliza Supabase para autenticação.

### Serviços Principais (`src/app/services`)

#### AuthService
*   Gerencia login, cadastro e sessão com Supabase.
*   Métodos: `signIn`, `signUp`, `signOut`, `getSessionToken`.
*   Propriedade: `user$` (Observable do estado do usuário).

#### TransactionService
*   Consome `/api/transactions`.
*   `findAll(year, month)`: Retorna lista de transações.
*   `getSummary()`: Retorna resumo do dashboard.
*   `create(transaction)`: Envia nova transação.

#### InvestmentService
*   Consome `/api/investments`.
*   `findAll()`: Retorna lista de investimentos.
*   `sync(itemId)`: Solicita sincronização com Pluggy.

#### OpenFinanceService
*   Consome `/api/open-finance`.
*   `getConnectToken()`: Obtém token para o widget Pluggy.
*   `syncConnection(itemId)`: Notifica backend sobre nova conexão.

### Interfaces (Models)

#### Transaction
```typescript
interface Transaction {
  id?: string;
  description: string;
  amount: number;
  date: string | Date;
  type: 'INCOME' | 'EXPENSE' | 'INVESTMENT';
  source: string;
  categoryName?: string;
  categoryColor?: string;
  categoryIcon?: string;
  merchantName?: string;
  totalInstallments?: number;
  currentInstallment?: number;
}
```

#### DashboardSummary
```typescript
interface DashboardSummary {
  totalIncome?: number;
  totalExpense?: number;
  balance?: number;
  currentBalance?: number;
}
```

### Autenticação
*   Utiliza **Supabase Auth**.
*   O token JWT do Supabase é enviado nas requisições para o Backend (que valida o token como Resource Server).
