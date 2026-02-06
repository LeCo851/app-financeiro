import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';
import { Transaction, TransactionService } from '../../services/transaction.service';

@Component({
  selector: 'app-transaction-list',
  standalone: true,
  imports: [CommonModule, TableModule, ButtonModule, TooltipModule],
  templateUrl: './transaction-list.html',
  styleUrl: './transaction-list.scss'
})
export class TransactionList {
  @Input() transactions: Transaction[] = [];
  @Input() loading: boolean = false;
  @Output() onEdit = new EventEmitter<Transaction>();
  @Output() onDelete = new EventEmitter<Transaction>();

  constructor(private service: TransactionService) {}

  editTransaction(transaction: Transaction) {
    this.onEdit.emit(transaction);
  }

  deleteTransaction(transaction: Transaction) {
    this.onDelete.emit(transaction);
  }

  toggleFixed(transaction: Transaction) {
    if (!transaction.id) return;

    // 1. Atualização Otimista (Muda na tela antes de ir pro servidor)
    const originalState = transaction.fixed;
    transaction.fixed = !transaction.fixed;

    // 2. Chama o Backend
    this.service.toggleFixedExpense(transaction.id).subscribe({
      next: () => {
        // Sucesso silencioso (já atualizamos a tela)
        console.log(`Transação ${transaction.description} atualizada.`);
      },
      error: (err) => {
        // Se der erro, desfaz a mudança na tela e avisa
        transaction.fixed = originalState;
        console.error('Erro ao marcar como fixo', err);
      }
    });
  }
}
