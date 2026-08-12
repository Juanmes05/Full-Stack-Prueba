import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Alquiler } from '../Modelo/alquiler.model';

@Injectable({
  providedIn: 'root'
})
export class AlquilerService {
  private apiUrl = 'http://localhost:8080/alquileres';

  constructor(private http: HttpClient) { }

  getAlquileres(): Observable<Alquiler[]> {
    return this.http.get<Alquiler[]>(this.apiUrl);
  }

  crearAlquiler(alquiler: Alquiler): Observable<Alquiler> {
    return this.http.post<Alquiler>(this.apiUrl, alquiler).pipe(
      catchError(this.manejarError)
    );
  }

  confirmarAlquiler(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/confirmar`, {}).pipe(
      catchError(this.manejarError)
    );
  }

  cancelarAlquiler(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/cancelar`, {}).pipe(
      catchError(this.manejarError)
    );
  }

  private manejarError(error: HttpErrorResponse) {
    let mensajeBackend = 'Error desconocido en el servidor';

    if (error.error) {
      if (typeof error.error === 'string') {
        mensajeBackend = error.error;
      } else if (error.error.error && typeof error.error.error === 'string') {
        mensajeBackend = error.error.error; 
      } else if (error.error.message) {
        mensajeBackend = error.error.message;
      } else if (error.error.mensaje) {
        mensajeBackend = error.error.mensaje;
      } else {
        mensajeBackend = JSON.stringify(error.error);
      }
    }
    return throwError(() => new Error(mensajeBackend));
  }
}