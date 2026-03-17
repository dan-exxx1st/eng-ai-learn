import { Component, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-vocabulary-review',
  standalone: true,
  imports: [RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    @if (loading()) {
      <div class="center"><mat-spinner /></div>
    } @else if (words().length === 0) {
      <mat-card class="done-card">
        <mat-card-content class="center-col">
          <mat-icon class="big-icon">celebration</mat-icon>
          <h2>All caught up!</h2>
          <p>No words to review right now. Come back later.</p>
          <button mat-raised-button color="primary" routerLink="/vocabulary">Back to Vocabulary</button>
        </mat-card-content>
      </mat-card>
    } @else if (!showAnswer()) {
      <mat-card class="review-card">
        <mat-card-header>
          <mat-card-title>Review ({{ currentIndex() + 1 }} / {{ words().length }})</mat-card-title>
        </mat-card-header>
        <mat-card-content class="center-col">
          <div class="word">{{ words()[currentIndex()].word }}</div>
          @if (words()[currentIndex()].context) {
            <p class="context">Context: {{ words()[currentIndex()].context }}</p>
          }
          <button mat-raised-button color="accent" (click)="showAnswer.set(true)">
            Show Translation
          </button>
        </mat-card-content>
      </mat-card>
    } @else {
      <mat-card class="review-card">
        <mat-card-header>
          <mat-card-title>{{ words()[currentIndex()].word }}</mat-card-title>
        </mat-card-header>
        <mat-card-content class="center-col">
          <div class="translation">{{ words()[currentIndex()].translation || 'No translation' }}</div>
          <p>How well did you know it?</p>
          <div class="quality-buttons">
            <button mat-raised-button color="warn" (click)="review(0)">Forgot</button>
            <button mat-raised-button (click)="review(2)">Hard</button>
            <button mat-raised-button color="primary" (click)="review(3)">OK</button>
            <button mat-raised-button color="accent" (click)="review(4)">Easy</button>
            <button mat-raised-button style="background:#4caf50;color:white" (click)="review(5)">Perfect</button>
          </div>
        </mat-card-content>
      </mat-card>
    }
  `,
  styles: [`
    .center { display: flex; justify-content: center; padding: 48px; }
    .center-col { display: flex; flex-direction: column; align-items: center; padding: 32px; gap: 16px; }
    .review-card { max-width: 600px; margin: 0 auto; }
    .done-card { max-width: 400px; margin: 0 auto; }
    .word { font-size: 48px; font-weight: bold; color: #3f51b5; }
    .translation { font-size: 32px; color: #333; }
    .context { color: #666; font-style: italic; }
    .quality-buttons { display: flex; gap: 8px; flex-wrap: wrap; justify-content: center; }
    .big-icon { font-size: 64px; width: 64px; height: 64px; color: #4caf50; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VocabularyReviewComponent implements OnInit {
  words = signal<any[]>([]);
  currentIndex = signal(0);
  showAnswer = signal(false);
  loading = signal(true);

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.http.get<any>('/api/vocabulary/review?size=20').subscribe({
      next: (res) => {
        this.words.set(res.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  review(quality: number) {
    const word = this.words()[this.currentIndex()];
    this.http.post<any>('/api/vocabulary/review', {
      wordId: word.id,
      quality
    }).subscribe();

    this.showAnswer.set(false);
    if (this.currentIndex() < this.words().length - 1) {
      this.currentIndex.update(i => i + 1);
    } else {
      // All reviewed, reload to check if more
      this.loading.set(true);
      this.currentIndex.set(0);
      this.http.get<any>('/api/vocabulary/review?size=20').subscribe({
        next: (res) => {
          this.words.set(res.content);
          this.loading.set(false);
        },
        error: () => { this.words.set([]); this.loading.set(false); }
      });
    }
  }
}
