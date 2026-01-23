import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ToolbarModule} from 'primeng/toolbar';
import {ButtonModule} from 'primeng/button';
import {CardModule} from 'primeng/card';
import {AuthService} from '../../services/auth.service';
import {TableModule} from 'primeng/table';
import { TransactionService, Transaction } from '../../services/transaction.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    ToolbarModule,
    ButtonModule,
    CardModule,
    TableModule
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit{
  transactions: Transaction[] = [];
  loading = true;

  constructor(
    private authService: AuthService,
    private transactionService: TransactionService
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
        console.log("Dados recebidos do Java: ", data);
      },
      error: (err) => {
        console.error("Erro ao buscar transações: ", err);
        this.loading = false;
      }
    })
  }

  logout(){
    this.authService.signOut();
  }
}
