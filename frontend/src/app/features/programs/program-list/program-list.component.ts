import { Component, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-program-list',
  standalone: true,
  imports: [RouterLink, MatCardModule, MatButtonModule, MatProgressSpinnerModule, MatChipsModule],
  template: `
    <div class="header">
      <h2>My Programs</h2>
      <button mat-raised-button color="primary" (click)="generateProgram()" [disabled]="generating()">
        @if (generating()) { Generating... } @else { Generate New Program }
      </button>
    </div>

    @if (loading()) {
      <div class="center"><mat-spinner /></div>
    } @else {
      <div class="programs-grid">
        @for (program of programs(); track program.id) {
          <mat-card>
            <mat-card-header>
              <mat-card-title>{{ program.title }}</mat-card-title>
              <mat-card-subtitle>
                <mat-chip>{{ program.languageLevel }}</mat-chip>
                <mat-chip>{{ program.specialization }}</mat-chip>
              </mat-card-subtitle>
            </mat-card-header>
            <mat-card-content>
              <p>{{ program.description }}</p>
              <p>{{ program.modules.length }} modules</p>
            </mat-card-content>
            <mat-card-actions>
              <a mat-button color="primary" [routerLink]="['/programs', program.id]">View Program</a>
            </mat-card-actions>
          </mat-card>
        } @empty {
          <p>No programs yet. Generate your first program!</p>
        }
      </div>
    }
  `,
  styles: [`
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    .programs-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(350px, 1fr)); gap: 16px; }
    .center { display: flex; justify-content: center; padding: 48px; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProgramListComponent implements OnInit {
  programs = signal<any[]>([]);
  loading = signal(true);
  generating = signal(false);

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadPrograms();
  }

  loadPrograms() {
    this.http.get<any[]>('/api/programs').subscribe({
      next: (res) => { this.programs.set(res); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  generateProgram() {
    this.generating.set(true);
    this.http.post<any>('/api/programs/generate', {}).subscribe({
      next: (program) => {
        this.programs.update(p => [program, ...p]);
        this.generating.set(false);
      },
      error: () => this.generating.set(false)
    });
  }
}
