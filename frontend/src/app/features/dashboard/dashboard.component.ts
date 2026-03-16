import { Component, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, MatCardModule, MatButtonModule, MatProgressSpinnerModule, MatTableModule, MatPaginatorModule, DatePipe],
  template: `
    @if (loading()) {
      <div class="center"><mat-spinner /></div>
    } @else {
      <h2>Dashboard</h2>
      <div class="cards">
        <mat-card>
          <mat-card-header><mat-card-title>Level</mat-card-title></mat-card-header>
          <mat-card-content>
            <div class="big-value">{{ summary()?.languageLevel || 'Not tested' }}</div>
            @if (!summary()?.languageLevel) {
              <a mat-button color="primary" routerLink="/placement-test">Take Test</a>
            }
          </mat-card-content>
        </mat-card>
        <mat-card>
          <mat-card-header><mat-card-title>Progress</mat-card-title></mat-card-header>
          <mat-card-content>
            <div class="big-value">{{ summary()?.completionPercentage }}%</div>
            <p>{{ summary()?.completedLessons }} / {{ summary()?.totalLessons }} lessons</p>
          </mat-card-content>
        </mat-card>
        <mat-card>
          <mat-card-header><mat-card-title>Active Programs</mat-card-title></mat-card-header>
          <mat-card-content>
            <div class="big-value">{{ summary()?.activeProgramsCount }}</div>
            <a mat-button color="primary" routerLink="/programs">View Programs</a>
          </mat-card-content>
        </mat-card>
      </div>

      <h3>Recent Lessons</h3>
      @if (history().length > 0) {
        <table mat-table [dataSource]="history()">
          <ng-container matColumnDef="title">
            <th mat-header-cell *matHeaderCellDef>Lesson</th>
            <td mat-cell *matCellDef="let item">{{ item.lessonTitle }}</td>
          </ng-container>
          <ng-container matColumnDef="type">
            <th mat-header-cell *matHeaderCellDef>Type</th>
            <td mat-cell *matCellDef="let item">{{ item.lessonType }}</td>
          </ng-container>
          <ng-container matColumnDef="score">
            <th mat-header-cell *matHeaderCellDef>Score</th>
            <td mat-cell *matCellDef="let item">{{ item.score ?? '-' }}</td>
          </ng-container>
          <ng-container matColumnDef="date">
            <th mat-header-cell *matHeaderCellDef>Completed</th>
            <td mat-cell *matCellDef="let item">{{ item.completedAt | date:'short' }}</td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        </table>
        <mat-paginator [length]="totalHistory()" [pageSize]="10"
                       (page)="onPage($event)" />
      } @else {
        <p>No completed lessons yet.</p>
      }
    }
  `,
  styles: [`
    .center { display: flex; justify-content: center; padding: 48px; }
    .cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 16px; margin-bottom: 32px; }
    .big-value { font-size: 48px; font-weight: bold; color: #3f51b5; text-align: center; }
    table { width: 100%; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent implements OnInit {
  summary = signal<any>(null);
  history = signal<any[]>([]);
  totalHistory = signal(0);
  loading = signal(true);
  displayedColumns = ['title', 'type', 'score', 'date'];

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.http.get<any>('/api/dashboard/summary').subscribe({
      next: (res) => { this.summary.set(res); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
    this.loadHistory(0, 10);
  }

  loadHistory(page: number, size: number) {
    this.http.get<any>(`/api/dashboard/history?page=${page}&size=${size}`).subscribe({
      next: (res) => {
        this.history.set(res.content);
        this.totalHistory.set(res.totalElements);
      }
    });
  }

  onPage(event: PageEvent) {
    this.loadHistory(event.pageIndex, event.pageSize);
  }
}
