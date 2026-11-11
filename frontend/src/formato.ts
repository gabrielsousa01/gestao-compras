const moedaFmt = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const numeroFmt = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 3 });
const dataHoraFmt = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' });

export const moeda = (valor: number) => moedaFmt.format(valor);
export const numero = (valor: number) => numeroFmt.format(valor);
export const dataHora = (iso: string) => dataHoraFmt.format(new Date(iso));
