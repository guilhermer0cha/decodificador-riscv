public class Output {
    public static void imprimirInstrucao(InputParse.Instrucaolida instrucao,
                                         BitSeparator extractor,
                                         FormatDetector detector) {

        int valor = instrucao.valor;
        String formato = detector.formato(valor);
        String mnemonico = detector.mnemonico(valor);

        System.out.println("------------------------------------------------");
        System.out.println("endereço: 0x" + String.format("%08X", instrucao.endereco));
        System.out.println("palavra original: " + instrucao.linhaOriginal);
        System.out.println("formato: " + formato);
        System.out.println("mnemônico: " + mnemonico);

        if (!detector.isValida(valor)) {
            System.out.println("válida: false");
            System.out.println("assembly: inválida");
            System.out.println("------------------------------------------------");
            return;
        }

        System.out.println("válida: true");
        System.out.println("---campos---");

        imprimirCampos(valor, formato, extractor);

        System.out.println("---imediatos---");
        imprimirImediato(valor, formato, extractor);

        String assembly = montarAssembly(
                instrucao.endereco, valor, formato, mnemonico, extractor);

        System.out.println("assembly: " + assembly);

        if (formato.equals("B") || formato.equals("J")) {
            int imediato = formato.equals("B")
                    ? extractor.immB(valor)
                    : extractor.immJ(valor);

            int destino = instrucao.endereco + imediato;

            System.out.println("destino absoluto: 0x"
                    + String.format("%08X", destino));
        }

        String pseudo = pseudoInstrucao(
                instrucao.endereco, valor, mnemonico, extractor);

        if (pseudo != null) {
            System.out.println("pseudo-instrução: " + pseudo);
        }

        System.out.println("------------------------------------------------");
    }

    private static void imprimirCampos(int instrucao,
                                       String formato,
                                       BitSeparator extractor) {

        switch (formato) {
            case "R":
                System.out.println("rd = " + extractor.rd(instrucao)
                        + " (" + nomeABI(extractor.rd(instrucao)) + ")");
                System.out.println("rs1 = " + extractor.rs1(instrucao)
                        + " (" + nomeABI(extractor.rs1(instrucao)) + ")");
                System.out.println("rs2 = " + extractor.rs2(instrucao)
                        + " (" + nomeABI(extractor.rs2(instrucao)) + ")");
                System.out.println("funct3 = " + extractor.funct3(instrucao));
                System.out.println("funct7 = " + extractor.funct7(instrucao));
                break;

            case "I":
                System.out.println("rd = " + extractor.rd(instrucao)
                        + " (" + nomeABI(extractor.rd(instrucao)) + ")");
                System.out.println("rs1 = " + extractor.rs1(instrucao)
                        + " (" + nomeABI(extractor.rs1(instrucao)) + ")");
                System.out.println("funct3 = " + extractor.funct3(instrucao));
                break;

            case "S":
                System.out.println("rs1 = " + extractor.rs1(instrucao)
                        + " (" + nomeABI(extractor.rs1(instrucao)) + ")");
                System.out.println("rs2 = " + extractor.rs2(instrucao)
                        + " (" + nomeABI(extractor.rs2(instrucao)) + ")");
                System.out.println("funct3 = " + extractor.funct3(instrucao));
                break;

            case "B":
                System.out.println("rs1 = " + extractor.rs1(instrucao)
                        + " (" + nomeABI(extractor.rs1(instrucao)) + ")");
                System.out.println("rs2 = " + extractor.rs2(instrucao)
                        + " (" + nomeABI(extractor.rs2(instrucao)) + ")");
                System.out.println("funct3 = " + extractor.funct3(instrucao));
                break;

            case "U":
            case "J":
                System.out.println("rd = " + extractor.rd(instrucao)
                        + " (" + nomeABI(extractor.rd(instrucao)) + ")");
                break;
        }
    }

    private static void imprimirImediato(int instrucao,
                                         String formato,
                                         BitSeparator extractor) {

        switch (formato) {
            case "I":
                System.out.println("immI = " + extractor.immI(instrucao));
                break;

            case "S":
                System.out.println("immS = " + extractor.immS(instrucao));
                break;

            case "B":
                System.out.println("immB = " + extractor.immB(instrucao));
                break;

            case "U":
                System.out.println("immU = " + extractor.immU(instrucao));
                break;

            case "J":
                System.out.println("immJ = " + extractor.immJ(instrucao));
                break;
        }
    }

    private static String montarAssembly(int endereco,
                                         int instrucao,
                                         String formato,
                                         String mnemonico,
                                         BitSeparator extractor) {

        switch (formato) {
            case "R":
                return mnemonico + " "
                        + nomeABI(extractor.rd(instrucao)) + ", "
                        + nomeABI(extractor.rs1(instrucao)) + ", "
                        + nomeABI(extractor.rs2(instrucao));

            case "I":
                if (mnemonico.equals("lb")
                        || mnemonico.equals("lh")
                        || mnemonico.equals("lw")
                        || mnemonico.equals("lbu")
                        || mnemonico.equals("lhu")
                        || mnemonico.equals("jalr")) {

                    return mnemonico + " "
                            + nomeABI(extractor.rd(instrucao)) + ", "
                            + extractor.immI(instrucao) + "("
                            + nomeABI(extractor.rs1(instrucao)) + ")";
                }

                return mnemonico + " "
                        + nomeABI(extractor.rd(instrucao)) + ", "
                        + nomeABI(extractor.rs1(instrucao)) + ", "
                        + extractor.immI(instrucao);

            case "S":
                return mnemonico + " "
                        + nomeABI(extractor.rs2(instrucao)) + ", "
                        + extractor.immS(instrucao) + "("
                        + nomeABI(extractor.rs1(instrucao)) + ")";

            case "B":
                return mnemonico + " "
                        + nomeABI(extractor.rs1(instrucao)) + ", "
                        + nomeABI(extractor.rs2(instrucao)) + ", "
                        + String.format("0x%08X",
                        endereco + extractor.immB(instrucao));

            case "U":
                // lui/auipc recebem os 20 bits superiores do imediato
                return mnemonico + " "
                        + nomeABI(extractor.rd(instrucao)) + ", "
                        + extractor.extract(instrucao, 12, 20);

            case "J":
                return mnemonico + " "
                        + nomeABI(extractor.rd(instrucao)) + ", "
                        + String.format("0x%08X",
                        endereco + extractor.immJ(instrucao));

            default:
                return "inválida";
        }
    }

    private static String pseudoInstrucao(int endereco,
                                          int instrucao,
                                          String mnemonico,
                                          BitSeparator extractor) {

        // nop = addi x0, x0, 0
        if (mnemonico.equals("addi")
                && extractor.rd(instrucao) == 0
                && extractor.rs1(instrucao) == 0
                && extractor.immI(instrucao) == 0) {
            return "nop";
        }

        // ret = jalr x0, 0(x1)
        if (mnemonico.equals("jalr")
                && extractor.rd(instrucao) == 0
                && extractor.rs1(instrucao) == 1
                && extractor.immI(instrucao) == 0) {
            return "ret";
        }

        // mv rd, rs1 = addi rd, rs1, 0
        if (mnemonico.equals("addi")
                && extractor.rd(instrucao) != 0
                && extractor.rs1(instrucao) != 0
                && extractor.immI(instrucao) == 0) {
            return "mv " + nomeABI(extractor.rd(instrucao))
                    + ", " + nomeABI(extractor.rs1(instrucao));
        }

        // li rd, imm = addi rd, x0, imm
        if (mnemonico.equals("addi")
                && extractor.rd(instrucao) != 0
                && extractor.rs1(instrucao) == 0) {
            return "li " + nomeABI(extractor.rd(instrucao))
                    + ", " + extractor.immI(instrucao);
        }

        // j destino = jal x0, destino
        if (mnemonico.equals("jal") && extractor.rd(instrucao) == 0) {
            return "j " + String.format("0x%08X",
                    endereco + extractor.immJ(instrucao));
        }

        // jr rs1 = jalr x0, 0(rs1)
        if (mnemonico.equals("jalr")
                && extractor.rd(instrucao) == 0
                && extractor.immI(instrucao) == 0) {
            return "jr " + nomeABI(extractor.rs1(instrucao));
        }

        return null;
    }

    private static String nomeABI(int registrador) {
        switch (registrador) {
            case 0:  return "zero";
            case 1:  return "ra";
            case 2:  return "sp";
            case 3:  return "gp";
            case 4:  return "tp";
            case 5:  return "t0";
            case 6:  return "t1";
            case 7:  return "t2";
            case 8:  return "s0";
            case 9:  return "s1";
            case 10: return "a0";
            case 11: return "a1";
            case 12: return "a2";
            case 13: return "a3";
            case 14: return "a4";
            case 15: return "a5";
            case 16: return "a6";
            case 17: return "a7";
            case 18: return "s2";
            case 19: return "s3";
            case 20: return "s4";
            case 21: return "s5";
            case 22: return "s6";
            case 23: return "s7";
            case 24: return "s8";
            case 25: return "s9";
            case 26: return "s10";
            case 27: return "s11";
            case 28: return "t3";
            case 29: return "t4";
            case 30: return "t5";
            case 31: return "t6";
            default: return "x" + registrador;
        }
    }
}
