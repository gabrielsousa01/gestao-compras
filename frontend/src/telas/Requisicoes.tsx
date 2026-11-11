import { useState, type FormEvent } from 'react';
import { api, type Requisicao, type StatusRequisicao } from '../api';
import { dataHora, moeda, numero } from '../formato';
import { useCarregar } from '../useCarregar';

const ROTULO_STATUS: Record<StatusRequisicao, string> = {
  PENDENTE: 'Pendente',
  APROVADA: 'Aprovada',
  REPROVADA: 'Reprovada',
};

export function Status({ status }: { status: StatusRequisicao }) {
  return <span className={`status status-${status.toLowerCase()}`}>{ROTULO_STATUS[status]}</span>;
}

export default function Requisicoes({ aoMudar }: { aoMudar: () => void }) {
  const lista = useCarregar(api.requisicoes);
  const [selecionada, setSelecionada] = useState<Requisicao | null>(null);
  const [diasCobertura, setDiasCobertura] = useState(15);
  const [diasSeguranca, setDiasSeguranca] = useState(3);
  const [erro, setErro] = useState<string | null>(null);
  const [ocupado, setOcupado] = useState(false);

  async function executar(acao: () => Promise<Requisicao>) {
    setOcupado(true);
    setErro(null);
    try {
      const resultado = await acao();
      setSelecionada(resultado);
      lista.recarregar();
      aoMudar();
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setOcupado(false);
    }
  }

  function gerar(evento: FormEvent) {
    evento.preventDefault();
    executar(() => api.gerarRequisicao(diasCobertura, diasSeguranca));
  }

  async function abrir(id: number) {
    setErro(null);
    try {
      setSelecionada(await api.requisicao(id));
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  return (
    <section>
      <h2>Requisições de compra</h2>

      <form className="cartao formulario" onSubmit={gerar}>
        <div>
          <h3>Gerar requisição automática</h3>
          <p className="ajuda">
            Calcula o que cada unidade precisa para o período, junta por item, arredonda para caixas fechadas do
            contrato e divide as caixas entre as unidades.
          </p>
        </div>
        <label>
          Dias de cobertura
          <input type="number" min={1} max={90} value={diasCobertura}
                 onChange={(e) => setDiasCobertura(Number(e.target.value))} />
        </label>
        <label>
          Dias de segurança
          <input type="number" min={0} max={30} value={diasSeguranca}
                 onChange={(e) => setDiasSeguranca(Number(e.target.value))} />
        </label>
        <button type="submit" className="primario" disabled={ocupado}>
          {ocupado ? 'Calculando…' : 'Gerar requisição'}
        </button>
      </form>

      {erro && <p className="erro" role="alert">{erro}</p>}

      <div className="duas-colunas">
        <div className="cartao">
          <h3>Histórico</h3>
          {lista.carregando && <p className="aviso">Carregando…</p>}
          {lista.erro && <p className="erro">{lista.erro}</p>}
          {lista.dados?.length === 0 && <p className="aviso">Nenhuma requisição ainda. Gere a primeira acima.</p>}
          <ul className="lista">
            {lista.dados?.map((r) => (
              <li key={r.id}>
                <button className={selecionada?.id === r.id ? 'linha selecionada' : 'linha'} onClick={() => abrir(r.id)}>
                  <span>
                    <strong>Nº {r.id}</strong> · {dataHora(r.criadaEm)}
                    <small>{r.quantidadeItens} entregas · {moeda(r.valorTotal)}</small>
                  </span>
                  <Status status={r.status} />
                </button>
              </li>
            ))}
          </ul>
        </div>

        {selecionada && (
          <Detalhe
            requisicao={selecionada}
            ocupado={ocupado}
            aoAprovar={() => executar(() => api.aprovar(selecionada.id))}
            aoReprovar={(motivo) => executar(() => api.reprovar(selecionada.id, motivo))}
          />
        )}
      </div>
    </section>
  );
}

function Detalhe({ requisicao, ocupado, aoAprovar, aoReprovar }: {
  requisicao: Requisicao;
  ocupado: boolean;
  aoAprovar: () => void;
  aoReprovar: (motivo: string) => void;
}) {
  const [motivo, setMotivo] = useState('');
  const porFornecedor = Map.groupBy(requisicao.itens, (i) => i.fornecedor.nome);

  return (
    <div className="cartao detalhe">
      <div className="cabecalho-detalhe">
        <h3>Requisição nº {requisicao.id}</h3>
        <Status status={requisicao.status} />
      </div>
      <p className="ajuda">
        Período: {requisicao.diasCobertura} dias + {requisicao.diasSeguranca} de segurança · Total{' '}
        <strong>{moeda(requisicao.valorTotal)}</strong>
      </p>

      {requisicao.itensSemContrato.length > 0 && (
        <div className="alerta">
          <strong>Sem contrato ativo (não entram no pedido):</strong>
          <ul>{requisicao.itensSemContrato.map((t) => <li key={t}>{t}</li>)}</ul>
        </div>
      )}

      {[...porFornecedor].map(([fornecedor, itens]) => (
        <div key={fornecedor} className="bloco-fornecedor">
          <h4>{fornecedor}</h4>
          <div className="rolagem">
            <table>
              <thead>
                <tr>
                  <th>Unidade</th>
                  <th>Item</th>
                  <th className="num">Precisa</th>
                  <th className="num">Caixas</th>
                  <th className="num">Recebe</th>
                  <th className="num">Total</th>
                </tr>
              </thead>
              <tbody>
                {itens.map((i) => (
                  <tr key={i.id}>
                    <td>{i.unidade.nome}</td>
                    <td>{i.item.nome}</td>
                    <td className="num">{numero(i.necessidade)} {i.item.unidadeMedida}</td>
                    <td className="num" title={`caixa de ${numero(i.quantidadePorCaixa)} ${i.item.unidadeMedida}`}>
                      {i.caixas}
                    </td>
                    <td className="num">{numero(i.quantidade)} {i.item.unidadeMedida}</td>
                    <td className="num">{moeda(i.valorTotal)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ))}

      {requisicao.status === 'PENDENTE' && (
        <div className="acoes">
          <button className="primario" disabled={ocupado || requisicao.itens.length === 0} onClick={aoAprovar}>
            Aprovar e gerar pedidos
          </button>
          <input placeholder="Motivo da reprovação" value={motivo} maxLength={255}
                 onChange={(e) => setMotivo(e.target.value)} aria-label="Motivo da reprovação" />
          <button className="perigo" disabled={ocupado || !motivo.trim()} onClick={() => aoReprovar(motivo)}>
            Reprovar
          </button>
        </div>
      )}

      {requisicao.status === 'REPROVADA' && (
        <p className="ajuda">Motivo: {requisicao.motivoReprovacao}</p>
      )}

      {requisicao.pedidos.length > 0 && (
        <div className="bloco-fornecedor">
          <h4>Pedidos gerados</h4>
          <ul className="lista-simples">
            {requisicao.pedidos.map((p) => (
              <li key={p.id}>
                Pedido nº {p.id} · {p.fornecedor.nome} · {moeda(p.valorTotal)} ·{' '}
                <a href={api.urlPdf(p.id)} target="_blank" rel="noreferrer">abrir PDF</a>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
