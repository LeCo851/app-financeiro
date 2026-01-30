import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// PrimeNG Modules
import { ToolbarModule } from 'primeng/toolbar';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { FloatLabelModule } from 'primeng/floatlabel';
import { DatePickerModule } from 'primeng/datepicker';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';     // <--- NOVO: Para as tags coloridas
import { TooltipModule } from 'primeng/tooltip'; // <--- NOVO: Para dicas de tela
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';

// PLOTLY (Via Window - Solução Estável)

// Services & Models
import { AuthService } from '../../services/auth.service';
import { TransactionService, Transaction } from '../../services/transaction.service';
import { OpenFinanceService } from '../../services/open-finance.service';
import { InvestmentService } from '../../services/investment.service'; // <--- NOVO
import { Investment } from '../../models/investment.model'; // <--- NOVO
import { PluggyConnect } from 'pluggy-connect-sdk';
import {PlotlyModule} from 'angular-plotly.js';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    ToolbarModule,
    ButtonModule,
    CardModule,
    TableModule,
    DialogModule,
    InputTextModule,
    InputNumberModule,
    DatePickerModule,
    SelectModule,
    FloatLabelModule,
    FormsModule,
    TagModule,       // <--- Adicionado
    TooltipModule,   // <--- Adicionado
    PlotlyModule,
    IconFieldModule,
    InputIconModule
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {

  // --- DADOS ---
  transactions: Transaction[] = [];
  investments: Investment[] = []; // <--- Lista de Investimentos

  loading = true;
  saving = false;
  connecting = false;
  refreshing = false;
  isSyncing = false; // <--- Controle do Dialog de Sincronização

  // --- KPIs ---
  totalBalance = 0; // Saldo do Mês/Ano selecionado
  currentBalance = 0; // Saldo Corrente (Acumulado até hoje)
  averageIncome = 0; // Saldo Médio Mensal (Salário Médio)
  totalIncome = 0;
  totalExpense = 0;
  totalInvested = 0; // <--- Total acumulado em Investimentos

  // --- GRÁFICOS PLOTLY ---
  public donutGraph: any = { data: [], layout: {}, config: {} };
  public barGraph: any = { data: [], layout: {}, config: {} };

  // --- CONTROLE UI ---
  displayDialog = false;
  searchValue: string | undefined;

  // --- FILTROS DE DATA ---
  selectedYear: number = new Date().getFullYear();
  selectedMonth: number = new Date().getMonth() + 1; // Janeiro é 0 no JS

  years: any[] = [];
  months = [
    { label: 'Janeiro', value: 1 },
    { label: 'Fevereiro', value: 2 },
    { label: 'Março', value: 3 },
    { label: 'Abril', value: 4 },
    { label: 'Maio', value: 5 },
    { label: 'Junho', value: 6 },
    { label: 'Julho', value: 7 },
    { label: 'Agosto', value: 8 },
    { label: 'Setembro', value: 9 },
    { label: 'Outubro', value: 10 },
    { label: 'Novembro', value: 11 },
    { label: 'Dezembro', value: 12 }
  ];

  currentTransaction: Transaction = {
    source: '',
    description: '',
    amount: 0,
    date: new Date(),
    type: 'INCOME'
  };

  types = [
    { label: 'Receita', value: 'INCOME' },
    { label: 'Despesa', value: 'EXPENSE' },
    { label: 'Investimento', value: 'INVESTMENT' } // Opcional: permitir lançar manual
  ];

  constructor(
    private authService: AuthService,
    private transactionService: TransactionService,
    private openFinanceService: OpenFinanceService,
    private investmentService: InvestmentService // <--- Injeção do Service
  ) {}

  ngOnInit() {
    this.initYears();
    this.initChartsConfig(); // Configura visual dos gráficos
    this.loadData();
  }

  initYears() {
    const currentYear = new Date().getFullYear();
    // Gera lista de anos: 5 anos atrás até 1 ano à frente
    for (let i = currentYear - 5; i <= currentYear + 1; i++) {
      this.years.push({ label: i.toString(), value: i });
    }
    // Ordena decrescente para o ano atual aparecer primeiro/perto
    this.years.sort((a, b) => b.value - a.value);
  }

  loadData() {
    this.loading = true;

    // 1. Carrega Transações com filtro de Ano e Mês
    this.transactionService.findAll(this.selectedYear, this.selectedMonth).subscribe({
      next: (data) => {
        this.transactions = data;
        this.calculateMetrics();     // Calcula Totais do Mês
        this.updatePlotlyCharts();   // Atualiza Gráficos
      },
      error: (err) => {
        console.error("Erro transações:", err);
        this.loading = false;
      }
    });

    // 2. Carrega Saldo Corrente (Acumulado)
    this.transactionService.getSummary().subscribe({
      next: (summary: any) => {
        if (summary.currentBalance !== undefined) {
          this.currentBalance = summary.currentBalance;
        }
        if (summary.averageIncome !== undefined) {
          this.averageIncome = summary.averageIncome;
        }
      },
      error: (err) => console.error("Erro ao carregar saldo corrente:", err)
    });

    // 3. Carrega Investimentos
    this.loadInvestments();
  }

  loadInvestments() {
    this.investmentService.findAll().subscribe({
      next: (data) => {
        this.investments = data;
        // Soma o saldo atual de todos os investimentos
        this.totalInvested = this.investments.reduce((acc, inv) => acc + inv.balance, 0);
        this.loading = false; // Finaliza loading geral
      },
      error: (err) => {
        console.error("Erro investimentos:", err);
        this.loading = false;
      }
    });
  }

  calculateMetrics() {
    // Lógica do "Caixa Limpo":
    // Receita = INCOME
    // Despesa = EXPENSE
    // Investment = Ignora nos gráficos, mas conta no Saldo Total

    this.totalIncome = this.transactions
      .filter(t => t.type === 'INCOME')
      .reduce((acc, t) => acc + t.amount, 0);

    this.totalExpense = this.transactions
      .filter(t => t.type === 'EXPENSE')
      .reduce((acc, t) => acc + t.amount, 0);

    // Saldo Total DO MÊS = Receitas - Despesas (Investimentos e Transferências são neutros)
    this.totalBalance = this.totalIncome + this.totalExpense;
  }

  // --- CONFIGURAÇÃO PLOTLY ---
  initChartsConfig() {
    const commonConfig = { responsive: true, displayModeBar: false };
    const commonLayout = {
      paper_bgcolor: 'rgba(0,0,0,0)',
      plot_bgcolor: 'rgba(0,0,0,0)',
      font: { family: 'Inter, sans-serif', color: '#4b5563' },
      margin: { t: 20, b: 20, l: 20, r: 20 },
      autosize: true
    };

    this.donutGraph.config = commonConfig;
    this.donutGraph.layout = { ...commonLayout, showlegend: false };

    this.barGraph.config = commonConfig;
    this.barGraph.layout = { ...commonLayout,
      barmode: 'group',
      xaxis: { showgrid: false },
      yaxis: { showgrid: true, gridcolor: '#e5e7eb' }
    };
  }

  updatePlotlyCharts() {
    // 1. Donut (Só Despesas Reais)
    const expensesMap: Record<string, number> = {};
    const labels: string[] = [];
    // Cores (opcional, pode deixar o plotly decidir ou mapear)

    this.transactions
      .filter(t => t.type === 'EXPENSE') // Ignora INVESTIMENTOS
      .forEach(t => {
        const cat = t.categoryName || 'Sem Categoria';
        expensesMap[cat] = (expensesMap[cat] || 0) + Math.abs(t.amount);
        if (!labels.includes(cat)) labels.push(cat);
      });

    this.donutGraph.data = [{
      values: labels.map(l => expensesMap[l]),
      labels: labels,
      type: 'pie',
      hole: 0.6,
      textinfo: 'percent',
      hoverinfo: 'label+value+percent'
    }];

    // 2. Barras (Receita vs Despesa)
    this.barGraph.data = [
      {
        x: ['Total'], y: [this.totalIncome], name: 'Receitas', type: 'bar',
        marker: { color: '#22c55e' }
      },
      {
        x: ['Total'], y: [this.totalExpense], name: 'Despesas', type: 'bar',
        marker: { color: '#ef4444' }
      }
    ];
  }

  // --- AÇÕES UI ---

  showDialog() {
    this.currentTransaction = {source: '', description: '', amount: 0, date: new Date(), type: 'INCOME' };
    this.displayDialog = true;
  }

  saveTransaction() {
    if (!this.currentTransaction.description || !this.currentTransaction.amount) return;
    this.saving = true;

    const payload = { ...this.currentTransaction };
    if (payload.date instanceof Date) {
      payload.date = payload.date.toISOString().split('T')[0];
    }

    this.transactionService.create(payload).subscribe({
      next: () => {
        this.displayDialog = false;
        this.saving = false;
        this.loadData();
      },
      error: (err) => {
        console.error('Erro ao salvar:', err);
        this.saving = false;
      }
    });
  }

  connectBank() {
    this.connecting = true;
    this.openFinanceService.getConnectToken().subscribe({
      next: res => {
        this.initPluggyWidget(res.accessToken);
        this.connecting = false;
      },
      error: err => {
        console.error("Erro Open Finance:", err);
        this.connecting = false;
      }
    });
  }

  refreshData() {
    this.refreshing = true;
    console.log("Iniciando atualização")
    this.transactionService.sync().subscribe({
      next: () => {
        this.loadData(); // Recarrega KPIs, Gráficos e Tabelas
        this.refreshing = false;
      },
      error: (err) => {
        console.error('Erro ao sincronizar dados:', err);
        this.refreshing = false;
      }
    });
  }

  private initPluggyWidget(accessToken: string) {
    const pluggyConnect = new PluggyConnect({
      connectToken: accessToken,
      includeSandbox: true,
      onSuccess: itemData => this.syncData(itemData.item.id),
      onError: error => console.error("Erro Widget:", error),
    });
    pluggyConnect.init();
  }

  // --- SYNC ENCADEADO ---botão conectar conta
  private syncData(itemId: string) {
    this.isSyncing = true; // Ativa o aviso amigável
    if (!itemId) return;

    console.log("1. Salvando conexão (Item ID)...");

    // Passo 1: Salva o ID no Backend para uso futuro
    this.transactionService.savePluggyItemId(itemId).subscribe({
      next: () => {
        console.log("Item ID Salvo. 2. Sincronizando Transações...");

        // Passo 2: Sincroniza Transações
        this.openFinanceService.syncConnection(itemId).subscribe({
          next: () => {
            console.log("Transações OK. 3. Sincronizando Investimentos...");

            // Passo 3: Sincroniza Investimentos

            this.investmentService.sync(itemId).subscribe({
              next: () => {
                console.log("Investimentos OK. Recarregando tela...");
                this.loadData(); // Recarrega tudo
                this.isSyncing = false; // Finaliza o aviso
              },
              error: (err) => {
                console.error("Erro ao sync investimentos", err);
                this.loadData(); // Recarrega o que deu certo
                this.isSyncing = false;
              }
            });
          },
          error: (err) => {
            console.error("Erro ao sync transações:", err);
            this.isSyncing = false;
          }
        });
      },
      error: (err) => {
        console.error("Erro ao salvar Item ID:", err);
        this.isSyncing = false;
      }
    });
  }

  deleteTransaction(transaction: Transaction){
    if(!transaction.id) return;

    if(confirm('Tem certeza que deseja excluir essa transação?')){
      this.transactionService.delete(transaction.id).subscribe({
        next: () => {
          this.transactions = this.transactions.filter(t => t.id !== transaction.id);

          this.calculateMetrics();
          this.updatePlotlyCharts();

          this.transactionService.getSummary().subscribe(res =>{
            if (res.currentBalance !== undefined) this.currentBalance = res.currentBalance;
          });
        },
        error: (err) => console.error("Erro ao excluior: ", err)
      });
    }
  }

  logout() {
    this.authService.signOut();
  }
}
