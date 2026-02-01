import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DynamicDialogRef, DynamicDialogConfig } from 'primeng/dynamicdialog';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { TransactionType } from '../../models/Transaction.model';
import { TransactionService } from '../../services/transaction.service';
import { CategoryService } from '../../services/category.service';

@Component({
  selector: 'app-edit-transaction-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    SelectModule,
    InputTextModule
  ],
  templateUrl: './edit-transaction-dialog.html',
  styleUrl: './edit-transaction-dialog.scss'
})
export class EditTransactionDialog implements OnInit {
  private fb = inject(FormBuilder);
  private transactionService = inject(TransactionService);
  private categoryService = inject(CategoryService);

  public ref = inject(DynamicDialogRef);
  public config = inject(DynamicDialogConfig);

  categories: any[] = [];

  types = [
    { label: 'Receita (Entrada)', value: 'INCOME' },
    { label: 'Despesa (Saída)', value: 'EXPENSE' },
    { label: 'Investimento', value: 'INVESTMENT' },
    { label: 'Transferência', value: 'TRANSFER' }
  ];

  form = this.fb.group({
    description: ['', Validators.required],
    type: ['EXPENSE', Validators.required],
    categoryId: ['', Validators.required]
  });

  ngOnInit() {
    // 1. Busca categorias PRIMEIRO para garantir que o dropdown tenha opções
    this.categoryService.findAll().subscribe({
      next: (data) => {
        this.categories = data.map(c => ({
          label: c.name,
          value: c.id,
          icon: c.icon,
          color: c.color
        }));

        // 2. DEPOIS carrega os dados da transação no form
        this.loadTransactionData();
      },
      error: (err) => console.error('Erro ao buscar categorias', err)
    });
  }

  loadTransactionData() {
    const tx = this.config.data;
    if (tx) {
      // Tenta encontrar o ID da categoria de várias formas possíveis
      let catId = '';

      if (tx.category && tx.category.id) {
        catId = tx.category.id;
      } else if (tx.categoryId) {
        catId = tx.categoryId;
      } else if (tx.categoryName) {
        // Fallback: tenta encontrar pelo nome se o ID não estiver disponível
        const found = this.categories.find(c => c.label === tx.categoryName);
        if (found) catId = found.value;
      }

      this.form.patchValue({
        description: tx.description,
        type: tx.type,
        categoryId: catId
      });
    }
  }

  save() {
    if (this.form.valid) {
      const id = this.config.data.id;

      const payload = {
        description: this.form.value.description as string,
        type: this.form.value.type as TransactionType,
        categoryId: this.form.value.categoryId as string
      };

      this.transactionService.updateTransaction(id, payload).subscribe({
        next: (updatedTx) => this.ref.close(updatedTx),
        error: (err) => console.error('Erro ao salvar', err)
      });
    }
  }
}
