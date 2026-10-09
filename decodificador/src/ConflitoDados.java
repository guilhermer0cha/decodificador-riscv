//conflitoDados: um par produtor/consumidor que viola a distância mínima.
public class ConflitoDados {

    /**
     *distância a partir da qual NÃO há mais conflito (premissa 3).
     *o banco de registradores escreve na primeira metade do ciclo e lê na segunda,
     *então o produtor (que escreve no WB) e o consumidor (que lê no ID)
     *podem usar o MESMO ciclo. Isso dá d = 3 como primeira distância segura.
     */
    public static final int DISTANCIA_SEGURA = 3;

    public final Instrucao produtor; // quem escreve o registrador
    public final Instrucao consumidor; // quem lê o registrador cedo demais
    public final int registrador; // o registrador envolvido
    public final int distancia; // d: quantas instruções separam os dois (1 = vizinhas)

    //se o conflito só acontece quando um desvio é TOMADO, guardamos qual é o desvio.
    //para conflitos "normais" (ordem do arquivo) fica null.
    public final Instrucao desvioDeOrigem;

    public ConflitoDados(Instrucao produtor, Instrucao consumidor, int registrador,
                         int distancia, Instrucao desvioDeOrigem) {
        this.produtor = produtor;
        this.consumidor = consumidor;
        this.registrador = registrador;
        this.distancia = distancia;
        this.desvioDeOrigem = desvioDeOrigem;
    }

    /**fórmula do enunciado: nops = 3 - d. Para d=1 são 2 nops, para d=2 é 1 nop. */
    public int nopsNecessarios() {
        return DISTANCIA_SEGURA - distancia;
    }

    /**formato do exemplo do enunciado:  consumidor <- produtor [reg, d=?, N nops] */
    @Override
    public String toString() {
        int n = nopsNecessarios();
        String texto = consumidor + " <- " + produtor
                + " [" + Output.nomeABI(registrador) + ", d=" + distancia + ", "
                + n + (n == 1 ? " nop]" : " nops]");
        if (desvioDeOrigem != null) {
            texto += "  (caminho tomado do desvio em "
                    + String.format("0x%08X", desvioDeOrigem.endereco) + ")";
        }
        return texto;
    }
}
