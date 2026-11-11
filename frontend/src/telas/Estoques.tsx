import { api } from '../api';
import { numero } from '../formato';
import { useCarregar } from '../useCarregar';

/** Saldo e cobertura de cada item por unidade, com alerta para o que está acabando. */
export default function Estoques() {
  const { dados, erro, carregando } = useCarregar(api.estoques);

  if (carregando) return <p className="aviso">Carregando estoque…</p>;
  if (erro) return <p className="erro">{erro}</p>;
  if (!dados?.length) return <p className="aviso">Nenhum estoque cadastrado.</p>;

  const unidades = [...new Set(dados.map((e) => e.unidade.nome))].sort((a, b) => a.localeCompare(b, 'pt-BR'));

  return (
    <section>
      <h2>Estoque por unidade</h2>
      <p className="ajuda">
        Dias de cobertura = saldo ÷ consumo médio diário. Abaixo de 7 dias aparece em vermelho.
      </p>
      <div className="grade-cartoes">
        {unidades.map((unidade) => (
          <article key={unidade} className="cartao">
            <h3>{unidade}</h3>
            <table>
              <thead>
                <tr>
                  <th>Item</th>
                  <th className="num">Saldo</th>
                  <th className="num">Consumo/dia</th>
                  <th className="num">Cobertura</th>
                </tr>
              </thead>
              <tbody>
                {dados
                  .filter((e) => e.unidade.nome === unidade)
                  .sort((a, b) => a.item.nome.localeCompare(b.item.nome, 'pt-BR'))
                  .map((e) => {
                    const critico = e.diasDeCobertura !== null && e.diasDeCobertura < 7;
                    return (
                      <tr key={e.id}>
                        <td>{e.item.nome}</td>
                        <td className="num">
                          {numero(e.saldo)} {e.item.unidadeMedida}
                        </td>
                        <td className="num">{numero(e.consumoMedioDiario)}</td>
                        <td className={critico ? 'num critico' : 'num'}>
                          {e.diasDeCobertura === null
                            ? '—'
                            : `${numero(e.diasDeCobertura)} ${e.diasDeCobertura < 2 ? 'dia' : 'dias'}`}
                        </td>
                      </tr>
                    );
                  })}
              </tbody>
            </table>
          </article>
        ))}
      </div>
    </section>
  );
}
