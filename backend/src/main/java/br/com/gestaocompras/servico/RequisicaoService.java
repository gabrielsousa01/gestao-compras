package br.com.gestaocompras.servico;

import br.com.gestaocompras.api.Dtos.PedidoResumoDto;
import br.com.gestaocompras.api.Dtos.RequisicaoDto;
import br.com.gestaocompras.api.Dtos.RequisicaoResumoDto;
import br.com.gestaocompras.dominio.Contrato;
import br.com.gestaocompras.dominio.Estoque;
import br.com.gestaocompras.dominio.Fornecedor;
import br.com.gestaocompras.dominio.Item;
import br.com.gestaocompras.dominio.NaoEncontradoException;
import br.com.gestaocompras.dominio.PedidoCompra;
import br.com.gestaocompras.dominio.PedidoItem;
import br.com.gestaocompras.dominio.RegraDeNegocioException;
import br.com.gestaocompras.dominio.Requisicao;
import br.com.gestaocompras.dominio.RequisicaoItem;
import br.com.gestaocompras.regras.CalculoNecessidade;
import br.com.gestaocompras.regras.RateioCaixas;
import br.com.gestaocompras.repositorio.ContratoRepository;
import br.com.gestaocompras.repositorio.EstoqueRepository;
import br.com.gestaocompras.repositorio.PedidoCompraRepository;
import br.com.gestaocompras.repositorio.RequisicaoRepository;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gera, aprova e reprova requisições; na aprovação, monta um pedido por fornecedor. */
@Service
public class RequisicaoService {

    private final EstoqueRepository estoques;
    private final ContratoRepository contratos;
    private final RequisicaoRepository requisicoes;
    private final PedidoCompraRepository pedidos;
    private final Clock relogio;

    public RequisicaoService(EstoqueRepository estoques, ContratoRepository contratos,
                             RequisicaoRepository requisicoes, PedidoCompraRepository pedidos, Clock relogio) {
        this.estoques = estoques;
        this.contratos = contratos;
        this.requisicoes = requisicoes;
        this.pedidos = pedidos;
        this.relogio = relogio;
    }

    /**
     * Calcula a necessidade de cada unidade, junta tudo por item, arredonda para caixas
     * fechadas do contrato e rateia as caixas entre as unidades.
     */
    @Transactional
    public RequisicaoDto gerar(int diasCobertura, int diasSeguranca) {
        Map<Long, Contrato> contratoPorItem = contratoMaisBaratoPorItem();
        Map<Item, List<Estoque>> estoquesPorItem = estoques.findAllByOrderByUnidadeNomeAscItemNomeAsc().stream()
                .sorted(Comparator.comparing((Estoque e) -> e.getItem().getNome()))
                .collect(Collectors.groupingBy(Estoque::getItem, LinkedHashMap::new, Collectors.toList()));

        Requisicao requisicao = new Requisicao(diasCobertura, diasSeguranca, LocalDateTime.now(relogio));

        estoquesPorItem.forEach((item, estoquesDoItem) -> {
            List<BigDecimal> necessidades = estoquesDoItem.stream()
                    .map(e -> CalculoNecessidade.calcular(e.getConsumoMedioDiario(), e.getSaldo(), diasCobertura, diasSeguranca))
                    .toList();
            BigDecimal total = necessidades.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            if (total.signum() == 0) {
                return;
            }

            Contrato contrato = contratoPorItem.get(item.getId());
            if (contrato == null) {
                requisicao.registrarSemContrato(item.getNome() + ": faltam " + formatar(total) + " " + item.getUnidadeMedida());
                return;
            }

            int caixas = RateioCaixas.totalDeCaixas(total, contrato.getQuantidadePorCaixa());
            List<Integer> rateio = RateioCaixas.distribuir(caixas, necessidades);
            for (int i = 0; i < estoquesDoItem.size(); i++) {
                if (rateio.get(i) > 0) {
                    requisicao.adicionarItem(new RequisicaoItem(estoquesDoItem.get(i).getUnidade(), contrato,
                            necessidades.get(i), rateio.get(i)));
                }
            }
        });

        if (requisicao.getItens().isEmpty() && requisicao.getItensSemContrato().isEmpty()) {
            throw new RegraDeNegocioException("Nenhuma unidade precisa de compra para o período informado.");
        }
        return RequisicaoDto.de(requisicoes.save(requisicao), List.of());
    }

    @Transactional
    public RequisicaoDto aprovar(Long id) {
        Requisicao requisicao = buscar(id);
        if (requisicao.getItens().isEmpty()) {
            throw new RegraDeNegocioException("A requisição não tem itens com contrato; não há pedido a gerar.");
        }
        LocalDateTime agora = LocalDateTime.now(relogio);
        requisicao.aprovar(agora);

        Map<Fornecedor, List<RequisicaoItem>> porFornecedor = requisicao.getItens().stream()
                .collect(Collectors.groupingBy(i -> i.getContrato().getFornecedor(), LinkedHashMap::new, Collectors.toList()));
        List<PedidoCompra> gerados = new ArrayList<>();
        porFornecedor.forEach((fornecedor, itens) -> {
            PedidoCompra pedido = new PedidoCompra(requisicao, fornecedor, agora);
            itens.forEach(i -> pedido.adicionarItem(new PedidoItem(i)));
            gerados.add(pedidos.save(pedido));
        });
        return RequisicaoDto.de(requisicao, gerados);
    }

    @Transactional
    public RequisicaoDto reprovar(Long id, String motivo) {
        Requisicao requisicao = buscar(id);
        requisicao.reprovar(motivo.trim(), LocalDateTime.now(relogio));
        return RequisicaoDto.de(requisicao, List.of());
    }

    @Transactional(readOnly = true)
    public List<RequisicaoResumoDto> listar() {
        return requisicoes.findAllByOrderByIdDesc().stream().map(RequisicaoResumoDto::de).toList();
    }

    @Transactional(readOnly = true)
    public RequisicaoDto detalhar(Long id) {
        return RequisicaoDto.de(buscar(id), pedidos.findByRequisicaoIdOrderByIdAsc(id));
    }

    @Transactional(readOnly = true)
    public List<PedidoResumoDto> pedidosDaRequisicao(Long id) {
        return pedidos.findByRequisicaoIdOrderByIdAsc(id).stream().map(PedidoResumoDto::de).toList();
    }

    private Requisicao buscar(Long id) {
        return requisicoes.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Requisição " + id + " não encontrada."));
    }

    /** Se houver mais de um contrato ativo para o mesmo item, usa o de menor preço unitário. */
    private Map<Long, Contrato> contratoMaisBaratoPorItem() {
        return contratos.findByAtivoTrue().stream().collect(Collectors.toMap(
                c -> c.getItem().getId(),
                Function.identity(),
                (a, b) -> a.getPrecoUnitario().compareTo(b.getPrecoUnitario()) <= 0 ? a : b));
    }

    private static String formatar(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));
        formato.setMaximumFractionDigits(3);
        return formato.format(valor);
    }
}
