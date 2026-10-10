/*
 *uma posicao do programa CORRIGIDO, pode ser uma instrucao do programa original
 *(possivelmente em outro lugar) ou um nop inserido.
 */
public class ItemPrograma {

    public enum Tipo { ORIGINAL, NOP_DADOS, NOP_SLOT, MOVIDA }

    public final Instrucao original;  // null se for um nop inserido
    public final int indiceOriginal;  // posicao no programa original (-1 se inserido)
    public final Tipo tipo;

    public int endereco; // preenchidos no final da correcao
    public int palavra;

    public ItemPrograma(Instrucao original, int indiceOriginal, Tipo tipo) {
        this.original = original;
        this.indiceOriginal = indiceOriginal;
        this.tipo = tipo;
    }

    public boolean ehNopInserido() {
        return original == null;
    }

    public int[] lidos() {
        return original == null ? new int[0] : original.registradoresLidos();
    }

    public int escrito() {
        return original == null ? Instrucao.NENHUM : original.registradorEscrito();
    }

    public boolean ehControle() {
        return original != null && original.ehControle();
    }

    public boolean ehSaltoIncondicional() {
        return original != null && original.ehSaltoIncondicional();
    }

    /* texto da coluna "marcacao" da listagem */
    public String marcacao() {
        switch (tipo) {
            case NOP_DADOS: return "<< NOP inserido (conflito de dados)";
            case NOP_SLOT:  return "<< NOP inserido (slot de atraso)";
            case MOVIDA:    return "<< instrucao movida (era 0x" + String.format("%08X", original.endereco) + ")";
            default:        return "";
        }
    }
}
