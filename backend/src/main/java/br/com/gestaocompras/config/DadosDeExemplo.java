package br.com.gestaocompras.config;

import br.com.gestaocompras.dominio.Contrato;
import br.com.gestaocompras.dominio.Estoque;
import br.com.gestaocompras.dominio.Fornecedor;
import br.com.gestaocompras.dominio.Item;
import br.com.gestaocompras.dominio.Unidade;
import br.com.gestaocompras.repositorio.ContratoRepository;
import br.com.gestaocompras.repositorio.EstoqueRepository;
import br.com.gestaocompras.repositorio.FornecedorRepository;
import br.com.gestaocompras.repositorio.ItemRepository;
import br.com.gestaocompras.repositorio.UnidadeRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carrega dados fictícios na primeira subida (banco vazio) para o sistema já abrir com
 * unidades, contratos e estoques. Nomes e valores são inventados.
 */
@Component
@ConditionalOnProperty(name = "app.dados-de-exemplo", havingValue = "true", matchIfMissing = true)
public class DadosDeExemplo implements ApplicationRunner {

    private final UnidadeRepository unidades;
    private final ItemRepository itens;
    private final FornecedorRepository fornecedores;
    private final ContratoRepository contratos;
    private final EstoqueRepository estoques;

    public DadosDeExemplo(UnidadeRepository unidades, ItemRepository itens, FornecedorRepository fornecedores,
                          ContratoRepository contratos, EstoqueRepository estoques) {
        this.unidades = unidades;
        this.itens = itens;
        this.fornecedores = fornecedores;
        this.contratos = contratos;
        this.estoques = estoques;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (unidades.count() > 0) {
            return;
        }

        Unidade jardim = unidades.save(new Unidade("CEI Jardim das Flores"));
        Unidade vila = unidades.save(new Unidade("CCA Vila Esperança"));
        Unidade monte = unidades.save(new Unidade("EMEI Monte Azul"));

        Item arroz = itens.save(new Item("Arroz", "kg"));
        Item feijao = itens.save(new Item("Feijão", "kg"));
        Item leite = itens.save(new Item("Leite", "L"));
        Item oleo = itens.save(new Item("Óleo de soja", "L"));
        Item macarrao = itens.save(new Item("Macarrão", "kg"));
        Item banana = itens.save(new Item("Banana", "kg"));
        Item sal = itens.save(new Item("Sal", "kg"));

        Fornecedor graoBom = fornecedores.save(new Fornecedor("Distribuidora Grão Bom", "pedidos@graobom.example"));
        Fornecedor serraVerde = fornecedores.save(new Fornecedor("Laticínios Serra Verde", "vendas@serraverde.example"));
        Fornecedor central = fornecedores.save(new Fornecedor("Atacado Central", "compras@atacadocentral.example"));

        contratos.saveAll(List.of(
                new Contrato(graoBom, arroz, bd("5.49"), bd("30")),
                new Contrato(graoBom, feijao, bd("7.90"), bd("10")),
                new Contrato(serraVerde, leite, bd("4.79"), bd("12")),
                new Contrato(central, oleo, bd("8.50"), bd("18")),
                new Contrato(central, macarrao, bd("4.20"), bd("20")),
                new Contrato(central, sal, bd("2.10"), bd("10"))));

        // saldo atual, consumo médio por dia (banana fica sem contrato de propósito)
        estoques.saveAll(List.of(
                new Estoque(jardim, arroz, bd("20"), bd("4")),
                new Estoque(jardim, feijao, bd("8"), bd("1.5")),
                new Estoque(jardim, leite, bd("30"), bd("6")),
                new Estoque(jardim, oleo, bd("4"), bd("0.4")),
                new Estoque(jardim, macarrao, bd("15"), bd("1.2")),
                new Estoque(jardim, banana, bd("5"), bd("3")),
                new Estoque(jardim, sal, bd("20"), bd("0.1")),

                new Estoque(vila, arroz, bd("45"), bd("3")),
                new Estoque(vila, feijao, bd("10"), bd("1")),
                new Estoque(vila, leite, bd("12"), bd("4")),
                new Estoque(vila, oleo, bd("6"), bd("0.3")),
                new Estoque(vila, macarrao, bd("4"), bd("0.8")),
                new Estoque(vila, banana, bd("2"), bd("2")),
                new Estoque(vila, sal, bd("15"), bd("0.08")),

                new Estoque(monte, arroz, bd("10"), bd("5")),
                new Estoque(monte, feijao, bd("3"), bd("2")),
                new Estoque(monte, leite, bd("50"), bd("8")),
                new Estoque(monte, oleo, bd("2"), bd("0.5")),
                new Estoque(monte, macarrao, bd("10"), bd("1.5")),
                new Estoque(monte, banana, bd("0"), bd("4")),
                new Estoque(monte, sal, bd("12"), bd("0.1"))));
    }

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }
}
