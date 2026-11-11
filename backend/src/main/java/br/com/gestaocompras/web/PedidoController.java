package br.com.gestaocompras.web;

import br.com.gestaocompras.api.Dtos.PedidoDto;
import br.com.gestaocompras.api.Dtos.PedidoResumoDto;
import br.com.gestaocompras.servico.PedidoService;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidos;

    public PedidoController(PedidoService pedidos) {
        this.pedidos = pedidos;
    }

    @GetMapping
    public List<PedidoResumoDto> listar() {
        return pedidos.listar();
    }

    @GetMapping("/{id}")
    public PedidoDto detalhar(@PathVariable Long id) {
        return pedidos.detalhar(id);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        byte[] conteudo = pedidos.gerarPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename("pedido-" + id + ".pdf").build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(conteudo);
    }
}
