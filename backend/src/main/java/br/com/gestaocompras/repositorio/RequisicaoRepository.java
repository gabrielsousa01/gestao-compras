package br.com.gestaocompras.repositorio;

import br.com.gestaocompras.dominio.Requisicao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequisicaoRepository extends JpaRepository<Requisicao, Long> {

    List<Requisicao> findAllByOrderByIdDesc();
}
