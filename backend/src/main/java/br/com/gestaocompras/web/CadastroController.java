package br.com.gestaocompras.web;

import br.com.gestaocompras.api.Dtos.ContratoDto;
import br.com.gestaocompras.api.Dtos.EstoqueDto;
import br.com.gestaocompras.api.Dtos.FornecedorDto;
import br.com.gestaocompras.api.Dtos.UnidadeDto;
import br.com.gestaocompras.servico.CadastroService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CadastroController {

    private final CadastroService cadastros;

    public CadastroController(CadastroService cadastros) {
        this.cadastros = cadastros;
    }

    @GetMapping("/unidades")
    public List<UnidadeDto> unidades() {
        return cadastros.unidades();
    }

    @GetMapping("/fornecedores")
    public List<FornecedorDto> fornecedores() {
        return cadastros.fornecedores();
    }

    @GetMapping("/contratos")
    public List<ContratoDto> contratos() {
        return cadastros.contratos();
    }

    @GetMapping("/estoques")
    public List<EstoqueDto> estoques() {
        return cadastros.estoques();
    }
}
