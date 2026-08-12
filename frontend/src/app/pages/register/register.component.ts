import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { USER_TYPE_OPTIONS, UserType } from '../../models/auth.models';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="auth-card card">
        <h1>Créer un compte</h1>
        <p class="text-muted mb-2">Rejoignez Khedma Taktak — candidats, étudiants et métiers manuels</p>

        @if (errorMessage) {
          <div class="alert alert-error">{{ errorMessage }}</div>
        }

        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="form-row">
            <div class="form-group">
              <label for="firstName">Prénom</label>
              <input id="firstName" type="text" formControlName="firstName" placeholder="Ali" autocomplete="given-name" />
              @if (form.controls.firstName.touched && form.controls.firstName.invalid) {
                <div class="error">Prénom requis</div>
              }
            </div>
            <div class="form-group">
              <label for="lastName">Nom</label>
              <input id="lastName" type="text" formControlName="lastName" placeholder="Ben" autocomplete="family-name" />
              @if (form.controls.lastName.touched && form.controls.lastName.invalid) {
                <div class="error">Nom requis</div>
              }
            </div>
          </div>

          <div class="form-group">
            <label for="userType">Type de profil</label>
            <select id="userType" formControlName="userType">
              @for (opt of userTypeOptions; track opt.value) {
                <option [value]="opt.value">{{ opt.label }}</option>
              }
            </select>
          </div>

          <div class="form-group">
            <label for="email">Email</label>
            <input id="email" type="email" formControlName="email" placeholder="vous@exemple.com" autocomplete="email" />
            @if (form.controls.email.touched && form.controls.email.invalid) {
              <div class="error">Email valide requis</div>
            }
          </div>

          <div class="form-group">
            <label for="password">Mot de passe</label>
            <input id="password" type="password" formControlName="password" placeholder="••••••••" autocomplete="new-password" />
            @if (form.controls.password.touched && form.controls.password.invalid) {
              <div class="error">Minimum 8 caractères</div>
            }
          </div>

          <button type="submit" class="btn btn-primary btn-full" [disabled]="loading || form.invalid">
            @if (loading) {
              Création…
            } @else {
              Créer mon compte
            }
          </button>
        </form>

        <p class="auth-footer text-muted">
          Déjà inscrit ? <a routerLink="/login">Se connecter</a>
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
      max-width: 480px;
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
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  loading = false;
  errorMessage = '';
  userTypeOptions = USER_TYPE_OPTIONS;

  form = this.fb.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    userType: ['CANDIDATE' as UserType, Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.register(this.form.getRawValue()).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.error || 'Inscription impossible. Réessayez.';
      },
      complete: () => {
        this.loading = false;
      },
    });
  }
}
