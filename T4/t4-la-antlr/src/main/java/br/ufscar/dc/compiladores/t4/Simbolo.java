package br.ufscar.dc.compiladores.t4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entrada da tabela de símbolos.
 *
 * Para o T4, procedimentos e funções precisam armazenar a assinatura dos
 * parâmetros formais para validação das chamadas.
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
    public final List<TipoLA> parametros;

    public Simbolo(String nome, Categoria categoria, TipoLA tipo) {
        this(nome, categoria, tipo, Collections.emptyList());
    }

    public Simbolo(String nome, Categoria categoria, TipoLA tipo, List<TipoLA> parametros) {
        this.nome = nome;
        this.categoria = categoria;
        this.tipo = tipo;
        this.parametros = Collections.unmodifiableList(new ArrayList<>(parametros));
    }

    public boolean isRotina() {
        return categoria == Categoria.PROCEDIMENTO || categoria == Categoria.FUNCAO;
    }
}
