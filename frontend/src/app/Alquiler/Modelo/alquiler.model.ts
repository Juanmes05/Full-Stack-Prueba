import { Vehiculo } from '../../Vehiculo/Modelos/vehiculo.model';

export type EstadoAlquiler = 'PENDIENTE' | 'CONFIRMADO' | 'CANCELADO';

export interface Alquiler {
  id?: number;
  fechaInicio: string;
  fechaFin: string;
  pasajerosPrevistos: number;
  estado?: EstadoAlquiler;
  vehiculoId?: number;
  vehiculo?: Vehiculo;
}
