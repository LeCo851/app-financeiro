import {Component, OnInit, ViewChild, Renderer2, Inject, PLATFORM_ID, inject} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';

// PrimeNG Modules
import { ToolbarModule } from 'primeng/toolbar';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { Table, TableModule } from 'primeng/table';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { FloatLabelModule } from 'primeng/floatlabel';
import { DatePickerModule } from 'primeng/datepicker';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';

// RxJS
import { forkJoin } from 'rxjs';

// Services & Models
import { AuthService } from '../../services/auth.service';
import { TransactionService, Transaction } from '../../services/transaction.service';
import { OpenFinanceService } from '../../services/open-finance.service';
import { InvestmentService } from '../../services/investment.service';
import { Investment } from '../../models/investment.model';
import { PluggyConnect } from 'pluggy-connect-sdk';
import { PlotlyModule } from 'angular-plotly.js';
import {DialogService} from 'primeng/dynamicdialog';
import {EditTransactionDialog} from '../../components/edit-transaction-dialog/edit-transaction-dialog';

// Interface estendida
interface DashboardTransaction extends Transaction {
  typeLabel: string;
}

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
    TagModule,
    TooltipModule,
    PlotlyModule,
    IconFieldModule,
    InputIconModule
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
  providers:[DialogService]
})
export class Dashboard implements OnInit {
  @ViewChild('dt') dt!: Table;

  private dialogService = inject(DialogService);
  // --- DADOS ---
  transactions: DashboardTransaction[] = [];
  investments: Investment[] = [];

  loading = true;
  saving = false;
  connecting = false;
  refreshing = false;
  isSyncing = false;

  // --- KPIs ---
  totalBalance = 0;
  currentBalance = 0;
  averageIncome = 0;
  totalIncome = 0;
  totalExpense = 0;
  totalInvested = 0;

  // --- GRÁFICOS PLOTLY ---
  public donutGraph: any = { data: [], layout: {}, config: {} };
  public barGraph: any = { data: [], layout: {}, config: {} };
  public treemapGraph: any = { data: [], layout: {}, config: {} };

  // --- CONTROLE UI ---
  displayDialog = false;
  searchValue: string | undefined;
  isDarkMode = false;

  // --- FILTROS DE DATA ---
  selectedYear: number = new Date().getFullYear();
  selectedMonth: number = new Date().getMonth() + 1;

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
    { label: 'Investimento', value: 'INVESTMENT' }
  ];

  // --- CORES DO TEMA ---
  readonly colors = {
    primary: '#6366f1',   // Indigo 500
    success: '#10b981',   // Emerald 500
    danger: '#ef4444',    // Red 500
    warning: '#f59e0b',   // Amber 500
    info: '#3b82f6',      // Blue 500
    investment: '#8b5cf6', // Violet 500
    text: '#64748b',      // Slate 500
    grid: '#f1f5f9',      // Slate 100
    bg: '#ffffff',
    palette: [
      '#6366f1', // Indigo
      '#3b82f6', // Blue
      '#0ea5e9', // Sky
      '#14b8a6', // Teal
      '#8b5cf6', // Violet
      '#ec4899', // Pink
      '#f59e0b', // Amber
      '#64748b'  // Slate
    ]
  };

  constructor(
    private authService: AuthService,
    private transactionService: TransactionService,
    private openFinanceService: OpenFinanceService,
    private investmentService: InvestmentService,
    private renderer: Renderer2,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit() {
    this.initTheme();
    this.initYears();
    this.initChartsConfig();
    this.loadData();
    this.loadHistoryData();
  }

  // --- THEME LOGIC ---
  initTheme() {
    if (isPlatformBrowser(this.platformId)) {
      const savedTheme = localStorage.getItem('theme');
      if (savedTheme === 'dark') {
        this.isDarkMode = true;
        this.renderer.addClass(document.body, 'dark-theme');
      }
    }
  }

  toggleTheme() {
    this.isDarkMode = !this.isDarkMode;
    if (this.isDarkMode) {
      this.renderer.addClass(document.body, 'dark-theme');
      localStorage.setItem('theme', 'dark');
    } else {
      this.renderer.removeClass(document.body, 'dark-theme');
      localStorage.setItem('theme', 'light');
    }
    this.updateChartsTheme();
  }

  updateChartsTheme() {
    const textColor = this.isDarkMode ? '#94a3b8' : '#64748b'; // Slate 400 vs 500
    const gridColor = this.isDarkMode ? '#334155' : '#f1f5f9'; // Slate 700 vs 100
    const bgColor = this.isDarkMode ? '#1e293b' : '#ffffff'; // Slate 800 vs White (Tooltip)

    const updateLayout = (layout: any) => {
      layout.font = { ...layout.font, color: textColor };
      if (layout.xaxis) {
        layout.xaxis.tickfont = { color: textColor };
        layout.xaxis.gridcolor = gridColor;
      }
      if (layout.yaxis) {
        layout.yaxis.tickfont = { color: textColor };
        layout.yaxis.gridcolor = gridColor;
      }
      layout.hoverlabel = {
        ...layout.hoverlabel,
        bgcolor: bgColor,
        bordercolor: gridColor,
        font: { color: this.isDarkMode ? '#f1f5f9' : '#1e293b' }
      };
    };

    updateLayout(this.donutGraph.layout);
    updateLayout(this.barGraph.layout);
    updateLayout(this.treemapGraph.layout);

    // Força atualização dos gráficos
    this.donutGraph = { ...this.donutGraph };
    this.barGraph = { ...this.barGraph };
    this.treemapGraph = { ...this.treemapGraph };
  }

  initYears() {
    const currentYear = new Date().getFullYear();
    for (let i = currentYear - 5; i <= currentYear + 1; i++) {
      this.years.push({ label: i.toString(), value: i });
    }
    this.years.sort((a, b) => b.value - a.value);
  }

  loadData() {
    this.loading = true;

    // 1. Carrega Transações do Mês Selecionado (Tabela e Donut)
    this.transactionService.findAll(this.selectedYear, this.selectedMonth).subscribe({
      next: (data) => {
        this.transactions = data.map(t => ({
          ...t,
          typeLabel: this.getTypeLabel(t.type)
        }));

        this.calculateMetrics();
        this.updateDonutChart();
      },
      error: (err) => {
        console.error("Erro transações:", err);
        this.loading = false;
      }
    });

    // 2. Carrega Saldo Corrente
    this.transactionService.getSummary().subscribe({
      next: (summary: any) => {
        if (summary.currentBalance !== undefined) this.currentBalance = summary.currentBalance;
        if (summary.averageIncome !== undefined) this.averageIncome = summary.averageIncome;
      },
      error: (err) => console.error("Erro saldo:", err)
    });

    // 3. Carrega Investimentos
    this.loadInvestments();
  }

  loadHistoryData() {
    const currentYear = new Date().getFullYear();
    const lastYear = currentYear - 1;

    forkJoin([
      this.transactionService.findAll(lastYear),
      this.transactionService.findAll(currentYear)
    ]).subscribe({
      next: ([lastYearData, currentYearData]) => {
        const allData = [...lastYearData, ...currentYearData];
        this.processHistoryChart(allData);
        this.updateTreemap(allData);
      },
      error: (err) => console.error("Erro histórico:", err)
    });
  }

  processHistoryChart(allTransactions: Transaction[]) {
    const today = new Date();
    const monthsMap = new Map<string, { income: number, expense: number, label: string, year: number, month: number }>();

    for (let i = 11; i >= 0; i--) {
      const d = new Date(today.getFullYear(), today.getMonth() - i, 1);
      const key = `${d.getFullYear()}-${d.getMonth() + 1}`;
      const monthName = this.months.find(m => m.value === d.getMonth() + 1)?.label?.substring(0, 3) || '';
      const label = `${monthName}/${d.getFullYear().toString().substring(2)}`;

      monthsMap.set(key, { income: 0, expense: 0, label: label, year: d.getFullYear(), month: d.getMonth() + 1 });
    }

    const processedIds = new Set<string>();

    allTransactions.forEach(t => {
      if (t.id && processedIds.has(t.id)) return;
      if (t.id) processedIds.add(t.id);

      let year: number;
      let month: number;

      if (typeof t.date === 'string') {
        const parts = t.date.split('-');
        year = parseInt(parts[0], 10);
        month = parseInt(parts[1], 10);
      } else {
        const d = new Date(t.date);
        year = d.getFullYear();
        month = d.getMonth() + 1;
      }

      const key = `${year}-${month}`;

      if (monthsMap.has(key)) {
        const entry = monthsMap.get(key)!;
        if (t.type === 'INCOME') {
          entry.income += t.amount;
        } else if (t.type === 'EXPENSE') {
          entry.expense += t.amount;
        }
      }
    });

    const labels: string[] = [];
    const incomes: number[] = [];
    const expenses: number[] = [];
    const metaData: any[] = [];

    monthsMap.forEach((value) => {
      labels.push(value.label);
      incomes.push(value.income);
      expenses.push(value.expense);
      metaData.push({ year: value.year, month: value.month });
    });

    // Formatação de moeda para os tooltips
    const formattedIncomes = incomes.map(v => this.formatCurrency(v));
    const formattedExpenses = expenses.map(v => this.formatCurrency(v));

    this.barGraph.data = [
      {
        x: labels, y: incomes, name: 'Receitas', type: 'bar',
        marker: { color: this.colors.success, opacity: 0.9 },
        customdata: metaData,
        text: formattedIncomes,
        hovertemplate: '<b>%{x}</b><br>Receitas: %{text}<extra></extra>'
      },
      {
        x: labels, y: expenses, name: 'Despesas', type: 'bar',
        marker: { color: this.colors.danger, opacity: 0.9 },
        customdata: metaData,
        text: formattedExpenses,
        hovertemplate: '<b>%{x}</b><br>Despesas: %{text}<extra></extra>'
      }
    ];
  }

  // --- TREEMAP LOGIC ---
  updateTreemap(transactions: Transaction[]) {
    const nodes = new Map<string, { id: string, label: string, parent: string, value: number, color: string }>();

    // Cria dois ramos principais na raiz para separar totalmente as árvores
    nodes.set("Receitas", { id: "Receitas", label: "Receitas", parent: "", value: 0, color: this.colors.success });
    nodes.set("Despesas", { id: "Despesas", label: "Despesas", parent: "", value: 0, color: this.colors.danger });

    const processedIds = new Set<string>();

    transactions.forEach(t => {
      if (t.type === 'TRANSFER' || t.type === 'INVESTMENT') return;
      if (t.id && processedIds.has(t.id)) return;
      if (t.id) processedIds.add(t.id);

      let year: number;
      let month: number;
      if (typeof t.date === 'string') {
        const parts = t.date.split('-');
        year = parseInt(parts[0], 10);
        month = parseInt(parts[1], 10);
      } else {
        const d = new Date(t.date);
        year = d.getFullYear();
        month = d.getMonth() + 1;
      }

      const monthName = this.months.find(m => m.value === month)?.label || 'Mês';
      const category = (t.categoryName || 'Sem Categoria').trim();
      const amount = Math.abs(t.amount);

      // Define a raiz baseada no tipo
      const rootKey = t.type === 'INCOME' ? 'Receitas' : 'Despesas';

      // IDs Hierárquicos
      const yearId = `${rootKey}-${year}`;
      const monthId = `${rootKey}-${year}-${month}`;
      const catId = `${rootKey}-${year}-${month}-${category}`;

      // Cores de fundo (Tons pastéis mais suaves)
      let yearColor, monthColor, catColor;

      if (t.type === 'INCOME') {
        yearColor = '#ecfdf5'; // Emerald-50
        monthColor = '#a7f3d0'; // Emerald-200
        catColor = '#6ee7b7';   // Emerald-300
      } else {
        yearColor = '#fef2f2'; // Red-50
        monthColor = '#fecaca'; // Red-200
        catColor = '#fca5a5';   // Red-300
      }

      // 1. Nó ANO
      if (!nodes.has(yearId)) {
        nodes.set(yearId, {
          id: yearId,
          label: `${year}`,
          parent: rootKey,
          value: 0,
          color: yearColor
        });
      }

      // 2. Nó MÊS
      if (!nodes.has(monthId)) {
        nodes.set(monthId, {
          id: monthId,
          label: monthName,
          parent: yearId,
          value: 0,
          color: monthColor
        });
      }

      // 3. Nó CATEGORIA (Folha final)
      if (!nodes.has(catId)) {
        nodes.set(catId, {
          id: catId,
          label: category,
          parent: monthId,
          value: 0,
          color: catColor
        });
      }

      // Soma valor na categoria e propaga para cima
      const catNode = nodes.get(catId)!;
      catNode.value += amount;

      const monthNode = nodes.get(monthId)!;
      monthNode.value += amount;

      const yearNode = nodes.get(yearId)!;
      yearNode.value += amount;

      const rootNode = nodes.get(rootKey)!;
      rootNode.value += amount;
    });

    const ids: string[] = [];
    const labels: string[] = [];
    const parents: string[] = [];
    const values: number[] = [];
    const colors: string[] = [];
    const formattedValues: string[] = [];

    nodes.forEach(node => {
      ids.push(node.id);
      labels.push(node.label);
      parents.push(node.parent);
      values.push(node.value);
      colors.push(node.color);
      formattedValues.push(this.formatCurrency(node.value));
    });

    this.treemapGraph.data = [{
      type: 'treemap',
      ids: ids,
      labels: labels,
      parents: parents,
      values: values,
      marker: { colors: colors },
      text: formattedValues,
      textinfo: 'label+text',
      branchvalues: 'total',
      hovertemplate: '<b>%{label}</b><br>%{text}<br><extra></extra>',
      tiling: { packing: 'squarify' },
      pathbar: { visible: true }
    }];
  }

  getTypeLabel(type: string): string {
    switch(type) {
      case 'INCOME': return 'Entrada';
      case 'EXPENSE': return 'Saída';
      case 'INVESTMENT': return 'Investimento';
      case 'TRANSFER': return 'Transferência';
      default: return 'Outro';
    }
  }

  loadInvestments() {
    this.investmentService.findAll().subscribe({
      next: (data) => {
        this.investments = data;
        this.totalInvested = this.investments.reduce((acc, inv) => acc + inv.balance, 0);
        this.loading = false;
      },
      error: (err) => {
        console.error("Erro investimentos:", err);
        this.loading = false;
      }
    });
  }

  calculateMetrics() {
    this.totalIncome = this.transactions
      .filter(t => t.type === 'INCOME')
      .reduce((acc, t) => acc + t.amount, 0);

    this.totalExpense = this.transactions
      .filter(t => t.type === 'EXPENSE')
      .reduce((acc, t) => acc + t.amount, 0);

    this.totalBalance = this.totalIncome + this.totalExpense;
  }

  initChartsConfig() {
    const commonConfig = {
      responsive: true,
      displayModeBar: 'hover', // Reativado: mostra a barra ao passar o mouse
      scrollZoom: true, // Reativado: permite zoom
      modeBarButtonsToRemove: ['lasso2d', 'select2d', 'sendDataToCloud'] // Remove botões desnecessários
    };

    const commonLayout = {
      paper_bgcolor: 'rgba(0,0,0,0)',
      plot_bgcolor: 'rgba(0,0,0,0)',
      font: { family: 'Inter, sans-serif', color: this.colors.text, size: 12 },
      margin: { t: 20, b: 30, l: 40, r: 20 },
      autosize: true,
      hovermode: 'closest',
      hoverlabel: {
        bgcolor: '#ffffff',
        bordercolor: '#e2e8f0',
        font: { color: '#1e293b', family: 'Inter, sans-serif' }
      }
    };

    // --- DONUT ---
    this.donutGraph.config = { ...commonConfig };
    this.donutGraph.layout = {
      ...commonLayout,
      showlegend: true,
      legend: { orientation: 'h', y: -0.1, font: { size: 11 } },
      margin: { t: 10, b: 50, l: 10, r: 10 }
    };

    // --- BAR ---
    this.barGraph.config = { ...commonConfig };
    this.barGraph.layout = {
      ...commonLayout,
      dragmode: 'zoom', // Reativado: permite zoom/pan
      barmode: 'group',
      bargap: 0.3,
      xaxis: {
        showgrid: false,
        fixedrange: false, // Reativado: permite mover o eixo
        tickfont: { color: this.colors.text }
      },
      yaxis: {
        showgrid: true,
        gridcolor: this.colors.grid,
        fixedrange: false, // Reativado: permite mover o eixo
        tickfont: { color: this.colors.text }
      },
      legend: { orientation: 'h', y: -0.2, x: 0.3 }
    };

    // --- TREEMAP ---
    this.treemapGraph.config = { ...commonConfig };
    this.treemapGraph.layout = {
      ...commonLayout,
      margin: { t: 0, b: 0, l: 0, r: 0 }
    };
  }

  updateDonutChart() {
    const expensesMap: Record<string, number> = {};
    const labels: string[] = [];

    this.transactions
      .filter(t => t.type === 'EXPENSE')
      .forEach(t => {
        const cat = t.categoryName || 'Sem Categoria';
        expensesMap[cat] = (expensesMap[cat] || 0) + Math.abs(t.amount);
        if (!labels.includes(cat)) labels.push(cat);
      });

    // Ordenar por valor para cores ficarem consistentes (maiores fatias = cores primárias)
    labels.sort((a, b) => expensesMap[b] - expensesMap[a]);

    // Formatação de moeda
    const formattedValues = labels.map(l => this.formatCurrency(expensesMap[l]));

    this.donutGraph.data = [{
      values: labels.map(l => expensesMap[l]),
      labels: labels,
      type: 'pie',
      hole: 0.65,
      textinfo: 'percent',
      textposition: 'inside',
      automargin: true,
      marker: {
        colors: this.colors.palette // Usa a paleta do tema
      },
      text: formattedValues, // Passa valores formatados para o hover
      hovertemplate: '<b>%{label}</b><br>%{text}<br>(%{percent})<extra></extra>',
      textfont: {
        color: '#ffffff',
        weight: 600
      }
    }];
  }

  // Helper para formatar moeda
  formatCurrency(value: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
  }

  // --- AÇÕES UI ---

  onChartClick(event: any, chartType: 'donut' | 'bar') {
    if (!event || !event.points || event.points.length === 0) return;

    const point = event.points[0];

    if (chartType === 'donut') {
      const category = point.label;
      this.searchValue = category;
      this.dt.filterGlobal(category, 'contains');

    } else if (chartType === 'bar') {
      const meta = point.customdata;
      if (meta) {
        this.selectedYear = meta.year;
        this.selectedMonth = meta.month;
        this.loadData();
      }

      const traceName = point.data.name;
      let filterValue = '';
      if (traceName === 'Receitas') filterValue = 'Entrada';
      else if (traceName === 'Despesas') filterValue = 'Saída';

      if (filterValue) {
        setTimeout(() => {
          this.searchValue = filterValue;
          this.dt.filterGlobal(filterValue, 'contains');
        }, 500);
      }
    }
  }

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
        this.loadHistoryData();
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
    this.transactionService.sync().subscribe({
      next: () => {
        this.loadData();
        this.loadHistoryData();
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

  private syncData(itemId: string) {
    this.isSyncing = true;
    if (!itemId) return;

    this.transactionService.savePluggyItemId(itemId).subscribe({
      next: () => {
        this.openFinanceService.syncConnection(itemId).subscribe({
          next: () => {
            this.investmentService.sync(itemId).subscribe({
              next: () => {
                this.loadData();
                this.loadHistoryData();
                this.isSyncing = false;
              },
              error: () => {
                this.loadData();
                this.isSyncing = false;
              }
            });
          },
          error: () => this.isSyncing = false
        });
      },
      error: () => this.isSyncing = false
    });
  }

  deleteTransaction(transaction: Transaction){
    if(!transaction.id) return;

    if(confirm('Tem certeza que deseja excluir essa transação?')){
      this.transactionService.delete(transaction.id).subscribe({
        next: () => {
          this.transactions = this.transactions.filter(t => t.id !== transaction.id);
          this.calculateMetrics();
          this.updateDonutChart();
          this.loadHistoryData();

          this.transactionService.getSummary().subscribe(res =>{
            if (res.currentBalance !== undefined) this.currentBalance = res.currentBalance;
          });
        },
        error: (err) => console.error("Erro ao excluior: ", err)
      });
    }
  }
  editTransaction(transaction: any) {
    if (this.dialogService) {
      const ref = this.dialogService.open(EditTransactionDialog, {
        header: 'Editar Transação',
        width: '450px',
        contentStyle: { overflow: 'visible' },
        baseZIndex: 10000,
        data: transaction
      });

      if (ref) {
        ref.onClose.subscribe((updatedTransaction) => {
          if (updatedTransaction) {
            this.loadData();
          }
        });
      }
    }
  }

  toggleFixed(transaction: Transaction) {
    if (!transaction.id) return;

    this.transactionService.toggleFixedExpense(transaction.id).subscribe({
      next: () => {
        transaction.fixed = !transaction.fixed;
      },
      error: (err) => console.error('Erro ao alterar status fixo:', err)
    });
  }

  logout() {
    this.authService.signOut();
  }
}
