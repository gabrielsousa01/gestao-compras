package br.com.gestaocompras.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

/** Saldo atual e consumo médio diário de um item em uma unidade. */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"unidade_id", "item_id"}))
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Unidade unidade;

    @ManyToOne(optional = false)
    private Item item;

    @Column(precision = 12, scale = 3)
    private BigDecimal saldo;

    @Column(precision = 12, scale = 3)
    private BigDecimal consumoMedioDiario;

    protected Estoque() {
    }

    public Estoque(Unidade unidade, Item item, BigDecimal saldo, BigDecimal consumoMedioDiario) {
        this.unidade = unidade;
        this.item = item;
        this.saldo = saldo;
        this.consumoMedioDiario = consumoMedioDiario;
    }

    public Long getId() {
        return id;
    }

    public Unidade getUnidade() {
        return unidade;
    }

    public Item getItem() {
        return item;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public BigDecimal getConsumoMedioDiario() {
        return consumoMedioDiario;
    }
}
