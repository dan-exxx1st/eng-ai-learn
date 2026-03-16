import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { LayoutComponent } from './shared/components/layout/layout.component';

export const routes: Routes = [
  {
    path: 'auth',
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
      },
      {
        path: 'register',
        loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
      },
      { path: '', redirectTo: 'login', pathMatch: 'full' }
    ]
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'placement-test',
        loadComponent: () => import('./features/placement-test/test-page/test-page.component').then(m => m.TestPageComponent)
      },
      {
        path: 'placement-test/result/:testId',
        loadComponent: () => import('./features/placement-test/result/result.component').then(m => m.ResultComponent)
      },
      {
        path: 'programs',
        loadComponent: () => import('./features/programs/program-list/program-list.component').then(m => m.ProgramListComponent)
      },
      {
        path: 'programs/:programId',
        loadComponent: () => import('./features/programs/program-detail/program-detail.component').then(m => m.ProgramDetailComponent)
      },
      {
        path: 'lessons/:lessonId',
        loadComponent: () => import('./features/programs/lesson-view/lesson-view.component').then(m => m.LessonViewComponent)
      },
      {
        path: 'lessons/:lessonId/exercises',
        loadComponent: () => import('./features/programs/exercise/exercise.component').then(m => m.ExerciseComponent)
      },
      {
        path: 'admin/users',
        loadComponent: () => import('./features/admin/user-list/user-list.component').then(m => m.UserListComponent),
        canActivate: [adminGuard]
      },
      {
        path: 'admin/materials',
        loadComponent: () => import('./features/admin/material-list/material-list.component').then(m => m.MaterialListComponent),
        canActivate: [adminGuard]
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
