package br.ufscar.dc.compiladores.t5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Entrada da tabela de símbolos usada pelo gerador. */
public class CSimbolo {
    public enum Categoria { VARIAVEL, CONSTANTE, TIPO, PROCEDIMENTO, FUNCAO, PARAMETRO }
    public final String nome;
    public final Categoria categoria;
    public final CTipo tipo;
    public final boolean porReferencia;
    public final List<ParametroFormal> parametros;

    public CSimbolo(String nome, Categoria categoria, CTipo tipo) { this(nome, categoria, tipo, false, Collections.emptyList()); }
    public CSimbolo(String nome, Categoria categoria, CTipo tipo, boolean porReferencia) { this(nome, categoria, tipo, porReferencia, Collections.emptyList()); }
    public CSimbolo(String nome, Categoria categoria, CTipo tipo, boolean porReferencia, List<ParametroFormal> parametros) {
        this.nome = nome; this.categoria = categoria; this.tipo = tipo; this.porReferencia = porReferencia;
        this.parametros = Collections.unmodifiableList(new ArrayList<>(parametros));
    }
    public boolean isRotina() { return categoria == Categoria.PROCEDIMENTO || categoria == Categoria.FUNCAO; }
}
