import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-wrapper">
      <div class="card login-card">
        <div class="login-header">
          <h2>Devoluciones App</h2>
          <p>Inicio de Sesión — Sistema de Gestión</p>
        </div>

        <div *ngIf="errorMessage" [class]="errorStatus >= 400 && errorStatus < 500 ? 'alert alert-warning' : 'alert alert-danger'">
          <strong>{{ errorStatus >= 400 && errorStatus < 500 ? 'Advertencia:' : 'Error del servidor:' }}</strong> {{ errorMessage }}
        </div>

        <form (ngSubmit)="onLogin()">
          <div class="form-group">
            <label class="form-label" for="username">Usuario</label>
            <input
              type="text"
              id="username"
              class="form-control"
              [(ngModel)]="username"
              name="username"
              placeholder="Ej: analista1 o supervisor1"
              required
            />
          </div>

          <div class="form-group">
            <label class="form-label" for="password">Contraseña</label>
            <input
              type="password"
              id="password"
              class="form-control"
              [(ngModel)]="password"
              name="password"
              placeholder="••••••••"
              required
            />
          </div>

          <button type="submit" class="btn btn-primary w-full" [disabled]="loading">
            {{ loading ? 'Iniciando sesión...' : 'Ingresar' }}
          </button>
        </form>

        <div class="seed-hints">
          <p><strong>Usuarios de prueba (Seed):</strong></p>
          <ul>
            <li><code>analista1</code> / <code>password123</code> (Rol: ANALISTA)</li>
            <li><code>supervisor1</code> / <code>password123</code> (Rol: SUPERVISOR)</li>
          </ul>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-wrapper {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
      padding: 1rem;
    }
    .login-card {
      width: 100%;
      max-width: 420px;
      padding: 2.5rem;
    }
    .login-header {
      text-align: center;
      margin-bottom: 2rem;
    }
    .login-header h2 {
      font-size: 1.75rem;
      color: #0f172a;
      margin-bottom: 0.5rem;
    }
    .login-header p {
      color: #64748b;
      font-size: 0.9rem;
    }
    .w-full {
      width: 100%;
      padding: 0.75rem;
      font-size: 1rem;
    }
    .seed-hints {
      margin-top: 2rem;
      padding-top: 1rem;
      border-top: 1px solid #e2e8f0;
      font-size: 0.8rem;
      color: #64748b;
    }
    .seed-hints code {
      background: #f1f5f9;
      padding: 0.1rem 0.3rem;
      border-radius: 4px;
      color: #0f172a;
    }
  `]
})
export class LoginComponent {
  username = 'analista1';
  password = 'password123';
  loading = false;
  errorMessage = '';
  errorStatus = 0;

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  onLogin(): void {
    if (!this.username || !this.password) {
      this.errorMessage = 'Por favor complete todos los campos.';
      this.errorStatus = 400;
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.errorStatus = 0;

    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: () => {
        this.loading = false;
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/solicitudes';
        this.router.navigateByUrl(returnUrl);
      },
      error: (err) => {
        this.loading = false;
        this.errorStatus = err.status || 500;
        this.errorMessage = err.error?.detalle || 'Credenciales inválidas. Verifique usuario y contraseña.';
      }
    });
  }
}
