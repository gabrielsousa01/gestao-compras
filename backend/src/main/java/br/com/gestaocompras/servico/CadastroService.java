package br.com.gestaocompras.servico;

import br.com.gestaocompras.api.Dtos.ContratoDto;
import br.com.gestaocompras.api.Dtos.EstoqueDto;
import br.com.gestaocompras.api.Dtos.FornecedorDto;
import br.com.gestaocompras.api.Dtos.UnidadeDto;
import br.com.gestaocompras.repositorio.ContratoRepository;
import br.com.gestaocompras.repositorio.EstoqueRepository;
import br.com.gestaocompras.repositorio.FornecedorRepository;
import br.com.gestaocompras.repositorio.UnidadeRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consultas dos cadastros básicos usados nas telas. */
@Service
@Transactional(readOnly = true)
public class CadastroService {

    private final UnidadeRepository unidades;
    private final FornecedorRepository fornecedores;
    private final ContratoRepository contratos;
    private final EstoqueRepository estoques;

    public CadastroService(UnidadeRepository unidades, FornecedorRepository fornecedores,
                           ContratoRepository contratos, EstoqueRepository estoques) {
        this.unidades = unidades;
        this.fornecedores = fornecedores;
        this.contratos = contratos;
        this.estoques = estoques;
    }

    public List<UnidadeDto> unidades() {
        return unidades.findAll(Sort.by("nome")).stream().map(UnidadeDto::de).toList();
    }

    public List<FornecedorDto> fornecedores() {
        return fornecedores.findAll(Sort.by("nome")).stream().map(FornecedorDto::de).toList();
    }

    public List<ContratoDto> contratos() {
        return contratos.findAll(Sort.by("item.nome")).stream().map(ContratoDto::de).toList();
    }

    public List<EstoqueDto> estoques() {
        return estoques.findAllByOrderByUnidadeNomeAscItemNomeAsc().stream().map(EstoqueDto::de).toList();
    }
}
