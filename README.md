# App Financeiro Inteligente

Sistema completo de gestão financeira pessoal desenvolvido com **Java (Spring Boot)** no backend e **Angular** no frontend. O projeto utiliza Inteligência Artificial (OpenAI/Llama) para categorização automática de transações e consultoria financeira personalizada, além de integração com Open Finance via **Pluggy** e importação de arquivos **OFX**.

---

## 🚀 Funcionalidades Principais

### 1. Gestão Financeira
- **Controle de Transações**: Registro de receitas, despesas, transferências e investimentos.
- **Dashboard Interativo**: Visão geral de saldo, receitas vs. despesas, e gráficos de evolução patrimonial.
- **Importação OFX**: Suporte para importação de extratos bancários (Conta Corrente e Cartão de Crédito).
- **Open Finance**: Sincronização automática de contas e transações bancárias via integração com a **Pluggy**.

### 2. Inteligência Artificial (Spring AI)
- **Categorização Automática**: O sistema utiliza LLMs (Llama 3 via Groq ou OpenAI) para analisar a descrição das transações e atribuir a categoria correta (ex: Uber -> Transporte).
- **Consultor Financeiro (Advisor)**: Chatbot integrado que atua como um planejador financeiro (CFP), analisando o contexto real do usuário (gastos, renda, regra 50/30/20) para dar conselhos personalizados.

### 3. Investimentos
- **Carteira de Ativos**: Gestão de Renda Fixa, Ações, Fundos, etc.
- **Sincronização**: Atualização automática de saldos e posições via Open Finance.

---

## 🛠️ Tecnologias Utilizadas

### Backend (Java / Spring Boot)
- **Java 21**
- **Spring Boot 3.5.9**
- **Spring AI (1.0.0-M6)**: Integração com modelos de IA.
- **Spring Data JPA / Hibernate**: Persistência de dados.
- **PostgreSQL**: Banco de dados relacional.
- **Flyway**: Migração e versionamento de banco de dados.
- **Spring Security (OAuth2 Resource Server)**: Segurança via tokens JWT.
- **OFX4J**: Processamento de arquivos OFX.
- **Lombok**: Redução de boilerplate code.

### Frontend (Angular)
- **Angular 21**
- **PrimeNG**: Biblioteca de componentes de UI.
- **Supabase Auth**: Autenticação e gestão de usuários.
- **Plotly.js**: Gráficos interativos para o dashboard.
- **Pluggy Connect SDK**: Widget para conexão de contas bancárias.

---

## 📂 Estrutura do Projeto

### Backend (`/backend`)
A arquitetura segue o padrão de camadas (Controller, Service, Repository, Model).

- **`controller`**: Endpoints REST (`TransactionController`, `InvestmentController`, `AdvisorController`, etc.).
- **`service`**: Regras de negócio.
    - **`ai`**: Serviços de IA (`AdvisorService`, `AiCategorizationService`).
    - **`importation`**: Processamento de arquivos (`OfxParserService`).
    - **`integration`**: Clientes externos (`PluggyService`).
- **`model`**: Entidades JPA (`User`, `Account`, `Transaction`, `Investment`).
- **`dto`**: Objetos de transferência de dados.

### Frontend (`/frontend`)
Aplicação Angular modularizada.

- **`src/app/services`**: Comunicação com a API (`TransactionService`, `AuthService`, `OpenFinanceService`).
- **`src/app/pages`**: Componentes de visualização (Dashboard, Extrato, Investimentos).

---

## ⚙️ Configuração e Execução

### Pré-requisitos
- Java 21+
- Node.js 18+
- Docker (opcional, para banco de dados)
- PostgreSQL
- Conta no Supabase (para Auth)
- Chaves de API: OpenAI/Groq (para IA) e Pluggy (para Open Finance)

### Passos para rodar o Backend
1. Navegue até a pasta `backend`.
2. Configure as variáveis de ambiente (ou arquivo `.env` / `application.properties`):
   - `SPRING_DATASOURCE_URL`: URL do PostgreSQL.
   - `SPRING_AI_OPENAI_API_KEY`: Chave da API de IA.
   - `PLUGGY_CLIENT_ID` e `PLUGGY_CLIENT_SECRET`: Credenciais da Pluggy.
3. Execute o projeto:
   ```bash
   ./mvnw spring-boot:run
   ```

### Passos para rodar o Frontend
1. Navegue até a pasta `frontend`.
2. Instale as dependências:
   ```bash
   npm install
   ```
3. Inicie o servidor de desenvolvimento:
   ```bash
   ng serve
   ```
4. Acesse `http://localhost:4200`.

---

## 🧠 Detalhes da IA (Advisor)

O **AdvisorService** constrói um contexto financeiro detalhado do usuário antes de enviar o prompt para a IA. Ele analisa:
1. **Macroeconomia Pessoal**: Renda, gastos fixos, saldo livre.
2. **Vilões do Orçamento**: Top categorias de gastos.
3. **Movimentações Recentes**: Gastos dos últimos dias.
4. **Regra 50/30/20**: Verifica se o usuário está dentro das diretrizes ideais de orçamento.

Com isso, a IA não responde apenas genericamente, mas com base na realidade financeira atual do usuário.

---

## 📄 Licença

Este projeto é desenvolvido para fins de estudo e portfólio.
