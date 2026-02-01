import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment'; // Ajuste baseado na sua pasta environments
import { Observable } from 'rxjs';

export interface Category {
  id: string;
  name: string;
  icon?: string;
  color?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private http = inject(HttpClient);
  // Garanta que environment.apiUrl esteja definido no seu environment.ts
  private apiUrl = `${environment.apiUrl}/categories`;

  findAll(): Observable<Category[]> {
    return this.http.get<Category[]>(this.apiUrl);
  }
}
