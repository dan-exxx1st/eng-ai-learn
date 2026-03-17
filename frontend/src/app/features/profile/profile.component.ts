import { Component, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    FormsModule, MatCardModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatProgressSpinnerModule
  ],
  template: `
    @if (loading()) {
      <div class="center"><mat-spinner /></div>
    } @else if (profile()) {
      <h2>Profile</h2>
      <mat-card>
        <mat-card-content>
          <div class="form">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Email</mat-label>
              <input matInput [value]="profile()!.email" disabled>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>First Name</mat-label>
              <input matInput [(ngModel)]="firstName">
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Last Name</mat-label>
              <input matInput [(ngModel)]="lastName">
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Specialization</mat-label>
              <mat-select [(ngModel)]="specialization">
                @for (spec of specializations; track spec) {
                  <mat-option [value]="spec">{{ spec }}</mat-option>
                }
              </mat-select>
            </mat-form-field>

            <div class="info-row">
              <span>Language Level: <strong>{{ profile()!.languageLevel || 'Not tested yet' }}</strong></span>
              <span>Role: <strong>{{ profile()!.role }}</strong></span>
            </div>

            @if (message()) {
              <p class="success">{{ message() }}</p>
            }
          </div>
        </mat-card-content>
        <mat-card-actions>
          <button mat-raised-button color="primary" (click)="save()" [disabled]="saving()">
            @if (saving()) { Saving... } @else { Save }
          </button>
        </mat-card-actions>
      </mat-card>
    }
  `,
  styles: [`
    .center { display: flex; justify-content: center; padding: 48px; }
    .form { display: flex; flex-direction: column; gap: 4px; max-width: 500px; }
    .full-width { width: 100%; }
    .info-row { display: flex; gap: 32px; margin: 8px 0 16px; color: #666; }
    .success { color: green; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProfileComponent implements OnInit {
  profile = signal<any>(null);
  loading = signal(true);
  saving = signal(false);
  message = signal('');

  firstName = '';
  lastName = '';
  specialization = '';

  specializations = [
    'BACKEND', 'FRONTEND', 'FULLSTACK', 'DEVOPS',
    'DATA_SCIENCE', 'QA', 'MOBILE', 'GAMEDEV', 'SECURITY'
  ];

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.http.get<any>('/api/profile').subscribe({
      next: (p) => {
        this.profile.set(p);
        this.firstName = p.firstName || '';
        this.lastName = p.lastName || '';
        this.specialization = p.specialization || '';
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  save() {
    this.saving.set(true);
    this.message.set('');
    this.http.put<any>('/api/profile', {
      firstName: this.firstName,
      lastName: this.lastName,
      specialization: this.specialization || null
    }).subscribe({
      next: (p) => {
        this.profile.set(p);
        this.saving.set(false);
        this.message.set('Profile saved!');
      },
      error: () => this.saving.set(false)
    });
  }
}
