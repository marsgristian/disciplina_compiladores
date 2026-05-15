package br.ufscar.dc.compiladores.t3;

/**
 * Resultado da avaliação de uma expressão.
 *
 * hasErroSemantico evita cascatas indesejadas. Por exemplo, em:
 *
 * x <- y
 *
 * se y não foi declarado, a expressão é inválida, mas normalmente não queremos
 * acrescentar também "atribuicao nao compativel para x".
 *
 * hasIncompatibilidadeTipo indica expressão bem formada sintaticamente, mas
 * sem tipo válido por combinação incompatível, como literal + logico.
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
