import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { SolicitudService } from '../../../core/services/solicitud.service';

@Component({
  selector: 'app-formulario',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="form-container">
      <div class="card">
        <div class="form-header">
          <a routerLink="/solicitudes" class="back-link">&larr; Volver</a>
          <h2>{{ esEdicion ? 'Editar Solicitud (Borrador)' : 'Crear Nueva Solicitud' }}</h2>
          <p class="subtitle">Complete los antecedentes bancarios y del cliente</p>
        </div>

        <div *ngIf="errorMessage" [class]="errorStatus >= 400 && errorStatus < 500 ? 'alert alert-warning' : 'alert alert-danger'">
          <strong>{{ errorStatus >= 400 && errorStatus < 500 ? 'Advertencia:' : 'Error del servidor:' }}</strong> {{ errorMessage }}
        </div>

        <form [formGroup]="form" (ngSubmit)="guardar()">
          <div class="form-grid">
            <!-- RUT Cliente -->
            <div class="form-group">
              <label class="form-label" for="rutCliente">RUT Cliente *</label>
              <input
                type="text"
                id="rutCliente"
                class="form-control"
                formControlName="rutCliente"
                placeholder="12345678-5"
                [class.is-invalid]="campoInvalido('rutCliente')"
              />
              <div *ngIf="campoInvalido('rutCliente')" class="invalid-feedback">
                <span *ngIf="form.get('rutCliente')?.errors?.['required']">El RUT es obligatorio.</span>
                <span *ngIf="form.get('rutCliente')?.errors?.['pattern']">Formato de RUT inválido (ej: 12345678-5 o 12345678-K).</span>
              </div>
            </div>

            <!-- Nombre Cliente -->
            <div class="form-group">
              <label class="form-label" for="nombreCliente">Nombre Cliente *</label>
              <input
                type="text"
                id="nombreCliente"
                class="form-control"
                formControlName="nombreCliente"
                placeholder="Nombre completo del titular"
                [class.is-invalid]="campoInvalido('nombreCliente')"
              />
              <div *ngIf="campoInvalido('nombreCliente')" class="invalid-feedback">
                El nombre del cliente es obligatorio.
              </div>
            </div>

            <!-- Monto -->
            <div class="form-group">
              <label class="form-label" for="monto">Monto (CLP) *</label>
              <input
                type="number"
                id="monto"
                class="form-control"
                formControlName="monto"
                placeholder="Ej: 150000"
                [class.is-invalid]="campoInvalido('monto')"
              />
              <div *ngIf="campoInvalido('monto')" class="invalid-feedback">
                <span *ngIf="form.get('monto')?.errors?.['required']">El monto es obligatorio.</span>
                <span *ngIf="form.get('monto')?.errors?.['min']">El monto debe ser mayor a $0 CLP.</span>
                <span *ngIf="form.get('monto')?.errors?.['max']">El monto no puede superar $10.000.000 CLP.</span>
              </div>
            </div>

            <!-- Banco Destino -->
            <div class="form-group">
              <label class="form-label" for="bancoDestino">Banco Destino *</label>
              <select
                id="bancoDestino"
                class="form-control"
                formControlName="bancoDestino"
                [class.is-invalid]="campoInvalido('bancoDestino')"
              >
                <option value="">Seleccione un banco</option>
                <option value="BANCO CHILE">BANCO CHILE</option>
                <option value="BANCO ESTADO">BANCO ESTADO (CUENTA RUT)</option>
                <option value="BANCO SANTANDER">BANCO SANTANDER</option>
                <option value="BANCO BCI">BANCO BCI</option>
                <option value="BANCO ITAU">BANCO ITAU</option>
                <option value="SCOTIABANK">SCOTIABANK</option>
              </select>
              <div *ngIf="campoInvalido('bancoDestino')" class="invalid-feedback">
                El banco destino es obligatorio.
              </div>
            </div>

            <!-- Cuenta Destino -->
            <div class="form-group">
              <label class="form-label" for="cuentaDestino">Cuenta Destino *</label>
              <input
                type="text"
                id="cuentaDestino"
                class="form-control"
                formControlName="cuentaDestino"
                placeholder="Número de cuenta corriente o vista"
                [class.is-invalid]="campoInvalido('cuentaDestino')"
              />
              <div *ngIf="campoInvalido('cuentaDestino')" class="invalid-feedback">
                La cuenta destino es obligatoria.
              </div>
            </div>

            <!-- Referencia Banco -->
            <div class="form-group">
              <label class="form-label" for="referenciaBanco">Referencia Banco / Transacción *</label>
              <input
                type="text"
                id="referenciaBanco"
                class="form-control"
                formControlName="referenciaBanco"
                placeholder="Ej: REF-2026-9901"
                [class.is-invalid]="campoInvalido('referenciaBanco')"
              />
              <div *ngIf="campoInvalido('referenciaBanco')" class="invalid-feedback">
                La referencia del banco es obligatoria.
              </div>
            </div>
          </div>

          <div class="form-buttons">
            <button type="submit" class="btn btn-primary" [disabled]="loading">
              {{ loading ? 'Guardando...' : (esEdicion ? 'Guardar Cambios' : 'Crear Solicitud') }}
            </button>
            <a routerLink="/solicitudes" class="btn btn-secondary">Cancelar</a>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .form-container {
      max-width: 800px;
      margin: 0 auto;
    }
    .form-header {
      margin-bottom: 1.5rem;
    }
    .back-link {
      font-size: 0.85rem;
      color: #64748b;
      margin-bottom: 0.5rem;
      display: inline-block;
    }
    .subtitle {
      color: #64748b;
      font-size: 0.9rem;
    }
    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1.25rem;
    }
    @media (max-width: 640px) {
      .form-grid {
        grid-template-columns: 1fr;
      }
    }
    .is-invalid {
      border-color: #dc2626 !important;
    }
    .form-buttons {
      display: flex;
      gap: 1rem;
      margin-top: 1.5rem;
      padding-top: 1.25rem;
      border-top: 1px solid #e2e8f0;
    }
  `]
})
export class FormularioComponent implements OnInit {
  form!: FormGroup;
  esEdicion = false;
  solicitudId: number | null = null;
  loading = false;
  errorMessage = '';
  errorStatus = 0;

  // Regex mirror for Chilean RUT (e.g. 12345678-5 or 12345678-K)
  private rutRegex = /^(\d{7,8}-[\dkK])$/;

  constructor(
    private fb: FormBuilder,
    private solicitudService: SolicitudService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.route.paramMap.subscribe(params => {
      const idStr = params.get('id');
      if (idStr) {
        this.esEdicion = true;
        this.solicitudId = +idStr;
        this.cargarSolicitud(this.solicitudId);
      }
    });
  }

  private initForm(): void {
    this.form = this.fb.group({
      rutCliente: ['', [Validators.required, Validators.pattern(this.rutRegex)]],
      nombreCliente: ['', Validators.required],
      monto: ['', [Validators.required, Validators.min(1)]],
      bancoDestino: ['', Validators.required],
      cuentaDestino: ['', Validators.required],
      referenciaBanco: ['', Validators.required]
    });
  }

  cargarSolicitud(id: number): void {
    this.loading = true;
    this.errorStatus = 0;
    this.solicitudService.obtenerPorId(id).subscribe({
      next: (base) => {
        this.loading = false;
        if (base.solicitud.estado !== 'BORRADOR') {
          this.errorMessage = 'Solo se pueden editar solicitudes en estado BORRADOR.';
          this.errorStatus = 400;
          this.form.disable();
          return;
        }
        this.form.patchValue({
          rutCliente: base.solicitud.rutCliente,
          nombreCliente: base.solicitud.nombreCliente,
          monto: base.solicitud.monto,
          bancoDestino: base.solicitud.bancoDestino,
          cuentaDestino: base.solicitud.cuentaDestino,
          referenciaBanco: base.solicitud.referenciaBanco
        });
      },
      error: (err) => {
        this.loading = false;
        this.errorStatus = err.status || 500;
        this.errorMessage = err.error?.detalle || 'No se pudo cargar la información de la solicitud.';
      }
    });
  }

  campoInvalido(nombreCampo: string): boolean {
    const campo = this.form.get(nombreCampo);
    return !!(campo && campo.invalid && (campo.dirty || campo.touched));
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.errorStatus = 0;

    const payload = this.form.value;

    const call$ = this.esEdicion && this.solicitudId
      ? this.solicitudService.actualizar(this.solicitudId, payload)
      : this.solicitudService.crear(payload);

    call$.subscribe({
      next: (res) => {
        this.loading = false;
        this.router.navigate(['/solicitudes', res.solicitud.id]);
      },
      error: (err) => {
        this.loading = false;
        this.errorStatus = err.status || 500;
        this.errorMessage = err.error?.detalle || 'Error al guardar la solicitud en el servidor.';
      }
    });
  }
}
