public class BitSeparator {
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
        return extract(instruction, 10, 5);
    }

    public int funct7(int instruction) {
        return extract(instruction, 25, 7);
    }
}
