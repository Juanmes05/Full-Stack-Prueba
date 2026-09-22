import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { AlquilerService } from '../Alquiler/Servicio/alquiler.service';
import { Alquiler, EstadoAlquiler } from '../Alquiler/Modelo/alquiler.model';
import { TIPOS_VEHICULO, TipoVehiculo } from '../Vehiculo/Modelos/vehiculo.model';
import { duracionLegible } from '../Compartido/formato';
import { IconoComponent } from '../Compartido/icono.component';

@Component({
  selector: 'app-lista-reservas',
  standalone: true,
  imports: [CommonModule, RouterLink, IconoComponent],
  templateUrl: './lista-reservas.html',
  styleUrls: ['./lista-reservas.css']
})
export class ListaReservasComponent implements OnInit {
  readonly duracion = duracionLegible;
  readonly filtros: { estado: EstadoAlquiler | null; nombre: string }[] = [
    { estado: null, nombre: 'Todas' },
    { estado: 'PENDIENTE', nombre: 'Pendientes' },
    { estado: 'CONFIRMADO', nombre: 'Confirmadas' },
    { estado: 'CANCELADO', nombre: 'Canceladas' }
  ];

  // Todas las reservas (para los contadores) y las del filtro elegido, que se piden al backend
  todos = signal<Alquiler[]>([]);
  alquileres = signal<Alquiler[]>([]);
  estadoFiltro = signal<EstadoAlquiler | null>(null);
  cargando = signal(true);
  mensajeError = signal<string | null>(null);
  mensajeExito = signal<string | null>(null);
  // Reserva cuya cancelación está esperando confirmación del usuario
  cancelandoId = signal<number | null>(null);
  procesandoId = signal<number | null>(null);

  contadores = computed(() => {
    const todos = this.todos();
    return {
      total: todos.length,
      PENDIENTE: todos.filter(a => a.estado === 'PENDIENTE').length,
      CONFIRMADO: todos.filter(a => a.estado === 'CONFIRMADO').length,
      CANCELADO: todos.filter(a => a.estado === 'CANCELADO').length
    };
  });

  constructor(private alquilerService: AlquilerService) {}

  ngOnInit(): void {
    this.cargarDatos();
  }

  filtrar(estado: EstadoAlquiler | null): void {
    this.estadoFiltro.set(estado);
    this.cancelandoId.set(null);
    this.cargarDatos();
  }

  nombreFiltroActual(): string {
    return this.filtros.find(f => f.estado === this.estadoFiltro())?.nombre ?? '';
  }

  contador(estado: EstadoAlquiler | null): number {
    const c = this.contadores();
    return estado ? c[estado] : c.total;
  }

  cargarDatos(): void {
    const estado = this.estadoFiltro();
    this.cargando.set(true);

    forkJoin({
      todos: this.alquilerService.getAlquileres(),
      filtrados: estado ? this.alquilerService.getAlquileres(estado) : of(null)
    }).subscribe({
      next: ({ todos, filtrados }) => {
        // Las más recientes primero
        const ordenar = (lista: Alquiler[]) => [...lista].sort((a, b) => (b.id ?? 0) - (a.id ?? 0));
        this.todos.set(todos);
        this.alquileres.set(ordenar(filtrados ?? todos));
        this.cargando.set(false);
      },
      error: (err) => {
        this.mensajeError.set(err.message);
        this.cargando.set(false);
      }
    });
  }

  cambiarEstado(id: number | undefined, accion: 'confirmar' | 'cancelar'): void {
    if (!id) return;

    const request = accion === 'confirmar'
      ? this.alquilerService.confirmarAlquiler(id)
      : this.alquilerService.cancelarAlquiler(id);

    this.mensajeError.set(null);
    this.mensajeExito.set(null);
    this.cancelandoId.set(null);
    this.procesandoId.set(id);

    request.subscribe({
      next: (alquiler) => {
        this.mensajeExito.set(`Reserva #${alquiler.id} ${accion === 'confirmar' ? 'confirmada' : 'cancelada'}.`);
        this.procesandoId.set(null);
        this.cargarDatos();
      },
      error: (err) => {
        this.mensajeError.set(err.message);
        this.procesandoId.set(null);
      }
    });
  }

  nombreTipo(tipo: TipoVehiculo | undefined): string {
    return TIPOS_VEHICULO.find(t => t.valor === tipo)?.nombre ?? '';
  }
}
