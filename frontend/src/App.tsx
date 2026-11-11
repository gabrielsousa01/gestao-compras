import { useState } from 'react';
import Estoques from './telas/Estoques';
import Pedidos from './telas/Pedidos';
import Requisicoes from './telas/Requisicoes';

type Aba = 'estoques' | 'requisicoes' | 'pedidos';

const ABAS: { id: Aba; rotulo: string }[] = [
  { id: 'estoques', rotulo: 'Estoque' },
  { id: 'requisicoes', rotulo: 'Requisições' },
  { id: 'pedidos', rotulo: 'Pedidos de compra' },
];

export default function App() {
  const [aba, setAba] = useState<Aba>('estoques');
  // Ao aprovar uma requisição, a tela de pedidos precisa recarregar.
  const [versao, setVersao] = useState(0);

  return (
    <div className="app">
      <header className="topo">
        <h1>Gestão de Compras</h1>
        <nav className="abas" aria-label="Seções">
          {ABAS.map((a) => (
            <button
              key={a.id}
              className={aba === a.id ? 'aba ativa' : 'aba'}
              aria-current={aba === a.id ? 'page' : undefined}
              onClick={() => setAba(a.id)}
            >
              {a.rotulo}
            </button>
          ))}
        </nav>
      </header>

      <main>
        {aba === 'estoques' && <Estoques />}
        {aba === 'requisicoes' && <Requisicoes aoMudar={() => setVersao((v) => v + 1)} />}
        {aba === 'pedidos' && <Pedidos key={versao} />}
      </main>
    </div>
  );
}
