//uma instrução de desvio/salto e a instrucao que ocupa o seu slot
//o slot de atraso é uma instrucao depois do desvio
//o desvio so é resolvido no estagio ID, essa instrucao ja foi buscada (IF) e sempre executa, tomando o desvio ou nao
//conflito controle: uma instrucao executa sem que ninguem tenha decidido que ela deveria executar

public class ConflitoControle {

    public final Instrucao desvio;
    public final Instrucao slot; // null se o desvio for a última instrução do arquivo

    public ConflitoControle(Instrucao desvio, Instrucao slot) {
        this.desvio = desvio;
        this.slot = slot;
    }

    @Override
    public String toString() {
        String ocupante = (slot == null) ? "(nenhuma, fim do programa)" : slot.toString();
        return desvio + "  slot ocupado por: " + ocupante;
    }
}

