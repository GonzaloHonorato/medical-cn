import { Inject, Injectable, PLATFORM_ID, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import {
  AccountInfo,
  AuthenticationResult,
  PublicClientApplication
} from '@azure/msal-browser';
import { authConfig } from '../auth.config';
import { RuntimeConfigService } from './runtime-config.service';

export interface AuthenticatedUser {
  name: string;
  email: string;
  username: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly currentUser = signal<AuthenticatedUser | null>(null);
  readonly userName = signal('');
  readonly idToken = signal('');
  readonly accessToken = signal('');
  readonly errorMessage = signal('');
  readonly isReady = signal(false);

  private msalInstance: PublicClientApplication | null = null;
  private initializePromise: Promise<void> | null = null;
  private readonly isBrowser: boolean;

  constructor(
    @Inject(PLATFORM_ID) platformId: object,
    private runtimeConfig: RuntimeConfigService
  ) {
    this.isBrowser = isPlatformBrowser(platformId);

    if (this.isBrowser) {
      this.initializePromise = this.initialize();
    }
  }

  async initialize(): Promise<void> {
    try {
      this.msalInstance = new PublicClientApplication({
        auth: authConfig
      });

      await this.msalInstance.initialize();

      const response = await this.msalInstance.handleRedirectPromise();
      this.applyAuthenticationResult(response);

      if (!response) {
        const accounts = this.msalInstance.getAllAccounts();

        if (accounts.length > 0) {
          this.msalInstance.setActiveAccount(accounts[0]);
          this.setUserFromAccount(accounts[0]);
          await this.loadTokenSilently();
        }
      }

      this.isReady.set(true);
    } catch (error) {
      console.error(error);
      this.errorMessage.set('Error inicializando Azure Active Directory');
    }
  }

  async login(): Promise<void> {
    if (!this.isBrowser) {
      return;
    }

    if (!this.initializePromise) {
      this.initializePromise = this.initialize();
    }

    await this.initializePromise;

    if (!this.msalInstance) {
      return;
    }

    try {
      await this.msalInstance.loginRedirect({
        scopes: this.runtimeConfig.scopes()
      });
    } catch (error) {
      console.error(error);
      this.errorMessage.set('Error iniciando sesión');
    }
  }

  async logout(): Promise<void> {
    if (!this.msalInstance) {
      return;
    }

    await this.msalInstance.logoutRedirect({
      account: this.msalInstance.getActiveAccount()
    });
  }

  async getApiToken(): Promise<string> {
    const account = this.msalInstance?.getActiveAccount();

    if (!this.msalInstance || !account) {
      return this.accessToken() || this.idToken();
    }

    try {
      const response = await this.msalInstance.acquireTokenSilent({
        account,
        scopes: this.runtimeConfig.scopes()
      });

      this.idToken.set(response.idToken);
      this.accessToken.set(response.accessToken);
      return response.accessToken || response.idToken;
    } catch (error) {
      console.warn('No se pudo obtener access token para API, se usara el token disponible.', error);
      return this.accessToken() || this.idToken();
    }
  }

  private applyAuthenticationResult(response: AuthenticationResult | null): void {
    if (!response) {
      return;
    }

    this.msalInstance?.setActiveAccount(response.account);
    this.setUserFromAccount(response.account);
    this.idToken.set(response.idToken);
    this.accessToken.set(response.accessToken);
  }

  private setUserFromAccount(account: AccountInfo | null): void {
    const claims = account?.idTokenClaims as Record<string, unknown> | undefined;
    const email = this.getFirstStringClaim(claims, ['emails', 'email']) || account?.username || '';
    const name =
      this.getStringClaim(claims, 'name') ||
      this.getStringClaim(claims, 'given_name') ||
      email ||
      account?.username ||
      '';
    const username = account?.username || email;

    this.userName.set(username);
    this.currentUser.set({
      name,
      email,
      username
    });
  }

  private getStringClaim(
    claims: Record<string, unknown> | undefined,
    key: string
  ): string {
    const value = claims?.[key];
    return typeof value === 'string' ? value : '';
  }

  private getFirstStringClaim(
    claims: Record<string, unknown> | undefined,
    keys: string[]
  ): string {
    for (const key of keys) {
      const value = claims?.[key];

      if (typeof value === 'string') {
        return value;
      }

      if (Array.isArray(value) && typeof value[0] === 'string') {
        return value[0];
      }
    }

    return '';
  }

  private async loadTokenSilently(): Promise<void> {
    const account = this.msalInstance?.getActiveAccount();

    if (!this.msalInstance || !account) {
      return;
    }

    try {
      const response = await this.msalInstance.acquireTokenSilent({
        account,
        scopes: this.runtimeConfig.scopes()
      });

      this.idToken.set(response.idToken);
      this.accessToken.set(response.accessToken);
    } catch (error) {
      console.warn('No se pudo obtener el token en silencio', error);
    }
  }
}
