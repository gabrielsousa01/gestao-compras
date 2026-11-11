package br.com.gestaocompras.api;

import br.com.gestaocompras.dominio.Contrato;
import br.com.gestaocompras.dominio.Estoque;
import br.com.gestaocompras.dominio.Fornecedor;
import br.com.gestaocompras.dominio.Item;
import br.com.gestaocompras.dominio.PedidoCompra;
import br.com.gestaocompras.dominio.PedidoItem;
import br.com.gestaocompras.dominio.Requisicao;
import br.com.gestaocompras.dominio.RequisicaoItem;
import br.com.gestaocompras.dominio.StatusRequisicao;
import br.com.gestaocompras.dominio.Unidade;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Objetos que entram e saem da API REST. As entidades JPA nunca são expostas diretamente. */
public final class Dtos {

    private Dtos() {
    }

    public record UnidadeDto(Long id, String nome) {
        public static UnidadeDto de(Unidade u) {
            return new UnidadeDto(u.getId(), u.getNome());
        }
    }

    public record ItemDto(Long id, String nome, String unidadeMedida) {
        public static ItemDto de(Item i) {
            return new ItemDto(i.getId(), i.getNome(), i.getUnidadeMedida());
        }
    }

    public record FornecedorDto(Long id, String nome, String email) {
        public static FornecedorDto de(Fornecedor f) {
            return new FornecedorDto(f.getId(), f.getNome(), f.getEmail());
        }
    }

    public record ContratoDto(Long id, FornecedorDto fornecedor, ItemDto item, BigDecimal precoUnitario,
                              BigDecimal quantidadePorCaixa, boolean ativo) {
        public static ContratoDto de(Contrato c) {
            return new ContratoDto(c.getId(), FornecedorDto.de(c.getFornecedor()), ItemDto.de(c.getItem()),
                    c.getPrecoUnitario(), c.getQuantidadePorCaixa(), c.isAtivo());
        }
    }

    public record EstoqueDto(Long id, UnidadeDto unidade, ItemDto item, BigDecimal saldo,
                             BigDecimal consumoMedioDiario, BigDecimal diasDeCobertura) {
        public static EstoqueDto de(Estoque e) {
            BigDecimal dias = e.getConsumoMedioDiario().signum() == 0
                    ? null
                    : e.getSaldo().divide(e.getConsumoMedioDiario(), 1, java.math.RoundingMode.DOWN);
            return new EstoqueDto(e.getId(), UnidadeDto.de(e.getUnidade()), ItemDto.de(e.getItem()),
                    e.getSaldo(), e.getConsumoMedioDiario(), dias);
        }
    }

    public record GerarRequisicaoRequest(
            @Min(value = 1, message = "Os dias de cobertura devem ser pelo menos 1.")
            @Max(value = 90, message = "Os dias de cobertura devem ser no máximo 90.")
            int diasCobertura,
            @Min(value = 0, message = "Os dias de segurança não podem ser negativos.")
            @Max(value = 30, message = "Os dias de segurança devem ser no máximo 30.")
            int diasSeguranca) {
    }

    public record ReprovarRequest(
            @NotBlank(message = "Informe o motivo da reprovação.")
            @Size(max = 255, message = "O motivo deve ter no máximo 255 caracteres.")
            String motivo) {
    }

    public record RequisicaoItemDto(Long id, UnidadeDto unidade, ItemDto item, FornecedorDto fornecedor,
                                    BigDecimal necessidade, int caixas, BigDecimal quantidadePorCaixa,
                                    BigDecimal quantidade, BigDecimal precoUnitario, BigDecimal valorTotal) {
        public static RequisicaoItemDto de(RequisicaoItem i) {
            Contrato c = i.getContrato();
            return new RequisicaoItemDto(i.getId(), UnidadeDto.de(i.getUnidade()), ItemDto.de(c.getItem()),
                    FornecedorDto.de(c.getFornecedor()), i.getNecessidade(), i.getCaixas(),
                    c.getQuantidadePorCaixa(), i.getQuantidade(), c.getPrecoUnitario(), i.getValorTotal());
        }
    }

    public record RequisicaoResumoDto(Long id, LocalDateTime criadaEm, StatusRequisicao status,
                                      int diasCobertura, int diasSeguranca, int quantidadeItens,
                                      BigDecimal valorTotal) {
        public static RequisicaoResumoDto de(Requisicao r) {
            return new RequisicaoResumoDto(r.getId(), r.getCriadaEm(), r.getStatus(), r.getDiasCobertura(),
                    r.getDiasSeguranca(), r.getItens().size(), r.getValorTotal());
        }
    }

    public record RequisicaoDto(Long id, LocalDateTime criadaEm, StatusRequisicao status, int diasCobertura,
                                int diasSeguranca, String motivoReprovacao, LocalDateTime decididaEm,
                                BigDecimal valorTotal, List<RequisicaoItemDto> itens,
                                List<String> itensSemContrato, List<PedidoResumoDto> pedidos) {
        public static RequisicaoDto de(Requisicao r, List<PedidoCompra> pedidos) {
            return new RequisicaoDto(r.getId(), r.getCriadaEm(), r.getStatus(), r.getDiasCobertura(),
                    r.getDiasSeguranca(), r.getMotivoReprovacao(), r.getDecididaEm(), r.getValorTotal(),
                    r.getItens().stream().map(RequisicaoItemDto::de).toList(),
                    List.copyOf(r.getItensSemContrato()),
                    pedidos.stream().map(PedidoResumoDto::de).toList());
        }
    }

    public record PedidoItemDto(UnidadeDto unidadeEntrega, ItemDto item, int caixas, BigDecimal quantidade,
                                BigDecimal precoUnitario, BigDecimal valorTotal) {
        public static PedidoItemDto de(PedidoItem i) {
            return new PedidoItemDto(UnidadeDto.de(i.getUnidadeEntrega()), ItemDto.de(i.getItem()), i.getCaixas(),
                    i.getQuantidade(), i.getPrecoUnitario(), i.getValorTotal());
        }
    }

    public record PedidoResumoDto(Long id, Long requisicaoId, FornecedorDto fornecedor, LocalDateTime criadoEm,
                                  int quantidadeItens, BigDecimal valorTotal) {
        public static PedidoResumoDto de(PedidoCompra p) {
            return new PedidoResumoDto(p.getId(), p.getRequisicao().getId(), FornecedorDto.de(p.getFornecedor()),
                    p.getCriadoEm(), p.getItens().size(), p.getValorTotal());
        }
    }

    public record PedidoDto(Long id, Long requisicaoId, FornecedorDto fornecedor, LocalDateTime criadoEm,
                            BigDecimal valorTotal, List<PedidoItemDto> itens) {
        public static PedidoDto de(PedidoCompra p) {
            return new PedidoDto(p.getId(), p.getRequisicao().getId(), FornecedorDto.de(p.getFornecedor()),
                    p.getCriadoEm(), p.getValorTotal(), p.getItens().stream().map(PedidoItemDto::de).toList());
        }
    }
}
