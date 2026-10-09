//serve p guardar os indices do programa (posicao 0, 1, 2...)

public class BlocoBasico {

    public final int numero;
    public final int indiceInicio;
    public final int indiceFim;

    public BlocoBasico(int numero, int indiceInicio, int indiceFim){
        this.numero = numero;
        this.indiceInicio =  indiceInicio;
        this.indiceFim = indiceFim;
    }

    public int tamanho(){
        return indiceFim - indiceInicio + 1;
    }
}
