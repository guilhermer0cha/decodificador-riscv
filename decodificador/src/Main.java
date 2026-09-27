public class Main {
    public static void main(String[] args){
        InputParse parser = new InputParse();
        BitSeparator extractor = new BitSeparator();

        //tirei os exemplos do enunciado do trabalho(um de cada formato)
        //(R, I, S e B) pra testar se a extração bate
        String[] exemplos = {
          "0x00500413", //addi s0, zero, 5 (tipo I)
          "0x00C58633", //add a2, a1, a2 (tipo R)
          "0x0064A423", //sw t1, 8(s1) (tipo S)
          "0xFE628CE3", //beq t0, t1, -8 (tipo B)
          "0x000002B7", // lui x5, 0 (tipo U
          "0x00000297", // auipc x5, 0 (tipo U
          "0x0000006F"  // jal x0, 0 (tipo J
        };

        for(String textoInstrucao : exemplos){
            int instrucao = parser.parse(textoInstrucao);
            FormatDetector detector = new FormatDetector(extractor);
            String formato = detector.formato(instrucao);
            String mnemonico = detector.mnemonico(instrucao);

            System.out.println("------------------------------------------------");
            System.out.println("original: " + textoInstrucao);
            System.out.println("binário: " + parser.toBinary(instrucao));

            System.out.println("Opcode: "
                    + parser.toBinary(extractor.opcode(instrucao))
                    + " (decimal "
                    + extractor.opcode(instrucao)
                    + ")");

            System.out.println("formato: " + formato);
            System.out.println("Mnemônico: " + mnemonico);
            System.out.println("válida: " + detector.isValida(instrucao));

            System.out.println("---campos---");

            switch (formato) {

                case "R":
                    System.out.println("rd = " + extractor.rd(instrucao));
                    System.out.println("rs1 = " + extractor.rs1(instrucao));
                    System.out.println("rs2 = " + extractor.rs2(instrucao));
                    System.out.println("funct3 = " + extractor.funct3(instrucao));
                    System.out.println("funct7 = " + extractor.funct7(instrucao));
                    break;

                case "I":
                    System.out.println("rd = " + extractor.rd(instrucao));
                    System.out.println("rs1 = " + extractor.rs1(instrucao));
                    System.out.println("funct3 = " + extractor.funct3(instrucao));
                    break;

                case "S":
                    System.out.println("rs1 = " + extractor.rs1(instrucao));
                    System.out.println("rs2 = " + extractor.rs2(instrucao));
                    System.out.println("funct3 = " + extractor.funct3(instrucao));
                    break;

                case "B":
                    System.out.println("rs1 = " + extractor.rs1(instrucao));
                    System.out.println("rs2 = " + extractor.rs2(instrucao));
                    System.out.println("funct3 = " + extractor.funct3(instrucao));
                    break;

                case "U":
                    System.out.println("rd = " + extractor.rd(instrucao));
                    break;

                case "J":
                    System.out.println("rd = " + extractor.rd(instrucao));
                    break;
            }

            System.out.println("---imediatos---");

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
            System.out.println("------------------------------------------------");
        }
    }
}