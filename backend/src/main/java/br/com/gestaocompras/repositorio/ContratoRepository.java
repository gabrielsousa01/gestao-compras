package br.com.gestaocompras.repositorio;

import br.com.gestaocompras.dominio.Contrato;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    List<Contrato> findByAtivoTrue();
}
