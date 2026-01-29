import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environment/environment';
import { Investment } from '../models/investment.model';

@Injectable({
  providedIn: 'root'
})
export class InvestmentService {

  // Ajuste a URL base se necessário (ex: http://localhost:8080/api)
  private readonly API_URL = `${environment.apiUrl}/investments`;

  constructor(private http: HttpClient) { }

  // GET /api/investments
  findAll(): Observable<Investment[]> {
    return this.http.get<Investment[]>(this.API_URL);
  }

  // POST /api/investments/sync
  // Envia o itemId para o backend buscar na Pluggy
  sync(itemId: string): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/sync`, { itemId });
  }
}
