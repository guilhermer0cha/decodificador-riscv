import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/*
 *R7 (arquivos de saida) e R8 (relatorio de custo)
 */
public class Saida {

    private final BitSeparator extrator;
    private final FormatDetector detector;

    public Saida(BitSeparator extrator, FormatDetector detector) {
        this.extrator = extrator;
        this.detector = detector;
    }

    /* texto assembly da palavra no endereco dado (ja com o alvo dos desvios recalculado) */
    public String desmontar(int endereco, int palavra) {
        Instrucao inst = Instrucao.decodificar(
                new InputParse.Instrucaolida(endereco, palavra, ""), extrator, detector);
        return inst.assembly;
    }

    public void escreverPrograma(Path arquivo, ResultadoCorrecao r) throws IOException {
        List<String> linhas = new ArrayList<>();
        linhas.add("# programa corrigido - tecnica: " + r.nomeTecnica);
        for (ItemPrograma it : r.itens) {
            linhas.add("# " + desmontar(it.endereco, it.palavra));
            linhas.add(String.format("0x%08X", it.palavra));
        }
        Files.write(arquivo, linhas);
    }

    public void escreverListagem(Path arquivo, ResultadoCorrecao r) throws IOException {
        List<String> linhas = new ArrayList<>();
        linhas.add("LISTAGEM - " + r.nomeTecnica);
        linhas.add("endereco     codigo      instrucao");
        for (ItemPrograma it : r.itens) {
            linhas.add(String.format("0x%08X  0x%08X  %-28s %s",
                    it.endereco, it.palavra, desmontar(it.endereco, it.palavra), it.marcacao()).stripTrailing());
        }
        Files.write(arquivo, linhas);
    }

    public void imprimirListagem(ResultadoCorrecao r) {
        System.out.println("PROGRAMA CORRIGIDO - " + r.nomeTecnica.toUpperCase());
        for (ItemPrograma it : r.itens) {
            System.out.println(String.format(" 0x%08X  0x%08X  %-28s %s",
                    it.endereco, it.palavra, desmontar(it.endereco, it.palavra), it.marcacao()).stripTrailing());
        }
        System.out.println();
    }

    // R8

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private static String rotuloComPontos(String rotulo) {
        StringBuilder sb = new StringBuilder(rotulo).append(' ');
        while (sb.length() < 21) {
            sb.append('.');
        }
        return sb.append(' ').toString();
    }

    private static String linhaCusto(String rotulo, int original, ResultadoCorrecao r) {
        int acrescimo = r.totalInstrucoes() - original;
        double perc = original == 0 ? 0 : 100.0 * acrescimo / original;
        return String.format(PT_BR, " %s%d instrucoes (+%d, +%.1f%%)",
                rotuloComPontos(rotulo), r.totalInstrucoes(), acrescimo, perc);
    }

    public static void imprimirCusto(int original, List<ResultadoCorrecao> resultados) {
        System.out.println("CUSTO");
        System.out.println(" " + rotuloComPontos("original") + original + " instrucoes");
        for (ResultadoCorrecao r : resultados) {
            System.out.println(linhaCusto(r.nomeTecnica, original, r));
        }
        if (resultados.size() >= 2) {
            ResultadoCorrecao a = resultados.get(0);
            ResultadoCorrecao b = resultados.get(1);
            int economia = a.totalInstrucoes() - b.totalInstrucoes();
            System.out.println(String.format(PT_BR,
                    " comparacao: '%s' elimina %d nop(s) em relacao a '%s' (%.1f%% menos)",
                    b.nomeTecnica, economia, a.nomeTecnica,
                    a.nopsInseridos() == 0 ? 0.0 : 100.0 * economia / a.nopsInseridos()));
        }
    }
}
