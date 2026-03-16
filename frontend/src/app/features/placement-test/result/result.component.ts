import { Component, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-result',
  standalone: true,
  imports: [MatCardModule, MatButtonModule, MatProgressSpinnerModule, RouterLink],
  template: `
    @if (loading()) {
      <div class="center"><mat-spinner /></div>
    } @else if (result()) {
      <mat-card>
        <mat-card-header>
          <mat-card-title>Test Complete!</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="result-display">
            <div class="level">{{ result()!.determinedLevel }}</div>
            <p>Score: {{ result()!.score }} / {{ result()!.totalQuestions }}</p>
          </div>
        </mat-card-content>
        <mat-card-actions>
          <button mat-raised-button color="primary" routerLink="/programs">Start Learning</button>
          <button mat-button routerLink="/dashboard">Go to Dashboard</button>
        </mat-card-actions>
      </mat-card>
    }
  `,
  styles: [`
    .center { display: flex; justify-content: center; padding: 48px; }
    .result-display { text-align: center; padding: 24px; }
    .level { font-size: 64px; font-weight: bold; color: #3f51b5; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ResultComponent implements OnInit {
  loading = signal(true);
  result = signal<any>(null);

  constructor(private http: HttpClient, private route: ActivatedRoute) {}

  ngOnInit() {
    const testId = this.route.snapshot.paramMap.get('testId');
    this.http.get(`/api/placement-test/${testId}/result`).subscribe({
      next: (res) => {
        this.result.set(res);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
