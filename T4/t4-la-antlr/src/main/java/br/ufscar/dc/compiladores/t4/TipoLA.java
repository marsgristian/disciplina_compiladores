package br.ufscar.dc.compiladores.t4;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Representação de tipos da LA.
 *
 * O T4 exige tipos básicos, ponteiros, registros nomeados e tipo inválido.
 * O tipo inválido permite continuar a análise após um erro semântico.
 */
public class TipoLA {
    public enum Kind {
        INTEIRO,
        REAL,
        LITERAL,
        LOGICO,
        REGISTRO,
        PONTEIRO,
        INVALIDO
    }

    public static final TipoLA INTEIRO = new TipoLA(Kind.INTEIRO);
    public static final TipoLA REAL = new TipoLA(Kind.REAL);
    public static final TipoLA LITERAL = new TipoLA(Kind.LITERAL);
    public static final TipoLA LOGICO = new TipoLA(Kind.LOGICO);
    public static final TipoLA INVALIDO = new TipoLA(Kind.INVALIDO);

    public final Kind kind;
    public final String nomeRegistro;
    public final TipoLA apontado;
    public final Map<String, TipoLA> campos;

    private TipoLA(Kind kind) {
        this(kind, null, null, new LinkedHashMap<>());
    }

    private TipoLA(Kind kind, String nomeRegistro, TipoLA apontado, Map<String, TipoLA> campos) {
        this.kind = kind;
        this.nomeRegistro = nomeRegistro;
        this.apontado = apontado;
        this.campos = campos == null ? new LinkedHashMap<>() : campos;
    }

    public static TipoLA registro(String nome, Map<String, TipoLA> campos) {
        return new TipoLA(Kind.REGISTRO, nome, null, campos);
    }

    public static TipoLA ponteiro(TipoLA apontado) {
        return new TipoLA(Kind.PONTEIRO, null, apontado, new LinkedHashMap<>());
    }

    public boolean isNumerico() {
        return kind == Kind.INTEIRO || kind == Kind.REAL;
    }

    public boolean isInvalido() {
        return kind == Kind.INVALIDO;
    }

    public TipoLA deref() {
        if (kind == Kind.PONTEIRO && apontado != null) {
            return apontado;
        }

        return INVALIDO;
    }

    /**
     * Compatibilidade de atribuição:
     * - inteiro/real são compatíveis entre si;
     * - literal com literal;
     * - logico com logico;
     * - ponteiro com ponteiro compatível;
     * - registro com registro de mesmo tipo nomeado.
     */
    public boolean aceitaAtribuicaoDe(TipoLA origem) {
        if (this.isInvalido() || origem == null || origem.isInvalido()) {
            return false;
        }

        if (this.isNumerico() && origem.isNumerico()) {
            return true;
        }

        if (this.kind == Kind.LITERAL && origem.kind == Kind.LITERAL) {
            return true;
        }

        if (this.kind == Kind.LOGICO && origem.kind == Kind.LOGICO) {
            return true;
        }

        if (this.kind == Kind.PONTEIRO && origem.kind == Kind.PONTEIRO) {
            return Objects.equals(this.apontado, origem.apontado);
        }

        if (this.kind == Kind.REGISTRO && origem.kind == Kind.REGISTRO) {
            if (this == origem) {
                return true;
            }

            return this.nomeRegistro != null
                    && origem.nomeRegistro != null
                    && this.nomeRegistro.equals(origem.nomeRegistro);
        }

        return false;
    }

    /**
     * Compatibilidade de parâmetros é mais estrita que atribuição:
     * a quantidade, ordem e tipo devem ser exatos, exceto que endereço já é
     * representado como ponteiro pelo avaliador de expressões.
     */
    public boolean aceitaParametroDe(TipoLA argumento) {
        if (this.isInvalido() || argumento == null || argumento.isInvalido()) {
            return false;
        }

        if (this.kind != argumento.kind) {
            return false;
        }

        if (this.kind == Kind.PONTEIRO) {
            return Objects.equals(this.apontado, argumento.apontado);
        }

        if (this.kind == Kind.REGISTRO) {
            if (this == argumento) {
                return true;
            }

            return this.nomeRegistro != null
                    && argumento.nomeRegistro != null
                    && this.nomeRegistro.equals(argumento.nomeRegistro);
        }

        return this.kind == argumento.kind;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TipoLA)) {
            return false;
        }

        TipoLA o = (TipoLA) other;

        if (kind != o.kind) {
            return false;
        }

        if (kind == Kind.REGISTRO && (nomeRegistro == null || o.nomeRegistro == null)) {
            return this == o;
        }

        return Objects.equals(nomeRegistro, o.nomeRegistro)
                && Objects.equals(apontado, o.apontado);
    }

    @Override
    public int hashCode() {
        if (kind == Kind.REGISTRO && nomeRegistro == null) {
            return System.identityHashCode(this);
        }

        return Objects.hash(kind, nomeRegistro, apontado);
    }
}
