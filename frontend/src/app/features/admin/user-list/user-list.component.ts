import { Component, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [FormsModule, MatTableModule, MatButtonModule, MatFormFieldModule, MatInputModule,
            MatPaginatorModule, MatProgressSpinnerModule, MatChipsModule, DatePipe],
  template: `
    <h2>User Management</h2>
    <mat-form-field appearance="outline" class="search-field">
      <mat-label>Search users</mat-label>
      <input matInput [(ngModel)]="search" (keyup.enter)="searchUsers()">
    </mat-form-field>

    @if (loading()) {
      <div class="center"><mat-spinner /></div>
    } @else {
      <table mat-table [dataSource]="users()">
        <ng-container matColumnDef="email">
          <th mat-header-cell *matHeaderCellDef>Email</th>
          <td mat-cell *matCellDef="let user">{{ user.email }}</td>
        </ng-container>
        <ng-container matColumnDef="name">
          <th mat-header-cell *matHeaderCellDef>Name</th>
          <td mat-cell *matCellDef="let user">{{ user.firstName }} {{ user.lastName }}</td>
        </ng-container>
        <ng-container matColumnDef="role">
          <th mat-header-cell *matHeaderCellDef>Role</th>
          <td mat-cell *matCellDef="let user"><mat-chip>{{ user.role }}</mat-chip></td>
        </ng-container>
        <ng-container matColumnDef="level">
          <th mat-header-cell *matHeaderCellDef>Level</th>
          <td mat-cell *matCellDef="let user">{{ user.languageLevel || '-' }}</td>
        </ng-container>
        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Status</th>
          <td mat-cell *matCellDef="let user">
            <mat-chip [class]="user.blocked ? 'blocked' : 'active'">
              {{ user.blocked ? 'Blocked' : 'Active' }}
            </mat-chip>
          </td>
        </ng-container>
        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Actions</th>
          <td mat-cell *matCellDef="let user">
            @if (user.blocked) {
              <button mat-button color="primary" (click)="unblock(user.id)">Unblock</button>
            } @else {
              <button mat-button color="warn" (click)="block(user.id)">Block</button>
            }
          </td>
        </ng-container>
        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>
      <mat-paginator [length]="total()" [pageSize]="20" (page)="onPage($event)" />
    }
  `,
  styles: [`
    .search-field { width: 400px; margin-bottom: 16px; }
    .center { display: flex; justify-content: center; padding: 48px; }
    table { width: 100%; }
    .blocked { background: #f44336 !important; color: white !important; }
    .active { background: #4caf50 !important; color: white !important; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserListComponent implements OnInit {
  users = signal<any[]>([]);
  total = signal(0);
  loading = signal(true);
  search = '';
  displayedColumns = ['email', 'name', 'role', 'level', 'status', 'actions'];

  constructor(private http: HttpClient) {}

  ngOnInit() { this.loadUsers(0, 20); }

  loadUsers(page: number, size: number) {
    this.loading.set(true);
    const params = this.search ? `?search=${this.search}&page=${page}&size=${size}` : `?page=${page}&size=${size}`;
    this.http.get<any>(`/api/admin/users${params}`).subscribe({
      next: (res) => {
        this.users.set(res.content);
        this.total.set(res.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  searchUsers() { this.loadUsers(0, 20); }
  onPage(event: PageEvent) { this.loadUsers(event.pageIndex, event.pageSize); }

  block(userId: string) {
    this.http.post<any>(`/api/admin/users/${userId}/block`, {}).subscribe(
      updated => this.users.update(users => users.map(u => u.id === userId ? updated : u))
    );
  }

  unblock(userId: string) {
    this.http.post<any>(`/api/admin/users/${userId}/unblock`, {}).subscribe(
      updated => this.users.update(users => users.map(u => u.id === userId ? updated : u))
    );
  }
}
