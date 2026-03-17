import { Component, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-vocabulary',
  standalone: true,
  imports: [
    FormsModule, RouterLink, MatTableModule, MatButtonModule, MatIconModule,
    MatFormFieldModule, MatInputModule, MatPaginatorModule,
    MatProgressSpinnerModule, MatChipsModule, MatButtonToggleModule, DatePipe
  ],
  template: `
    <div class="title-row">
      <h2>My Vocabulary</h2>
      <button mat-raised-button color="accent" routerLink="/vocabulary/review">
        <mat-icon>replay</mat-icon> Review ({{ reviewCount() }})
      </button>
    </div>

    <div class="controls">
      <div class="add-word">
        <mat-form-field appearance="outline">
          <mat-label>Add new word (auto-translated)</mat-label>
          <input matInput [(ngModel)]="newWord" (keyup.enter)="addWord()">
        </mat-form-field>
        <button mat-raised-button color="primary" (click)="addWord()" [disabled]="!newWord.trim() || adding()">
          @if (adding()) { Translating... } @else { <mat-icon>add</mat-icon> Add }
        </button>
      </div>

      <mat-button-toggle-group [(ngModel)]="filter" (change)="loadWords(0, 20)">
        <mat-button-toggle value="all">All</mat-button-toggle>
        <mat-button-toggle value="new">New</mat-button-toggle>
        <mat-button-toggle value="learned">Learned</mat-button-toggle>
      </mat-button-toggle-group>
    </div>

    @if (loading()) {
      <div class="center"><mat-spinner /></div>
    } @else {
      <table mat-table [dataSource]="words()">
        <ng-container matColumnDef="word">
          <th mat-header-cell *matHeaderCellDef>Word</th>
          <td mat-cell *matCellDef="let w" class="word-cell">{{ w.word }}</td>
        </ng-container>
        <ng-container matColumnDef="translation">
          <th mat-header-cell *matHeaderCellDef>Translation</th>
          <td mat-cell *matCellDef="let w">{{ w.translation || '-' }}</td>
        </ng-container>
        <ng-container matColumnDef="context">
          <th mat-header-cell *matHeaderCellDef>Context</th>
          <td mat-cell *matCellDef="let w" class="context-cell">{{ w.context || '-' }}</td>
        </ng-container>
        <ng-container matColumnDef="source">
          <th mat-header-cell *matHeaderCellDef>Source</th>
          <td mat-cell *matCellDef="let w"><mat-chip>{{ w.source || 'manual' }}</mat-chip></td>
        </ng-container>
        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Status</th>
          <td mat-cell *matCellDef="let w">
            <button mat-icon-button [color]="w.learned ? 'primary' : 'warn'" (click)="toggleLearned(w)">
              <mat-icon>{{ w.learned ? 'check_circle' : 'radio_button_unchecked' }}</mat-icon>
            </button>
          </td>
        </ng-container>
        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef></th>
          <td mat-cell *matCellDef="let w">
            <button mat-icon-button color="warn" (click)="deleteWord(w.id)">
              <mat-icon>delete</mat-icon>
            </button>
          </td>
        </ng-container>
        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>
      <mat-paginator [length]="total()" [pageSize]="20" (page)="onPage($event)" />
    }
  `,
  styles: [`
    .title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    .controls { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; flex-wrap: wrap; gap: 16px; }
    .add-word { display: flex; gap: 12px; align-items: center; }
    .center { display: flex; justify-content: center; padding: 48px; }
    table { width: 100%; }
    .word-cell { font-weight: bold; }
    .context-cell { max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VocabularyComponent implements OnInit {
  words = signal<any[]>([]);
  total = signal(0);
  loading = signal(true);
  adding = signal(false);
  reviewCount = signal(0);
  newWord = '';
  filter = 'all';
  displayedColumns = ['word', 'translation', 'context', 'source', 'status', 'actions'];

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadWords(0, 20);
    this.http.get<number>('/api/vocabulary/review/count').subscribe(c => this.reviewCount.set(c));
  }

  loadWords(page: number, size: number) {
    this.loading.set(true);
    const learned = this.filter === 'learned' ? '&learned=true' : this.filter === 'new' ? '&learned=false' : '';
    this.http.get<any>(`/api/vocabulary?page=${page}&size=${size}${learned}`).subscribe({
      next: (res) => {
        this.words.set(res.content);
        this.total.set(res.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  addWord() {
    const word = this.newWord.trim();
    if (!word) return;
    this.adding.set(true);
    this.http.post<any>('/api/vocabulary', {
      word,
      source: 'manual'
    }).subscribe({
      next: (w) => {
        this.words.update(words => [w, ...words]);
        this.newWord = '';
        this.adding.set(false);
      },
      error: () => this.adding.set(false)
    });
  }

  toggleLearned(word: any) {
    this.http.post<any>(`/api/vocabulary/${word.id}/toggle-learned`, {}).subscribe(
      updated => this.words.update(words => words.map(w => w.id === word.id ? updated : w))
    );
  }

  deleteWord(id: string) {
    this.http.delete(`/api/vocabulary/${id}`).subscribe(
      () => this.words.update(words => words.filter(w => w.id !== id))
    );
  }

  onPage(event: PageEvent) { this.loadWords(event.pageIndex, event.pageSize); }
}
