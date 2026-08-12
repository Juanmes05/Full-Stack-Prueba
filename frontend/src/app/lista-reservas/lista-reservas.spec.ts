import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListaReservasComponent } from './lista-reservas';

describe('ListaReservas', () => {
  let component: ListaReservasComponent;
  let fixture: ComponentFixture<ListaReservasComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListaReservasComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ListaReservasComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
