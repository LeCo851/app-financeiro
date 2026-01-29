import { Injectable } from '@angular/core';
import {environment} from '../../environment/environment';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';


export interface Transaction {
  id?: string;
  description: string;
  amount: number;
  date: string | Date;
  type: 'INCOME' | 'EXPENSE' | 'INVESTMENT';
  source: string;
  categoryName?: string;
  categoryColor?: string;
  categoryIcon?: string;
  merchantName?: string;

  totalInstallments?: number;
  currentInstallment?: number;
}

export interface DashboardSummary {
  totalIncome?: number;
  totalExpense?: number;
  balance?: number;
  currentBalance?: number;
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

  getSummary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.apiUrl}/summary`);
  }

  create(transaction: Transaction): Observable<Transaction>{
    return this.http.post<Transaction>(this.apiUrl, transaction);
  }
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
