export type EstadoSolicitud = 'BORRADOR' | 'EN_REVISION' | 'APROBADA' | 'RECHAZADA' | 'PAGADA';
export type OrigenSolicitud = 'MANUAL' | 'CARGA_MASIVA';

export interface SolicitudResponse {
  id: number;
  folio: string;
  rutCliente: string;
  nombreCliente: string;
  monto: number;
  moneda: string;
  bancoDestino: string;
  cuentaDestino: string;
  referenciaBanco: string;
  origen: OrigenSolicitud;
  estado: EstadoSolicitud;
  motivoRechazo?: string;
  vecesReabierta: number;
  creadaPor: string;
  fechaCreacion: string;
  actualizadaPor?: string;
  fechaActualizacion?: string;
}

export interface UsuarioInfo {
  username: string;
  rol: string;
  email: string;
}

export interface SolicitudBase {
  solicitud: SolicitudResponse;
  usuario: UsuarioInfo;
}

export interface PaginaResultado<T> {
  contenido: T[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
}

export interface EventoSolicitud {
  id: number;
  solicitudId: number;
  estadoOrigen: EstadoSolicitud;
  estadoDestino: EstadoSolicitud;
  usuario: string;
  fecha: string;
  comentario?: string;
}

export interface LoginRequest {
  username: string;
  password?: string;
}

export interface AuthResponse {
  access_token: string;
  expires_in: number;
  rol: string;
  username: string;
  email: string;
}

export interface DetalleCargaError {
  fila: number;
  rutCliente?: string;
  motivoError: string;
  datosFila?: string;
}

export interface ResumenCargaMasiva {
  cargaId: number;
  nombreArchivo: string;
  totalRegistros: number;
  registrosExitosos: number;
  registrosFallidos: number;
  fechaCarga: string;
  errores: DetalleCargaError[];
}

export interface SolicitudFiltros {
  estado?: EstadoSolicitud | '';
  rut?: string;
  origen?: OrigenSolicitud | '';
  fechaDesde?: string;
  fechaHasta?: string;
  page?: number;
  size?: number;
}
