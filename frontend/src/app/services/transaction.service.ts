import { Injectable } from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {TransactionUpdateDto} from '../models/Transaction.model';


export interface Transaction {
  id?: string;
  description: string;
  amount: number;
  date: string | Date;
  type: 'INCOME' | 'EXPENSE' | 'INVESTMENT' | 'TRANSFER';
  source: string;
  categoryName?: string;
  categoryColor?: string;
  categoryIcon?: string;
  merchantName?: string;
  fixed?: boolean;

  totalInstallments?: number;
  currentInstallment?: number;
}

export interface DashboardSummary {
  totalIncome?: number;
  totalExpense?: number;
  balance?: number;
  currentBalance?: number;
  averageIncome?: number;
  totalFixedExpense?: number;
  safeToExpend?: number;
  commitmentPct?: number;
}

@Injectable({
  providedIn: 'root',
})
export class TransactionService {
  private apiUrl = `${environment.apiUrl}/transactions`;

  constructor(private http: HttpClient) {}

  findAll(year?: number, month?: number): Observable<Transaction[]>{
    let params = new HttpParams();
    if (year) params = params.set('year', year);
    if (month) params = params.set('month', month);

    return this.http.get<Transaction[]>(this.apiUrl, { params });
  }

  getSummary(year?: number, month?: number): Observable<DashboardSummary> {
    let params = new HttpParams();
    if (year) params = params.set('year', year);
    if (month) params = params.set('month', month);
    return this.http.get<DashboardSummary>(`${this.apiUrl}/summary`, { params });
  }

  create(transaction: Transaction): Observable<Transaction>{
    return this.http.post<Transaction>(this.apiUrl, transaction);
  }
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  sync(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/sync`, {});
  }

  savePluggyItemId(itemId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/link-pluggy`, { itemId });
  }
  updateTransaction(id: string, dto: TransactionUpdateDto): Observable<Transaction>{
    return this.http.patch<Transaction>(`${this.apiUrl}/${id}`,dto)
  }
  toggleFixedExpense(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/toggle-fixed`, {});
  }
}
