import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav class="navbar">
      <div class="nav-brand">
        <a routerLink="/solicitudes" class="brand-link">
          <span class="brand-icon">🔄</span>
          <span class="brand-name">DevolucionesApp</span>
        </a>
      </div>

      <div class="nav-links">
        <a routerLink="/solicitudes" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}">Bandeja</a>
        <a routerLink="/solicitudes/nueva" routerLinkActive="active">Nueva Solicitud</a>
        <a routerLink="/carga-masiva" routerLinkActive="active">Carga Masiva (CSV)</a>
      </div>

      <div class="nav-user" *ngIf="authService.currentUser() as user">
        <div class="user-info">
          <span class="username">{{ user.username }}</span>
          <span class="user-role" [class.supervisor]="user.rol === 'SUPERVISOR'">{{ user.rol }}</span>
        </div>
        <button class="btn btn-secondary btn-sm" (click)="onLogout()">Salir</button>
      </div>
    </nav>

    <main class="container">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    .navbar {
      background: #ffffff;
      border-bottom: 1px solid #e2e8f0;
      padding: 0.75rem 1.5rem;
      display: flex;
      align-items: center;
      justify-content: space-between;
      box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.05);
    }
    .brand-link {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-weight: 700;
      font-size: 1.1rem;
      color: #0f172a;
    }
    .nav-links {
      display: flex;
      gap: 1.5rem;
    }
    .nav-links a {
      color: #64748b;
      font-weight: 500;
      font-size: 0.95rem;
      padding: 0.4rem 0.8rem;
      border-radius: 6px;
      transition: all 0.2s;
    }
    .nav-links a:hover, .nav-links a.active {
      color: #2563eb;
      background: #eff6ff;
      text-decoration: none;
    }
    .nav-user {
      display: flex;
      align-items: center;
      gap: 1rem;
    }
    .user-info {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
    }
    .username {
      font-size: 0.85rem;
      font-weight: 600;
      color: #0f172a;
    }
    .user-role {
      font-size: 0.7rem;
      font-weight: 700;
      padding: 0.1rem 0.4rem;
      border-radius: 4px;
      background: #e2e8f0;
      color: #475569;
    }
    .user-role.supervisor {
      background: #dbeafe;
      color: #1e40af;
    }
    .btn-sm {
      padding: 0.3rem 0.75rem;
      font-size: 0.8rem;
    }
  `]
})
export class LayoutComponent {
  authService = inject(AuthService);
  private router = inject(Router);

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
