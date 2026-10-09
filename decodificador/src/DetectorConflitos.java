import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

//implementa r1(dados) e r2(controle)

public class DetectorConflitos {

    private final List<Instrucao> programa;

    // endereço -> posição na lista. Serve para, dado o destino de um desvio
    // (um endereço), descobrir QUAL instrução da lista é o alvo.
    private final Map<Integer, Integer> indicePorEndereco = new HashMap<>();

    public DetectorConflitos(List<Instrucao> programa) {
        this.programa = programa;
        for (int i = 0; i < programa.size(); i++) {
            indicePorEndereco.put(programa.get(i).endereco, i);
        }
    }

    // R1 - CONFLITOS DE DADOS

    /*
     *p cada instrução (consumidor), olha as 2 instruções anteriores procurando quem escreve um registrador que ela lê
     *olhamos 2 para trás porque a partir de d = 3 já não existe conflito (ConflitoDados.DISTANCIA_SEGURA).
     *
     *só conta dependência VERDADEIRA (leitura após escrita). escrita após leitura e escrita após escrita não geram conflito num pipeline em ordem.
     */
    public List<ConflitoDados> detectarConflitosDeDados() {
        List<ConflitoDados> achados = new ArrayList<>();

        for (int i = 0; i < programa.size(); i++) {
            Instrucao consumidor = programa.get(i);

            // só os registradores que ESTE formato realmente lê (ver Instrucao)
            for (int reg : consumidor.registradoresLidos()) {
                ConflitoDados c = buscarProdutorNaOrdemDoArquivo(i, reg);
                if (c != null) {
                    achados.add(c);
                }
            }
        }
        return achados;
    }

    /*
     *anda para trás a partir do consumidor (posição i) procurando o PRODUTOR MAIS
     *RECENTE do registrador. Começamos em d=1 e subimos; o primeiro que escreve o
     *registrador é o mais recente, então paramos nele. (Um produtor mais antigo que
     *foi "sobrescrito" por outro mais novo não importa.)
     */
    private ConflitoDados buscarProdutorNaOrdemDoArquivo(int i, int reg) {
        for (int d = 1; d < ConflitoDados.DISTANCIA_SEGURA; d++) {
            int j = i - d;
            if (j < 0) {
                return null; // acabou o programa
            }
            // Se j é o slot de um jal/jalr, depois dele o fluxo vai para o destino do
            // salto e NÃO para a instrução j+1. Logo j não "alimenta" quem vem depois
            // no arquivo, e olhar mais para trás seria olhar um caminho que não existe.
            if (ehSlotDeSaltoIncondicional(j)) {
                return null;
            }
            if (programa.get(j).registradorEscrito() == reg) {
                return new ConflitoDados(programa.get(j), programa.get(i), reg, d, null);
            }
        }
        return null;
    }

    /* a instrução na posição j é o slot de um jal/jalr? (a instrução anterior é um desses) */
    private boolean ehSlotDeSaltoIncondicional(int j) {
        return j >= 1 && programa.get(j - 1).ehSaltoIncondicional();
    }

    // R2 - CONFLITOS DE CONTROLE

    /* lista cada desvio/salto e quem ocupa o seu slot de atraso. */
    public List<ConflitoControle> detectarSlotsDeAtraso() {
        List<ConflitoControle> achados = new ArrayList<>();
        for (int i = 0; i < programa.size(); i++) {
            Instrucao atual = programa.get(i);
            if (atual.temSlotDeAtraso()) {
                Instrucao slot = (i + 1 < programa.size()) ? programa.get(i + 1) : null;
                achados.add(new ConflitoControle(atual, slot));
            }
        }
        return achados;
    }

    /*
     *divide o programa em blocos básicos
     *metodo dos "líderes": líder = primeira instrução de um bloco. são líderes:
     *1) a primeira instrução do programa
     *2) todo alvo de desvio/jal (alguém pode chegar ali vindo de outro lugar)
     *3) a instrução logo DEPOIS do slot de um desvio (o desvio e o slot fecham o bloco)
     * Cada bloco vai de um líder até a instrução anterior ao próximo líder
     */
    public List<BlocoBasico> identificarBlocos() {
        TreeSet<Integer> lideres = new TreeSet<>(); // TreeSet = ordenado e sem repetição
        if (programa.isEmpty()) {
            return new ArrayList<>();
        }
        lideres.add(0);

        for (int i = 0; i < programa.size(); i++) {
            Instrucao atual = programa.get(i);
            if (!atual.ehControle()) {
                continue;
            }
            if (atual.temAlvoConhecido()) {
                Integer indiceAlvo = indicePorEndereco.get(atual.enderecoAlvo());
                if (indiceAlvo != null) { //null = alvo fora do arquivo (ex.: função da biblioteca)
                    lideres.add(indiceAlvo);
                }
            }
            if (i + 2 < programa.size()) { //i = desvio, i+1 = slot, i+2 = novo bloco
                lideres.add(i + 2);
            }
        }

        List<Integer> listaLideres = new ArrayList<>(lideres);
        List<BlocoBasico> blocos = new ArrayList<>();
        for (int k = 0; k < listaLideres.size(); k++) {
            int inicio = listaLideres.get(k);
            int fim = (k + 1 < listaLideres.size())
                    ? listaLideres.get(k + 1) - 1
                    : programa.size() - 1;
            blocos.add(new BlocoBasico(k + 1, inicio, fim));
        }
        return blocos;
    }

    /*
     *conflitos de dados que ATRAVESSAM a fronteira de um desvio.
     *o conflito da R1 só olha a ordem do arquivo, o que cobre o caminho
     *"desvio NÃO tomado" (a execução segue pelo arquivo normalmente). mas se o desvio
     *for TOMADO, a execução pula para o alvo e a sequência real vira:
     *...desvio, slot, alvo0, alvo1...
     *
     *quem ta logo antes do alvo NESSA sequência é o slot (d=1) e o próprio desvio
     * (d=2, só importa para jal, que escreve rd). Essa vizinhança não existe na ordem
     * do arquivo, então a R1 não enxerga esses pares. É isso que checamos aqui.
     *
     * Só os 2 primeiros do alvo (alvo0 e alvo1) podem ser afetados, porque o slot fica
     * a d=1 e d=2 deles, e a partir de d=3 não há conflito.
     */
    public List<ConflitoDados> detectarConflitosAtravesDosDesvios() {
        List<ConflitoDados> achados = new ArrayList<>();

        for (int i = 0; i < programa.size(); i++) {
            Instrucao desvio = programa.get(i);
            if (!desvio.temSlotDeAtraso() || !desvio.temAlvoConhecido()) {
                continue; // jalr: destino desconhecido, não dá para analisar o caminho tomado
            }
            if (i + 1 >= programa.size()) {
                continue; // sem slot no arquivo, não há o que analisar
            }
            Integer indiceAlvo = indicePorEndereco.get(desvio.enderecoAlvo());
            if (indiceAlvo == null) {
                continue; // alvo fora do arquivo
            }

            // monta o trecho real do caminho tomado: desvio, slot, alvo0, alvo1
            List<Instrucao> caminho = new ArrayList<>();
            caminho.add(desvio);
            caminho.add(programa.get(i + 1));
            for (int n = 0; n < 2 && indiceAlvo + n < programa.size(); n++) {
                caminho.add(programa.get(indiceAlvo + n));
            }

            // os consumidores são os elementos do alvo: posições 2 e 3 do "caminho"
            for (int posConsumidor = 2; posConsumidor < caminho.size(); posConsumidor++) {
                Instrucao consumidor = caminho.get(posConsumidor);
                for (int reg : consumidor.registradoresLidos()) {
                    ConflitoDados c = buscarProdutorCruzandoDesvio(caminho, posConsumidor, reg, desvio);
                    if (c != null) {
                        achados.add(c);
                    }
                }
            }
        }
        return achados;
    }

    /*
     *igual à busca da R1, mas sobre o trecho "caminho". Só devolve conflito se o
     *produtor mais recente for o desvio ou o slot (posições 0 e 1), ou seja, se o
     *par realmente ATRAVESSA a fronteira. Se o produtor mais recente estiver dentro
     *do próprio alvo, o par já é coberto pela R1 e não é repetido aqui.
     */
    private ConflitoDados buscarProdutorCruzandoDesvio(List<Instrucao> caminho,
                                                       int posConsumidor, int reg,
                                                       Instrucao desvio) {
        for (int d = 1; d < ConflitoDados.DISTANCIA_SEGURA; d++) {
            int posProdutor = posConsumidor - d;
            if (posProdutor < 0) {
                return null;
            }
            if (caminho.get(posProdutor).registradorEscrito() == reg) {
                boolean atravessa = posProdutor <= 1; // 0 = desvio, 1 = slot
                return atravessa
                        ? new ConflitoDados(caminho.get(posProdutor), caminho.get(posConsumidor),
                        reg, d, desvio)
                        : null;
            }
        }
        return null;
    }
}

