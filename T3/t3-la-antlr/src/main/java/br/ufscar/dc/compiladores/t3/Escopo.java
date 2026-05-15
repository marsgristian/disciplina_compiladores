package br.ufscar.dc.compiladores.t3;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Um escopo da linguagem LA.
 *
 * Cada escopo possui uma referência para o escopo pai. A busca de
 * identificadores percorre essa cadeia; a checagem de redeclaração consulta
 * apenas o escopo atual.
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

    public Simbolo buscarLocal(String nome) {
        return simbolos.get(nome);
    }
}
