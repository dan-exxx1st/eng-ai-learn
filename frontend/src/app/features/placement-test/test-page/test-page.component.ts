import { Component, signal, ChangeDetectionStrategy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatRadioModule } from '@angular/material/radio';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';

interface Question {
  index: number;
  question: string;
  options: string[];
  difficulty: string;
}

@Component({
  selector: 'app-test-page',
  standalone: true,
  imports: [
    FormsModule, MatCardModule, MatButtonModule, MatRadioModule,
    MatProgressSpinnerModule, MatProgressBarModule, MatFormFieldModule,
    MatInputModule, MatIconModule, MatChipsModule
  ],
  template: `
    @if (!testId()) {
      <mat-card>
        <mat-card-header><mat-card-title>English Placement Test</mat-card-title></mat-card-header>
        <mat-card-content>
          <p>This test consists of 20 questions to determine your English level.</p>
          <p>Questions progress from A1 to C1 difficulty.</p>
        </mat-card-content>
        <mat-card-actions>
          <button mat-raised-button color="primary" (click)="startTest()" [disabled]="starting()">
            @if (starting()) { Starting... } @else { Start Test }
          </button>
        </mat-card-actions>
      </mat-card>
    } @else if (loading()) {
      <mat-card>
        <mat-card-content class="center">
          <mat-spinner diameter="40" />
          <p>{{ currentQuestionIndex() === 0 ? 'Preparing test...' : 'Loading ' + generatingLevel() + ' level questions...' }}</p>
        </mat-card-content>
      </mat-card>
    } @else if (currentQuestion()) {
      <mat-card>
        <mat-card-header>
          <mat-card-title>Question {{ currentQuestionIndex() + 1 }} / 20</mat-card-title>
          <mat-card-subtitle>Difficulty: {{ currentQuestion()!.difficulty }}</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <mat-progress-bar mode="determinate" [value]="(currentQuestionIndex() + 1) * 5" />
          <p class="question-text">{{ currentQuestion()!.question }}</p>
          <mat-radio-group [(ngModel)]="selectedAnswer" class="options">
            @for (option of currentQuestion()!.options; track option) {
              <mat-radio-button [value]="option.charAt(0)">{{ option }}</mat-radio-button>
            }
          </mat-radio-group>
        </mat-card-content>
        <mat-card-actions>
          <button mat-raised-button color="primary" (click)="submitAnswer()"
                  [disabled]="!selectedAnswer || submitting()">Submit Answer</button>
        </mat-card-actions>
        @if (feedback()) {
          <mat-card-content>
            <p [class]="feedbackCorrect() ? 'correct' : 'incorrect'">
              {{ feedbackCorrect() ? 'Correct!' : 'Incorrect. ' + feedback() }}
            </p>
          </mat-card-content>
        }
      </mat-card>

      <!-- Save unknown word -->
      <mat-card class="vocab-card">
        <mat-card-content>
          <div class="vocab-input">
            <mat-form-field appearance="outline" class="word-field">
              <mat-label>Unknown word</mat-label>
              <input matInput [(ngModel)]="newWord" placeholder="Type a word you don't know"
                     (keyup.enter)="saveWord()">
            </mat-form-field>
            <button mat-raised-button color="accent" (click)="saveWord()" [disabled]="!newWord.trim()">
              <mat-icon>bookmark_add</mat-icon> Save
            </button>
          </div>
          @if (savedWords().length > 0) {
            <div class="saved-words">
              @for (w of savedWords(); track w) {
                <mat-chip>{{ w }}</mat-chip>
              }
            </div>
          }
        </mat-card-content>
      </mat-card>
    }
  `,
  styles: [`
    .center { display: flex; flex-direction: column; align-items: center; padding: 48px; gap: 16px; }
    .question-text { font-size: 18px; margin: 16px 0; }
    .options { display: flex; flex-direction: column; gap: 12px; }
    .correct { color: green; font-weight: bold; }
    .incorrect { color: red; }
    mat-progress-bar { margin-bottom: 16px; }
    .vocab-card { margin-top: 16px; }
    .vocab-input { display: flex; align-items: center; gap: 12px; }
    .word-field { flex: 1; }
    .saved-words { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 8px; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TestPageComponent {
  private readonly levels = ['A1', 'A2', 'B1', 'B2', 'C1'];

  starting = signal(false);
  loading = signal(false);
  submitting = signal(false);
  testId = signal<string | null>(null);
  currentQuestionIndex = signal(0);
  currentQuestion = signal<Question | null>(null);
  feedback = signal('');
  feedbackCorrect = signal(false);
  generatingLevel = signal('');
  savedWords = signal<string[]>([]);
  selectedAnswer = '';
  newWord = '';

  constructor(private http: HttpClient, private router: Router) {}

  startTest() {
    this.starting.set(true);
    this.http.post<any>('/api/placement-test/start', {}).subscribe({
      next: (res) => {
        this.testId.set(res.testId);
        this.starting.set(false);
        this.loadQuestion(0);
      },
      error: () => this.starting.set(false)
    });
  }

  loadQuestion(index: number) {
    this.loading.set(true);
    this.feedback.set('');
    this.selectedAnswer = '';
    this.generatingLevel.set(this.levels[Math.floor(index / 4)] || '');
    this.http.get<Question>(`/api/placement-test/${this.testId()}/question/${index}`).subscribe({
      next: (q) => {
        this.currentQuestion.set(q);
        this.currentQuestionIndex.set(index);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  submitAnswer() {
    this.submitting.set(true);
    this.http.post<any>('/api/placement-test/answer', {
      testId: this.testId(),
      questionIndex: this.currentQuestionIndex(),
      answer: this.selectedAnswer
    }).subscribe({
      next: (res) => {
        this.feedbackCorrect.set(res.correct);
        this.feedback.set(res.correct ? '' : `Correct answer: ${res.correctAnswer}. ${res.explanation}`);
        this.submitting.set(false);

        setTimeout(() => {
          if (res.answeredCount >= res.totalQuestions) {
            this.router.navigate(['/placement-test/result', this.testId()]);
          } else {
            this.loadQuestion(this.currentQuestionIndex() + 1);
          }
        }, 1500);
      },
      error: () => this.submitting.set(false)
    });
  }

  saveWord() {
    const word = this.newWord.trim();
    if (!word) return;

    const question = this.currentQuestion();
    this.http.post<any>('/api/vocabulary', {
      word,
      context: question?.question || '',
      source: 'placement_test'
    }).subscribe({
      next: () => {
        this.savedWords.update(words => [...words, word]);
        this.newWord = '';
      }
    });
  }
}
