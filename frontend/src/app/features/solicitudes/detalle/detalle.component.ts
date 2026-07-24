import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { SolicitudService } from '../../../core/services/solicitud.service';
import { AuthService } from '../../../core/services/auth.service';
import { EventoSolicitud, SolicitudBase } from '../../../core/models/solicitud.model';

@Component({
  selector: 'app-detalle',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div *ngIf="loading" class="loading">Cargando detalle de la solicitud...</div>

    <div *ngIf="!loading && baseData">
      <!-- Top Bar -->
      <div class="detail-header">
        <div>
          <a routerLink="/solicitudes" class="back-link">&larr; Volver a la bandeja</a>
          <h2 class="title">
            Solicitud {{ baseData.solicitud.folio }}
            <span class="badge" [ngClass]="'badge-' + baseData.solicitud.estado">
              {{ baseData.solicitud.estado }}
            </span>
          </h2>
        </div>

        <div class="header-right">
          <a
            *ngIf="baseData.solicitud.estado === 'BORRADOR'"
            [routerLink]="['/solicitudes', baseData.solicitud.id, 'editar']"
            class="btn btn-secondary"
          >
            Editar Borrador
          </a>
        </div>
      </div>

      <div *ngIf="errorMessage" class="alert alert-danger">
        {{ errorMessage }}
      </div>

      <div *ngIf="successMessage" class="alert alert-success">
        {{ successMessage }}
      </div>

      <!-- Action Panel Card according to State & Role -->
      <div class="card action-card" *ngIf="ofreceAcciones()">
        <h4 class="action-card-title">Acciones Disponibles (Rol: {{ authService.getRole() }})</h4>
        
        <div class="action-buttons">
          <!-- Transiciones desde BORRADOR (ANALISTA o SUPERVISOR) -->
          <ng-container *ngIf="baseData.solicitud.estado === 'BORRADOR'">
            <button class="btn btn-primary" (click)="ejecutarAccion('enviar')">
              Enviar a Revisión
            </button>
            <button class="btn btn-danger" (click)="ejecutarAccion('anular')">
              Anular Solicitud
            </button>
          </ng-container>

          <!-- Transiciones desde EN_REVISION (Solo SUPERVISOR - Regla R2) -->
          <ng-container *ngIf="baseData.solicitud.estado === 'EN_REVISION'">
            <ng-container *ngIf="authService.getRole() === 'SUPERVISOR'; else noPermitidoSupervisor">
              <button class="btn btn-success" (click)="ejecutarAccion('aprobar')">
                Aprobar Solicitud
              </button>
              <button class="btn btn-danger" (click)="abrirModalRechazo()">
                Rechazar Solicitud
              </button>
            </ng-container>
            <ng-template #noPermitidoSupervisor>
              <p class="role-warning">
                🔒 El estado es <strong>EN_REVISION</strong>. Solo un usuario con rol <strong>SUPERVISOR</strong> puede Aprobar o Rechazar esta solicitud.
              </p>
            </ng-template>
          </ng-container>

          <!-- Transicion desde APROBADA (Solo SUPERVISOR - Regla R2) -->
          <ng-container *ngIf="baseData.solicitud.estado === 'APROBADA'">
            <ng-container *ngIf="authService.getRole() === 'SUPERVISOR'; else noPermitidoPagar">
              <button class="btn btn-success" (click)="ejecutarAccion('pagar')">
                Emitir Pago (PAGAR)
              </button>
            </ng-container>
            <ng-template #noPermitidoPagar>
              <p class="role-warning">
                🔒 El estado es <strong>APROBADA</strong>. Solo un usuario con rol <strong>SUPERVISOR</strong> puede ejecutar el Pago.
              </p>
            </ng-template>
          </ng-container>

          <!-- Transicion desde RECHAZADA (Reabrir max 1 vez - Regla R4) -->
          <ng-container *ngIf="baseData.solicitud.estado === 'RECHAZADA'">
            <button
              class="btn btn-warning"
              [disabled]="baseData.solicitud.vecesReabierta >= 1"
              (click)="ejecutarAccion('reabrir')"
            >
              Reabrir a Borrador {{ baseData.solicitud.vecesReabierta >= 1 ? '(Límite máximo alcanzado)' : '' }}
            </button>
          </ng-container>
        </div>

        <!-- Optional Comentario Field for Action -->
        <div class="form-group comentario-box" *ngIf="mostrarComentarioBox">
          <label class="form-label">Comentario de Auditoría (Opcional)</label>
          <input
            type="text"
            class="form-control"
            [(ngModel)]="comentario"
            placeholder="Ingrese observaciones para el evento de auditoría"
          />
        </div>

        <!-- Modal Rechazo Form -->
        <div *ngIf="mostrarRechazoForm" class="rechazo-box card">
          <h5>Rechazar Solicitud</h5>
          <div class="form-group">
            <label class="form-label">Motivo de Rechazo (Obligatorio)</label>
            <textarea
              class="form-control"
              rows="2"
              [(ngModel)]="motivoRechazo"
              placeholder="Escriba el motivo formal de rechazo..."
            ></textarea>
          </div>
          <div class="form-group">
            <label class="form-label">Comentario Adicional (Opcional)</label>
            <input type="text" class="form-control" [(ngModel)]="comentario" />
          </div>
          <div class="rechazo-actions">
            <button class="btn btn-danger" (click)="confirmarRechazo()">Confirmar Rechazo</button>
            <button class="btn btn-secondary" (click)="mostrarRechazoForm = false">Cancelar</button>
          </div>
        </div>
      </div>

      <!-- Detail Grid Card -->
      <div class="detail-grid">
        <div class="card main-info">
          <h3>Datos Principales</h3>
          <div class="info-table">
            <div class="info-row">
              <span class="info-label">RUT Cliente:</span>
              <span class="info-value"><strong>{{ baseData.solicitud.rutCliente }}</strong></span>
            </div>
            <div class="info-row">
              <span class="info-label">Nombre Cliente:</span>
              <span class="info-value">{{ baseData.solicitud.nombreCliente }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Monto Afecto:</span>
              <span class="info-value monto">{{ baseData.solicitud.monto | currency:'CLP':'symbol-narrow':'1.0-0' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Banco / Cuenta:</span>
              <span class="info-value">{{ baseData.solicitud.bancoDestino }} — {{ baseData.solicitud.cuentaDestino }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Referencia Banco:</span>
              <span class="info-value font-mono">{{ baseData.solicitud.referenciaBanco }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Origen Registro:</span>
              <span class="info-value">{{ baseData.solicitud.origen }}</span>
            </div>
            <div class="info-row" *ngIf="baseData.solicitud.motivoRechazo">
              <span class="info-label text-danger">Motivo Rechazo:</span>
              <span class="info-value text-danger"><strong>{{ baseData.solicitud.motivoRechazo }}</strong></span>
            </div>
            <div class="info-row">
              <span class="info-label">Reaperturas:</span>
              <span class="info-value">{{ baseData.solicitud.vecesReabierta }} / 1</span>
            </div>
          </div>
        </div>

        <!-- Audit Info Card -->
        <div class="card audit-info">
          <h3>Información de Auditoría</h3>
          <div class="info-table">
            <div class="info-row">
              <span class="info-label">Creada por:</span>
              <span class="info-value">{{ baseData.solicitud.creadaPor }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Fecha Creación:</span>
              <span class="info-value">{{ baseData.solicitud.fechaCreacion | date:'dd/MM/yyyy HH:mm:ss' }}</span>
            </div>
            <div class="info-row" *ngIf="baseData.solicitud.actualizadaPor">
              <span class="info-label">Última Modificación por:</span>
              <span class="info-value">{{ baseData.solicitud.actualizadaPor }}</span>
            </div>
            <div class="info-row" *ngIf="baseData.solicitud.fechaActualizacion">
              <span class="info-label">Fecha Modificación:</span>
              <span class="info-value">{{ baseData.solicitud.fechaActualizacion | date:'dd/MM/yyyy HH:mm:ss' }}</span>
            </div>
          </div>

          <div class="user-exec-box card">
            <small>Operador Ejecutor Actual:</small>
            <div><strong>{{ baseData.usuario.username }}</strong> ({{ baseData.usuario.rol }})</div>
            <small>{{ baseData.usuario.email }}</small>
          </div>
        </div>
      </div>

      <!-- Events Audit Timeline Card -->
      <div class="card">
        <h3>Historial de Eventos (Auditoría R6)</h3>

        <div *ngIf="eventos.length === 0" class="empty-events">
          No hay eventos de auditoría registrados.
        </div>

        <div *ngIf="eventos.length > 0" class="timeline">
          <div class="timeline-item" *ngFor="let ev of eventos">
            <div class="timeline-badge"></div>
            <div class="timeline-content">
              <div class="timeline-header">
                <span class="badge" [ngClass]="'badge-' + ev.estadoDestino">{{ ev.estadoDestino }}</span>
                <span class="timeline-date">{{ ev.fecha | date:'dd/MM/yyyy HH:mm:ss' }}</span>
              </div>
              <p class="timeline-user">
                Usuario: <strong>{{ ev.usuario }}</strong> 
                (Transición: {{ ev.estadoOrigen }} &rarr; {{ ev.estadoDestino }})
              </p>
              <p class="timeline-comment" *ngIf="ev.comentario">
                &ldquo;{{ ev.comentario }}&rdquo;
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 1.5rem;
    }
    .back-link {
      font-size: 0.85rem;
      color: #64748b;
      margin-bottom: 0.5rem;
      display: inline-block;
    }
    .title {
      font-size: 1.5rem;
      display: flex;
      align-items: center;
      gap: 1rem;
    }
    .action-card {
      background: #f8fafc;
      border-left: 4px solid #2563eb;
    }
    .action-card-title {
      font-size: 0.95rem;
      margin-bottom: 1rem;
      color: #334155;
    }
    .action-buttons {
      display: flex;
      gap: 0.75rem;
      flex-wrap: wrap;
      margin-bottom: 1rem;
    }
    .comentario-box {
      margin-top: 1rem;
    }
    .rechazo-box {
      margin-top: 1rem;
      background: #fff;
      border: 1px solid #fecaca;
    }
    .rechazo-actions {
      display: flex;
      gap: 0.5rem;
    }
    .role-warning {
      color: #b45309;
      background: #fffbeb;
      padding: 0.75rem;
      border-radius: 6px;
      font-size: 0.9rem;
      border: 1px solid #fef3c7;
    }
    .detail-grid {
      display: grid;
      grid-template-columns: 2fr 1fr;
      gap: 1.5rem;
    }
    @media (max-width: 768px) {
      .detail-grid {
        grid-template-columns: 1fr;
      }
    }
    .info-table {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      margin-top: 1rem;
    }
    .info-row {
      display: flex;
      justify-content: space-between;
      border-bottom: 1px solid #f1f5f9;
      padding-bottom: 0.5rem;
      font-size: 0.9rem;
    }
    .info-label {
      color: #64748b;
    }
    .monto {
      font-size: 1.1rem;
      color: #166534;
    }
    .font-mono {
      font-family: monospace;
    }
    .user-exec-box {
      margin-top: 1.5rem;
      background: #eff6ff;
      border-color: #bfdbfe;
      padding: 0.75rem;
      font-size: 0.85rem;
    }
    /* Timeline */
    .timeline {
      position: relative;
      padding-left: 1.5rem;
      margin-top: 1.5rem;
    }
    .timeline::before {
      content: '';
      position: absolute;
      left: 7px;
      top: 0;
      bottom: 0;
      width: 2px;
      background: #e2e8f0;
    }
    .timeline-item {
      position: relative;
      margin-bottom: 1.5rem;
    }
    .timeline-badge {
      position: absolute;
      left: -1.5rem;
      top: 4px;
      width: 14px;
      height: 14px;
      border-radius: 50%;
      background: #2563eb;
      border: 3px solid #fff;
    }
    .timeline-content {
      background: #f8fafc;
      padding: 0.75rem 1rem;
      border-radius: 8px;
      border: 1px solid #e2e8f0;
    }
    .timeline-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.35rem;
    }
    .timeline-date {
      font-size: 0.75rem;
      color: #64748b;
    }
    .timeline-user {
      font-size: 0.85rem;
      color: #334155;
    }
    .timeline-comment {
      font-size: 0.85rem;
      font-style: italic;
      color: #475569;
      margin-top: 0.35rem;
    }
    .text-danger {
      color: #dc2626;
    }
    .loading {
      text-align: center;
      padding: 3rem;
      color: #64748b;
    }
  `]
})
export class DetalleComponent implements OnInit {
  baseData: SolicitudBase | null = null;
  eventos: EventoSolicitud[] = [];
  loading = true;
  errorMessage = '';
  successMessage = '';

  comentario = '';
  motivoRechazo = '';
  mostrarComentarioBox = true;
  mostrarRechazoForm = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private solicitudService: SolicitudService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.cargarDatos(id);
    }
  }

  cargarDatos(id: number): void {
    this.loading = true;
    this.solicitudService.obtenerPorId(id).subscribe({
      next: (base) => {
        this.baseData = base;
        this.loading = false;
        this.cargarHistorial(id);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.mensaje || 'No se pudo cargar la solicitud.';
      }
    });
  }

  cargarHistorial(id: number): void {
    this.solicitudService.obtenerHistorial(id).subscribe({
      next: (evs) => {
        this.eventos = evs;
      }
    });
  }

  ofreceAcciones(): boolean {
    if (!this.baseData) return false;
    const est = this.baseData.solicitud.estado;
    return est !== 'PAGADA';
  }

  ejecutarAccion(tipo: 'enviar' | 'anular' | 'aprobar' | 'pagar' | 'reabrir'): void {
    if (!this.baseData) return;
    const id = this.baseData.solicitud.id;
    this.errorMessage = '';
    this.successMessage = '';

    let call$;
    switch (tipo) {
      case 'enviar': call$ = this.solicitudService.enviarARevision(id, this.comentario); break;
      case 'anular': call$ = this.solicitudService.anular(id, this.comentario); break;
      case 'aprobar': call$ = this.solicitudService.aprobar(id, this.comentario); break;
      case 'pagar': call$ = this.solicitudService.pagar(id, this.comentario); break;
      case 'reabrir': call$ = this.solicitudService.reabrir(id, this.comentario); break;
    }

    call$.subscribe({
      next: (res) => {
        this.baseData = res;
        this.comentario = '';
        this.successMessage = `Transición a ${res.solicitud.estado} realizada con éxito.`;
        this.cargarHistorial(id);
      },
      error: (err) => {
        this.errorMessage = err.error?.mensaje || 'Error al ejecutar la acción.';
      }
    });
  }

  abrirModalRechazo(): void {
    this.mostrarRechazoForm = true;
    this.motivoRechazo = '';
  }

  confirmarRechazo(): void {
    if (!this.baseData) return;
    if (!this.motivoRechazo.trim()) {
      this.errorMessage = 'El motivo de rechazo es obligatorio (Regla R3).';
      return;
    }

    const id = this.baseData.solicitud.id;
    this.solicitudService.rechazar(id, this.motivoRechazo, this.comentario).subscribe({
      next: (res) => {
        this.baseData = res;
        this.mostrarRechazoForm = false;
        this.motivoRechazo = '';
        this.comentario = '';
        this.successMessage = 'Solicitud rechazada con éxito.';
        this.cargarHistorial(id);
      },
      error: (err) => {
        this.errorMessage = err.error?.mensaje || 'Error al rechazar la solicitud.';
      }
    });
  }
}
