package br.com.gestaocompras.dominio;

/** Violação de regra de negócio: vira HTTP 409 na API. */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
