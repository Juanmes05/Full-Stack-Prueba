import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AlquilerService } from '../Alquiler/Servicio/alquiler.service'; 
import { Alquiler } from '../Alquiler/Modelo/alquiler.model';

@Component({
  selector: 'app-lista-reservas',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './lista-reservas.html',
  styleUrls: ['./lista-reservas.css'] 
})
export class ListaReservasComponent implements OnInit {
  alquileres: Alquiler[] = [];
  mensajeError: string | null = null;

  constructor(
    private alquilerService: AlquilerService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.alquilerService.getAlquileres().subscribe({
      next: (data) => {
        this.alquileres = data;
        this.cdr.detectChanges();
      }
    });
  }

  cambiarEstado(id: number | undefined, accion: 'confirmar' | 'cancelar'): void {
    if (!id) return;
    
    const request = accion === 'confirmar' 
      ? this.alquilerService.confirmarAlquiler(id)
      : this.alquilerService.cancelarAlquiler(id);

    request.subscribe({
      next: () => this.cargarDatos(),
      error: (err) => {
        this.mensajeError = err.message;
        this.cdr.detectChanges();
      }
    });
  }
}