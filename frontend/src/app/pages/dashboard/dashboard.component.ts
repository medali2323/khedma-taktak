import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { USER_TYPE_OPTIONS } from '../../models/auth.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container">
      <div class="card">
        <h1>Tableau de bord</h1>
        @if (user) {
          <p>Bienvenue, <strong>{{ user.firstName }} {{ user.lastName }}</strong></p>
          <ul class="info-list">
            <li><span class="text-muted">Email</span> {{ user.email }}</li>
            <li><span class="text-muted">Type</span> {{ userTypeLabel }}</li>
            <li><span class="text-muted">ID</span> <code>{{ user.userId }}</code></li>
          </ul>
        } @else {
          <p class="text-muted">Session introuvable.</p>
        }

        <div class="actions">
          <a routerLink="/wizard" class="btn btn-primary">Compléter mon profil</a>
          <a routerLink="/ats" class="btn btn-secondary">Score CV / ATS</a>
          <a routerLink="/" class="btn btn-secondary">Accueil</a>
          <button type="button" class="btn btn-secondary" (click)="logout()">Déconnexion</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .info-list {
      list-style: none;
      padding: 0;
      margin: 1rem 0;
    }

    .info-list li {
      padding: 0.35rem 0;
    }

    .info-list span {
      display: inline-block;
      min-width: 4rem;
    }

    code {
      font-size: 0.85rem;
      word-break: break-all;
    }

    .actions {
      display: flex;
      gap: 0.75rem;
      margin-top: 1.5rem;
    }
  `],
})
export class DashboardComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  user = this.authService.getStoredUser();

  get userTypeLabel(): string {
    if (!this.user) return '';
    return USER_TYPE_OPTIONS.find((o) => o.value === this.user!.userType)?.label ?? this.user.userType;
  }

  logout(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }
}
