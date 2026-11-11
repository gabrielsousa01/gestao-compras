package br.com.gestaocompras.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Quanto uma unidade vai receber de um item, já em caixas fechadas do contrato. */
@Entity
public class RequisicaoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Requisicao requisicao;

    @ManyToOne(optional = false)
    private Unidade unidade;

    @ManyToOne(optional = false)
    private Contrato contrato;

    /** Necessidade calculada da unidade, antes de arredondar para caixas. */
    @Column(precision = 12, scale = 3)
    private BigDecimal necessidade;

    private int caixas;

    @Column(precision = 12, scale = 3)
    private BigDecimal quantidade;

    @Column(precision = 14, scale = 2)
    private BigDecimal valorTotal;

    protected RequisicaoItem() {
    }

    public RequisicaoItem(Unidade unidade, Contrato contrato, BigDecimal necessidade, int caixas) {
        this.unidade = unidade;
        this.contrato = contrato;
        this.necessidade = necessidade;
        this.caixas = caixas;
        this.quantidade = contrato.getQuantidadePorCaixa().multiply(BigDecimal.valueOf(caixas));
        this.valorTotal = quantidade.multiply(contrato.getPrecoUnitario()).setScale(2, RoundingMode.HALF_UP);
    }

    void vincular(Requisicao requisicao) {
        this.requisicao = requisicao;
    }

    public Long getId() {
        return id;
    }

    public Unidade getUnidade() {
        return unidade;
    }

    public Contrato getContrato() {
        return contrato;
    }

    public BigDecimal getNecessidade() {
        return necessidade;
    }

    public int getCaixas() {
        return caixas;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }
}
