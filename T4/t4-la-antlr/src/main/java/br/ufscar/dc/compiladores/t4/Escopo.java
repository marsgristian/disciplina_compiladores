package br.ufscar.dc.compiladores.t4;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Escopo léxico encadeado.
 *
 * A checagem de redeclaração usa apenas o escopo atual. A busca de uso de
 * identificadores percorre o escopo atual e seus ancestrais.
 */
public class Escopo {
    private final Escopo pai;
    private final Map<String, Simbolo> simbolos = new LinkedHashMap<>();

    public Escopo(Escopo pai) {
        this.pai = pai;
    }

    public Escopo getPai() {
        return pai;
    }

    public boolean contemLocal(String nome) {
        return simbolos.containsKey(nome);
    }

    public void inserir(Simbolo simbolo) {
        simbolos.put(simbolo.nome, simbolo);
    }

    public Simbolo buscar(String nome) {
        Simbolo local = simbolos.get(nome);

        if (local != null) {
            return local;
        }

        if (pai != null) {
            return pai.buscar(nome);
        }

        return null;
    }
}
