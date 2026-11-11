import { useCallback, useEffect, useState } from 'react';

/** Carrega dados de uma chamada assíncrona e expõe carregando/erro/recarregar. */
export function useCarregar<T>(buscar: () => Promise<T>) {
  const [dados, setDados] = useState<T | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(true);

  const recarregar = useCallback(() => {
    setCarregando(true);
    setErro(null);
    buscar()
      .then(setDados)
      .catch((e: Error) => setErro(e.message))
      .finally(() => setCarregando(false));
  }, [buscar]);

  useEffect(recarregar, [recarregar]);

  return { dados, erro, carregando, recarregar };
}
