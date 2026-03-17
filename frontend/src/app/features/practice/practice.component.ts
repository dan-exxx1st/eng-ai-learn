import { Component, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatChipsModule } from '@angular/material/chips';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-practice',
  standalone: true,
  imports: [MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule, MatPaginatorModule, MatChipsModule, DatePipe],
  template: `
    <h2>Practice Sessions</h2>

    <div class="scenarios">
      <mat-card class="scenario-card" (click)="startSession('CODE_REVIEW')">
        <mat-card-header>
          <mat-icon matCardAvatar>code</mat-icon>
          <mat-card-title>Code Review</mat-card-title>
          <mat-card-subtitle>Practice discussing code with a senior developer</mat-card-subtitle>
        </mat-card-header>
      </mat-card>

      <mat-card class="scenario-card" (click)="startSession('JOB_INTERVIEW')">
        <mat-card-header>
          <mat-icon matCardAvatar>work</mat-icon>
          <mat-card-title>Job Interview</mat-card-title>
          <mat-card-subtitle>Prepare for technical interviews in English</mat-card-subtitle>
        </mat-card-header>
      </mat-card>

      <mat-card class="scenario-card" (click)="startSession('DAILY_STANDUP')">
        <mat-card-header>
          <mat-icon matCardAvatar>groups</mat-icon>
          <mat-card-title>Daily Standup</mat-card-title>
          <mat-card-subtitle>Practice reporting progress at team meetings</mat-card-subtitle>
        </mat-card-header>
      </mat-card>
    </div>

    @if (starting()) {
      <div class="center"><mat-spinner diameter="30" /> Starting session...</div>
    }

    <h3>Past Sessions</h3>
    @if (loading()) {
      <div class="center"><mat-spinner /></div>
    } @else if (sessions().length > 0) {
      <div class="sessions-list">
        @for (session of sessions(); track session.id) {
          <mat-card class="session-card" (click)="openSession(session.id)">
            <mat-card-header>
              <mat-card-title>{{ formatScenario(session.scenarioType) }}</mat-card-title>
              <mat-card-subtitle>{{ session.createdAt | date:'medium' }}</mat-card-subtitle>
            </mat-card-header>
            <mat-card-content>
              <mat-chip>{{ session.completed ? 'Completed' : 'In Progress' }}</mat-chip>
              @if (session.score) {
                <mat-chip>Score: {{ session.score }}/100</mat-chip>
              }
            </mat-card-content>
          </mat-card>
        }
      </div>
      <mat-paginator [length]="total()" [pageSize]="10" (page)="onPage($event)" />
    } @else {
      <p>No sessions yet. Start a practice session above!</p>
    }
  `,
  styles: [`
    .scenarios { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; margin-bottom: 32px; }
    .scenario-card { cursor: pointer; transition: transform 0.2s; }
    .scenario-card:hover { transform: translateY(-2px); }
    mat-icon[matCardAvatar] { font-size: 40px; width: 40px; height: 40px; }
    .center { display: flex; align-items: center; gap: 12px; padding: 24px; justify-content: center; }
    .sessions-list { display: flex; flex-direction: column; gap: 8px; }
    .session-card { cursor: pointer; }
    .session-card:hover { background: #f5f5f5; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PracticeComponent implements OnInit {
  sessions = signal<any[]>([]);
  total = signal(0);
  loading = signal(true);
  starting = signal(false);

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() { this.loadSessions(0, 10); }

  loadSessions(page: number, size: number) {
    this.http.get<any>(`/api/practice?page=${page}&size=${size}`).subscribe({
      next: (res) => {
        this.sessions.set(res.content);
        this.total.set(res.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  startSession(type: string) {
    this.starting.set(true);
    this.http.post<any>('/api/practice/start', { scenarioType: type }).subscribe({
      next: (session) => {
        this.starting.set(false);
        this.router.navigate(['/practice', session.id]);
      },
      error: () => this.starting.set(false)
    });
  }

  openSession(id: string) {
    this.router.navigate(['/practice', id]);
  }

  formatScenario(type: string): string {
    const map: Record<string, string> = {
      CODE_REVIEW: 'Code Review',
      JOB_INTERVIEW: 'Job Interview',
      DAILY_STANDUP: 'Daily Standup'
    };
    return map[type] || type;
  }

  onPage(event: PageEvent) { this.loadSessions(event.pageIndex, event.pageSize); }
}
