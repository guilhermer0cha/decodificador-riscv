import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class InputParse {

    //guarda cada instrucao ja lida no arquivo, junto com o endereco(pc) dela

    public static class Instrucaolida{
        public final int endereco; //o pc dessa instrução
        public final int valor; //instrução ja convertida p numero de 32 bits
        public final String linhaOriginal; //texto original da linha (p mostrar na saida)

        public Instrucaolida(int endereco, int valor, String linhaOriginal){
            this.endereco = endereco;
            this.valor = valor;
            this.linhaOriginal = linhaOriginal;
        }
    }

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

    //verifica se uma llinha deve ser ignorada por estar em branco ou por ser um comentario
    //(enunciado pede p ignorar antes de tentar decodificar linha por linha)
    public boolean isLinhaIgnoravel(String linha){
        String linhaSemEspacos = linha.trim();

        boolean estaEmBranco = linhaSemEspacos.isEmpty();
        boolean eComentario = linhaSemEspacos.startsWith("#") || linhaSemEspacos.startsWith("//");

        return estaEmBranco || eComentario;
    }

    //le arquivo inteiro da memoria de instrucoes linha por linha e devolve a lista de instrucoes
    //ja decodificadas em numero, cada uma c seu endereço (pc)
    //aqui tambem faz o calculo do endereco --> a primeira instrucao recebe "enderecoBase"
    // --> cada instrucao seguinte fica 4 bytes a frente da anterior (32 bits = 4 bytes)
    public List<Instrucaolida> lerArquivo(String caminhoDoArquivo, int enderecoBase) throws IOException {
        //le todas as linhas do arquivo de uma vez e guarda em uma lista de texto
        List<String> todasAsLinhas = Files.readAllLines(Path.of(caminhoDoArquivo));

        List<Instrucaolida> instrucoes = new ArrayList<>();

        int endrecoAtual = enderecoBase;

        for(int i = 0; i < todasAsLinhas.size(); i++){
            String linha = todasAsLinhas.get(i);

            if(isLinhaIgnoravel(linha)){
                continue;
            }
            //tenta converter a linha p numero, se a linha estiver ilegivel (nem hex nem binario)
            //avisa no console mas nao para o programa
            try {
                int valor = parse(linha);
                instrucoes.add(new Instrucaolida(endrecoAtual, valor, linha.trim()));
                //cada instrucao ocupa 4 bytes, o pc da proxima instrucao é = enderecoAtual + 4
                endrecoAtual += 4;
            } catch (NumberFormatException erro){
                System.err.println("a linha " + (i + 1) + "(\"" + linha.trim() + "\") " + "não pôde ser convertida em número e foi ignorada.");
            }
        }
        //devolve as instrucoes validas
        return instrucoes;
    }

}