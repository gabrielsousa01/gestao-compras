package br.com.gestaocompras.repositorio;

import br.com.gestaocompras.dominio.PedidoCompra;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoCompraRepository extends JpaRepository<PedidoCompra, Long> {

    List<PedidoCompra> findAllByOrderByIdDesc();

    List<PedidoCompra> findByRequisicaoIdOrderByIdAsc(Long requisicaoId);
}
