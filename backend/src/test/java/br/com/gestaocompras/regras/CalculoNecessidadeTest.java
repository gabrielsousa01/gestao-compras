package br.com.gestaocompras.regras;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CalculoNecessidadeTest {

    @Test
    void necessidadeEhConsumoDoPeriodoMenosSaldo() {
        // 4 kg/dia × (15 + 3) dias = 72 kg; já tem 20 kg → faltam 52 kg
        assertThat(CalculoNecessidade.calcular(bd("4"), bd("20"), 15, 3)).isEqualByComparingTo("52");
    }

    @Test
    void saldoQueCobreOPeriodoNaoGeraCompra() {
        assertThat(CalculoNecessidade.calcular(bd("0.1"), bd("20"), 15, 3)).isEqualByComparingTo("0");
    }

    @Test
    void arredondaParaCimaParaNaoFaltar() {
        // 0,3333 × 10 = 3,333 → 3,334 (3 casas, para cima)
        assertThat(CalculoNecessidade.calcular(bd("0.3333"), bd("0"), 10, 0)).isEqualByComparingTo("3.333");
        assertThat(CalculoNecessidade.calcular(bd("0.33334"), bd("0"), 10, 0)).isEqualByComparingTo("3.334");
    }

    @Test
    void saldoNegativoEhTratadoComoZero() {
        assertThat(CalculoNecessidade.calcular(bd("2"), bd("-5"), 5, 0)).isEqualByComparingTo("10");
    }

    @Test
    void rejeitaParametrosInvalidos() {
        assertThatThrownBy(() -> CalculoNecessidade.calcular(bd("1"), bd("0"), 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CalculoNecessidade.calcular(bd("1"), bd("0"), 5, -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CalculoNecessidade.calcular(bd("-1"), bd("0"), 5, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }
}
