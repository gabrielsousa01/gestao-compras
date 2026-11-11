package br.com.gestaocompras.repositorio;

import br.com.gestaocompras.dominio.Unidade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnidadeRepository extends JpaRepository<Unidade, Long> {
}
