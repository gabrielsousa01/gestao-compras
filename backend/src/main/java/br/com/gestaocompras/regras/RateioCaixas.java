package br.com.gestaocompras.regras;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * O fornecedor só vende caixas fechadas. Esta regra decide quantas caixas comprar
 * para a soma das unidades e como dividi-las entre elas.
 *
 * <ol>
 *   <li>Total de caixas = necessidade somada ÷ tamanho da caixa, arredondado para cima
 *       (compra-se uma caixa a mais em vez de deixar faltar).</li>
 *   <li>Cada unidade recebe a parte inteira da sua cota proporcional
 *       ({@code caixas × necessidade da unidade ÷ necessidade total}).</li>
 *   <li>As caixas que sobram vão, uma a uma, para as unidades com a maior parte
 *       fracionária (método do maior resto). Em empate, vence quem precisa de mais
 *       e, depois, quem vem primeiro na lista, para o resultado ser sempre o mesmo.</li>
 * </ol>
 *
 * Comprar tudo junto e ratear evita o desperdício de arredondar caixa por caixa em
 * cada unidade.
 */
public final class RateioCaixas {

    private RateioCaixas() {
    }

    /** Número de caixas fechadas que cobre a necessidade total. */
    public static int totalDeCaixas(BigDecimal necessidadeTotal, BigDecimal quantidadePorCaixa) {
        if (quantidadePorCaixa == null || quantidadePorCaixa.signum() <= 0) {
            throw new IllegalArgumentException("A quantidade por caixa deve ser maior que zero.");
        }
        if (necessidadeTotal.signum() <= 0) {
            return 0;
        }
        return necessidadeTotal.divide(quantidadePorCaixa, 0, RoundingMode.CEILING).intValueExact();
    }

    /**
     * Divide {@code caixas} entre as unidades na proporção das necessidades.
     * A lista devolvida tem o mesmo tamanho e ordem de {@code necessidades}, e a soma
     * é exatamente {@code caixas}. Unidades com necessidade zero recebem zero.
     */
    public static List<Integer> distribuir(int caixas, List<BigDecimal> necessidades) {
        if (caixas < 0) {
            throw new IllegalArgumentException("O número de caixas não pode ser negativo.");
        }
        BigDecimal total = necessidades.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Integer> resultado = new ArrayList<>(necessidades.size());
        if (caixas == 0 || total.signum() <= 0) {
            necessidades.forEach(n -> resultado.add(0));
            return resultado;
        }

        List<Cota> cotas = new ArrayList<>(necessidades.size());
        int distribuidas = 0;
        for (int i = 0; i < necessidades.size(); i++) {
            BigDecimal exata = BigDecimal.valueOf(caixas).multiply(necessidades.get(i)).divide(total, 10, RoundingMode.HALF_UP);
            int inteira = exata.setScale(0, RoundingMode.FLOOR).intValueExact();
            resultado.add(inteira);
            distribuidas += inteira;
            cotas.add(new Cota(i, exata.subtract(BigDecimal.valueOf(inteira)), necessidades.get(i)));
        }

        cotas.sort(Comparator.comparing(Cota::resto).reversed()
                .thenComparing(Comparator.comparing(Cota::necessidade).reversed())
                .thenComparingInt(Cota::indice));

        int sobra = caixas - distribuidas;
        for (int k = 0; k < sobra; k++) {
            int indice = cotas.get(k).indice();
            resultado.set(indice, resultado.get(indice) + 1);
        }
        return resultado;
    }

    private record Cota(int indice, BigDecimal resto, BigDecimal necessidade) {
    }
}
