import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../../core/services/auth.service';
import { TranslateSelectionDirective } from '../../directives/translate-selection.directive';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    RouterOutlet, RouterLink, RouterLinkActive,
    MatSidenavModule, MatToolbarModule, MatListModule, MatIconModule, MatButtonModule,
    TranslateSelectionDirective
  ],
  template: `
    <mat-sidenav-container class="layout-container">
      <mat-sidenav mode="side" opened class="sidenav">
        <div class="logo">DevLingo</div>
        <mat-nav-list>
          <a mat-list-item routerLink="/dashboard" routerLinkActive="active">
            <mat-icon matListItemIcon>dashboard</mat-icon>
            <span matListItemTitle>Dashboard</span>
          </a>
          <a mat-list-item routerLink="/programs" routerLinkActive="active">
            <mat-icon matListItemIcon>school</mat-icon>
            <span matListItemTitle>Programs</span>
          </a>
          <a mat-list-item routerLink="/placement-test" routerLinkActive="active">
            <mat-icon matListItemIcon>quiz</mat-icon>
            <span matListItemTitle>Placement Test</span>
          </a>
          <a mat-list-item routerLink="/vocabulary" routerLinkActive="active">
            <mat-icon matListItemIcon>translate</mat-icon>
            <span matListItemTitle>Vocabulary</span>
          </a>
          <a mat-list-item routerLink="/practice" routerLinkActive="active">
            <mat-icon matListItemIcon>chat</mat-icon>
            <span matListItemTitle>Practice</span>
          </a>
          <a mat-list-item routerLink="/profile" routerLinkActive="active">
            <mat-icon matListItemIcon>person</mat-icon>
            <span matListItemTitle>Profile</span>
          </a>
          @if (authService.isAdmin()) {
            <a mat-list-item routerLink="/admin/users" routerLinkActive="active">
              <mat-icon matListItemIcon>people</mat-icon>
              <span matListItemTitle>Users</span>
            </a>
            <a mat-list-item routerLink="/admin/materials" routerLinkActive="active">
              <mat-icon matListItemIcon>folder</mat-icon>
              <span matListItemTitle>Materials</span>
            </a>
          }
        </mat-nav-list>
      </mat-sidenav>
      <mat-sidenav-content>
        <mat-toolbar color="primary">
          <span>DevLingo</span>
          <span class="spacer"></span>
          <span class="user-email">{{ authService.user()?.email }}</span>
          <button mat-icon-button (click)="authService.logout()">
            <mat-icon>logout</mat-icon>
          </button>
        </mat-toolbar>
        <div class="content" appTranslateSelection>
          <router-outlet />
        </div>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .layout-container { height: 100vh; }
    .sidenav { width: 240px; }
    .logo { padding: 16px; font-size: 24px; font-weight: bold; text-align: center; }
    .spacer { flex: 1 1 auto; }
    .user-email { margin-right: 8px; font-size: 14px; }
    .content { padding: 24px; }
    .active { background: rgba(0,0,0,0.04); }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LayoutComponent {
  constructor(public authService: AuthService) {}
}
