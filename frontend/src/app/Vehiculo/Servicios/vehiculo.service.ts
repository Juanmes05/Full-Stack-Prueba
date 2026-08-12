import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Vehiculo } from '../Modelos/vehiculo.model';

@Injectable({
  providedIn: 'root'
})
export class VehiculoService {
  private apiUrl = 'http://localhost:8080/vehiculos'; 

  constructor(private http: HttpClient) { }

  getVehiculos(): Observable<Vehiculo[]> {
    return this.http.get<Vehiculo[]>(this.apiUrl);
  }

  crearVehiculo(vehiculo: Vehiculo): Observable<Vehiculo> {
    return this.http.post<Vehiculo>(this.apiUrl, vehiculo);
  }
}