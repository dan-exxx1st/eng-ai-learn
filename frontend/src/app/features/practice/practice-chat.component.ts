import { Component, signal, OnInit, ChangeDetectionStrategy, ElementRef, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';

interface Message {
  role: string;
  content: string;
}

@Component({
  selector: 'app-practice-chat',
  standalone: true,
  imports: [
    FormsModule, MatCardModule, MatButtonModule, MatIconModule,
    MatFormFieldModule, MatInputModule, MatProgressSpinnerModule, MatChipsModule
  ],
  template: `
    @if (loading()) {
      <div class="center"><mat-spinner /></div>
    } @else if (session()) {
      <div class="chat-header">
        <h2>{{ formatScenario(session()!.scenarioType) }}</h2>
        @if (!session()!.completed) {
          <button mat-raised-button color="warn" (click)="endSession()" [disabled]="ending()">
            @if (ending()) { Generating feedback... } @else { End Session }
          </button>
        }
      </div>

      <div class="chat-messages" #chatContainer>
        @for (msg of session()!.messages; track $index) {
          <div class="message" [class]="msg.role">
            <div class="message-role">{{ msg.role === 'assistant' ? 'AI' : 'You' }}</div>
            <div class="message-content">{{ msg.content }}</div>
          </div>
        }
        @if (sending()) {
          <div class="message assistant">
            <div class="message-role">AI</div>
            <div class="message-content typing">Typing...</div>
          </div>
        }
      </div>

      @if (!session()!.completed) {
        <div class="chat-input">
          <mat-form-field appearance="outline" class="input-field">
            <mat-label>Your message in English...</mat-label>
            <input matInput [(ngModel)]="userMessage" (keyup.enter)="sendMessage()"
                   [disabled]="sending()">
          </mat-form-field>
          <button mat-fab color="primary" (click)="sendMessage()"
                  [disabled]="!userMessage.trim() || sending()">
            <mat-icon>send</mat-icon>
          </button>
        </div>
      } @else if (session()!.feedback) {
        <mat-card class="feedback-card">
          <mat-card-header>
            <mat-card-title>Session Feedback</mat-card-title>
            @if (session()!.score) {
              <mat-chip color="primary">Score: {{ session()!.score }}/100</mat-chip>
            }
          </mat-card-header>
          <mat-card-content>
            <pre class="feedback-text">{{ session()!.feedback }}</pre>
          </mat-card-content>
          <mat-card-actions>
            <button mat-raised-button color="primary" routerLink="/practice">Back to Practice</button>
          </mat-card-actions>
        </mat-card>
      }
    }
  `,
  styles: [`
    .chat-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    .center { display: flex; justify-content: center; padding: 48px; }
    .chat-messages { display: flex; flex-direction: column; gap: 12px; max-height: 500px; overflow-y: auto; padding: 16px; background: #fafafa; border-radius: 8px; margin-bottom: 16px; }
    .message { max-width: 75%; padding: 12px 16px; border-radius: 12px; }
    .message.assistant { align-self: flex-start; background: #e3f2fd; }
    .message.user { align-self: flex-end; background: #e8f5e9; }
    .message-role { font-size: 12px; font-weight: bold; color: #666; margin-bottom: 4px; }
    .message-content { white-space: pre-wrap; }
    .typing { color: #999; font-style: italic; }
    .chat-input { display: flex; gap: 12px; align-items: center; }
    .input-field { flex: 1; }
    .feedback-card { margin-top: 16px; }
    .feedback-text { white-space: pre-wrap; font-family: inherit; font-size: 14px; line-height: 1.6; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PracticeChatComponent implements OnInit {
  @ViewChild('chatContainer') chatContainer!: ElementRef;

  session = signal<any>(null);
  loading = signal(true);
  sending = signal(false);
  ending = signal(false);
  userMessage = '';

  private sessionId = '';

  constructor(private http: HttpClient, private route: ActivatedRoute, private router: Router) {}

  ngOnInit() {
    this.sessionId = this.route.snapshot.paramMap.get('sessionId')!;
    this.loadSession();
  }

  loadSession() {
    this.http.get<any>(`/api/practice/${this.sessionId}`).subscribe({
      next: (s) => {
        this.session.set(s);
        this.loading.set(false);
        setTimeout(() => this.scrollToBottom(), 100);
      },
      error: () => this.loading.set(false)
    });
  }

  sendMessage() {
    const msg = this.userMessage.trim();
    if (!msg) return;

    this.sending.set(true);
    this.userMessage = '';

    // Optimistically add user message
    this.session.update(s => ({
      ...s,
      messages: [...s.messages, { role: 'user', content: msg }]
    }));
    this.scrollToBottom();

    this.http.post<any>('/api/practice/message', {
      sessionId: this.sessionId,
      message: msg
    }).subscribe({
      next: (response) => {
        this.session.update(s => ({
          ...s,
          messages: [...s.messages, { role: 'assistant', content: response.content }]
        }));
        this.sending.set(false);
        this.scrollToBottom();
      },
      error: () => this.sending.set(false)
    });
  }

  endSession() {
    this.ending.set(true);
    this.http.post<any>(`/api/practice/${this.sessionId}/end`, {}).subscribe({
      next: (s) => {
        this.session.set(s);
        this.ending.set(false);
      },
      error: () => this.ending.set(false)
    });
  }

  formatScenario(type: string): string {
    const map: Record<string, string> = {
      CODE_REVIEW: 'Code Review Practice',
      JOB_INTERVIEW: 'Job Interview Practice',
      DAILY_STANDUP: 'Daily Standup Practice'
    };
    return map[type] || type;
  }

  private scrollToBottom() {
    setTimeout(() => {
      if (this.chatContainer?.nativeElement) {
        this.chatContainer.nativeElement.scrollTop = this.chatContainer.nativeElement.scrollHeight;
      }
    }, 50);
  }
}
