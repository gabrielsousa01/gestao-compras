package br.com.gestaocompras.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

/** Fluxo completo pela API, com os dados de exemplo: gerar → aprovar → pedidos → PDF. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FluxoDeCompraTest {

    private static final String PERIODO = "{\"diasCobertura\": 15, \"diasSeguranca\": 3}";

    @Autowired
    private MockMvc mvc;

    @Test
    void gerarCalculaCaixasERateiaEntreAsUnidades() throws Exception {
        mvc.perform(post("/api/requisicoes").contentType(MediaType.APPLICATION_JSON).content(PERIODO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/requisicoes/")))
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                // Arroz: precisam 52 + 9 + 80 = 141 kg → 5 caixas de 30 kg → 2 / 0 / 3
                .andExpect(jsonPath("$.itens[?(@.item.nome == 'Arroz')]", hasSize(2)))
                .andExpect(jsonPath("$.itens[?(@.item.nome == 'Arroz' && @.unidade.nome == 'CEI Jardim das Flores')].caixas").value(2))
                .andExpect(jsonPath("$.itens[?(@.item.nome == 'Arroz' && @.unidade.nome == 'EMEI Monte Azul')].caixas").value(3))
                // Sal: o estoque já cobre o período
                .andExpect(jsonPath("$.itens[?(@.item.nome == 'Sal')]", hasSize(0)))
                // Banana: falta, mas não tem contrato
                .andExpect(jsonPath("$.itensSemContrato[0]").value("Banana: faltam 155 kg"));
    }

    @Test
    void aprovarGeraUmPedidoPorFornecedorEOPdf() throws Exception {
        long id = criarRequisicao();

        mvc.perform(post("/api/requisicoes/{id}/aprovar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.pedidos", hasSize(3)));

        String pedidos = mvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andReturn().getResponse().getContentAsString();
        int pedidoId = JsonPath.read(pedidos, "$[0].id");

        mvc.perform(get("/api/pedidos/{id}/pdf", pedidoId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().string(containsString("%PDF")));
    }

    @Test
    void requisicaoDecididaNaoPodeSerDecididaDeNovo() throws Exception {
        long id = criarRequisicao();
        mvc.perform(post("/api/requisicoes/{id}/aprovar", id)).andExpect(status().isOk());

        mvc.perform(post("/api/requisicoes/{id}/aprovar", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("já está aprovada")));
        mvc.perform(post("/api/requisicoes/{id}/reprovar", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"teste\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void reprovarExigeMotivo() throws Exception {
        long id = criarRequisicao();

        mvc.perform(post("/api/requisicoes/{id}/reprovar", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Informe o motivo da reprovação."));

        mvc.perform(post("/api/requisicoes/{id}/reprovar", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"Orçamento do mês esgotado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REPROVADA"))
                .andExpect(jsonPath("$.motivoReprovacao").value("Orçamento do mês esgotado"));
    }

    @Test
    void periodoInvalidoEhRecusado() throws Exception {
        mvc.perform(post("/api/requisicoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"diasCobertura\": 0, \"diasSeguranca\": 3}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Os dias de cobertura devem ser pelo menos 1."));
    }

    @Test
    void requisicaoInexistenteDa404() throws Exception {
        mvc.perform(get("/api/requisicoes/999")).andExpect(status().isNotFound());
    }

    private long criarRequisicao() throws Exception {
        String corpo = mvc.perform(post("/api/requisicoes").contentType(MediaType.APPLICATION_JSON).content(PERIODO))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(corpo, "$.id")).longValue();
    }
}
