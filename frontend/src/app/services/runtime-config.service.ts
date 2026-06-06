import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

interface FrontendConfig {
  apiBaseUrl?: string;
  authScopes?: string[];
}

@Injectable({ providedIn: 'root' })
export class RuntimeConfigService {
  private http = inject(HttpClient);
  private apiBaseUrl = '';
  private authScopes = ['openid', 'profile'];

  async load(): Promise<void> {
    try {
      const config = await firstValueFrom(this.http.get<FrontendConfig>('/frontend-config.json'));
      this.apiBaseUrl = this.normalizeBaseUrl(config.apiBaseUrl ?? '');
      this.authScopes = this.normalizeScopes(config.authScopes);
    } catch (error) {
      console.warn('No se pudo cargar configuracion runtime, se usaran valores locales.', error);
    }
  }

  apiUrl(path: string): string {
    const normalizedPath = path.startsWith('/') ? path : `/${path}`;
    return `${this.apiBaseUrl}${normalizedPath}`;
  }

  scopes(): string[] {
    return this.authScopes;
  }

  private normalizeBaseUrl(value: string): string {
    const trimmed = value.trim();
    return trimmed.endsWith('/') ? trimmed.slice(0, -1) : trimmed;
  }

  private normalizeScopes(value: string[] | undefined): string[] {
    const scopes = value?.map(scope => scope.trim()).filter(Boolean) ?? [];
    return scopes.length > 0 ? scopes : ['openid', 'profile'];
  }
}
