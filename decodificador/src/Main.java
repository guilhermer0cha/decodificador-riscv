import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        InputParse parser = new InputParse();
        BitSeparator extractor = new BitSeparator();
        FormatDetector detector = new FormatDetector(extractor);
        Statistics relatorio = new Statistics();

        // caminho e endereco-base podem ser informados pela linha de comando
        String caminho = args.length > 0 ? args[0] : "memoria.txt";
        int enderecoBase = args.length > 1 ? Integer.decode(args[1]) : 0;

        // a tabela de CPI pode ser fornecida via linha de comando
        // a partir do 3º argumento (ordem dos CPIs: R I S B U J)
        if (args.length >= 8) {
            try {
                relatorio.setCpiFormato("R", Double.parseDouble(args[2]));
                relatorio.setCpiFormato("I", Double.parseDouble(args[3]));
                relatorio.setCpiFormato("S", Double.parseDouble(args[4]));
                relatorio.setCpiFormato("B", Double.parseDouble(args[5]));
                relatorio.setCpiFormato("U", Double.parseDouble(args[6]));
                relatorio.setCpiFormato("J", Double.parseDouble(args[7]));
            } catch (NumberFormatException e) {
                System.err.println("falha ao ler valores numéricos de CPI dos argumentos. valores padrão serão utilizados (CPI = 1.0).");
            }
        }

        try {
            List<InputParse.Instrucaolida> instrucoes =
                    parser.lerArquivo(caminho, enderecoBase);

            System.out.println("=============== SAÍDA ===============");

            for (InputParse.Instrucaolida instrucao : instrucoes) {
                Output.imprimirInstrucao(instrucao, extractor, detector);

                // registrar o formato da instrução analisada no relatório
                String formato = detector.formato(instrucao.valor);
                relatorio.registrarInstrucao(formato);
            }

            // imprimir o relatório estatístico ao final da listagem
            relatorio.imprimirRelatorio();

        } catch (IOException erro) {
            System.err.println("não foi possível ler o arquivo: " + erro.getMessage());
        }
    }
}