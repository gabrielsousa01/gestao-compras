package br.com.gestaocompras.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;

/**
 * Contrato de fornecimento de um item: o fornecedor só entrega caixas fechadas
 * de {@code quantidadePorCaixa} (na unidade de medida do item) ao preço unitário combinado.
 */
@Entity
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Fornecedor fornecedor;

    @ManyToOne(optional = false)
    private Item item;

    @Column(precision = 12, scale = 2)
    private BigDecimal precoUnitario;

    @Column(precision = 12, scale = 3)
    private BigDecimal quantidadePorCaixa;

    private boolean ativo;

    protected Contrato() {
    }

    public Contrato(Fornecedor fornecedor, Item item, BigDecimal precoUnitario, BigDecimal quantidadePorCaixa) {
        this.fornecedor = fornecedor;
        this.item = item;
        this.precoUnitario = precoUnitario;
        this.quantidadePorCaixa = quantidadePorCaixa;
        this.ativo = true;
    }

    public Long getId() {
        return id;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public Item getItem() {
        return item;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public BigDecimal getQuantidadePorCaixa() {
        return quantidadePorCaixa;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
