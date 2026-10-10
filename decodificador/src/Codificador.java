/*faz o caminho inverso do BitSeparator para os formatos B e J.
 *dado o valor original de 32 bits e um deslocamento NOVO, devolve a palavra com
 *os campos do imediato reescritos (todo o resto: opcode, registradores, funct3 - fica igual).
*/
public class Codificador {

    public static final int OPCODE_BRANCH = 0x63; //beq, bne, blt, bge, bltu, bgeu
    public static final int OPCODE_JAL = 0x6F;

    /*true se a palavra tem um imediato de deslocamento que precisa ser recalculado (B ou jal) */
    public static boolean ehDesvioOuJal(int palavra) {
        int op = palavra & 0x7F;
        return op == OPCODE_BRANCH || op == OPCODE_JAL;
    }

    /*reescreve o imediato de um desvio (B) ou jal (J).
     *deslocamento = enderecoAlvo - enderecoDaInstrucao (em bytes, sempre par)
     */
    public static int recodificar(int palavra, int deslocamento) {
        int op = palavra & 0x7F;
        if ((deslocamento & 1) != 0) {
            throw new IllegalArgumentException("deslocamento impar: " + deslocamento);
        }

        if (op == OPCODE_BRANCH) {
            //mm[12|10:5] vai nos bits 31:25 e imm[4:1|11] nos bits 11:7
            if (deslocamento < -4096 || deslocamento > 4094) {
                throw new IllegalArgumentException(
                        "deslocamento " + deslocamento + " fora do alcance de um desvio condicional (+-4 KiB)");
            }
            int mantem = palavra & 0x01FFF07F; // opcode, funct3, rs1, rs2
            int imm = ((deslocamento >> 12) & 0x1) << 31
                    | ((deslocamento >> 5) & 0x3F) << 25
                    | ((deslocamento >> 1) & 0xF) << 8
                    | ((deslocamento >> 11) & 0x1) << 7;
            return mantem | imm;
        }

        if (op == OPCODE_JAL) {
            //imm[20|10:1|11|19:12] vai nos bits 31:12
            if (deslocamento < -(1 << 20) || deslocamento > (1 << 20) - 2) {
                throw new IllegalArgumentException(
                        "deslocamento " + deslocamento + " fora do alcance do jal (+-1 MiB)");
            }
            int mantem = palavra & 0xFFF; // opcode e rd
            int imm = ((deslocamento >> 20) & 0x1) << 31
                    | ((deslocamento >> 1) & 0x3FF) << 21
                    | ((deslocamento >> 11) & 0x1) << 20
                    | ((deslocamento >> 12) & 0xFF) << 12;
            return mantem | imm;
        }

        throw new IllegalArgumentException("instrucao nao e desvio nem jal");
    }
}
