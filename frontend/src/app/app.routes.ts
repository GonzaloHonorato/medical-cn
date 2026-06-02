import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./components/monitor-alertas/monitor-alertas.component').then(m => m.MonitorAlertasComponent)
  },
  { path: '**', redirectTo: '' }
];
