import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DynamicDialogRef, DynamicDialogConfig } from 'primeng/dynamicdialog';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { TransactionType } from '../../models/Transaction.model';
// Seus Services (Caminhos ajustados para sua estrutura)
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
    const tx = this.config.data;

    // 1. Carrega dados no form
    if (tx) {
      this.form.patchValue({
        description: tx.description,
        type: tx.type,
        // Atenção aqui: verifique se seu objeto tx vem como 'category: {id: ...}' ou 'categoryId'
        categoryId: tx.category?.id || tx.categoryId
      });
    }

    // 2. Busca categorias do banco
    this.categoryService.findAll().subscribe({
      next: (data) => {
        this.categories = data.map(c => ({
          label: c.name,
          value: c.id,
          icon: c.icon,
          color: c.color
        }));
      },
      error: (err) => console.error('Erro ao buscar categorias', err)
    });
  }

  save() {
    if (this.form.valid) {
      const id = this.config.data.id;

      // CORREÇÃO AQUI: Tipagem explícita
      const payload = {
        description: this.form.value.description as string,
        type: this.form.value.type as TransactionType, // <--- O PULO DO GATO
        categoryId: this.form.value.categoryId as string
      };

      this.transactionService.updateTransaction(id, payload).subscribe({
        next: (updatedTx) => this.ref.close(updatedTx),
        error: (err) => console.error('Erro ao salvar', err)
      });
    }
  }
}
