package br.com.gestaocompras.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;

/**
 * Linha do pedido. Preço e tamanho da caixa são copiados do contrato na aprovação,
 * para que uma mudança futura no contrato não altere um pedido já enviado.
 */
@Entity
public class PedidoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private PedidoCompra pedido;

    @ManyToOne(optional = false)
    private Unidade unidadeEntrega;

    @ManyToOne(optional = false)
    private Item item;

    private int caixas;

    @Column(precision = 12, scale = 3)
    private BigDecimal quantidade;

    @Column(precision = 12, scale = 2)
    private BigDecimal precoUnitario;

    @Column(precision = 14, scale = 2)
    private BigDecimal valorTotal;

    protected PedidoItem() {
    }

    public PedidoItem(RequisicaoItem origem) {
        this.unidadeEntrega = origem.getUnidade();
        this.item = origem.getContrato().getItem();
        this.caixas = origem.getCaixas();
        this.quantidade = origem.getQuantidade();
        this.precoUnitario = origem.getContrato().getPrecoUnitario();
        this.valorTotal = origem.getValorTotal();
    }

    void vincular(PedidoCompra pedido) {
        this.pedido = pedido;
    }

    public Long getId() {
        return id;
    }

    public Unidade getUnidadeEntrega() {
        return unidadeEntrega;
    }

    public Item getItem() {
        return item;
    }

    public int getCaixas() {
        return caixas;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }
}
