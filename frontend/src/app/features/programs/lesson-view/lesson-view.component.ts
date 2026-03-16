import { Component, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-lesson-view',
  standalone: true,
  imports: [MatCardModule, MatButtonModule, MatProgressSpinnerModule, RouterLink],
  template: `
    @if (loading()) {
      <div class="center">
        <div>
          <mat-spinner />
          <p>Generating lesson content...</p>
        </div>
      </div>
    } @else if (lesson()) {
      <mat-card>
        <mat-card-header>
          <mat-card-title>{{ lesson()!.title }}</mat-card-title>
          <mat-card-subtitle>{{ lesson()!.lessonType }}</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          @if (parsedContent()) {
            <h3>Introduction</h3>
            <p>{{ parsedContent()!.introduction }}</p>
            @if (parsedContent()!.keyPoints) {
              <h3>Key Points</h3>
              <ul>
                @for (point of parsedContent()!.keyPoints; track point) {
                  <li>{{ point }}</li>
                }
              </ul>
            }
            @if (parsedContent()!.examples) {
              <h3>Examples</h3>
              <ul>
                @for (example of parsedContent()!.examples; track example) {
                  <li>{{ example }}</li>
                }
              </ul>
            }
          }
        </mat-card-content>
        <mat-card-actions>
          <a mat-raised-button color="primary" [routerLink]="['/lessons', lessonId, 'exercises']">
            Practice Exercises
          </a>
          <button mat-button (click)="completeLesson()">Mark as Complete</button>
        </mat-card-actions>
      </mat-card>
    }
  `,
  styles: [`.center { display: flex; justify-content: center; align-items: center; padding: 48px; text-align: center; }`],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LessonViewComponent implements OnInit {
  lesson = signal<any>(null);
  parsedContent = signal<any>(null);
  loading = signal(true);
  lessonId = '';

  constructor(private http: HttpClient, private route: ActivatedRoute) {}

  ngOnInit() {
    this.lessonId = this.route.snapshot.paramMap.get('lessonId')!;
    this.http.get(`/api/lessons/${this.lessonId}`).subscribe({
      next: (res: any) => {
        this.lesson.set(res);
        try { this.parsedContent.set(JSON.parse(res.content)); } catch {}
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  completeLesson() {
    this.http.post(`/api/lessons/${this.lessonId}/complete`, {}).subscribe();
  }
}
