import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="auth-card card">
        <h1>Connexion</h1>
        <p class="text-muted mb-2">Accédez à votre espace Khedma Taktak</p>

        @if (errorMessage) {
          <div class="alert alert-error">{{ errorMessage }}</div>
        }

        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label for="email">Email</label>
            <input id="email" type="email" formControlName="email" placeholder="vous@exemple.com" autocomplete="email" />
            @if (form.controls.email.touched && form.controls.email.invalid) {
              <div class="error">Email valide requis</div>
            }
          </div>

          <div class="form-group">
            <label for="password">Mot de passe</label>
            <input id="password" type="password" formControlName="password" placeholder="••••••••" autocomplete="current-password" />
            @if (form.controls.password.touched && form.controls.password.invalid) {
              <div class="error">Mot de passe requis</div>
            }
          </div>

          <button type="submit" class="btn btn-primary btn-full" [disabled]="loading || form.invalid">
            @if (loading) {
              Connexion…
            } @else {
              Se connecter
            }
          </button>
        </form>

        <p class="auth-footer text-muted">
          Pas encore de compte ? <a routerLink="/register">Créer un compte</a>
        </p>
      </div>
    </div>
  `,
  styles: [`
    .auth-page {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: calc(100vh - 124px);
      padding: 1rem;
    }

    .auth-card {
      width: 100%;
      max-width: 420px;
    }

    .btn-full {
      width: 100%;
      margin-top: 0.5rem;
    }

    .auth-footer {
      margin-top: 1.5rem;
      text-align: center;
      font-size: 0.875rem;
    }
  `],
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  loading = false;
  errorMessage = '';

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.error || 'Email ou mot de passe incorrect';
      },
      complete: () => {
        this.loading = false;
      },
    });
  }
}
