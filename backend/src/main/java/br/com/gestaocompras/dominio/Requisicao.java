package br.com.gestaocompras.dominio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Requisição de compra consolidada de todas as unidades, gerada pelo cálculo automático. */
@Entity
public class Requisicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime criadaEm;

    private int diasCobertura;

    private int diasSeguranca;

    @Enumerated(EnumType.STRING)
    private StatusRequisicao status;

    private String motivoReprovacao;

    private LocalDateTime decididaEm;

    @OneToMany(mappedBy = "requisicao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<RequisicaoItem> itens = new ArrayList<>();

    /** Itens que precisariam ser comprados, mas não têm contrato ativo. */
    @ElementCollection
    @CollectionTable(name = "requisicao_sem_contrato")
    @Column(name = "descricao")
    private List<String> itensSemContrato = new ArrayList<>();

    protected Requisicao() {
    }

    public Requisicao(int diasCobertura, int diasSeguranca, LocalDateTime criadaEm) {
        this.diasCobertura = diasCobertura;
        this.diasSeguranca = diasSeguranca;
        this.criadaEm = criadaEm;
        this.status = StatusRequisicao.PENDENTE;
    }

    public void adicionarItem(RequisicaoItem item) {
        item.vincular(this);
        itens.add(item);
    }

    public void registrarSemContrato(String descricao) {
        itensSemContrato.add(descricao);
    }

    public void aprovar(LocalDateTime quando) {
        exigirPendente();
        this.status = StatusRequisicao.APROVADA;
        this.decididaEm = quando;
    }

    public void reprovar(String motivo, LocalDateTime quando) {
        exigirPendente();
        this.status = StatusRequisicao.REPROVADA;
        this.motivoReprovacao = motivo;
        this.decididaEm = quando;
    }

    private void exigirPendente() {
        if (status != StatusRequisicao.PENDENTE) {
            throw new RegraDeNegocioException(
                    "A requisição " + id + " já está " + status.name().toLowerCase() + " e não pode ser decidida de novo.");
        }
    }

    public BigDecimal getValorTotal() {
        return itens.stream().map(RequisicaoItem::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public int getDiasCobertura() {
        return diasCobertura;
    }

    public int getDiasSeguranca() {
        return diasSeguranca;
    }

    public StatusRequisicao getStatus() {
        return status;
    }

    public String getMotivoReprovacao() {
        return motivoReprovacao;
    }

    public LocalDateTime getDecididaEm() {
        return decididaEm;
    }

    public List<RequisicaoItem> getItens() {
        return itens;
    }

    public List<String> getItensSemContrato() {
        return itensSemContrato;
    }
}
