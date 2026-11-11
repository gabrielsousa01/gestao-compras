package br.com.gestaocompras.dominio;

/** Recurso inexistente: vira HTTP 404 na API. */
public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
