import { Component, signal, computed, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-result',
  standalone: true,
  imports: [MatCardModule, MatButtonModule, MatProgressSpinnerModule, MatExpansionModule, MatIconModule, MatChipsModule, RouterLink],
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
            <p class="score">Score: {{ result()!.score }} / {{ result()!.totalQuestions }}
              ({{ Math.round(result()!.score / result()!.totalQuestions * 100) }}%)</p>
          </div>
        </mat-card-content>
        <mat-card-actions>
          <button mat-raised-button color="primary" routerLink="/programs">Start Learning</button>
          <button mat-button routerLink="/dashboard">Go to Dashboard</button>
        </mat-card-actions>
      </mat-card>

      <!-- Mistakes summary -->
      @if (mistakes().length > 0) {
        <h3 class="section-title">
          <mat-icon color="warn">error_outline</mat-icon>
          Mistakes ({{ mistakes().length }})
        </h3>
        <mat-accordion>
          @for (item of mistakes(); track item.index) {
            <mat-expansion-panel>
              <mat-expansion-panel-header>
                <mat-panel-title>
                  <mat-icon color="warn" class="status-icon">close</mat-icon>
                  Q{{ item.index + 1 }}
                </mat-panel-title>
                <mat-panel-description>
                  <mat-chip>{{ item.difficulty }}</mat-chip>
                </mat-panel-description>
              </mat-expansion-panel-header>
              <p class="question-text">{{ item.question }}</p>
              <p class="answer wrong">Your answer: <strong>{{ item.userAnswer }}</strong></p>
              <p class="answer right">Correct answer: <strong>{{ item.correctAnswer }}</strong></p>
              @if (item.explanation) {
                <p class="explanation">{{ item.explanation }}</p>
              }
            </mat-expansion-panel>
          }
        </mat-accordion>
      }

      <!-- Correct answers -->
      @if (correct().length > 0) {
        <h3 class="section-title">
          <mat-icon color="primary">check_circle</mat-icon>
          Correct ({{ correct().length }})
        </h3>
        <mat-accordion>
          @for (item of correct(); track item.index) {
            <mat-expansion-panel>
              <mat-expansion-panel-header>
                <mat-panel-title>
                  <mat-icon color="primary" class="status-icon">check</mat-icon>
                  Q{{ item.index + 1 }}
                </mat-panel-title>
                <mat-panel-description>
                  <mat-chip>{{ item.difficulty }}</mat-chip>
                </mat-panel-description>
              </mat-expansion-panel-header>
              <p class="question-text">{{ item.question }}</p>
              <p class="answer right">Your answer: <strong>{{ item.userAnswer }}</strong></p>
              @if (item.explanation) {
                <p class="explanation">{{ item.explanation }}</p>
              }
            </mat-expansion-panel>
          }
        </mat-accordion>
      }
    }
  `,
  styles: [`
    .center { display: flex; justify-content: center; padding: 48px; }
    .result-display { text-align: center; padding: 24px; }
    .level { font-size: 64px; font-weight: bold; color: #3f51b5; }
    .score { font-size: 20px; color: #666; }
    .section-title { display: flex; align-items: center; gap: 8px; margin-top: 24px; }
    .status-icon { margin-right: 8px; }
    .question-text { font-size: 16px; margin: 8px 0; }
    .answer { margin: 4px 0; }
    .wrong { color: #d32f2f; }
    .right { color: #388e3c; }
    .explanation { color: #666; font-style: italic; margin-top: 8px; padding: 8px; background: #f5f5f5; border-radius: 4px; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ResultComponent implements OnInit {
  Math = Math;
  loading = signal(true);
  result = signal<any>(null);

  mistakes = computed(() => {
    const r = this.result();
    if (!r?.details) return [];
    return r.details.filter((d: any) => !d.correct);
  });

  correct = computed(() => {
    const r = this.result();
    if (!r?.details) return [];
    return r.details.filter((d: any) => d.correct);
  });

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
