package br.ufscar.dc.compiladores.t5;

/** Assinatura de parâmetro formal para geração de protótipos e chamadas. */
public class ParametroFormal {
    public final String nome;
    public final CTipo tipo;
    public final boolean porReferencia;
    public ParametroFormal(String nome, CTipo tipo, boolean porReferencia) {
        this.nome = nome; this.tipo = tipo; this.porReferencia = porReferencia;
    }
}
