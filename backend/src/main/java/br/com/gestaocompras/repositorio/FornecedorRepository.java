package br.com.gestaocompras.repositorio;

import br.com.gestaocompras.dominio.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
}
