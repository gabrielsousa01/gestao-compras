package br.com.gestaocompras.dominio;

public enum StatusRequisicao {
    /** Gerada pelo cálculo e aguardando decisão. */
    PENDENTE,
    /** Aprovada: os pedidos de compra por fornecedor já foram gerados. */
    APROVADA,
    REPROVADA
}
