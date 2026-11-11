package br.com.gestaocompras.dominio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Pedido enviado a um fornecedor, gerado na aprovação da requisição. */
@Entity
public class PedidoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Requisicao requisicao;

    @ManyToOne(optional = false)
    private Fornecedor fornecedor;

    private LocalDateTime criadoEm;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<PedidoItem> itens = new ArrayList<>();

    protected PedidoCompra() {
    }

    public PedidoCompra(Requisicao requisicao, Fornecedor fornecedor, LocalDateTime criadoEm) {
        this.requisicao = requisicao;
        this.fornecedor = fornecedor;
        this.criadoEm = criadoEm;
    }

    public void adicionarItem(PedidoItem item) {
        item.vincular(this);
        itens.add(item);
    }

    public BigDecimal getValorTotal() {
        return itens.stream().map(PedidoItem::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() {
        return id;
    }

    public Requisicao getRequisicao() {
        return requisicao;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public List<PedidoItem> getItens() {
        return itens;
    }
}
