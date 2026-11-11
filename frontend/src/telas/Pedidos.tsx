import { api } from '../api';
import { dataHora, moeda } from '../formato';
import { useCarregar } from '../useCarregar';

export default function Pedidos() {
  const { dados, erro, carregando } = useCarregar(api.pedidos);

  return (
    <section>
      <h2>Pedidos de compra</h2>
      <p className="ajuda">Um pedido por fornecedor, gerado quando a requisição é aprovada.</p>
      <div className="cartao">
        {carregando && <p className="aviso">Carregando…</p>}
        {erro && <p className="erro">{erro}</p>}
        {dados?.length === 0 && <p className="aviso">Nenhum pedido ainda. Aprove uma requisição para gerar.</p>}
        {!!dados?.length && (
          <div className="rolagem">
            <table>
              <thead>
                <tr>
                  <th>Pedido</th>
                  <th>Fornecedor</th>
                  <th>Requisição</th>
                  <th>Emitido em</th>
                  <th className="num">Entregas</th>
                  <th className="num">Valor</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {dados.map((p) => (
                  <tr key={p.id}>
                    <td>Nº {p.id}</td>
                    <td>
                      {p.fornecedor.nome}
                      <small className="bloco">{p.fornecedor.email}</small>
                    </td>
                    <td>Nº {p.requisicaoId}</td>
                    <td>{dataHora(p.criadoEm)}</td>
                    <td className="num">{p.quantidadeItens}</td>
                    <td className="num">{moeda(p.valorTotal)}</td>
                    <td>
                      <a className="botao-link" href={api.urlPdf(p.id)} target="_blank" rel="noreferrer">PDF</a>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
}
