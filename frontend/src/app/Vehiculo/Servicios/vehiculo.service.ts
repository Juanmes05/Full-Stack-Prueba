import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Vehiculo } from '../Modelos/vehiculo.model';
import { Alquiler } from '../../Alquiler/Modelo/alquiler.model';
import { API_URL, manejarError } from '../../Compartido/api';

@Injectable({
  providedIn: 'root'
})
export class VehiculoService {
  private apiUrl = `${API_URL}/vehiculos`;

  constructor(private http: HttpClient) { }

  getVehiculos(): Observable<Vehiculo[]> {
    return this.http.get<Vehiculo[]>(this.apiUrl).pipe(
      catchError(manejarError)
    );
  }

  getVehiculo(id: number): Observable<Vehiculo> {
    return this.http.get<Vehiculo>(`${this.apiUrl}/${id}`).pipe(
      catchError(manejarError)
    );
  }

  crearVehiculo(vehiculo: Vehiculo): Observable<Vehiculo> {
    return this.http.post<Vehiculo>(this.apiUrl, vehiculo).pipe(
      catchError(manejarError)
    );
  }

  getAlquileresDeVehiculo(id: number): Observable<Alquiler[]> {
    return this.http.get<Alquiler[]>(`${this.apiUrl}/${id}/alquileres`).pipe(
      catchError(manejarError)
    );
  }

  actualizarKilometraje(id: number, kilometraje: number): Observable<Vehiculo> {
    return this.http.put<Vehiculo>(`${this.apiUrl}/${id}/kilometraje`, { kilometraje }).pipe(
      catchError(manejarError)
    );
  }

  // Fechas en formato "2026-10-01T09:00", tal como las da un <input type="datetime-local">
  getVehiculosLibres(inicio: string, fin: string, pasajeros: number): Observable<Vehiculo[]> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fin', fin)
      .set('pasajeros', pasajeros);
    return this.http.get<Vehiculo[]>(`${this.apiUrl}/libres`, { params }).pipe(
      catchError(manejarError)
    );
  }
}
