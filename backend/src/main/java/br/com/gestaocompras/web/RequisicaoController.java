package br.com.gestaocompras.web;

import br.com.gestaocompras.api.Dtos.GerarRequisicaoRequest;
import br.com.gestaocompras.api.Dtos.ReprovarRequest;
import br.com.gestaocompras.api.Dtos.RequisicaoDto;
import br.com.gestaocompras.api.Dtos.RequisicaoResumoDto;
import br.com.gestaocompras.servico.RequisicaoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/requisicoes")
public class RequisicaoController {

    private final RequisicaoService requisicoes;

    public RequisicaoController(RequisicaoService requisicoes) {
        this.requisicoes = requisicoes;
    }

    @GetMapping
    public List<RequisicaoResumoDto> listar() {
        return requisicoes.listar();
    }

    @GetMapping("/{id}")
    public RequisicaoDto detalhar(@PathVariable Long id) {
        return requisicoes.detalhar(id);
    }

    /** Roda o cálculo automático e cria uma requisição pendente de aprovação. */
    @PostMapping
    public ResponseEntity<RequisicaoDto> gerar(@Valid @RequestBody GerarRequisicaoRequest pedido) {
        RequisicaoDto criada = requisicoes.gerar(pedido.diasCobertura(), pedido.diasSeguranca());
        return ResponseEntity.created(URI.create("/api/requisicoes/" + criada.id())).body(criada);
    }

    @PostMapping("/{id}/aprovar")
    public RequisicaoDto aprovar(@PathVariable Long id) {
        return requisicoes.aprovar(id);
    }

    @PostMapping("/{id}/reprovar")
    public RequisicaoDto reprovar(@PathVariable Long id, @Valid @RequestBody ReprovarRequest pedido) {
        return requisicoes.reprovar(id, pedido.motivo());
    }
}
