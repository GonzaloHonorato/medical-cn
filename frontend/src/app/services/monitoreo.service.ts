import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, from, switchMap } from 'rxjs';
import { AuthService } from './auth.service';
import { RuntimeConfigService } from './runtime-config.service';
import { DashboardMedico, SignoVital, SignoVitalRequest } from '../models/monitoreo.model';

@Injectable({ providedIn: 'root' })
export class MonitoreoService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  private runtimeConfig = inject(RuntimeConfigService);

  getDashboard(): Observable<DashboardMedico> {
    return from(this.authHeaders()).pipe(
      switchMap(headers =>
        this.http.get<DashboardMedico>(this.runtimeConfig.apiUrl('/api/dashboard'), { headers })
      )
    );
  }

  registrarSignosVitales(request: SignoVitalRequest): Observable<SignoVital> {
    return from(this.authHeaders()).pipe(
      switchMap(headers =>
        this.http.post<SignoVital>(this.runtimeConfig.apiUrl('/api/signos-vitales'), request, { headers })
      )
    );
  }

  atenderAlerta(id: number): Observable<void> {
    return from(this.authHeaders()).pipe(
      switchMap(headers =>
        this.http.patch<void>(this.runtimeConfig.apiUrl(`/api/alertas/${id}/atender`), null, { headers })
      )
    );
  }

  private async authHeaders(): Promise<HttpHeaders> {
    const token = await this.auth.getApiToken();
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}
