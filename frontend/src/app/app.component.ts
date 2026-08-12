import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <header class="header">
      <div class="container header-inner">
        <strong>Khedma Taktak</strong>
      </div>
    </header>
    <main class="main">
      <router-outlet />
    </main>
  `,
  styles: [`
    .header {
      background: var(--color-surface);
      border-bottom: 1px solid var(--color-border);
    }
    .header-inner {
      height: 56px;
      display: flex;
      align-items: center;
    }
    .main {
      padding: 2rem 0;
    }
  `],
})
export class AppComponent {}
