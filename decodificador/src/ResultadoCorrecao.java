import java.util.List;
/* o programa corrigido por uma tecnica + as contagens usadas no relatorio de custo (R8) */
public class ResultadoCorrecao {

    public final String nomeTecnica;
    public final List<ItemPrograma> itens;
    public final int nopsDados;
    public final int nopsSlot;
    public final int movidas;

    public ResultadoCorrecao(String nomeTecnica, List<ItemPrograma> itens) {
        this.nomeTecnica = nomeTecnica;
        this.itens = itens;
        int d = 0, s = 0, m = 0;
        for (ItemPrograma it : itens) {
            if (it.tipo == ItemPrograma.Tipo.NOP_DADOS) d++;
            if (it.tipo == ItemPrograma.Tipo.NOP_SLOT) s++;
            if (it.tipo == ItemPrograma.Tipo.MOVIDA) m++;
        }
        this.nopsDados = d;
        this.nopsSlot = s;
        this.movidas = m;
    }

    public int totalInstrucoes() {
        return itens.size();
    }

    public int nopsInseridos() {
        return nopsDados + nopsSlot;
    }
}
