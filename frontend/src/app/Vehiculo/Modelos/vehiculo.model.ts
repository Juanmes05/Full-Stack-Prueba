export interface Vehiculo {
  id?: number;
  matricula: string;
  capacidadMaxima: number;
  kilometrajeActual: number;
  tipo: 'FURGONETA' | 'TURISMO' | 'MOTO';
}