// Tipos e chamadas da API REST do backend (Spring Boot).

export interface Unidade { id: number; nome: string }
export interface Item { id: number; nome: string; unidadeMedida: string }
export interface Fornecedor { id: number; nome: string; email: string }

export interface Contrato {
  id: number;
  fornecedor: Fornecedor;
  item: Item;
  precoUnitario: number;
  quantidadePorCaixa: number;
  ativo: boolean;
}

export interface Estoque {
  id: number;
  unidade: Unidade;
  item: Item;
  saldo: number;
  consumoMedioDiario: number;
  diasDeCobertura: number | null;
}

export type StatusRequisicao = 'PENDENTE' | 'APROVADA' | 'REPROVADA';

export interface RequisicaoItem {
  id: number;
  unidade: Unidade;
  item: Item;
  fornecedor: Fornecedor;
  necessidade: number;
  caixas: number;
  quantidadePorCaixa: number;
  quantidade: number;
  precoUnitario: number;
  valorTotal: number;
}

export interface PedidoResumo {
  id: number;
  requisicaoId: number;
  fornecedor: Fornecedor;
  criadoEm: string;
  quantidadeItens: number;
  valorTotal: number;
}

export interface RequisicaoResumo {
  id: number;
  criadaEm: string;
  status: StatusRequisicao;
  diasCobertura: number;
  diasSeguranca: number;
  quantidadeItens: number;
  valorTotal: number;
}

export interface Requisicao {
  id: number;
  criadaEm: string;
  status: StatusRequisicao;
  diasCobertura: number;
  diasSeguranca: number;
  motivoReprovacao: string | null;
  decididaEm: string | null;
  valorTotal: number;
  itens: RequisicaoItem[];
  itensSemContrato: string[];
  pedidos: PedidoResumo[];
}

/** Erro devolvido pela API no formato Problem Details (RFC 9457). */
export class ErroApi extends Error {}

async function chamar<T>(caminho: string, opcoes?: RequestInit): Promise<T> {
  const resposta = await fetch(`/api${caminho}`, {
    ...opcoes,
    headers: { 'Content-Type': 'application/json', ...opcoes?.headers },
  });
  if (!resposta.ok) {
    let mensagem = `Erro ${resposta.status}`;
    try {
      const problema = await resposta.json();
      if (problema?.detail) mensagem = problema.detail;
    } catch {
      // corpo vazio ou não-JSON: fica a mensagem genérica
    }
    throw new ErroApi(mensagem);
  }
  return resposta.json() as Promise<T>;
}

export const api = {
  estoques: () => chamar<Estoque[]>('/estoques'),
  contratos: () => chamar<Contrato[]>('/contratos'),
  requisicoes: () => chamar<RequisicaoResumo[]>('/requisicoes'),
  requisicao: (id: number) => chamar<Requisicao>(`/requisicoes/${id}`),
  gerarRequisicao: (diasCobertura: number, diasSeguranca: number) =>
    chamar<Requisicao>('/requisicoes', {
      method: 'POST',
      body: JSON.stringify({ diasCobertura, diasSeguranca }),
    }),
  aprovar: (id: number) => chamar<Requisicao>(`/requisicoes/${id}/aprovar`, { method: 'POST' }),
  reprovar: (id: number, motivo: string) =>
    chamar<Requisicao>(`/requisicoes/${id}/reprovar`, {
      method: 'POST',
      body: JSON.stringify({ motivo }),
    }),
  pedidos: () => chamar<PedidoResumo[]>('/pedidos'),
  urlPdf: (pedidoId: number) => `/api/pedidos/${pedidoId}/pdf`,
};
