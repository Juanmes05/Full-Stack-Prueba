import { Routes } from '@angular/router';
import { AlquileresComponent } from './Alquiler/alquileres.component';
import { VehiculosComponent } from './Vehiculo/vehiculos.component';
import { ListaReservasComponent } from './lista-reservas/lista-reservas';

export const routes: Routes = [
  { path: '', component: AlquileresComponent, title: 'JM5 · Alquila coches y furgonetas' },
  { path: 'vehiculos', component: VehiculosComponent, title: 'JM5 · Vehículos' },
  { path: 'alquileres', redirectTo: '', pathMatch: 'full' },
  { path: 'lista-reservas', component: ListaReservasComponent, title: 'JM5 · Reservas' },
  { path: '**', redirectTo: '' }
];
