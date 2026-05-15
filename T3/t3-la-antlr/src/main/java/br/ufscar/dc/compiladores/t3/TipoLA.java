package br.ufscar.dc.compiladores.t3;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Representa o tipo de uma expressão, variável, constante ou função da LA.
 *
 * Além dos tipos básicos, o T3 precisa representar ponteiros, registros
 * nomeados e tipos inválidos. O tipo inválido é usado para evitar cascatas de
 * erros quando uma expressão já possui erro anterior.
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

    /**
     * Remove um nível de ponteiro. Usado em comandos do tipo ^p <- valor.
     */
    public TipoLA deref() {
        if (kind == Kind.PONTEIRO && apontado != null) {
            return apontado;
        }

        return INVALIDO;
    }

    /**
     * Verifica compatibilidade de atribuição conforme o enunciado do T3:
     * - real/inteiro recebem real/inteiro;
     * - literal recebe literal;
     * - logico recebe logico;
     * - ponteiro recebe endereço/ponteiro compatível;
     * - registro recebe registro com o mesmo nome.
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
            return this.nomeRegistro != null
                    && origem.nomeRegistro != null
                    && this.nomeRegistro.equals(origem.nomeRegistro);
        }

        return false;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TipoLA)) {
            return false;
        }

        TipoLA o = (TipoLA) other;

        return kind == o.kind
                && Objects.equals(nomeRegistro, o.nomeRegistro)
                && Objects.equals(apontado, o.apontado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, nomeRegistro, apontado);
    }
}
