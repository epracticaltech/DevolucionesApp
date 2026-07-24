import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { LoginComponent } from './features/auth/login.component';
import { LayoutComponent } from './shared/layout/layout.component';
import { BandejaComponent } from './features/solicitudes/bandeja/bandeja.component';
import { DetalleComponent } from './features/solicitudes/detalle/detalle.component';
import { FormularioComponent } from './features/solicitudes/formulario/formulario.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'solicitudes', pathMatch: 'full' },
      { path: 'solicitudes', component: BandejaComponent },
      { path: 'solicitudes/nueva', component: FormularioComponent },
      { path: 'solicitudes/:id', component: DetalleComponent },
      { path: 'solicitudes/:id/editar', component: FormularioComponent },
      {
        path: 'carga-masiva',
        loadComponent: () => import('./features/carga-masiva/carga-masiva.component').then(m => m.CargaMasivaComponent)
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
