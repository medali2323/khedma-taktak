import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  AtsAnalysisResult,
  TradeCategory,
} from '../../models/portfolio.models';
import { PortfolioService } from '../../services/portfolio.service';

@Component({
  selector: 'app-ats',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="container">
      <div class="page-header">
        <div>
          <h1>Score CV / ATS</h1>
          <p class="text-muted">Analysez votre profil et obtenez un score ATS avec des recommandations</p>
        </div>
        <a routerLink="/dashboard" class="btn btn-secondary">Retour dashboard</a>
      </div>

      <div class="ats-grid">
        <div class="card">
          <h2>Paramètres d'analyse</h2>
          <form [formGroup]="form" (ngSubmit)="analyze()">
            <div class="form-group">
              <label>Mode</label>
              <select formControlName="mode">
                <option value="OFFICE">Bureau / académique (ATS)</option>
                <option value="TRADES">Métier manuel (BTP, artisanat…)</option>
              </select>
            </div>
            <div class="form-group">
              <label>Domaine / métier cible</label>
              @if (form.controls.mode.value === 'TRADES') {
                <select formControlName="targetDomain">
                  <option value="">— Sélectionner —</option>
                  @for (trade of trades; track trade.code) {
                    <option [value]="trade.code">{{ trade.labelFr }}</option>
                  }
                </select>
              } @else {
                <input formControlName="targetDomain" placeholder="tech, marketing, finance…" />
              }
            </div>
            <div class="actions">
              <button type="submit" class="btn btn-secondary" [disabled]="loading">Analyser</button>
              <button type="button" class="btn btn-primary" (click)="optimize()" [disabled]="loading">
                Générer version optimisée
              </button>
            </div>
          </form>
        </div>

        @if (errorMessage) {
          <div class="alert alert-error full-width">{{ errorMessage }}</div>
        }

        @if (analysis) {
          <div class="card score-card">
            <h2>Score : {{ analysis.score }}/100</h2>
            <p class="text-muted">Mode {{ analysis.mode }} — {{ analysis.targetDomain || 'général' }}</p>
            <div class="score-bar">
              <div class="score-fill" [style.width.%]="analysis.score"></div>
            </div>
          </div>

          <div class="card">
            <h3>Points forts</h3>
            <ul>@for (s of analysis.strengths; track s) { <li>{{ s }}</li> }</ul>
          </div>

          <div class="card">
            <h3>Points faibles</h3>
            <ul>@for (w of analysis.weaknesses; track w) { <li>{{ w }}</li> }</ul>
          </div>

          <div class="card">
            <h3>Recommandations</h3>
            <ul>@for (r of analysis.recommendations; track r) { <li>{{ r }}</li> }</ul>
          </div>

          @if (analysis.suggestedKeywords.length) {
            <div class="card">
              <h3>Mots-clés suggérés</h3>
              <div class="keywords">
                @for (k of analysis.suggestedKeywords; track k) {
                  <span class="keyword">{{ k }}</span>
                }
              </div>
            </div>
          }
        }

        @if (optimizedSummary) {
          <div class="card full-width">
            <h3>Résumé optimisé proposé</h3>
            <p class="optimized-text">{{ optimizedSummary }}</p>
            <a routerLink="/wizard" class="btn btn-primary">Appliquer dans le wizard</a>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin: 1.5rem 0 2rem; gap: 1rem; }
    .ats-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; }
    .full-width { grid-column: 1 / -1; }
    .actions { display: flex; gap: 0.75rem; margin-top: 1rem; flex-wrap: wrap; }
    .score-bar { height: 8px; background: var(--color-border); border-radius: 4px; overflow: hidden; margin-top: 1rem; }
    .score-fill { height: 100%; background: var(--color-primary); transition: width 0.3s; }
    .keywords { display: flex; flex-wrap: wrap; gap: 0.5rem; }
    .keyword { background: var(--color-bg-secondary, #f3f4f6); padding: 0.25rem 0.75rem; border-radius: var(--radius-sm, 4px); font-size: 0.875rem; }
    .optimized-text { white-space: pre-wrap; line-height: 1.6; }
    .alert-error { background: #fef2f2; color: #991b1b; border: 1px solid #fecaca; padding: 0.75rem 1rem; border-radius: var(--radius-md); }
    @media (max-width: 768px) { .ats-grid { grid-template-columns: 1fr; } .page-header { flex-direction: column; } }
  `],
})
export class AtsComponent implements OnInit {
  private readonly portfolio = inject(PortfolioService);
  private readonly fb = inject(FormBuilder);

  trades: TradeCategory[] = [];
  analysis: AtsAnalysisResult | null = null;
  optimizedSummary = '';
  loading = false;
  errorMessage = '';

  form = this.fb.nonNullable.group({
    mode: ['OFFICE' as 'OFFICE' | 'TRADES', Validators.required],
    targetDomain: [''],
  });

  ngOnInit(): void {
    this.portfolio.getTradeCategories().subscribe({
      next: (trades) => { this.trades = trades; },
    });
  }

  analyze(): void {
    this.runAnalysis(false);
  }

  optimize(): void {
    this.runAnalysis(true);
  }

  private runAnalysis(optimize: boolean): void {
    this.loading = true;
    this.errorMessage = '';
    const payload = this.form.getRawValue();

    if (optimize) {
      this.portfolio.optimizeCv(payload).subscribe({
        next: (opt) => {
          this.analysis = opt.analysis;
          this.optimizedSummary = opt.optimizedProfile.summary || '';
          this.loading = false;
        },
        error: () => {
          this.errorMessage = 'Analyse impossible. Complétez d\'abord votre profil dans le wizard.';
          this.loading = false;
        },
      });
      return;
    }

    this.portfolio.analyzeCv(payload).subscribe({
      next: (analysis) => {
        this.analysis = analysis;
        this.optimizedSummary = '';
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Analyse impossible. Complétez d\'abord votre profil dans le wizard.';
        this.loading = false;
      },
    });
  }
}
