import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Reordenador {
    private final List<Instrucao> programa;
    private final List<BlocoBasico> blocos;

    public Reordenador(List<Instrucao> programa, List<BlocoBasico> blocos) {
        this.programa = programa;
        this.blocos = blocos;
    }

    public ResultadoCorrecao reordenar(Corretor corretor) {
        List<Integer> novaOrdem = new ArrayList<>();
        Set<Integer> movidas = new HashSet<>();

        int instrucaoAtual = 0;

        for (BlocoBasico bloco : blocos) {
            // diciona instruções fora dos blocos identificados (caso existam)
            while (instrucaoAtual < bloco.indiceInicio) {
                novaOrdem.add(instrucaoAtual);
                if (programa.get(instrucaoAtual).temSlotDeAtraso()) {
                    novaOrdem.add(Corretor.SLOT_NOP);
                }
                instrucaoAtual++;
            }

            List<Integer> ordemBloco = new ArrayList<>();
            for (int i = bloco.indiceInicio; i <= bloco.indiceFim; i++) {
                ordemBloco.add(i);
            }

            // 1. isolar a instrução de desvio (se o bloco terminar com uma)
            int ultimo = ordemBloco.size() - 1;
            Integer idxDesvio = null;
            if (ultimo >= 0 && programa.get(ordemBloco.get(ultimo)).temSlotDeAtraso()) {
                idxDesvio = ordemBloco.remove(ultimo);
            }

            // 2. preencher o slot de atraso com uma instrução candidata
            int candidatoSlot = Corretor.SLOT_NOP;
            if (idxDesvio != null) {
                // procura de trás para frente para achar a instrução independente mais próxima
                for (int i = ordemBloco.size() - 1; i >= 0; i--) {
                    int candidato = ordemBloco.get(i);
                    Instrucao instCandidato = programa.get(candidato);
                    Instrucao desvio = programa.get(idxDesvio);

                    // não pode ter conflito direto com o desvio
                    if (temDependencia(instCandidato, desvio) || temDependencia(desvio, instCandidato)) {
                        continue;
                    }

                    // verifica se pode descer (passar por todas as instruções abaixo dela)
                    boolean podeDescer = true;
                    for (int j = i + 1; j < ordemBloco.size(); j++) {
                        Instrucao instMeio = programa.get(ordemBloco.get(j));
                        if (temDependencia(instCandidato, instMeio) || temDependencia(instMeio, instCandidato)) {
                            podeDescer = false;
                            break;
                        }
                    }

                    if (podeDescer) {
                        candidatoSlot = candidato;
                        ordemBloco.remove(i);
                        movidas.add(candidato);
                        break;
                    }
                }
            }

            // 3. afastar produtores e consumidores
            int maxPasses = ordemBloco.size();
            for (int passe = 0; passe < maxPasses; passe++) {
                boolean trocou = false;
                for (int i = 0; i < ordemBloco.size() - 1; i++) {
                    Instrucao atual = programa.get(ordemBloco.get(i));
                    Instrucao prox = programa.get(ordemBloco.get(i + 1));

                    // se são independentes entre si, podemos inverter a ordem
                    if (!temDependencia(atual, prox) && !temDependencia(prox, atual)) {
                        boolean deveTrocar = false;

                        if (i > 0) {
                            Instrucao anterior = programa.get(ordemBloco.get(i - 1));
                            // se "atual" tem dependência com "anterior" (RAW d=1), trocar com "prox" aumenta a distância
                            if (temRAW(anterior, atual)) {
                                deveTrocar = true;
                            }
                        }

                        if (deveTrocar) {
                            int temp = ordemBloco.get(i);
                            ordemBloco.set(i, ordemBloco.get(i + 1));
                            ordemBloco.set(i + 1, temp);
                            movidas.add(ordemBloco.get(i));
                            movidas.add(ordemBloco.get(i + 1));
                            trocou = true;
                        }
                    }
                }
                if (!trocou) break; // termina antecipadamente se não houver mais trocas úteis
            }

            // 4. montar a nova ordem final do bloco
            novaOrdem.addAll(ordemBloco);
            if (idxDesvio != null) {
                novaOrdem.add(idxDesvio);
                novaOrdem.add(candidatoSlot);
            }

            instrucaoAtual = bloco.indiceFim + 1;
        }

        // adiciona o que sobrar no final do programa
        while (instrucaoAtual < programa.size()) {
            novaOrdem.add(instrucaoAtual);
            if (programa.get(instrucaoAtual).temSlotDeAtraso()) {
                novaOrdem.add(Corretor.SLOT_NOP);
            }
            instrucaoAtual++;
        }

        return corretor.corrigir("reordenacao", novaOrdem, movidas);
    }

    private boolean temRAW(Instrucao produtor, Instrucao consumidor) {
        if (!produtor.escreveRegistrador()) return false;
        for (int regLido : consumidor.registradoresLidos()) {
            if (produtor.registradorEscrito() == regLido) return true;
        }
        return false;
    }

    // verifica todos os tipos de dependências estipulados
    private boolean temDependencia(Instrucao a, Instrucao b) {
        // RAW (leitura após escrita)
        if (temRAW(a, b)) return true;

        // WAR (escrita após leitura)
        if (b.escreveRegistrador()) {
            for (int regLido : a.registradoresLidos()) {
                if (b.registradorEscrito() == regLido) return true;
            }
        }

        // WAW (ewscrita após escrita)
        if (a.escreveRegistrador() && b.escreveRegistrador()) {
            if (a.registradorEscrito() == b.registradorEscrito()) return true;
        }

        // MEM (acessos à memória que possam se sobrepor)
        if (isMemoria(a) && isMemoria(b)) {
            boolean aStore = a.formato.equals("S");
            boolean bStore = b.formato.equals("S");

            if (aStore || bStore) return true;
        }

        return false;
    }

    private boolean isMemoria(Instrucao inst) {
        if (inst.mnemonico == null) return false;
        return inst.formato.equals("S") ||
                inst.mnemonico.equals("lw") || inst.mnemonico.equals("lh") ||
                inst.mnemonico.equals("lb") || inst.mnemonico.equals("lhu") || inst.mnemonico.equals("lbu");
    }
}