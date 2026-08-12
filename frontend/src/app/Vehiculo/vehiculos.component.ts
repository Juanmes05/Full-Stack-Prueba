import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { VehiculoService } from './Servicios/vehiculo.service';
import { Vehiculo } from './Modelos/vehiculo.model';

@Component({
  selector: 'app-vehiculos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './vehiculos.component.html',
  styleUrls: ['./vehiculos.component.css']
})
export class VehiculosComponent implements OnInit {
  vehiculos: Vehiculo[] = [];
  vehiculoForm: FormGroup;
  mensajeError: string | null = null;

  constructor(
    private vehiculoService: VehiculoService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {
    // Inicializamos el formulario con los campos necesarios
    this.vehiculoForm = this.fb.group({
      matricula: ['', Validators.required],
      capacidadMaxima: [1, [Validators.required, Validators.min(1)]],
      kilometrajeActual: [0, [Validators.required, Validators.min(0)]],
      tipo: ['TURISMO', Validators.required]
    });
  }

  ngOnInit(): void {
    this.cargarVehiculos();
  }

  cargarVehiculos(): void {
    this.vehiculoService.getVehiculos().subscribe({
      next: (data) => {
        this.vehiculos = data;
        this.cdr.detectChanges(); // <-- Obliga a Angular a repintar el HTML inmediatamente
      },
      error: (err) => console.error('Error al cargar vehículos', err)
    });
  }

  onSubmit(): void {
    if (this.vehiculoForm.valid) {
      const nuevoVehiculo: Vehiculo = this.vehiculoForm.value;
      
      this.vehiculoService.crearVehiculo(nuevoVehiculo).subscribe({
        next: (vehiculoGuardado) => {
          console.log('Vehículo creado con éxito:', vehiculoGuardado);
          // Recargamos la lista para ver el nuevo vehículo
          this.cargarVehiculos();
          // Reseteamos el formulario a sus valores por defecto
          this.vehiculoForm.reset({
            matricula: '',
            capacidadMaxima: 1,
            kilometrajeActual: 0,
            tipo: 'TURISMO'
          });
          this.cdr.detectChanges();
        },
        error: (err) => {this.mensajeError = 'Error al crear vehículo (¿matrícula duplicada?)'; 
          console.error(this.mensajeError, err);
          this.cdr.detectChanges();}
      });
    }
  }
}