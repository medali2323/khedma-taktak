import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container">
      <div class="card">
        <h1>Bienvenue sur Khedma Taktak</h1>
        <p class="text-muted">Plateforme de recrutement — candidats, étudiants et métiers manuels.</p>

        <div class="actions">
          @if (authService.isLoggedIn()) {
            <a routerLink="/dashboard" class="btn btn-primary">Tableau de bord</a>
          } @else {
            <a routerLink="/login" class="btn btn-primary">Connexion</a>
            <a routerLink="/register" class="btn btn-secondary">Inscription</a>
          }
        </div>

        <h2>Backend</h2>
        @if (loading) {
          <p class="text-muted">Vérification de l'API…</p>
        } @else if (health) {
          <p>Statut API : <strong>{{ health.status }}</strong> ({{ health.service }})</p>
        } @else {
          <p class="text-muted">API indisponible — démarrez le backend sur le port 8080.</p>
        }
      </div>
    </div>
  `,
  styles: [`
    .actions {
      display: flex;
      gap: 0.75rem;
      margin: 1.25rem 0 1.5rem;
    }
  `],
})
export class HomeComponent implements OnInit {
  private readonly http = inject(HttpClient);
  readonly authService = inject(AuthService);

  loading = true;
  health: { status: string; service: string } | null = null;

  ngOnInit(): void {
    this.http.get<{ status: string; service: string }>('/api/health').subscribe({
      next: (data) => {
        this.health = data;
        this.loading = false;
      },
      error: () => {
        this.health = null;
        this.loading = false;
      },
    });
  }
}
