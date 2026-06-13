import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { RuntimeConfigService } from './runtime-config.service';
import { DashboardMedico, EventoClinico, SignoVital, SignoVitalRequest } from '../models/monitoreo.model';

@Injectable({ providedIn: 'root' })
export class MonitoreoService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  private runtimeConfig = inject(RuntimeConfigService);

  getDashboard(): Observable<DashboardMedico> {
    return this.http.get<DashboardMedico>(this.runtimeConfig.apiUrl('/api/dashboard'), {
      headers: this.authHeaders()
    });
  }

  registrarSignosVitales(request: SignoVitalRequest): Observable<SignoVital> {
    return this.http.post<SignoVital>(this.runtimeConfig.apiUrl('/api/signos-vitales'), request, {
      headers: this.authHeaders()
    });
  }

  atenderAlerta(id: number): Observable<void> {
    return this.http.patch<void>(this.runtimeConfig.apiUrl(`/api/alertas/${id}/atender`), null, {
      headers: this.authHeaders()
    });
  }

  getEventosClinicos(): Observable<EventoClinico[]> {
    return this.http.get<EventoClinico[]>(this.runtimeConfig.apiUrl('/api/eventos-clinicos'), {
      headers: this.authHeaders()
    });
  }

  private authHeaders(): HttpHeaders {
    const token = this.auth.idToken();
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}
