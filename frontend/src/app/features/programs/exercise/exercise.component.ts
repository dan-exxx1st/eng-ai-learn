import { Component, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatRadioModule } from '@angular/material/radio';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-exercise',
  standalone: true,
  imports: [FormsModule, MatCardModule, MatButtonModule, MatRadioModule, MatFormFieldModule, MatInputModule, MatProgressSpinnerModule],
  template: `
    @if (loading()) {
      <div class="center"><mat-spinner /></div>
    } @else if (exercises().length > 0) {
      <h2>Exercises ({{ currentIndex() + 1 }} / {{ exercises().length }})</h2>
      @let ex = exercises()[currentIndex()];
      <mat-card>
        <mat-card-header>
          <mat-card-title>{{ ex.question }}</mat-card-title>
          <mat-card-subtitle>{{ ex.exerciseType }} - {{ ex.difficulty }}</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          @if (ex.options) {
            @let opts = parseOptions(ex.options);
            <mat-radio-group [(ngModel)]="selectedAnswer" class="options">
              @for (opt of opts; track opt) {
                <mat-radio-button [value]="opt">{{ opt }}</mat-radio-button>
              }
            </mat-radio-group>
          } @else {
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Your answer</mat-label>
              <textarea matInput [(ngModel)]="selectedAnswer" rows="3"></textarea>
            </mat-form-field>
          }
          @if (feedback()) {
            <p [class]="feedbackCorrect() ? 'correct' : 'incorrect'">{{ feedback() }}</p>
          }
        </mat-card-content>
        <mat-card-actions>
          @if (!feedback()) {
            <button mat-raised-button color="primary" (click)="submit()" [disabled]="!selectedAnswer">Submit</button>
          } @else if (currentIndex() < exercises().length - 1) {
            <button mat-raised-button color="primary" (click)="next()">Next</button>
          } @else {
            <button mat-raised-button color="accent" (click)="finish()">Finish</button>
          }
        </mat-card-actions>
      </mat-card>
    } @else {
      <p>No exercises available for this lesson.</p>
    }
  `,
  styles: [`
    .center { display: flex; justify-content: center; padding: 48px; }
    .options { display: flex; flex-direction: column; gap: 12px; margin: 16px 0; }
    .full-width { width: 100%; }
    .correct { color: green; font-weight: bold; }
    .incorrect { color: red; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExerciseComponent implements OnInit {
  exercises = signal<any[]>([]);
  loading = signal(true);
  currentIndex = signal(0);
  feedback = signal('');
  feedbackCorrect = signal(false);
  selectedAnswer = '';

  constructor(private http: HttpClient, private route: ActivatedRoute) {}

  ngOnInit() {
    const lessonId = this.route.snapshot.paramMap.get('lessonId');
    this.http.get<any[]>(`/api/exercises/lesson/${lessonId}`).subscribe({
      next: (res) => { this.exercises.set(res); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  parseOptions(options: string): string[] {
    try { return JSON.parse(options); } catch { return []; }
  }

  submit() {
    const ex = this.exercises()[this.currentIndex()];
    this.http.post<any>('/api/exercises/submit', {
      exerciseId: ex.id,
      answer: this.selectedAnswer
    }).subscribe(res => {
      this.feedbackCorrect.set(res.correct);
      this.feedback.set(res.correct ? 'Correct!' :
        `Incorrect. ${res.correctAnswer ? 'Correct answer: ' + res.correctAnswer + '. ' : ''}${res.explanation || ''}`);
    });
  }

  next() {
    this.currentIndex.update(i => i + 1);
    this.feedback.set('');
    this.selectedAnswer = '';
  }

  finish() {
    window.history.back();
  }
}
