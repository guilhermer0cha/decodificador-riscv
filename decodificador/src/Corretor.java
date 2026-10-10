import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 *corretor: implementa R3 (nops p/ conflitos de dados), R4 (nop no slot de atraso)
 *e R6 (recalculo de enderecos/deslocamentos).
 *
 *o programa corrigido e descrito por uma "ordem": a lista de instrucoes originais na ordem em que
 *ficarao, onde SLOT_NOP (-1) marca um nop de slot de atraso. Para cada instrucao original i existem
 *dois contadores de nops de dados:
 *pre[i]  = nops colocados ANTES do rotulo de i (so a execucao que "cai" de cima passa por eles)
 *post[i] = nops colocados DEPOIS do rotulo de i (todo mundo que chega em i passa por eles)
 *um desvio que cai em i entra em "entrada[i]" (= depois dos pre, antes dos post).
 *
 *isso importa porque o mesmo consumidor pode ser alcancado por dois caminhos (cair de cima ou ser
 *alvo de desvio) e cada caminho tem seus proprios produtores.
 *
 *depois, o programa e montado e VALIDADO caminho a caminho; onde sobrar conflito, entra mais um nop
 *e tudo e validado de novo, ate nao sobrar nenhum (ponto fixo). Inserir nop nunca cria conflito novo,
 *entao o laco sempre termina, e como so inserimos quando ha violacao, nao sobra nop desnecessario.
 *
 *caminhos validados:
 *1) sequencial: cada instrucao olha as 2 anteriores no arquivo (como a R1);
 *2) desvio tomado: ... desvio, slot, alvo0, alvo1 (como a R2).
 */
public class Corretor {

    public static final int SLOT_NOP = -1;

    private final List<Instrucao> programa;
    private final Map<Integer, Integer> indicePorEndereco = new HashMap<>();
    private final int enderecoBase;

    public Corretor(List<Instrucao> programa) {
        this.programa = programa;
        this.enderecoBase = programa.isEmpty() ? 0 : programa.get(0).endereco;
        for (int i = 0; i < programa.size(); i++) {
            indicePorEndereco.put(programa.get(i).endereco, i);
        }
    }

    //tecnica 1: so nops (R3 + R4)

    public ResultadoCorrecao corrigirSoComNops() {
        List<Integer> ordem = new ArrayList<>();
        for (int i = 0; i < programa.size(); i++) {
            ordem.add(i);
            if (programa.get(i).temSlotDeAtraso()) {
                ordem.add(SLOT_NOP); //R4: o slot de todo desvio/salto vira um nop
            }
        }
        return corrigir("somente nops", ordem, new HashSet<Integer>());
    }

    /*
     *corrige conflitos de dados para uma ordem qualquer (usada tambem pela reordenacao).
     *movidas = indices originais que mudaram de lugar (so para marcar na listagem)
     */
    public ResultadoCorrecao corrigir(String nome, List<Integer> ordem, Set<Integer> movidas) {
        int n = programa.size();
        int[] pre = new int[n];
        int[] post = new int[n];

        List<ItemPrograma> itens;
        int[] entrada = new int[n];

        while (true) {
            itens = montar(ordem, movidas, pre, post, entrada);
            if (!corrigirUmaViolacao(itens, entrada, pre, post)) {
                break;
            }
        }

        recalcularEnderecos(itens, entrada); // R6
        return new ResultadoCorrecao(nome, itens);
    }

    //montagem da lista de itens a partir de ordem/pre/post

    private List<ItemPrograma> montar(List<Integer> ordem, Set<Integer> movidas,
                                      int[] pre, int[] post, int[] entrada) {
        List<ItemPrograma> itens = new ArrayList<>();
        for (int o : ordem) {
            if (o == SLOT_NOP) {
                itens.add(new ItemPrograma(null, -1, ItemPrograma.Tipo.NOP_SLOT));
                continue;
            }
            for (int k = 0; k < pre[o]; k++) {
                itens.add(new ItemPrograma(null, -1, ItemPrograma.Tipo.NOP_DADOS));
            }
            entrada[o] = itens.size(); // o desvio para "o" cai aqui
            for (int k = 0; k < post[o]; k++) {
                itens.add(new ItemPrograma(null, -1, ItemPrograma.Tipo.NOP_DADOS));
            }
            ItemPrograma.Tipo tipo = movidas.contains(o)
                    ? ItemPrograma.Tipo.MOVIDA : ItemPrograma.Tipo.ORIGINAL;
            itens.add(new ItemPrograma(programa.get(o), o, tipo));
        }
        return itens;
    }

    //validacao: acha UMA violacao, corrige e avisa (true) para o chamador montar de novo

    private boolean corrigirUmaViolacao(List<ItemPrograma> itens, int[] entrada,
                                        int[] pre, int[] post) {
        //1) caminho sequencial
        for (int p = 0; p < itens.size(); p++) {
            int falta = nopsFaltando(itens, p, true);
            if (falta > 0) {
                aplicar(itens, p, falta, -1, pre, post);
                return true;
            }
        }

        //2) caminho do desvio tomado: desvio, slot, alvo0, alvo1
        for (int p = 0; p + 1 < itens.size(); p++) {
            ItemPrograma desvio = itens.get(p);
            if (desvio.original == null || !desvio.original.temAlvoConhecido()) {
                continue;
            }
            Integer t = indicePorEndereco.get(desvio.original.enderecoAlvo());
            if (t == null) {
                continue; // alvo fora do arquivo
            }
            List<ItemPrograma> caminho = new ArrayList<>();
            caminho.add(desvio);
            caminho.add(itens.get(p + 1)); // slot
            int e = entrada[t];
            for (int k = 0; k < 2 && e + k < itens.size(); k++) {
                caminho.add(itens.get(e + k));
            }
            for (int pos = 2; pos < caminho.size(); pos++) {
                int falta = nopsFaltando(caminho, pos, false);
                if (falta > 0) {
                    aplicar(itens, e + (pos - 2), falta, t, pre, post);
                    return true;
                }
            }
        }
        return false;
    }

    /*
     *quantos nops faltam entre o consumidor (posicao pos de seq) e seus produtores recentes.
     *olha d = 1 e d = 2; o primeiro que escreve o registrador e o mais recente.
     *cortarNoSaltoIncondicional: no caminho sequencial, depois do slot de um jal/jalr a execucao
     *nao vem de cima (vem de um retorno), entao nao olhamos para antes dele (mesma regra da R1).
     */
    private int nopsFaltando(List<ItemPrograma> seq, int pos, boolean cortarNoSaltoIncondicional) {
        int pior = 0;
        for (int reg : seq.get(pos).lidos()) {
            for (int d = 1; d < ConflitoDados.DISTANCIA_SEGURA; d++) {
                int j = pos - d;
                if (j < 0) {
                    break;
                }
                if (cortarNoSaltoIncondicional && j >= 1 && seq.get(j - 1).ehSaltoIncondicional()) {
                    break;
                }
                if (seq.get(j).escrito() == reg) {
                    pior = Math.max(pior, ConflitoDados.DISTANCIA_SEGURA - d);
                    break; // produtor mais recente
                }
            }
        }
        return pior;
    }

    /*
     * decide onde colocar os n nops que faltam antes do item idx:
     *  - se idx e o slot de um desvio: nao da para pôr nop entre o desvio e o slot, entao os nops vao
     *    antes do desvio (post do desvio);
     *  - se o conflito foi no caminho do desvio tomado e idx e o proprio alvo: post (todo mundo que
     *    entra no rotulo passa por eles);
     *  - senao: pre (so quem cai de cima precisa deles).
     */
    private void aplicar(List<ItemPrograma> itens, int idx, int n, int alvoViaSalto,
                         int[] pre, int[] post) {
        int c = itens.get(idx).indiceOriginal;
        if (idx > 0 && itens.get(idx - 1).ehControle()) {
            post[itens.get(idx - 1).indiceOriginal] += n;
        } else if (alvoViaSalto >= 0 && c == alvoViaSalto) {
            post[c] += n;
        } else {
            pre[c] += n;
        }
    }

    //R6: enderecos novos + deslocamentos recalculados + reescrita em linguagem de maquina

    private void recalcularEnderecos(List<ItemPrograma> itens, int[] entrada) {
        for (int p = 0; p < itens.size(); p++) {
            itens.get(p).endereco = enderecoBase + 4 * p;
        }
        for (ItemPrograma it : itens) {
            if (it.original == null) {
                it.palavra = Instrucao.CODIGO_NOP;
                continue;
            }
            Instrucao inst = it.original;
            if (inst.valida && Codificador.ehDesvioOuJal(inst.valor)) {
                Integer t = indicePorEndereco.get(inst.enderecoAlvo());
                // alvo dentro do arquivo: aponta para a nova posicao; fora do arquivo: mantem o absoluto
                int alvoNovo = (t != null) ? itens.get(entrada[t]).endereco : inst.enderecoAlvo();
                it.palavra = Codificador.recodificar(inst.valor, alvoNovo - it.endereco);
            } else {
                it.palavra = inst.valor;
            }
        }
    }
}
