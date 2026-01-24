import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ToolbarModule} from 'primeng/toolbar';
import {ButtonModule} from 'primeng/button';
import {CardModule} from 'primeng/card';
import {AuthService} from '../../services/auth.service';
import {TableModule} from 'primeng/table';
import { TransactionService, Transaction } from '../../services/transaction.service';
import {DialogModule} from 'primeng/dialog';
import {InputTextModule} from 'primeng/inputtext';
import {InputNumberModule} from 'primeng/inputnumber';
import {FloatLabelModule} from 'primeng/floatlabel';
import {DatePickerModule} from 'primeng/datepicker';
import {SelectModule} from 'primeng/select';
import {FormsModule} from '@angular/forms';
import {OpenFinanceService} from '../../services/open-finance.service';
import {PluggyConnect} from 'pluggy-connect-sdk';


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
    FormsModule
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit{
  transactions: Transaction[] = [];
  loading = true;
  saving = false;
  connecting = false;

  displayDialog = false;

  currentTransaction: Transaction ={
    description: '',
    amount: 0,
    date: new Date(),
    type: 'INCOME'
  };

  types = [
    { label: 'Receita', value: 'INCOME'},
    { label: 'Despesa', value: 'EXPENSE'}
  ];

  constructor(
    private authService: AuthService,
    private transactionService: TransactionService,
    private openFinanceService: OpenFinanceService
  ) {}

  ngOnInit() {
    this.loadData();
  }

  loadData(){
    this.loading = true;
    this.transactionService.findAll().subscribe({
      next: (data) =>{
        this.transactions = data;
        this.loading = false;
      },
      error: (err) => {
        console.error("Erro ao buscar transações: ", err);
        this.loading = false;
      }
    })
  }

  showDialog(){
    this.currentTransaction = {
      description: '',
      amount: 0,
      date: new Date(),
      type: 'INCOME'
    };
    this.displayDialog = true;
  }

  saveTransaction(){
    if(!this.currentTransaction.description || !this.currentTransaction.amount){
      return;
    }
    this.saving = true

    const payload = {...this.currentTransaction};
    if (payload.date instanceof Date) {
      payload.date = payload.date.toISOString().split('T')[0];
    }

    this.transactionService.create(payload).subscribe({
      next: (res) => {
        console.log('Salvo com sucesso', res);
        this.displayDialog = false;
        this.saving = false;
        this.loadData();
      },
      error: (err) => {
        console.error('Erro ao salvar: ', err);
        this.saving = false;
      }
    })
  }

  connectBank(){
    this.connecting = true;

    this.openFinanceService.getConnectToken().subscribe({
      next: res => {
        this.initPluggyWidget(res.accessToken);
        this.connecting = false;
        console.log('RESPOSTA DO BACKEND:', res);
      },
      error: err => {
        console.error("Erro ao conectar no Open Finance: ", err);
        this.connecting = false;
      }
    });
  }

  private initPluggyWidget(accessToken: string){
    const pluggyConnect = new PluggyConnect({
      connectToken: accessToken,
      includeSandbox: true,

      onSuccess: itemData => {
        console.log("Conexao realizada com sucesso", itemData);
        this.syncData(itemData.item.id);
      },
      onError: error => {
        console.error("Erro no Widget: ", error);
      },
    });
    pluggyConnect.init()
      .then(() => {
        console.log("Widget Pluggy aberto com sucesso.");
      })
      .catch((err: any) =>{
        console.error("Falha ao abrir o Widget Pluggy", err)
      })
  }

  private syncData(itemId: string){
    this.loading = true;
    console.log('ENVIANDO PARA O JAVA -> Item ID:', itemId);
    if (!itemId) {
      console.error('ABORTANDO: Item ID está vazio!');
      return;
    }
    this.openFinanceService.syncConnection(itemId).subscribe({
      next: () => {
        console.log("Sincronização concluída!");

        this.loadData();
        this.loading = false;
      },
      error: err => {
        console.error("Erro ao sincronizar: ", err);
        this.loading = false;
      }
    })
  }

  logout(){
    this.authService.signOut();
  }
}
