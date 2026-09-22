import { Component, input } from '@angular/core';
import { TipoVehiculo } from '../Vehiculo/Modelos/vehiculo.model';

// Silueta lateral del vehículo con la franja amarilla de la marca
@Component({
  selector: 'app-vehiculo-ilustracion',
  template: `
    <svg viewBox="0 0 200 80" role="img" [attr.aria-label]="'Ilustración de ' + tipo().toLowerCase()">
      <path class="sombra" d="M16 71a84 5 0 1 0 168 0a84 5 0 1 0 -168 0" />
      @switch (tipo()) {
        @case ('FURGONETA') {
          <path class="carroceria" d="M16 61C13 61 12 59 12 56L12 16C12 12 15 9 19 9L138 9C143 9 147 11 150 15L170 36L182 39C186 40 188 44 188 48L188 56C188 59 186 61 183 61Z" />
          <path class="cristal" d="M141 15C143 15 144 16 145 17L162 36L141 36Z" />
          <path class="cristal" d="M114 15H135V33H114Z" />
          <path class="junta" d="M108 12V57M60 12V57" />
          <path class="franja" d="M16 45H186" />
          <text class="rotulo" x="24" y="35">JM5</text>
          <path class="faro" d="M181 43h6v5h-6z" />
          <path class="rueda" d="M34 61a12 12 0 1 0 24 0a12 12 0 1 0 -24 0M142 61a12 12 0 1 0 24 0a12 12 0 1 0 -24 0" />
          <path class="llanta" d="M41 61a5 5 0 1 0 10 0a5 5 0 1 0 -10 0M149 61a5 5 0 1 0 10 0a5 5 0 1 0 -10 0" />
        }
        @case ('SUV') {
          <path class="carroceria" d="M18 61C14 61 12 58 12 54L12 42C12 37 15 34 20 33L44 31L60 14C62 12 65 11 68 11L140 11C145 11 149 13 152 17L164 31L180 34C185 35 188 39 188 44L188 56C188 59 186 61 183 61Z" />
          <path class="baca" d="M72 7H136" />
          <path class="cristal" d="M66 29L77 17H100V29Z" />
          <path class="cristal" d="M104 17H139C142 17 144 18 146 20L155 29H104Z" />
          <path class="junta" d="M102 31V56" />
          <path class="franja" d="M16 45H186" />
          <path class="faro" d="M181 38h6v5h-6z" />
          <path class="rueda" d="M37 61a13 13 0 1 0 26 0a13 13 0 1 0 -26 0M139 61a13 13 0 1 0 26 0a13 13 0 1 0 -26 0" />
          <path class="llanta" d="M44 61a6 6 0 1 0 12 0a6 6 0 1 0 -12 0M146 61a6 6 0 1 0 12 0a6 6 0 1 0 -12 0" />
        }
        @default {
          <path class="carroceria" d="M18 61C14 61 12 58 12 54L12 47C12 42 15 39 20 38L52 34L74 21C78 19 82 18 87 18L124 18C130 18 135 20 139 24L152 35L178 39C184 40 188 44 188 50L188 56C188 59 186 61 183 61Z" />
          <path class="cristal" d="M82 33L92 25C95 23 97 22 100 22H106V33Z" />
          <path class="cristal" d="M110 22H123C127 22 130 23 133 26L142 33H110Z" />
          <path class="junta" d="M108 35V56" />
          <path class="franja" d="M16 48H186" />
          <path class="faro" d="M181 43h6v4h-6z" />
          <path class="rueda" d="M40 61a12 12 0 1 0 24 0a12 12 0 1 0 -24 0M138 61a12 12 0 1 0 24 0a12 12 0 1 0 -24 0" />
          <path class="llanta" d="M47 61a5 5 0 1 0 10 0a5 5 0 1 0 -10 0M145 61a5 5 0 1 0 10 0a5 5 0 1 0 -10 0" />
        }
      }
    </svg>
  `,
  styles: `
    :host { display: block; }
    svg { display: block; width: 100%; height: auto; overflow: visible; }
    .sombra { fill: rgba(17, 18, 20, 0.1); }
    .carroceria { fill: var(--tinta, #111214); }
    .cristal { fill: #d9dee6; }
    .junta { stroke: rgba(255, 255, 255, 0.14); stroke-width: 1.5; fill: none; }
    .franja { stroke: var(--amarillo, #ffcc00); stroke-width: 3; fill: none; }
    .baca { stroke: var(--tinta, #111214); stroke-width: 3; stroke-linecap: round; fill: none; }
    .faro { fill: var(--amarillo, #ffcc00); }
    .rueda { fill: #0a0a0b; }
    .llanta { fill: #c9c9c3; }
    .rotulo { fill: var(--amarillo, #ffcc00); font: italic 800 17px var(--fuente, sans-serif); letter-spacing: -0.5px; }
  `
})
export class VehiculoIlustracionComponent {
  tipo = input.required<TipoVehiculo>();
}
