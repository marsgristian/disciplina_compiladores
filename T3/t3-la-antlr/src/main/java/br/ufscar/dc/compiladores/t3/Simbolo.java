package br.ufscar.dc.compiladores.t3;

/**
 * Entrada da tabela de símbolos.
 *
 * A categoria é mantida para permitir distinguir variável, constante, tipo,
 * procedimento e função nos trabalhos posteriores, mesmo que o T3 cobre apenas
 * uma parte da análise semântica.
 */
public class Simbolo {
    public enum Categoria {
        VARIAVEL,
        CONSTANTE,
        TIPO,
        PROCEDIMENTO,
        FUNCAO,
        PARAMETRO
    }

    public final String nome;
    public final Categoria categoria;
    public final TipoLA tipo;

    public Simbolo(String nome, Categoria categoria, TipoLA tipo) {
        this.nome = nome;
        this.categoria = categoria;
        this.tipo = tipo;
    }
}
