import { Vehiculo } from '/Users/jmesa/Desktop/Pagina/frontend/src/app/Vehiculo/Modelos/vehiculo.model';

export interface Alquiler {
  id?: number;
  fechaInicio: string; 
  fechaFin: string; 
  pasajerosPrevistos: number;
  estado?: 'PENDIENTE' | 'CONFIRMADO' | 'CANCELADO';
  vehiculoId?: number; 
  vehiculo?: Vehiculo;
}