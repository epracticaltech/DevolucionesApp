import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { SolicitudService } from '../../../core/services/solicitud.service';
import { AuthService } from '../../../core/services/auth.service';
import {
  EstadoSolicitud,
  OrigenSolicitud,
  PaginaResultado,
  SolicitudFiltros,
  SolicitudResponse
} from '../../../core/models/solicitud.model';

@Component({
  selector: 'app-bandeja',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="header-actions">
      <div>
        <h2>Bandeja de Solicitudes</h2>
        <p class="subtitle">Gestión de devoluciones y estados de solicitud</p>
      </div>
      <a routerLink="/solicitudes/nueva" class="btn btn-primary">
        <span>+</span> Nueva Solicitud
      </a>
    </div>

    <!-- Filters Card -->
    <div class="card filter-card">
      <div class="filter-grid">
        <div class="form-group">
          <label class="form-label">Estado</label>
          <select class="form-control" [(ngModel)]="filtros.estado" (change)="filtrar()">
            <option value="">Todos los estados</option>
            <option value="BORRADOR">BORRADOR</option>
            <option value="EN_REVISION">EN_REVISION</option>
            <option value="APROBADA">APROBADA</option>
            <option value="RECHAZADA">RECHAZADA</option>
            <option value="PAGADA">PAGADA</option>
          </select>
        </div>

        <div class="form-group">
          <label class="form-label">RUT Cliente</label>
          <input
            type="text"
            class="form-control"
            placeholder="Ej: 12345678-5"
            [(ngModel)]="filtros.rut"
            (keyup.enter)="filtrar()"
          />
        </div>

        <div class="form-group">
          <label class="form-label">Origen</label>
          <select class="form-control" [(ngModel)]="filtros.origen" (change)="filtrar()">
            <option value="">Todos los orígenes</option>
            <option value="MANUAL">MANUAL</option>
            <option value="CARGA_MASIVA">CARGA_MASIVA</option>
          </select>
        </div>

        <div class="filter-actions-col">
          <button class="btn btn-secondary" (click)="filtrar()">Buscar</button>
          <button class="btn btn-secondary" (click)="limpiarFiltros()">Limpiar</button>
        </div>
      </div>
    </div>

    <!-- Table Card -->
    <div class="card">
      <div *ngIf="loading" class="loading-spinner">Cargando solicitudes...</div>

      <div *ngIf="!loading && paginaData?.contenido?.length === 0" class="empty-state">
        No se encontraron solicitudes con los filtros aplicados.
      </div>

      <div *ngIf="!loading && paginaData && paginaData.contenido.length > 0" class="table-responsive">
        <table>
          <thead>
            <tr>
              <th>Folio</th>
              <th>RUT Cliente</th>
              <th>Nombre Cliente</th>
              <th>Monto</th>
              <th>Banco / Cuenta</th>
              <th>Origen</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let item of paginaData.contenido">
              <td>
                <a [routerLink]="['/solicitudes', item.id]" class="folio-link">
                  <strong>{{ item.folio }}</strong>
                </a>
              </td>
              <td>{{ item.rutCliente }}</td>
              <td>{{ item.nombreCliente }}</td>
              <td><strong>{{ item.monto | currency:'CLP':'symbol-narrow':'1.0-0' }}</strong></td>
              <td>
                <div class="banco-info">
                  <span>{{ item.bancoDestino }}</span>
                  <small>{{ item.cuentaDestino }}</small>
                </div>
              </td>
              <td>
                <span class="origen-tag" [class.carga]="item.origen === 'CARGA_MASIVA'">
                  {{ item.origen }}
                </span>
              </td>
              <td>
                <span class="badge" [ngClass]="'badge-' + item.estado">{{ item.estado }}</span>
              </td>
              <td>
                <div class="row-actions">
                  <a [routerLink]="['/solicitudes', item.id]" class="btn btn-secondary btn-xs">Ver</a>
                  <a *ngIf="item.estado === 'BORRADOR'" [routerLink]="['/solicitudes', item.id, 'editar']" class="btn btn-secondary btn-xs">Editar</a>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination Footer -->
      <div *ngIf="paginaData && paginaData.totalPaginas > 1" class="pagination-container">
        <span class="pagination-info">
          Mostrando página {{ paginaData.pagina + 1 }} de {{ paginaData.totalPaginas }} (Total: {{ paginaData.totalElementos }} items)
        </span>

        <div class="pagination-buttons">
          <button
            class="btn btn-secondary btn-xs"
            [disabled]="paginaData.pagina === 0"
            (click)="cambiarPagina(paginaData.pagina - 1)"
          >
            &laquo; Anterior
          </button>
          <button
            class="btn btn-secondary btn-xs"
            [disabled]="paginaData.pagina >= paginaData.totalPaginas - 1"
            (click)="cambiarPagina(paginaData.pagina + 1)"
          >
            Siguiente &raquo;
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .header-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;
    }
    .subtitle {
      color: #64748b;
      font-size: 0.9rem;
    }
    .filter-card {
      padding: 1.25rem;
    }
    .filter-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 1rem;
      align-items: end;
    }
    .filter-actions-col {
      display: flex;
      gap: 0.5rem;
      margin-bottom: 1.25rem;
    }
    .folio-link {
      color: #2563eb;
    }
    .banco-info {
      display: flex;
      flex-direction: column;
    }
    .banco-info small {
      color: #64748b;
      font-size: 0.75rem;
    }
    .origen-tag {
      font-size: 0.75rem;
      font-weight: 600;
      color: #475569;
      background: #f1f5f9;
      padding: 0.15rem 0.4rem;
      border-radius: 4px;
    }
    .origen-tag.carga {
      background: #f0fdf4;
      color: #166534;
    }
    .row-actions {
      display: flex;
      gap: 0.35rem;
    }
    .btn-xs {
      padding: 0.2rem 0.5rem;
      font-size: 0.75rem;
    }
    .loading-spinner, .empty-state {
      text-align: center;
      padding: 3rem;
      color: #64748b;
    }
    .pagination-container {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 1.25rem;
      margin-top: 1rem;
      border-top: 1px solid #e2e8f0;
    }
    .pagination-info {
      font-size: 0.85rem;
      color: #64748b;
    }
    .pagination-buttons {
      display: flex;
      gap: 0.5rem;
    }
  `]
})
export class BandejaComponent implements OnInit {
  filtros: SolicitudFiltros = {
    estado: '',
    rut: '',
    origen: '',
    page: 0,
    size: 10
  };

  paginaData: PaginaResultado<SolicitudResponse> | null = null;
  loading = false;

  constructor(
    private solicitudService: SolicitudService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.cargarSolicitudes();
  }

  cargarSolicitudes(): void {
    this.loading = true;
    this.solicitudService.listar(this.filtros).subscribe({
      next: (data) => {
        this.paginaData = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  filtrar(): void {
    this.filtros.page = 0;
    this.cargarSolicitudes();
  }

  limpiarFiltros(): void {
    this.filtros = {
      estado: '',
      rut: '',
      origen: '',
      page: 0,
      size: 10
    };
    this.cargarSolicitudes();
  }

  cambiarPagina(nuevaPagina: number): void {
    this.filtros.page = nuevaPagina;
    this.cargarSolicitudes();
  }
}
