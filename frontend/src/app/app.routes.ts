import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home/home.component').then((m) => m.HomeComponent),
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent),
    canActivate: [guestGuard],
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register.component').then((m) => m.RegisterComponent),
    canActivate: [guestGuard],
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./pages/dashboard/dashboard.component').then((m) => m.DashboardComponent),
    canActivate: [authGuard],
  },
  {
    path: 'wizard',
    loadComponent: () => import('./pages/wizard/wizard.component').then((m) => m.WizardComponent),
    canActivate: [authGuard],
  },
  {
    path: 'ats',
    loadComponent: () => import('./pages/ats/ats.component').then((m) => m.AtsComponent),
    canActivate: [authGuard],
  },
  { path: '**', redirectTo: '' },
];
