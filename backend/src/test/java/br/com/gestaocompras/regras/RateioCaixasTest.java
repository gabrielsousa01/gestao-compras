package br.com.gestaocompras.regras;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class RateioCaixasTest {

    @Test
    void totalDeCaixasArredondaParaCima() {
        assertThat(RateioCaixas.totalDeCaixas(bd("141"), bd("30"))).isEqualTo(5);
        assertThat(RateioCaixas.totalDeCaixas(bd("60"), bd("10"))).isEqualTo(6);
        assertThat(RateioCaixas.totalDeCaixas(bd("0.001"), bd("12"))).isEqualTo(1);
        assertThat(RateioCaixas.totalDeCaixas(bd("0"), bd("12"))).isZero();
    }

    @Test
    void caixaDeTamanhoInvalidoEhRejeitada() {
        assertThatThrownBy(() -> RateioCaixas.totalDeCaixas(bd("10"), bd("0")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sobraVaiParaQuemTemOMaiorResto() {
        // cotas exatas: 1,844 / 0,319 / 2,837 → inteiras 1/0/2, sobram 2 caixas
        // maiores restos: 0,844 (1ª) e 0,837 (3ª)
        assertThat(RateioCaixas.distribuir(5, List.of(bd("52"), bd("9"), bd("80")))).containsExactly(2, 0, 3);
    }

    @Test
    void divisaoExataNaoPrecisaDeSobra() {
        assertThat(RateioCaixas.distribuir(6, List.of(bd("20"), bd("10"), bd("30")))).containsExactly(2, 1, 3);
    }

    @Test
    void empateNoRestoFavoreceQuemPrecisaDeMais() {
        // cotas 0,5 e 1,5: restos iguais, a 2ª unidade precisa de mais
        assertThat(RateioCaixas.distribuir(2, List.of(bd("1"), bd("3")))).containsExactly(0, 2);
    }

    @Test
    void empateTotalFavoreceAOrdemDaLista() {
        assertThat(RateioCaixas.distribuir(1, List.of(bd("5"), bd("5")))).containsExactly(1, 0);
    }

    @Test
    void semNecessidadeNinguemRecebe() {
        assertThat(RateioCaixas.distribuir(3, List.of(bd("0"), bd("0")))).containsExactly(0, 0);
        assertThat(RateioCaixas.distribuir(0, List.of(bd("4"), bd("2")))).containsExactly(0, 0);
    }

    @Test
    void somaDoRateioEhSempreOTotalDeCaixas() {
        Random aleatorio = new Random(42);
        IntStream.range(0, 500).forEach(rodada -> {
            int unidades = 1 + aleatorio.nextInt(8);
            List<BigDecimal> necessidades = IntStream.range(0, unidades)
                    .mapToObj(i -> BigDecimal.valueOf(aleatorio.nextInt(1000), 1))
                    .toList();
            int caixas = aleatorio.nextInt(50);
            List<Integer> rateio = RateioCaixas.distribuir(caixas, necessidades);

            boolean temNecessidade = necessidades.stream().anyMatch(n -> n.signum() > 0);
            int esperado = temNecessidade ? caixas : 0;
            assertThat(rateio).hasSize(unidades);
            assertThat(rateio.stream().mapToInt(Integer::intValue).sum()).isEqualTo(esperado);
            for (int i = 0; i < unidades; i++) {
                if (necessidades.get(i).signum() == 0) {
                    assertThat(rateio.get(i)).isZero();
                }
            }
        });
    }

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }
}
