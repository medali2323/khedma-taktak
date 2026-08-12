import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">
      <div class="card">
        <h1>Bienvenue sur Khedma Taktak</h1>
        <p class="text-muted">Projet vierge Angular + Spring Boot — prêt pour le développement par étapes.</p>

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
})
export class HomeComponent implements OnInit {
  private readonly http = inject(HttpClient);

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
