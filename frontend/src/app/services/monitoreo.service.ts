import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { DashboardMedico, SignoVital, SignoVitalRequest } from '../models/monitoreo.model';

@Injectable({ providedIn: 'root' })
export class MonitoreoService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  getDashboard(): Observable<DashboardMedico> {
    return this.http.get<DashboardMedico>('/api/dashboard', {
      headers: this.authHeaders()
    });
  }

  registrarSignosVitales(request: SignoVitalRequest): Observable<SignoVital> {
    return this.http.post<SignoVital>('/api/signos-vitales', request, {
      headers: this.authHeaders()
    });
  }

  atenderAlerta(id: number): Observable<void> {
    return this.http.patch<void>(`/api/alertas/${id}/atender`, null, {
      headers: this.authHeaders()
    });
  }

  private authHeaders(): HttpHeaders {
    const token = this.auth.idToken();
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}
