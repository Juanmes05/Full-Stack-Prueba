import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { ListaReservasComponent } from './lista-reservas';
import { API_URL } from '../Compartido/api';

describe('ListaReservas', () => {
  let component: ListaReservasComponent;
  let fixture: ComponentFixture<ListaReservasComponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListaReservasComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ListaReservasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('should create', () => {
    http.expectOne(`${API_URL}/alquileres`).flush([]);
    expect(component).toBeTruthy();
  });

  it('should list the rentals with the vehicle plate and only the allowed actions', async () => {
    http.expectOne(`${API_URL}/alquileres`).flush([
      {
        id: 1, pasajerosPrevistos: 2, estado: 'PENDIENTE',
        fechaInicio: '2030-01-10T09:00:00', fechaFin: '2030-01-11T09:00:00',
        vehiculoId: 3, vehiculo: { id: 3, matricula: '1234ABC', tipo: 'TURISMO', capacidadMaxima: 5, kilometrajeActual: 1000 }
      },
      {
        id: 2, pasajerosPrevistos: 1, estado: 'CANCELADO',
        fechaInicio: '2030-02-10T09:00:00', fechaFin: '2030-02-11T09:00:00',
        vehiculoId: 3, vehiculo: { id: 3, matricula: '1234ABC', tipo: 'TURISMO', capacidadMaxima: 5, kilometrajeActual: 1000 }
      }
    ]);
    await fixture.whenStable();

    const filas = (fixture.nativeElement as HTMLElement).querySelectorAll('tbody tr');
    expect(filas.length).toBe(2);
    // Ordenadas de más reciente a más antigua: la cancelada (id 2) va primero y no tiene acciones
    expect(filas[0].querySelectorAll('button').length).toBe(0);
    expect(filas[1].textContent).toContain('1234ABC');
    expect(filas[1].textContent).toContain('Confirmar');
  });

  it('should ask the backend for the rentals of the selected state', () => {
    http.expectOne(`${API_URL}/alquileres`).flush([]);

    component.filtrar('CONFIRMADO');

    // Se piden las del estado elegido y todas, para los contadores de cada filtro
    const filtradas = http.expectOne(r => r.url === `${API_URL}/alquileres` && r.params.get('estado') === 'CONFIRMADO');
    const todas = http.expectOne(r => r.url === `${API_URL}/alquileres` && !r.params.has('estado'));
    todas.flush([]);
    filtradas.flush([]);
  });
});
