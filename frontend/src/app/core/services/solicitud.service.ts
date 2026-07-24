import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  EventoSolicitud,
  PaginaResultado,
  ResumenCargaMasiva,
  SolicitudBase,
  SolicitudFiltros,
  SolicitudResponse
} from '../models/solicitud.model';

@Injectable({
  providedIn: 'root'
})
export class SolicitudService {
  private apiUrl = `${environment.apiUrl}/solicitudes`;

  constructor(private http: HttpClient) {}

  listar(filtros: SolicitudFiltros = {}): Observable<PaginaResultado<SolicitudResponse>> {
    let params = new HttpParams();
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.rut) params = params.set('rut', filtros.rut);
    if (filtros.origen) params = params.set('origen', filtros.origen);
    if (filtros.fechaDesde) params = params.set('fechaDesde', filtros.fechaDesde);
    if (filtros.fechaHasta) params = params.set('fechaHasta', filtros.fechaHasta);
    params = params.set('page', (filtros.page ?? 0).toString());
    params = params.set('size', (filtros.size ?? 10).toString());

    return this.http.get<PaginaResultado<SolicitudResponse>>(this.apiUrl, { params });
  }

  obtenerPorId(id: number): Observable<SolicitudBase> {
    return this.http.get<SolicitudBase>(`${this.apiUrl}/${id}`);
  }

  obtenerHistorial(id: number): Observable<EventoSolicitud[]> {
    return this.http.get<EventoSolicitud[]>(`${this.apiUrl}/${id}/historial`);
  }

  crear(solicitud: Partial<SolicitudResponse>): Observable<SolicitudBase> {
    return this.http.post<SolicitudBase>(this.apiUrl, solicitud);
  }

  actualizar(id: number, solicitud: Partial<SolicitudResponse>): Observable<SolicitudBase> {
    return this.http.put<SolicitudBase>(`${this.apiUrl}/${id}`, solicitud);
  }

  enviarARevision(id: number, comentario?: string): Observable<SolicitudBase> {
    return this.http.post<SolicitudBase>(`${this.apiUrl}/${id}/enviar`, { comentario });
  }

  anular(id: number, comentario?: string): Observable<SolicitudBase> {
    return this.http.post<SolicitudBase>(`${this.apiUrl}/${id}/anular`, { comentario });
  }

  aprobar(id: number, comentario?: string): Observable<SolicitudBase> {
    return this.http.post<SolicitudBase>(`${this.apiUrl}/${id}/aprobar`, { comentario });
  }

  rechazar(id: number, motivoRechazo: string, comentario?: string): Observable<SolicitudBase> {
    return this.http.post<SolicitudBase>(`${this.apiUrl}/${id}/rechazar`, { motivoRechazo, comentario });
  }

  pagar(id: number, comentario?: string): Observable<SolicitudBase> {
    return this.http.post<SolicitudBase>(`${this.apiUrl}/${id}/pagar`, { comentario });
  }

  reabrir(id: number, comentario?: string): Observable<SolicitudBase> {
    return this.http.post<SolicitudBase>(`${this.apiUrl}/${id}/reabrir`, { comentario });
  }

  cargarCsv(file: File): Observable<ResumenCargaMasiva> {
    const formData = new FormData();
    formData.append('archivo', file);
    return this.http.post<ResumenCargaMasiva>(`${environment.apiUrl}/cargas-masivas`, formData);
  }
}
