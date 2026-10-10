import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/*
 *fluxo desta etapa:
 *1) ler o arquivo e decodificar com as classes da M1
 *2) GUARDAR cada instrução como um objeto Instrucao numa lista
 *3) detectar e mostrar os conflitos de dados e de controle
 */
public class MainM2 {

    public static void main(String[] args) {
        String caminho = args.length > 0 ? args[0] : "testes/teste1.txt";
        int enderecoBase = args.length > 1 ? Integer.decode(args[1]) : 0x00400000;

        //as peças da M1, reaproveitadas como estão
        InputParse parser = new InputParse();
        BitSeparator extrator = new BitSeparator();
        FormatDetector detector = new FormatDetector(extrator);

        try {
            //1) lê e converte as linhas em Instrucaolida (endereço + valor)
            List<InputParse.Instrucaolida> lidas = parser.lerArquivo(caminho, enderecoBase);

            //2) transforma cada uma em Instrucao: é aqui que o programa "guarda"
            //as informações (o que cada instrução lê e escreve)
            List<Instrucao> programa = new ArrayList<>();
            for (InputParse.Instrucaolida lida : lidas) {
                programa.add(Instrucao.decodificar(lida, extrator, detector));
            }

            imprimirPrograma(programa);

            //3) detecção (R1 e R2)
            DetectorConflitos detector2 = new DetectorConflitos(programa);
            imprimirConflitosDeDados(detector2);
            imprimirConflitosDeControle(detector2, programa);
            System.out.println();

            Corretor corretor = new Corretor(programa);
            Saida saida = new Saida(extrator, detector);
            List<ResultadoCorrecao> resultados = new ArrayList<>();

            ResultadoCorrecao soNops = corretor.corrigirSoComNops();
            saida.imprimirListagem(soNops);
            gravarEVerificar(saida, soNops, "somente_nops", caminho, extrator, detector, enderecoBase);
            resultados.add(soNops);

            Reordenador reordenador = new Reordenador(programa, detector2.identificarBlocos());
            ResultadoCorrecao comReordenacao = reordenador.reordenar(corretor);
            saida.imprimirListagem(comReordenacao);
            gravarEVerificar(saida, comReordenacao, "reordenacao", caminho, extrator, detector, enderecoBase);
            resultados.add(comReordenacao);

            //5) relatório de custo (R8)
            Saida.imprimirCusto(programa.size(), resultados);

        } catch (IOException erro) {
            System.err.println("não foi possível ler o arquivo: " + erro.getMessage());
        }
    }

    /*
     * grava os 2 arquivos da técnica (programa e listagem) e confere a saída: lê o arquivo de volta
     * com as classes da M1 e roda os detectores; o resultado tem que ter zero conflitos de dados.
     */
    private static void gravarEVerificar(Saida saida, ResultadoCorrecao r, String sufixo, String caminhoEntrada,
                                         BitSeparator extrator, FormatDetector detector, int base) throws IOException {
        Path entrada = Path.of(caminhoEntrada);
        String nome = entrada.getFileName().toString().replaceFirst("\\.[^.]*$", "");
        Path pasta = Path.of("saida");
        Files.createDirectories(pasta);
        Path arqPrograma = pasta.resolve(nome + "_" + sufixo + ".txt");
        Path arqListagem = pasta.resolve(nome + "_" + sufixo + "_listagem.txt");
        saida.escreverPrograma(arqPrograma, r);
        saida.escreverListagem(arqListagem, r);

        List<Instrucao> relido = new ArrayList<>();
        for (InputParse.Instrucaolida l : new InputParse().lerArquivo(arqPrograma.toString(), base)) {
            relido.add(Instrucao.decodificar(l, extrator, detector));
        }
        DetectorConflitos det = new DetectorConflitos(relido);
        int dados = det.detectarConflitosDeDados().size() + det.detectarConflitosAtravesDosDesvios().size();
        boolean mesmoTamanho = relido.size() == r.totalInstrucoes();
        System.out.println("arquivos: " + arqPrograma + " e " + arqListagem);
        System.out.println("releitura pela M1: " + relido.size() + " instrucoes, conflitos de dados restantes: " + dados
                + (dados == 0 && mesmoTamanho ? "  [OK]" : "  [FALHOU]"));
        System.out.println();
    }

    private static void imprimirPrograma(List<Instrucao> programa) {
        System.out.println("PROGRAMA ORIGINAL (" + programa.size() + " instrucoes)");
        for (Instrucao inst : programa) {
            System.out.println(String.format(" 0x%08X  0x%08X  %s", inst.endereco, inst.valor, inst.assembly));
        }
        System.out.println();
    }

    private static void imprimirConflitosDeDados(DetectorConflitos det) {
        System.out.println("CONFLITOS DE DADOS");
        List<ConflitoDados> conflitos = det.detectarConflitosDeDados();
        if (conflitos.isEmpty()) {
            System.out.println(" nenhum");
        }
        for (ConflitoDados c : conflitos) {
            System.out.println(" " + c);
        }
        System.out.println();
    }

    private static void imprimirConflitosDeControle(DetectorConflitos det, List<Instrucao> programa) {
        System.out.println("CONFLITOS DE CONTROLE");
        List<ConflitoControle> desvios = det.detectarSlotsDeAtraso();
        if (desvios.isEmpty()) {
            System.out.println(" nenhum desvio");
        }
        for (ConflitoControle c : desvios) {
            System.out.println(" " + c);
        }

        System.out.println();
        System.out.println("BLOCOS BASICOS");
        for (BlocoBasico b : det.identificarBlocos()) {
            Instrucao primeira = programa.get(b.indiceInicio);
            Instrucao ultima = programa.get(b.indiceFim);
            System.out.println(String.format(" bloco %d: 0x%08X ate 0x%08X (%d instrucoes)",
                    b.numero, primeira.endereco, ultima.endereco, b.tamanho()));
        }

        System.out.println();
        System.out.println("CONFLITOS DE DADOS ATRAVES DO DESVIO (quando o desvio e tomado)");
        List<ConflitoDados> atravessam = det.detectarConflitosAtravesDosDesvios();
        if (atravessam.isEmpty()) {
            System.out.println(" nenhum");
        }
        for (ConflitoDados c : atravessam) {
            System.out.println(" " + c);
        }
    }
}
