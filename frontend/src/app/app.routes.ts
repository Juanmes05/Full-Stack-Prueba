import { Routes } from '@angular/router';
import { AlquileresComponent } from './Alquiler/alquileres.component';
import { VehiculosComponent } from './Vehiculo/vehiculos.component';
import { ListaReservasComponent } from './lista-reservas/lista-reservas'; 

export const routes: Routes = [
  { path: '', component: AlquileresComponent }, 
  { path: 'vehiculos', component: VehiculosComponent },
  { path: 'alquileres', redirectTo: '', pathMatch: 'full' },
  { path: 'lista-reservas', component: ListaReservasComponent },
  { path: '**', redirectTo: '' } 
];