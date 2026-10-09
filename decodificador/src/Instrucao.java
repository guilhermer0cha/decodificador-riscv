//instrucao guarda tudo que o programa precisa saber sobre uma instrucao

public class Instrucao {

    //valor da instrucao nop = addi zero, zero, 0
    public static final int CODIGO_NOP = 0x00000013;

    //-1 -> "nao escreve em nenhum registrador"
    public static final int NENHUM = -1;

    //dados que vieram da m1
    public final int endereco; //pc da instrucao
    public final int valor; //a palavra de 32 bits
    public final String formato; //"R", "I", "S", "B", "U", "J" ou "invalido"
    public final String mnemonico; //"add", "lw", "bne" etc (null se inválida)
    public final boolean valida; //false se o opcode/funct nao for RV32I conhecido
    public final int imediato; //imediato ja com sinal (0 se o formato nao tem
    public final String assembly; //texto legivel, ex: "add t1, t0, s3"

    //dados m2
    private final int[] registradoresLidos; // sem repetidos e SEM o zero
    private final int registradorEscrito;   // NENHUM se não escreve (ou se for o zero)

    private Instrucao(int endereco, int valor, String formato, String mnemonico,
                      boolean valida, int imediato, String assembly,
                      int[] lidos, int escrito) {
        this.endereco = endereco;
        this.valor = valor;
        this.formato = formato;
        this.mnemonico = mnemonico;
        this.valida = valida;
        this.imediato = imediato;
        this.assembly = assembly;
        this.registradoresLidos = limparLidos(lidos);
        // premissa 5: escrever no zero não faz nada, então tratamos como "não escreve"
        this.registradorEscrito = (escrito == 0) ? NENHUM : escrito;
    }

    //cria uma instrucao a partir do que a m1 ja lia (instrucaolida),
    //reaproveitando o bitseparator e o formatdetector

    public static Instrucao decodificar(InputParse.Instrucaolida lida,
                                        BitSeparator ex, FormatDetector det) {
        int v = lida.valor;
        String formato = det.formato(v);

        // instrução inválida: não lê nem escreve nada, então nunca gera conflito
        if (!det.isValida(v)) {
            return new Instrucao(lida.endereco, v, formato, null, false, 0,
                    "inválida", new int[0], NENHUM);
        }

        String mnemonico = det.mnemonico(v);
        int[] lidos;
        int escrito;
        int imediato;

        switch (formato) {
            case "R":
                lidos = new int[]{ex.rs1(v), ex.rs2(v)};
                escrito = ex.rd(v);
                imediato = 0;
                break;
            case "I":
                lidos = new int[]{ex.rs1(v)};
                escrito = ex.rd(v);
                imediato = ex.immI(v);
                break;
            case "S":
                lidos = new int[]{ex.rs1(v), ex.rs2(v)};
                escrito = NENHUM;
                imediato = ex.immS(v);
                break;
            case "B":
                lidos = new int[]{ex.rs1(v), ex.rs2(v)};
                escrito = NENHUM;
                imediato = ex.immB(v);
                break;
            case "U":
                lidos = new int[0];
                escrito = ex.rd(v);
                imediato = ex.immU(v);
                break;
            case "J":
                lidos = new int[0];
                escrito = ex.rd(v);
                imediato = ex.immJ(v);
                break;
            default:
                lidos = new int[0];
                escrito = NENHUM;
                imediato = 0;
        }

        // o nop é addi zero,zero,0: a M1 imprimiria "addi zero, zero, 0".
        // Aqui mostramos "nop" porque é assim que o enunciado pede.
        String assembly = (v == CODIGO_NOP)
                ? "nop"
                : Output.montarAssembly(lida.endereco, v, formato, mnemonico, ex);

        return new Instrucao(lida.endereco, v, formato, mnemonico, true,
                imediato, assembly, lidos, escrito);
    }

    //remove registradores repetidos e o zero da lista de lidos.

    private static int[] limparLidos(int[] bruto) {
        int[] temp = new int[bruto.length];
        int qtd = 0;
        for (int reg : bruto) {
            if (reg == 0) continue;
            boolean jaTem = false;
            for (int i = 0; i < qtd; i++) {
                if (temp[i] == reg) jaTem = true;
            }
            if (!jaTem) temp[qtd++] = reg;
        }
        int[] resultado = new int[qtd];
        System.arraycopy(temp, 0, resultado, 0, qtd);
        return resultado;
    }

    //consultas (usadas pelos detectores
    //cópia do vetor de registradores lidos (cópia para ninguém alterar sem querer)
    public int[] registradoresLidos() {
        return registradoresLidos.clone();
    }

    public int registradorEscrito() {
        return registradorEscrito;
    }

    public boolean escreveRegistrador() {
        return registradorEscrito != NENHUM;
    }

    //desvio condicional (beq, bne, blt, bge, bltu, bgeu): formato B
    public boolean ehDesvioCondicional() {
        return valida && formato.equals("B");
    }

    //salto incondicional: jal e jalr. Depois deles o fluxo NUNCA cai na próxima instrução
    public boolean ehSaltoIncondicional() {
        return valida && (mnemonico.equals("jal") || mnemonico.equals("jalr"));
    }

    //qualquer instrução que muda o fluxo do programa
    public boolean ehControle() {
        return ehDesvioCondicional() || ehSaltoIncondicional();
    }

    public boolean temSlotDeAtraso() {
        return ehControle();
    }

    public boolean temAlvoConhecido() {
        return ehDesvioCondicional() || (valida && mnemonico.equals("jal"));
    }

    //destino absoluto de um desvio/jal. Só chame se temAlvoConhecido() for true
    public int enderecoAlvo() {
        return endereco + imediato;
    }

    //texto no formato "0x00400008 add t1, t0, s3"
    @Override
    public String toString() {
        return String.format("0x%08X %s", endereco, assembly);
    }
}

