package br.com.gestaocompras.repositorio;

import br.com.gestaocompras.dominio.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
