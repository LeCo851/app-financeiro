import {environment} from '../../environment/environment';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';


export interface ConnectTokenResponse {
  accessToken: string;
}

@Injectable({
  providedIn: 'root'
})

export class OpenFinanceService{
  private apiUrl = `${environment.apiUrl}/open-finance`;

  constructor(private http: HttpClient) {}

  getConnectToken(): Observable<ConnectTokenResponse>{
    return this.http.get<ConnectTokenResponse>(`${this.apiUrl}/connect-token`)
  }

  syncConnection(itemId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/sync`,{ itemId });
  }
}
