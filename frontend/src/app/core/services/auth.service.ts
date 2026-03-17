import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap, catchError, EMPTY } from 'rxjs';

export interface AuthResponse {
  userId: string;
  email: string;
  role: string;
  accessToken: string;
  refreshToken: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'access_token';
  private readonly refreshKey = 'refresh_token';
  private readonly userKey = 'user';

  private currentUser = signal<AuthResponse | null>(this.loadUser());

  user = this.currentUser.asReadonly();
  isAuthenticated = computed(() => !!this.currentUser());
  isAdmin = computed(() => this.currentUser()?.role === 'ADMIN');

  constructor(private http: HttpClient, private router: Router) {}

  register(request: RegisterRequest) {
    return this.http.post<AuthResponse>('/api/auth/register', request).pipe(
      tap(response => this.handleAuth(response))
    );
  }

  login(request: LoginRequest) {
    return this.http.post<AuthResponse>('/api/auth/login', request).pipe(
      tap(response => this.handleAuth(response))
    );
  }

  refresh() {
    const refreshToken = localStorage.getItem(this.refreshKey);
    return this.http.post<AuthResponse>('/api/auth/refresh', { refreshToken }).pipe(
      tap(response => this.handleAuth(response))
    );
  }

  logout() {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.refreshKey);
    localStorage.removeItem(this.userKey);
    this.currentUser.set(null);
    this.router.navigate(['/auth/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  validateSession() {
    if (!this.getToken()) return;
    this.http.get('/api/profile').pipe(
      catchError(() => { this.logout(); return EMPTY; })
    ).subscribe();
  }

  private handleAuth(response: AuthResponse) {
    localStorage.setItem(this.tokenKey, response.accessToken);
    localStorage.setItem(this.refreshKey, response.refreshToken);
    localStorage.setItem(this.userKey, JSON.stringify(response));
    this.currentUser.set(response);
  }

  private loadUser(): AuthResponse | null {
    const stored = localStorage.getItem(this.userKey);
    return stored ? JSON.parse(stored) : null;
  }
}
