public class InputParse {
    public int parse(String input) {
        input = input.trim();

        // remove os 2 primeiros digitos '0x' e converte texto hexadecimal (base 16)
        // para um número do tipo long e "wrappa" pra int
        if (input.startsWith("0x")) {
            return (int) Long.parseLong(input.substring(2), 16);
        }
        // caso nao comece com '0x' assume que a instrução ja está em binários
        return (int) Long.parseLong(input, 2);
    }


    // converte o número decimal para binário adicionando 0s a esquerda para garantir que tenha 32 digitos
    public String toBinary(int value) {
        return String.format(
                "%32s",
                Integer.toBinaryString(value)
        ).replace(' ', '0');
    }
}
