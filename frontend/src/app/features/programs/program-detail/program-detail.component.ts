import { Component, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-program-detail',
  standalone: true,
  imports: [RouterLink, MatCardModule, MatButtonModule, MatExpansionModule, MatListModule, MatIconModule, MatChipsModule, MatProgressSpinnerModule],
  template: `
    @if (loading()) {
      <div class="center"><mat-spinner /></div>
    } @else if (program()) {
      <h2>{{ program()!.title }}</h2>
      <p>{{ program()!.description }}</p>
      <mat-accordion>
        @for (module of program()!.modules; track module.id) {
          <mat-expansion-panel>
            <mat-expansion-panel-header>
              <mat-panel-title>{{ module.title }}</mat-panel-title>
              <mat-panel-description>
                <mat-chip>{{ module.status }}</mat-chip>
              </mat-panel-description>
            </mat-expansion-panel-header>
            <p>{{ module.description }}</p>
            <mat-list>
              @for (lesson of module.lessons; track lesson.id) {
                <mat-list-item>
                  <mat-icon matListItemIcon>{{ getLessonIcon(lesson.lessonType) }}</mat-icon>
                  <a matListItemTitle [routerLink]="['/lessons', lesson.id]">{{ lesson.title }}</a>
                  <span matListItemMeta>{{ lesson.lessonType }}</span>
                </mat-list-item>
              }
            </mat-list>
          </mat-expansion-panel>
        }
      </mat-accordion>
    }
  `,
  styles: [`.center { display: flex; justify-content: center; padding: 48px; }`],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProgramDetailComponent implements OnInit {
  program = signal<any>(null);
  loading = signal(true);

  constructor(private http: HttpClient, private route: ActivatedRoute) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('programId');
    this.http.get(`/api/programs/${id}`).subscribe({
      next: (res) => { this.program.set(res); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  getLessonIcon(type: string): string {
    const icons: Record<string, string> = {
      VOCABULARY: 'translate', GRAMMAR: 'spellcheck', READING: 'menu_book',
      LISTENING: 'headphones', WRITING: 'edit', CONVERSATION: 'chat'
    };
    return icons[type] || 'school';
  }
}
