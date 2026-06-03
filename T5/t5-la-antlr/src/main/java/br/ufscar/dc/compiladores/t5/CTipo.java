package br.ufscar.dc.compiladores.t5;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Tipo interno usado pelo gerador de código C. */
public class CTipo {
    public enum Kind { INTEIRO, REAL, LITERAL, LOGICO, REGISTRO, PONTEIRO, INVALIDO }

    public static final CTipo INTEIRO = new CTipo(Kind.INTEIRO);
    public static final CTipo REAL = new CTipo(Kind.REAL);
    public static final CTipo LITERAL = new CTipo(Kind.LITERAL);
    public static final CTipo LOGICO = new CTipo(Kind.LOGICO);
    public static final CTipo INVALIDO = new CTipo(Kind.INVALIDO);

    public final Kind kind;
    public final String nomeRegistro;
    public final CTipo apontado;
    public final Map<String, CTipo> campos;

    private CTipo(Kind kind) { this(kind, null, null, new LinkedHashMap<>()); }
    private CTipo(Kind kind, String nomeRegistro, CTipo apontado, Map<String, CTipo> campos) {
        this.kind = kind;
        this.nomeRegistro = nomeRegistro;
        this.apontado = apontado;
        this.campos = campos == null ? new LinkedHashMap<>() : campos;
    }

    public static CTipo registro(String nome, Map<String, CTipo> campos) { return new CTipo(Kind.REGISTRO, nome, null, campos); }
    public static CTipo ponteiro(CTipo base) { return new CTipo(Kind.PONTEIRO, null, base, new LinkedHashMap<>()); }
    public boolean isNumerico() { return kind == Kind.INTEIRO || kind == Kind.REAL; }
    public CTipo deref() { return kind == Kind.PONTEIRO && apontado != null ? apontado : INVALIDO; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof CTipo)) return false;
        CTipo t = (CTipo) o;
        return kind == t.kind && Objects.equals(nomeRegistro, t.nomeRegistro) && Objects.equals(apontado, t.apontado);
    }
    @Override public int hashCode() { return Objects.hash(kind, nomeRegistro, apontado); }
}
