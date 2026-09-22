import { Component, computed, input } from '@angular/core';

// Iconos de trazo (24x24) dibujados como una lista de trazados SVG
const ICONOS = {
  buscar: ['M3 11a8 8 0 1 0 16 0a8 8 0 1 0 -16 0', 'm21 21-4.3-4.3'],
  calendario: ['M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z', 'M3 10h18', 'M8 2v4', 'M16 2v4'],
  reloj: ['M2 12a10 10 0 1 0 20 0a10 10 0 1 0 -20 0', 'M12 6v6l4 2'],
  personas: [
    'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2', 'M5 7a4 4 0 1 0 8 0a4 4 0 1 0 -8 0',
    'M22 21v-2a4 4 0 0 0-3-3.87', 'M16 3.13a4 4 0 0 1 0 7.75'
  ],
  coche: [
    'M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2',
    'M5 17a2 2 0 1 0 4 0a2 2 0 1 0 -4 0', 'M9 17h6', 'M15 17a2 2 0 1 0 4 0a2 2 0 1 0 -4 0'
  ],
  furgoneta: [
    'M14 18V6a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v11a1 1 0 0 0 1 1h2', 'M15 18H9',
    'M19 18h2a1 1 0 0 0 1-1v-3.65a1 1 0 0 0-.22-.62l-3.48-4.35A1 1 0 0 0 17.52 8H14',
    'M15 18a2 2 0 1 0 4 0a2 2 0 1 0 -4 0', 'M5 18a2 2 0 1 0 4 0a2 2 0 1 0 -4 0'
  ],
  cuentakm: ['m12 14 4-4', 'M3.34 19a10 10 0 1 1 17.32 0'],
  lista: [
    'M9 2h6a1 1 0 0 1 1 1v2a1 1 0 0 1-1 1H9a1 1 0 0 1-1-1V3a1 1 0 0 1 1-1z',
    'M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2',
    'M12 11h4', 'M12 16h4', 'M8 11h.01', 'M8 16h.01'
  ],
  flecha: ['M5 12h14', 'm12 5 7 7-7 7'],
  derecha: ['m9 18 6-6-6-6'],
  mas: ['M5 12h14', 'M12 5v14'],
  check: ['M20 6 9 17l-5-5'],
  cerrar: ['M18 6 6 18', 'm6 6 12 12'],
  ok: ['M2 12a10 10 0 1 0 20 0a10 10 0 1 0 -20 0', 'm9 12 2 2 4-4'],
  alerta: ['M2 12a10 10 0 1 0 20 0a10 10 0 1 0 -20 0', 'M12 8v4', 'M12 16h.01'],
  aviso: ['m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3', 'M12 9v4', 'M12 17h.01'],
  bandeja: [
    'M22 12h-6l-2 3h-4l-2-3H2',
    'M5.45 5.11 2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z'
  ],
  actualizar: ['M21 12a9 9 0 0 0-9-9 9.75 9.75 0 0 0-6.74 2.74L3 8', 'M3 3v5h5', 'M3 12a9 9 0 0 0 9 9 9.75 9.75 0 0 0 6.74-2.74L21 16', 'M16 16h5v5']
} satisfies Record<string, string[]>;

export type NombreIcono = keyof typeof ICONOS;

@Component({
  selector: 'app-icono',
  template: `
    <svg viewBox="0 0 24 24" [attr.width]="tamano()" [attr.height]="tamano()" fill="none" stroke="currentColor"
         [attr.stroke-width]="grosor()" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
      @for (d of trazos(); track $index) {
        <path [attr.d]="d" />
      }
    </svg>
  `,
  styles: `:host { display: inline-flex; flex-shrink: 0; line-height: 0; }`
})
export class IconoComponent {
  nombre = input.required<NombreIcono>();
  tamano = input(18);
  grosor = input(2);

  trazos = computed(() => ICONOS[this.nombre()]);
}
