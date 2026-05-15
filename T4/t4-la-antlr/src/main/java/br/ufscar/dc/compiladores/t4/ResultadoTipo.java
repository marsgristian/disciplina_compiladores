package br.ufscar.dc.compiladores.t4;

/**
 * Resultado da inferência de tipo de uma expressão.
 *
 * hasErroSemantico indica que a expressão já gerou erro específico, como
 * identificador não declarado. Nesses casos evitamos cascata indevida de erro
 * de atribuição/parâmetro.
 */
public class ResultadoTipo {
    public final TipoLA tipo;
    public final boolean hasErroSemantico;
    public final boolean hasIncompatibilidadeTipo;

    private ResultadoTipo(TipoLA tipo, boolean hasErroSemantico, boolean hasIncompatibilidadeTipo) {
        this.tipo = tipo;
        this.hasErroSemantico = hasErroSemantico;
        this.hasIncompatibilidadeTipo = hasIncompatibilidadeTipo;
    }

    public static ResultadoTipo ok(TipoLA tipo) {
        return new ResultadoTipo(tipo, false, false);
    }

    public static ResultadoTipo erroSemantico() {
        return new ResultadoTipo(TipoLA.INVALIDO, true, false);
    }

    public static ResultadoTipo incompatibilidade() {
        return new ResultadoTipo(TipoLA.INVALIDO, false, true);
    }

    public ResultadoTipo combinarErro(ResultadoTipo outro) {
        return new ResultadoTipo(
                TipoLA.INVALIDO,
                this.hasErroSemantico || outro.hasErroSemantico,
                this.hasIncompatibilidadeTipo || outro.hasIncompatibilidadeTipo
        );
    }
}
