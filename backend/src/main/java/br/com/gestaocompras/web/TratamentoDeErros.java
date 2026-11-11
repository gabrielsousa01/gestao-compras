package br.com.gestaocompras.web;

import br.com.gestaocompras.dominio.NaoEncontradoException;
import br.com.gestaocompras.dominio.RegraDeNegocioException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Converte exceções em respostas no padrão RFC 9457 (Problem Details), sem vazar detalhes internos. */
@RestControllerAdvice
public class TratamentoDeErros {

    private static final Logger LOG = LoggerFactory.getLogger(TratamentoDeErros.class);

    @ExceptionHandler(NaoEncontradoException.class)
    public ProblemDetail naoEncontrado(NaoEncontradoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ProblemDetail regraDeNegocio(RegraDeNegocioException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalido(MethodArgumentNotValidException e) {
        String detalhe = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getDefaultMessage())
                .collect(Collectors.joining(" "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detalhe);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail corpoIlegivel(HttpMessageNotReadableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "O corpo da requisição está mal formado.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception e) {
        LOG.error("Erro inesperado", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente.");
    }
}
