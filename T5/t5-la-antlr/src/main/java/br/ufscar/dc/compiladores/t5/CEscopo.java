package br.ufscar.dc.compiladores.t5;

import java.util.LinkedHashMap;
import java.util.Map;

/** Escopo léxico simples com busca encadeada. */
public class CEscopo {
    private final CEscopo pai;
    private final Map<String, CSimbolo> simbolos = new LinkedHashMap<>();
    public CEscopo(CEscopo pai) { this.pai = pai; }
    public CEscopo getPai() { return pai; }
    public void inserir(CSimbolo s) { simbolos.put(s.nome, s); }
    public boolean contemLocal(String nome) { return simbolos.containsKey(nome); }
    public CSimbolo buscar(String nome) {
        CSimbolo s = simbolos.get(nome);
        if (s != null) return s;
        return pai == null ? null : pai.buscar(nome);
    }
}
