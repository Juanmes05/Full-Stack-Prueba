import { Component, OnInit, computed, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { forkJoin } from 'rxjs';
import { VehiculoService } from './Servicios/vehiculo.service';
import { TIPOS_VEHICULO, TipoVehiculo, Vehiculo } from './Modelos/vehiculo.model';
import { Alquiler } from '../Alquiler/Modelo/alquiler.model';
import { duracionLegible } from '../Compartido/formato';
import { IconoComponent } from '../Compartido/icono.component';
import { VehiculoIlustracionComponent } from '../Compartido/vehiculo-ilustracion.component';

interface Aviso {
  corto: string;
  largo: string;
}

@Component({
  selector: 'app-vehiculos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, IconoComponent, VehiculoIlustracionComponent],
  templateUrl: './vehiculos.component.html',
  styleUrls: ['./vehiculos.component.css'],
  host: { '(document:keydown.escape)': 'cerrarDetalle()' }
})
export class VehiculosComponent implements OnInit {
  readonly tipos = TIPOS_VEHICULO;
  readonly duracion = duracionLegible;

  vehiculos = signal<Vehiculo[]>([]);
  vehiculoForm: FormGroup;
  mensajeError = signal<string | null>(null);
  mensajeExito = signal<string | null>(null);
  guardando = signal(false);

  resumenTipos = computed(() => this.tipos.map(t => ({
    ...t,
    total: this.vehiculos().filter(v => v.tipo === t.valor).length
  })));

  // Detalle del vehículo seleccionado
  seleccionado = signal<Vehiculo | null>(null);
  alquileresSeleccionado = signal<Alquiler[]>([]);
  cargandoDetalle = signal(false);
  errorDetalle = signal<string | null>(null);
  exitoDetalle = signal<string | null>(null);
  kilometrajeForm: FormGroup;

  constructor(
    private vehiculoService: VehiculoService,
    private fb: FormBuilder
  ) {
    // Inicializamos el formulario con los campos necesarios
    this.vehiculoForm = this.fb.group({
      matricula: ['', Validators.required],
      capacidadMaxima: [1, [Validators.required, Validators.min(1)]],
      kilometrajeActual: [0, [Validators.required, Validators.min(0)]],
      tipo: ['TURISMO', Validators.required]
    });

    this.kilometrajeForm = this.fb.group({
      kilometraje: [0, [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    this.cargarVehiculos();
  }

  cargarVehiculos(): void {
    this.vehiculoService.getVehiculos().subscribe({
      next: (data) => this.vehiculos.set(data),
      error: (err) => this.mensajeError.set(err.message)
    });
  }

  onSubmit(): void {
    if (this.vehiculoForm.invalid) {
      return;
    }

    const nuevoVehiculo: Vehiculo = this.vehiculoForm.value;
    this.mensajeError.set(null);
    this.mensajeExito.set(null);
    this.guardando.set(true);

    this.vehiculoService.crearVehiculo(nuevoVehiculo).subscribe({
      next: (vehiculoGuardado) => {
        this.mensajeExito.set(`Vehículo ${vehiculoGuardado.matricula} añadido a la flota.`);
        this.guardando.set(false);
        // Recargamos la lista para ver el nuevo vehículo
        this.cargarVehiculos();
        // Reseteamos el formulario a sus valores por defecto
        this.vehiculoForm.reset({
          matricula: '',
          capacidadMaxima: 1,
          kilometrajeActual: 0,
          tipo: 'TURISMO'
        });
      },
      error: (err) => {
        this.mensajeError.set(err.message);
        this.guardando.set(false);
      }
    });
  }

  verDetalle(vehiculo: Vehiculo): void {
    if (vehiculo.id === undefined) {
      return;
    }

    this.seleccionado.set(vehiculo);
    this.errorDetalle.set(null);
    this.exitoDetalle.set(null);
    this.kilometrajeForm.reset({ kilometraje: vehiculo.kilometrajeActual });
    this.cargarDetalle(vehiculo.id);
  }

  cerrarDetalle(): void {
    this.seleccionado.set(null);
    this.alquileresSeleccionado.set([]);
  }

  actualizarKilometraje(): void {
    const vehiculo = this.seleccionado();
    const kilometraje = Number(this.kilometrajeForm.value.kilometraje);
    if (!vehiculo?.id || this.kilometrajeForm.invalid) {
      return;
    }

    this.errorDetalle.set(null);
    this.exitoDetalle.set(null);

    this.vehiculoService.actualizarKilometraje(vehiculo.id, kilometraje).subscribe({
      next: (actualizado) => {
        this.seleccionado.set(actualizado);
        this.vehiculos.update(lista => lista.map(v => v.id === actualizado.id ? actualizado : v));
        this.exitoDetalle.set(`Kilometraje actualizado a ${actualizado.kilometrajeActual.toLocaleString('es-ES')} km.`);
      },
      error: (err) => this.errorDetalle.set(err.message)
    });
  }

  nombreTipo(tipo: TipoVehiculo): string {
    return this.tipos.find(t => t.valor === tipo)?.nombre ?? tipo;
  }

  // Restricciones por kilometraje de la regla R8
  avisos(v: Vehiculo): Aviso[] {
    const avisos: Aviso[] = [];
    if (v.tipo === 'FURGONETA' && v.kilometrajeActual > 150000) {
      avisos.push({ corto: 'Alta rotación', largo: 'Más de 150.000 km: solo alquileres de menos de 6 horas' });
    }
    if (v.kilometrajeActual > 200000) {
      avisos.push({ corto: 'Desgaste', largo: 'Más de 200.000 km: como máximo el 50 % de las plazas' });
    }
    return avisos;
  }

  private cargarDetalle(id: number): void {
    this.cargandoDetalle.set(true);
    this.alquileresSeleccionado.set([]);

    forkJoin({
      vehiculo: this.vehiculoService.getVehiculo(id),
      alquileres: this.vehiculoService.getAlquileresDeVehiculo(id)
    }).subscribe({
      next: ({ vehiculo, alquileres }) => {
        // Si mientras tanto se ha elegido otro vehículo, se descarta la respuesta
        if (this.seleccionado()?.id !== id) {
          return;
        }
        this.seleccionado.set(vehiculo);
        this.alquileresSeleccionado.set([...alquileres].sort((a, b) => (b.id ?? 0) - (a.id ?? 0)));
        this.cargandoDetalle.set(false);
      },
      error: (err) => {
        this.errorDetalle.set(err.message);
        this.cargandoDetalle.set(false);
      }
    });
  }
}
