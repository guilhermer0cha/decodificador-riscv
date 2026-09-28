import java.util.HashMap;
import java.util.Map;

public class Statistics {
    private int totalInstrucoes = 0;
    private final Map<String, Integer> contagemPorFormato = new HashMap<>();
    private final Map<String, Double> tabelaCpi = new HashMap<>();

    public Statistics() {
        // inicializa a contagem dos formatos possíveis
        String[] formatos = {"R", "I", "S", "B", "U", "J", "invalido!"};
        for (String f : formatos) {
            contagemPorFormato.put(f, 0);
        }

        // tabela de CPI padrão inicial (CPI = 1.0 para todos)
        tabelaCpi.put("R", 1.0);
        tabelaCpi.put("I", 1.0);
        tabelaCpi.put("S", 1.0);
        tabelaCpi.put("B", 1.0);
        tabelaCpi.put("U", 1.0);
        tabelaCpi.put("J", 1.0);
    }

    public void setCpiFormato(String formato, double cpi) {
        if (tabelaCpi.containsKey(formato)) {
            tabelaCpi.put(formato, cpi);
        }
    }

    public void registrarInstrucao(String formato) {
        if (contagemPorFormato.containsKey(formato)) {
            contagemPorFormato.put(formato, contagemPorFormato.get(formato) + 1);
            totalInstrucoes++;
        }
    }

    public void imprimirRelatorio() {
        System.out.println("=============== RELATÓRIO ESTATÍSTICO ===============");
        if (totalInstrucoes == 0) {
            System.out.println("Nenhuma instrução analisada.");
            System.out.println("=====================================================");
            return;
        }

        double somaCpi = 0;
        int totalValidas = 0;

        String[] formatosValidos = {"R", "I", "S", "B", "U", "J"};

        System.out.println("Contagem e Percentual por Formato:");
        for (String f : formatosValidos) {
            int cont = contagemPorFormato.get(f);
            double percentual = ((double) cont / totalInstrucoes) * 100;
            System.out.printf("Formato %s: %d instrução(ões) (%.2f%%)%n", f, cont, percentual);

            somaCpi += cont * tabelaCpi.get(f);
            totalValidas += cont;
        }

        int invalidas = contagemPorFormato.getOrDefault("invalido!", 0);
        if (invalidas > 0) {
            double percInvalidas = ((double) invalidas / totalInstrucoes) * 100;
            System.out.printf("Inválidas: %d instrução(ões) (%.2f%%)%n", invalidas, percInvalidas);
        }

        if (totalValidas > 0) {
            // media aritmetica ponderada: somatorio (quantidade * CPI) / quantidade Total
            double cpiMedio = somaCpi / totalValidas;
            System.out.printf("%nCPI Médio do programa (Instruções Válidas): %.2f%n", cpiMedio);
        }
        System.out.println("=====================================================");
    }
}