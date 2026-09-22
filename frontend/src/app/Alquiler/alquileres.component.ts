import { Component, computed, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AlquilerService } from './Servicio/alquiler.service';
import { VehiculoService } from '../Vehiculo/Servicios/vehiculo.service';
import { Alquiler } from './Modelo/alquiler.model';
import { TIPOS_VEHICULO, TipoVehiculo, Vehiculo } from '../Vehiculo/Modelos/vehiculo.model';
import { duracionLegible } from '../Compartido/formato';
import { IconoComponent } from '../Compartido/icono.component';
import { VehiculoIlustracionComponent } from '../Compartido/vehiculo-ilustracion.component';

interface Busqueda {
  inicio: string;
  fin: string;
  pasajeros: number;
}

@Component({
  selector: 'app-alquileres',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, IconoComponent, VehiculoIlustracionComponent],
  templateUrl: './alquileres.component.html',
  styleUrls: ['./alquileres.component.css']
})
export class AlquileresComponent {
  readonly tipos = TIPOS_VEHICULO;
  readonly duracion = duracionLegible;

  alquilerForm: FormGroup;

  // null mientras no se ha hecho ninguna búsqueda
  vehiculosLibres = signal<Vehiculo[] | null>(null);
  busqueda = signal<Busqueda | null>(null);
  filtroTipo = signal<TipoVehiculo | ''>('');
  buscando = signal(false);
  reservandoId = signal<number | null>(null);
  reservaCreada = signal<Alquiler | null>(null);
  mensajeError = signal<string | null>(null);

  vehiculosFiltrados = computed(() => {
    const tipo = this.filtroTipo();
    const libres = this.vehiculosLibres() ?? [];
    return tipo ? libres.filter(v => v.tipo === tipo) : libres;
  });

  constructor(
    private alquilerService: AlquilerService,
    private vehiculoService: VehiculoService,
    private fb: FormBuilder
  ) {
    this.alquilerForm = this.fb.group({
      fechaInicio: ['', Validators.required],
      fechaFin: ['', Validators.required],
      pasajerosPrevistos: ['', [Validators.required, Validators.min(1)]],
      tipo: ['']
    });

    this.alquilerForm.get('tipo')!.valueChanges.subscribe(tipo => this.filtroTipo.set(tipo ?? ''));
  }

  onSubmit(): void {
    if (this.alquilerForm.invalid) {
      return;
    }

    const { fechaInicio, fechaFin, pasajerosPrevistos } = this.alquilerForm.value;
    const busqueda: Busqueda = { inicio: fechaInicio, fin: fechaFin, pasajeros: Number(pasajerosPrevistos) };

    this.mensajeError.set(null);
    this.reservaCreada.set(null);
    this.buscando.set(true);

    this.vehiculoService.getVehiculosLibres(busqueda.inicio, busqueda.fin, busqueda.pasajeros).subscribe({
      next: (data) => {
        this.busqueda.set(busqueda);
        this.vehiculosLibres.set(data);
        this.buscando.set(false);
      },
      error: (err) => {
        this.busqueda.set(null);
        this.vehiculosLibres.set(null);
        this.mensajeError.set(err.message);
        this.buscando.set(false);
      }
    });
  }

  reservar(vehiculo: Vehiculo): void {
    const busqueda = this.busqueda();
    if (!busqueda || vehiculo.id === undefined) {
      return;
    }

    const nuevoAlquiler: Alquiler = {
      vehiculoId: vehiculo.id,
      pasajerosPrevistos: busqueda.pasajeros,
      fechaInicio: busqueda.inicio,
      fechaFin: busqueda.fin
    };

    this.mensajeError.set(null);
    this.reservaCreada.set(null);
    this.reservandoId.set(vehiculo.id);

    this.alquilerService.crearAlquiler(nuevoAlquiler).subscribe({
      next: (alquiler) => {
        this.reservaCreada.set(alquiler);
        this.reservandoId.set(null);
      },
      error: (err) => {
        this.mensajeError.set(err.message);
        this.reservandoId.set(null);
      }
    });
  }

  nombreTipo(tipo: TipoVehiculo): string {
    return this.tipos.find(t => t.valor === tipo)?.nombre ?? tipo;
  }
}
