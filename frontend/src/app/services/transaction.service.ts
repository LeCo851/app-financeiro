import { Injectable } from '@angular/core';
import {environment} from '../../environment/environment';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';


export interface Transaction {
  id?: string;
  description: string;
  amount: number;
  date: string | Date;
  type: 'INCOME' | 'EXPENSE';
}

@Injectable({
  providedIn: 'root',
})
export class TransactionService {
  private apiUrl = `${environment.apiUrl}/transaction`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<Transaction[]>{
    return this.http.get<Transaction[]>(this.apiUrl);
  }

  create(transaction: Transaction): Observable<Transaction>{
    return this.http.post<Transaction>(this.apiUrl, transaction);
  }
}
