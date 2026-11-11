package br.com.gestaocompras.regras;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Quanto uma unidade precisa comprar de um item para ficar coberta por um período.
 *
 * <pre>
 * necessidade = consumo médio diário × (dias de cobertura + dias de segurança) − saldo atual
 * </pre>
 *
 * Nunca é negativa: se o saldo já cobre o período, a necessidade é zero.
 * O resultado é arredondado para cima em 3 casas, para não faltar por arredondamento.
 */
public final class CalculoNecessidade {

    private CalculoNecessidade() {
    }

    public static BigDecimal calcular(BigDecimal consumoMedioDiario, BigDecimal saldo, int diasCobertura, int diasSeguranca) {
        if (diasCobertura < 1) {
            throw new IllegalArgumentException("Os dias de cobertura devem ser pelo menos 1.");
        }
        if (diasSeguranca < 0) {
            throw new IllegalArgumentException("Os dias de segurança não podem ser negativos.");
        }
        if (consumoMedioDiario.signum() < 0) {
            throw new IllegalArgumentException("O consumo médio diário não pode ser negativo.");
        }
        BigDecimal saldoConsiderado = saldo.signum() < 0 ? BigDecimal.ZERO : saldo;
        BigDecimal alvo = consumoMedioDiario.multiply(BigDecimal.valueOf((long) diasCobertura + diasSeguranca));
        BigDecimal falta = alvo.subtract(saldoConsiderado);
        if (falta.signum() <= 0) {
            return BigDecimal.ZERO.setScale(3);
        }
        return falta.setScale(3, RoundingMode.UP);
    }
}
