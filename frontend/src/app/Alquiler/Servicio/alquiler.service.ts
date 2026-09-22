import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Alquiler, EstadoAlquiler } from '../Modelo/alquiler.model';
import { API_URL, manejarError } from '../../Compartido/api';

@Injectable({
  providedIn: 'root'
})
export class AlquilerService {
  private apiUrl = `${API_URL}/alquileres`;

  constructor(private http: HttpClient) { }

  getAlquileres(estado?: EstadoAlquiler): Observable<Alquiler[]> {
    const params = estado ? new HttpParams().set('estado', estado) : undefined;
    return this.http.get<Alquiler[]>(this.apiUrl, { params }).pipe(
      catchError(manejarError)
    );
  }

  crearAlquiler(alquiler: Alquiler): Observable<Alquiler> {
    return this.http.post<Alquiler>(this.apiUrl, alquiler).pipe(
      catchError(manejarError)
    );
  }

  confirmarAlquiler(id: number): Observable<Alquiler> {
    return this.http.post<Alquiler>(`${this.apiUrl}/${id}/confirmar`, {}).pipe(
      catchError(manejarError)
    );
  }

  cancelarAlquiler(id: number): Observable<Alquiler> {
    return this.http.post<Alquiler>(`${this.apiUrl}/${id}/cancelar`, {}).pipe(
      catchError(manejarError)
    );
  }
}
