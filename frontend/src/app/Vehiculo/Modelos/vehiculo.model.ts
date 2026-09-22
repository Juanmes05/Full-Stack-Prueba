export type TipoVehiculo = 'TURISMO' | 'SUV' | 'FURGONETA';

export const TIPOS_VEHICULO: { valor: TipoVehiculo; nombre: string }[] = [
  { valor: 'TURISMO', nombre: 'Turismo' },
  { valor: 'SUV', nombre: 'SUV' },
  { valor: 'FURGONETA', nombre: 'Furgoneta' }
];

export interface Vehiculo {
  id?: number;
  matricula: string;
  capacidadMaxima: number;
  kilometrajeActual: number;
  tipo: TipoVehiculo;
}
