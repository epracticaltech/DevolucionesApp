import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SolicitudService } from '../../core/services/solicitud.service';
import { ResumenCargaMasiva } from '../../core/models/solicitud.model';

@Component({
  selector: 'app-carga-masiva',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="carga-container">
      <div class="header-section">
        <h2>Carga Masiva de Devoluciones (CSV)</h2>
        <p class="subtitle">Suba un archivo CSV con hasta 1.000 solicitudes para procesamiento por lotes (Chunks de 100)</p>
      </div>

      <!-- Upload Form Card -->
      <div class="card upload-card">
        <div class="upload-dropzone">
          <input
            type="file"
            id="fileInput"
            accept=".csv"
            (change)="onFileSelected($event)"
            class="file-input"
          />
          <label for="fileInput" class="dropzone-label">
            <span class="file-icon">📄</span>
            <span class="file-text">{{ archivoSeleccionado ? archivoSeleccionado.name : 'Haga clic para seleccionar archivo CSV' }}</span>
            <small *ngIf="archivoSeleccionado" class="file-size">{{ (archivoSeleccionado.size / 1024) | number:'1.0-1' }} KB</small>
          </label>
        </div>

        <div *ngIf="errorMessage" class="alert alert-danger margin-top">
          {{ errorMessage }}
        </div>

        <div class="upload-actions">
          <button
            class="btn btn-primary btn-lg"
            [disabled]="!archivoSeleccionado || loading"
            (click)="subirArchivo()"
          >
            {{ loading ? 'Procesando lotes en el servidor...' : 'Procesar Carga Masiva' }}
          </button>
        </div>
      </div>

      <!-- Summary & Errors Card -->
      <div *ngIf="resumen" class="card summary-card">
        <h3>Resumen del Procesamiento de Carga</h3>
        
        <div class="summary-grid">
          <div class="stat-box">
            <span class="stat-label">Archivo</span>
            <span class="stat-value font-mono">{{ resumen.nombreArchivo }}</span>
          </div>

          <div class="stat-box">
            <span class="stat-label">Total Registros</span>
            <span class="stat-value">{{ resumen.totalRegistros }}</span>
          </div>

          <div class="stat-box success">
            <span class="stat-label">Procesados OK</span>
            <span class="stat-value">{{ resumen.registrosExitosos }}</span>
          </div>

          <div class="stat-box error" [class.has-errors]="resumen.registrosFallidos > 0">
            <span class="stat-label">Fallidos con Error</span>
            <span class="stat-value">{{ resumen.registrosFallidos }}</span>
          </div>
        </div>

        <!-- Errors Table -->
        <div *ngIf="resumen.errores && resumen.errores.length > 0" class="errors-section">
          <h4 class="errors-title">Detalle de Errores por Fila ({{ resumen.errores.length }} filas con fallas)</h4>

          <div class="table-responsive">
            <table>
              <thead>
                <tr>
                  <th>Fila #</th>
                  <th>RUT Cliente</th>
                  <th>Motivo de Error</th>
                  <th>Datos de la Fila</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let err of resumen.errores">
                  <td><strong>Fila {{ err.fila }}</strong></td>
                  <td>{{ err.rutCliente || 'N/A' }}</td>
                  <td><span class="text-danger">{{ err.motivoError }}</span></td>
                  <td><code class="data-code">{{ err.datosFila || '-' }}</code></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .carga-container {
      max-width: 960px;
      margin: 0 auto;
    }
    .header-section {
      margin-bottom: 1.5rem;
    }
    .subtitle {
      color: #64748b;
      font-size: 0.9rem;
    }
    .upload-card {
      padding: 2rem;
      text-align: center;
    }
    .upload-dropzone {
      border: 2px dashed #cbd5e1;
      border-radius: 12px;
      padding: 2.5rem;
      background: #f8fafc;
      cursor: pointer;
      transition: all 0.2s;
    }
    .upload-dropzone:hover {
      border-color: #2563eb;
      background: #eff6ff;
    }
    .file-input {
      display: none;
    }
    .dropzone-label {
      display: flex;
      flex-direction: column;
      align-items: center;
      cursor: pointer;
    }
    .file-icon {
      font-size: 2.5rem;
      margin-bottom: 0.5rem;
    }
    .file-text {
      font-size: 1rem;
      font-weight: 500;
      color: #0f172a;
    }
    .file-size {
      color: #64748b;
      margin-top: 0.25rem;
    }
    .upload-actions {
      margin-top: 1.5rem;
    }
    .btn-lg {
      padding: 0.75rem 2rem;
      font-size: 1rem;
    }
    .margin-top {
      margin-top: 1rem;
    }

    /* Summary Stats */
    .summary-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 1rem;
      margin-top: 1rem;
      margin-bottom: 2rem;
    }
    .stat-box {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 8px;
      padding: 1rem;
      display: flex;
      flex-direction: column;
    }
    .stat-label {
      font-size: 0.8rem;
      color: #64748b;
    }
    .stat-value {
      font-size: 1.25rem;
      font-weight: 700;
      color: #0f172a;
      margin-top: 0.25rem;
    }
    .stat-box.success .stat-value {
      color: #16a34a;
    }
    .stat-box.error.has-errors .stat-value {
      color: #dc2626;
    }
    .errors-section {
      margin-top: 1.5rem;
      padding-top: 1.5rem;
      border-top: 1px solid #e2e8f0;
    }
    .errors-title {
      color: #b91c1c;
      font-size: 1rem;
      margin-bottom: 1rem;
    }
    .text-danger {
      color: #dc2626;
    }
    .data-code {
      font-size: 0.75rem;
      background: #f1f5f9;
      padding: 0.2rem 0.4rem;
      border-radius: 4px;
    }
    .font-mono {
      font-family: monospace;
    }
  `]
})
export class CargaMasivaComponent {
  archivoSeleccionado: File | null = null;
  loading = false;
  errorMessage = '';
  resumen: ResumenCargaMasiva | null = null;

  constructor(private solicitudService: SolicitudService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.archivoSeleccionado = input.files[0];
      this.errorMessage = '';
    }
  }

  subirArchivo(): void {
    if (!this.archivoSeleccionado) return;

    this.loading = true;
    this.errorMessage = '';
    this.resumen = null;

    this.solicitudService.cargarCsv(this.archivoSeleccionado).subscribe({
      next: (res) => {
        this.loading = false;
        this.resumen = res;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.mensaje || 'Error al procesar el archivo CSV en el servidor.';
      }
    });
  }
}
