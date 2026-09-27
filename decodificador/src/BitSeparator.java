public class BitSeparator {
    //extrai uma quantidade de bits comecando em uma determinada posição
    public int extract(int instruction, int start, int length) {
        int mask = (1 << length) - 1;

        return (instruction >>> start) & mask;
    }

    public int opcode(int instruction) {
        return extract(instruction, 0, 7);
    }

    public int rd(int instruction) {
        return extract(instruction, 7, 5);
    }

    public int funct3(int instruction) {
        return extract(instruction, 12, 3);
    }

    public int rs1(int instruction) {
        return  extract(instruction, 15, 5);
    }

    public int rs2(int instruction) {
        return extract(instruction, 20, 5);
    }

    public int funct7(int instruction) {
        return extract(instruction, 25, 7);
    }

    //(parte do R3)
    
    public int estenderSinal(int valor, int quantidadeDeBits) {
        int deslocamento = 32 - quantidadeDeBits;
        return (valor << deslocamento) >> deslocamento;
    }


    //imediato tipo I (12 bits)
    public int immI(int instruction) {
        int bitsCrus = extract(instruction, 20, 12);
        return estenderSinal(bitsCrus, 12);
    }


    //imediato tipo S (12 bits)
    public int immS(int instruction) {
        int parteAlta = extract(instruction, 25, 7);
        int parteBaixa = extract(instruction, 7, 5);

        int bitsCrus = (parteAlta << 5) | parteBaixa;
        return estenderSinal(bitsCrus, 12);
    }


    //imediato tipo B (13 bits úteis, mas o bit 0 sempre é 0)
    public int immB(int instruction) {
        int bit12 = extract(instruction, 31, 1);
        int bit11 = extract(instruction, 7, 1);
        int bits10a5 = extract(instruction, 25, 6);
        int bits4a1 = extract(instruction, 8, 4);

        int bitsCrus =
                (bit12 << 12)
                        | (bit11 << 11)
                        | (bits10a5 << 5)
                        | (bits4a1 << 1);
        //o bit 0 já nasce em 0, não precisamos fazer nada com ele
        return estenderSinal(bitsCrus, 13);
    }

    //imediato tipo U (20 bits)
    public int immU(int instruction) {
        return instruction & 0xFFFFF000;
    }

    //imediato tipo J (21 bits úteis, mas o bit 0 sempre é 0)
    //  bit 0 do imediato -> sempre 0
    public int immJ(int instruction) {
        int bit20 = extract(instruction, 31, 1);
        int bits19a12 = extract(instruction, 12, 8);
        int bit11 = extract(instruction, 20, 1);
        int bits10a1 = extract(instruction, 21, 10);

        int bitsCrus =
                (bit20 << 20)
                        | (bits19a12 << 12)
                        | (bit11 << 11)
                        | (bits10a1 << 1);
        // o bit 0 já nasce em 0, não precisamos fazer nada com ele
        return estenderSinal(bitsCrus, 21);
    }
}