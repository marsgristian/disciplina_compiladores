package br.ufscar.dc.compiladores.t4;

/**
 * Representa um parâmetro formal durante a coleta da assinatura de uma função
 * ou procedimento.
 */
public class ParametroInfo {
    public final String nome;
    public final int linha;
    public final TipoLA tipo;

    public ParametroInfo(String nome, int linha, TipoLA tipo) {
        this.nome = nome;
        this.linha = linha;
        this.tipo = tipo;
    }
}
