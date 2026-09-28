import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        InputParse parser = new InputParse();
        BitSeparator extractor = new BitSeparator();
        FormatDetector detector = new FormatDetector(extractor);

        // caminho e endereco-base podem ser informados pela linha de comando
        String caminho = args.length > 0 ? args[0] : "memoria.txt";
        int enderecoBase = args.length > 1 ? Integer.decode(args[1]) : 0;

        try {
            List<InputParse.Instrucaolida> instrucoes =
                    parser.lerArquivo(caminho, enderecoBase);

            System.out.println("=============== SAÍDA ===============");

            for (InputParse.Instrucaolida instrucao : instrucoes) {
                Output.imprimirInstrucao(instrucao, extractor, detector);
            }

        } catch (IOException erro) {
            System.err.println("não foi possível ler o arquivo: " + erro.getMessage());
        }
    }
}
