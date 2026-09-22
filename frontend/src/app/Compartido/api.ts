import { HttpErrorResponse } from '@angular/common/http';
import { throwError } from 'rxjs';

export const API_URL = 'http://localhost:8080';

// El backend responde a los errores con {"error": "descripción"}
export function mensajeDeError(error: HttpErrorResponse): string {
  if (error.status === 0) {
    return `No se puede conectar con el servidor. Comprueba que el backend está arrancado en ${API_URL}.`;
  }

  const cuerpo = error.error;
  if (typeof cuerpo === 'string' && cuerpo.trim()) {
    return cuerpo;
  } else if (typeof cuerpo?.error === 'string') {
    return cuerpo.error;
  } else if (cuerpo?.message) {
    return cuerpo.message;
  } else if (cuerpo?.mensaje) {
    return cuerpo.mensaje;
  }
  return `Error ${error.status} en el servidor`;
}

export function manejarError(error: HttpErrorResponse) {
  return throwError(() => new Error(mensajeDeError(error)));
}
