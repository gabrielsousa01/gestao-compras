package br.com.gestaocompras.repositorio;

import br.com.gestaocompras.dominio.Estoque;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    List<Estoque> findAllByOrderByUnidadeNomeAscItemNomeAsc();
}
