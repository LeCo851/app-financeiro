// Enums para garantir que o texto vá igual ao do Java
export enum TransactionType {
  INCOME = 'INCOME',
  EXPENSE = 'EXPENSE',
  INVESTMENT = 'INVESTMENT',
  TRANSFER = 'TRANSFER'
}

// O DTO que o seu endpoint PATCH espera
export interface TransactionUpdateDto {
  categoryId?: string;   // Opcional
  type?: TransactionType; // Opcional
  description?: string;  // Opcional
}
