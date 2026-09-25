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
          "0xFE628CE3"  //beq t0, t1, -8 (tipo B)
        };

        for(String textoInstrucao : exemplos){
            int instrucao = parser.parse(textoInstrucao);

            System.out.println("palavra original: " + textoInstrucao);
            System.out.println("binario (32 bits): " + parser.toBinary(instrucao));
            System.out.println("opcode: " + parser.toBinary(extractor.opcode(instrucao))+ " (decimal " + extractor.opcode(instrucao) + ")");
            System.out.println("rd = " + extractor.rd(instrucao));
            System.out.println("funct3 = " + extractor.funct3(instrucao));
            System.out.println("rs1 = " + extractor.rs1(instrucao));
            System.out.println("rs2 = " + extractor.rs2(instrucao));
            System.out.println("funct7 = " + extractor.funct7(instrucao));
            System.out.println("------------------------------------------------");
        }

    }
}