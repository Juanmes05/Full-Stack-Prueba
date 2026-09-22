// "2 d 3 h", "5 h 30 min"...
export function duracionLegible(inicio: string, fin: string): string {
  const minutos = Math.round((new Date(fin).getTime() - new Date(inicio).getTime()) / 60000);
  if (!Number.isFinite(minutos) || minutos <= 0) {
    return '—';
  }

  const dias = Math.floor(minutos / 1440);
  const horas = Math.floor((minutos % 1440) / 60);
  const resto = minutos % 60;
  const partes = [];
  if (dias) partes.push(`${dias} d`);
  if (horas) partes.push(`${horas} h`);
  if (resto) partes.push(`${resto} min`);
  return partes.join(' ');
}
