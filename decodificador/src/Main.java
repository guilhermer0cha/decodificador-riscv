void main(String[] args) {
    InputParse parser = new InputParse();
    BitSeparator extractor = new BitSeparator();

    // converte hexadecimal para inteiro
    int instruction = parser.parse("0x100002b7");

    System.out.println(instruction);

    // converte o inteiro para a String em Binário de 32 bits
    System.out.println(parser.toBinary(instruction));

    // retira somente os bits do opcode e converte para binário 32 bits
    System.out.println(parser.toBinary(extractor.opcode(instruction)));
}