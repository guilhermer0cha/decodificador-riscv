public class FormatDetector {
    //cada upcode diz qual é o formato geral
    //valores tirados da tabela de upcodes guia prático do RISC-V
    private static final int opcode_tipo_R = 0b0110011;

    private static final int opcode_tipo_I_aritmeticas = 0b0010011;

    private static final int opcode_tipo_I_load  = 0b0000011;

    private static final int opcode_tipo_I_jalr  = 0b1100111;

    private static final int opcode_tipo_S = 0b0100011;

    private static final int opcode_tipo_B = 0b1100011;

    private static final int opcode_tipo_U_lui = 0b0110111;

    private static final int opcode_tipo_U_auipc = 0b0010111;

    private static final int opcode_tipo_J = 0b1101111;


    private final BitSeparator extrator;

    public FormatDetector(BitSeparator extrator) {
        this.extrator = extrator;
    }

    public String formato(int instrucao) {
        int opcode = extrator.opcode(instrucao);

        switch (opcode) {
            case opcode_tipo_R:
                return "R";

            case opcode_tipo_I_aritmeticas:
            case opcode_tipo_I_load:
            case opcode_tipo_I_jalr:
                return "I";

            case opcode_tipo_S:
                return "S";

            case opcode_tipo_B:
                return "B";

            case opcode_tipo_U_lui:
            case opcode_tipo_U_auipc:
                return "U";

            case opcode_tipo_J:
                return "J";

            default:
                return "invalido!";
        }
    }

    public String mnemonico(int instrucao) {
        int opcode = extrator.opcode(instrucao);
        int funct3 = extrator.funct3(instrucao);
        int funct7 = extrator.funct7(instrucao);

        switch (opcode) {
            case opcode_tipo_R:
                return mnemonicoTipoR(funct3, funct7);

            case opcode_tipo_I_aritmeticas:
                return mnemonicoTipoIAritmetico(funct3, funct7);

            case opcode_tipo_I_load:
                return mnemonicoTipoILoad(funct3);

            case opcode_tipo_I_jalr:
                return (funct3 == 0b000) ? "jalr" : null;

            case opcode_tipo_S:
                return mnemonicoTipoS(funct3);

            case opcode_tipo_B:
                return mnemonicoTipoB(funct3);

            case opcode_tipo_U_lui:
                return "lui";

            case opcode_tipo_U_auipc:
                return "auipc";

            case opcode_tipo_J:
                return "jal";

            default:
                return null;
        }
    }

    //tipo R: add, sub, sll, slt, sltu, xor, srl, sra, or, and 
    // opcode é sempre o mesmo (0110011); o que muda é funct3 e, em alguns
    private String mnemonicoTipoR(int funct3, int funct7) {
        switch (funct3) {
            case 0b000:
                if (funct7 == 0b0000000) return "add";
                if (funct7 == 0b0100000) return "sub";
                return null;

            case 0b001:
                return (funct7 == 0b0000000) ? "sll" : null;

            case 0b010:
                return (funct7 == 0b0000000) ? "slt" : null;

            case 0b011:
                return (funct7 == 0b0000000) ? "sltu" : null;

            case 0b100:
                return (funct7 == 0b0000000) ? "xor" : null;

            case 0b101:
                if (funct7 == 0b0000000) return "srl";
                if (funct7 == 0b0100000) return "sra";
                return null;

            case 0b110:
                return (funct7 == 0b0000000) ? "or" : null;

            case 0b111:
                return (funct7 == 0b0000000) ? "and" : null;

            default:
                return null;
        }
    }

    //tipo I aritmético/lógico: addi, slti, sltiu, xori, ori, andi, slli, srli, srai
    private String mnemonicoTipoIAritmetico(int funct3, int funct7) {
        switch (funct3) {
            case 0b000: return "addi";
            case 0b010: return "slti";
            case 0b011: return "sltiu";
            case 0b100: return "xori";
            case 0b110: return "ori";
            case 0b111: return "andi";

            case 0b001:
                return (funct7 == 0b0000000) ? "slli" : null;

            case 0b101:
                if (funct7 == 0b0000000) return "srli";
                if (funct7 == 0b0100000) return "srai";
                return null;

            default:
                return null;
        }
    }

    //tipo I de leitura de memória: lb, lh, lw, lbu, lhu 
    private String mnemonicoTipoILoad(int funct3) {
        switch (funct3) {
            case 0b000: return "lb";
            case 0b001: return "lh";
            case 0b010: return "lw";
            case 0b100: return "lbu";
            case 0b101: return "lhu";
            default: return null;
        }
    }


    //tipo S (escrita na memória): sb, sh, sw 
    private String mnemonicoTipoS(int funct3) {
        switch (funct3) {
            case 0b000: return "sb";
            case 0b001: return "sh";
            case 0b010: return "sw";
            default: return null;
        }
    }

    //tipo B (desvios condicionais): beq, bne, blt, bge, bltu, bgeu 
    private String mnemonicoTipoB(int funct3) {
        switch (funct3) {
            case 0b000: return "beq";
            case 0b001: return "bne";
            case 0b100: return "blt";
            case 0b101: return "bge";
            case 0b110: return "bltu";
            case 0b111: return "bgeu";
            default: return null;
        }
    }

    //"reportar como inválida qualquer palavra que não corresponda a uma instrução conhecida, informando o
    //endereço em que ela ocorre, sem interromper o processamento do restante do arquivo."
    public boolean isValida(int instrucao) {
        return mnemonico(instrucao) != null;
    }
}