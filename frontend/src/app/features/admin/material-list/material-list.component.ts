import { Component, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-material-list',
  standalone: true,
  imports: [MatTableModule, MatButtonModule, MatPaginatorModule, MatProgressSpinnerModule, MatChipsModule, MatIconModule, DatePipe],
  template: `
    <div class="header">
      <h2>Materials</h2>
      <input type="file" #fileInput hidden (change)="onFileSelected($event)">
      <button mat-raised-button color="primary" (click)="fileInput.click()" [disabled]="uploading()">
        @if (uploading()) { Uploading... } @else { Upload Material }
      </button>
    </div>

    @if (loading()) {
      <div class="center"><mat-spinner /></div>
    } @else {
      <table mat-table [dataSource]="materials()">
        <ng-container matColumnDef="name">
          <th mat-header-cell *matHeaderCellDef>File Name</th>
          <td mat-cell *matCellDef="let m">{{ m.originalName }}</td>
        </ng-container>
        <ng-container matColumnDef="type">
          <th mat-header-cell *matHeaderCellDef>Type</th>
          <td mat-cell *matCellDef="let m">{{ m.contentType }}</td>
        </ng-container>
        <ng-container matColumnDef="size">
          <th mat-header-cell *matHeaderCellDef>Size</th>
          <td mat-cell *matCellDef="let m">{{ (m.fileSize / 1024).toFixed(1) }} KB</td>
        </ng-container>
        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Status</th>
          <td mat-cell *matCellDef="let m"><mat-chip>{{ m.status }}</mat-chip></td>
        </ng-container>
        <ng-container matColumnDef="date">
          <th mat-header-cell *matHeaderCellDef>Uploaded</th>
          <td mat-cell *matCellDef="let m">{{ m.createdAt | date:'short' }}</td>
        </ng-container>
        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Actions</th>
          <td mat-cell *matCellDef="let m">
            <button mat-icon-button color="warn" (click)="deleteMaterial(m.id)">
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
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    .center { display: flex; justify-content: center; padding: 48px; }
    table { width: 100%; }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MaterialListComponent implements OnInit {
  materials = signal<any[]>([]);
  total = signal(0);
  loading = signal(true);
  uploading = signal(false);
  displayedColumns = ['name', 'type', 'size', 'status', 'date', 'actions'];

  constructor(private http: HttpClient) {}

  ngOnInit() { this.loadMaterials(0, 20); }

  loadMaterials(page: number, size: number) {
    this.loading.set(true);
    this.http.get<any>(`/api/admin/materials?page=${page}&size=${size}`).subscribe({
      next: (res) => {
        this.materials.set(res.content);
        this.total.set(res.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    const formData = new FormData();
    formData.append('file', input.files[0]);

    this.uploading.set(true);
    this.http.post<any>('/api/admin/materials', formData).subscribe({
      next: (material) => {
        this.materials.update(m => [material, ...m]);
        this.uploading.set(false);
      },
      error: () => this.uploading.set(false)
    });
  }

  deleteMaterial(id: string) {
    this.http.delete(`/api/admin/materials/${id}`).subscribe(
      () => this.materials.update(m => m.filter(mat => mat.id !== id))
    );
  }

  onPage(event: PageEvent) { this.loadMaterials(event.pageIndex, event.pageSize); }
}
