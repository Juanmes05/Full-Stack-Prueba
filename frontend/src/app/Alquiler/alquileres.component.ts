import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AlquilerService } from './Servicio/alquiler.service';
import { VehiculoService } from '../Vehiculo/Servicios/vehiculo.service';
import { Alquiler } from '../Alquiler/Modelo/alquiler.model';
import { Vehiculo } from '../Vehiculo/Modelos/vehiculo.model';

@Component({
  selector: 'app-alquileres',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './alquileres.component.html',
  styleUrls: ['./alquileres.component.css']
})
export class AlquileresComponent implements OnInit {
  alquileres: Alquiler[] = [];
  vehiculos: Vehiculo[] = [];
  alquilerForm: FormGroup;
  mensajeError: string | null = null;

  constructor(
    private alquilerService: AlquilerService,
    private vehiculoService: VehiculoService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {
    this.alquilerForm = this.fb.group({
      vehiculoId: ['', Validators.required],
      fechaInicio: ['', Validators.required],
      fechaFin: ['', Validators.required],
      pasajerosPrevistos: ['', [Validators.required, Validators.min(1)]]
    });
  }

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

    this.vehiculoService.getVehiculos().subscribe({
      next: (data) => {
        this.vehiculos = data;
        this.cdr.detectChanges();
      }
    });
  }

  onSubmit(): void {
    if (this.alquilerForm.valid) {
      this.mensajeError = null;
      const nuevoAlquiler: Alquiler = this.alquilerForm.value;
      
      this.alquilerService.crearAlquiler(nuevoAlquiler).subscribe({
        next: () => {
          this.cargarDatos();
          this.alquilerForm.reset({ vehiculoId: '', pasajerosPrevistos: '' });
        },
        error: (err) => {
          this.mensajeError = err.message;
          this.cdr.detectChanges();
        }
      });
    }
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