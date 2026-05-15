package br.ufscar.dc.compiladores.t3;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.ufscar.dc.compiladores.t3.Simbolo.Categoria;

/**
 * Visitante semântico do T3.
 *
 * Esta classe implementa as quatro verificações exigidas no enunciado:
 *
 * 1. identificador já declarado anteriormente no mesmo escopo;
 * 2. tipo não declarado;
 * 3. identificador não declarado;
 * 4. atribuição incompatível.
 *
 * Diferentemente do T2, erros semânticos não interrompem a análise. Todas as
 * mensagens são acumuladas em ordem de visita e escritas no final.
 */
public class SemanticoVisitor extends LABaseVisitor<Void> {
    private final List<String> erros = new ArrayList<>();
    private Escopo escopoAtual = new Escopo(null);

    public SemanticoVisitor() {
        inicializarTiposBasicos();
    }

    public List<String> getErros() {
        return erros;
    }

    private void inicializarTiposBasicos() {
        escopoAtual.inserir(new Simbolo("literal", Categoria.TIPO, TipoLA.LITERAL));
        escopoAtual.inserir(new Simbolo("inteiro", Categoria.TIPO, TipoLA.INTEIRO));
        escopoAtual.inserir(new Simbolo("real", Categoria.TIPO, TipoLA.REAL));
        escopoAtual.inserir(new Simbolo("logico", Categoria.TIPO, TipoLA.LOGICO));
    }

    @Override
    public Void visitPrograma(LAParser.ProgramaContext ctx) {
        visitDeclaracoes(ctx.declaracoes());
        visitCorpo(ctx.corpo());
        return null;
    }

    @Override
    public Void visitDeclaracoes(LAParser.DeclaracoesContext ctx) {
        for (LAParser.Decl_local_globalContext decl : ctx.decl_local_global()) {
            visitDecl_local_global(decl);
        }

        return null;
    }

    @Override
    public Void visitDecl_local_global(LAParser.Decl_local_globalContext ctx) {
        if (ctx.declaracao_local() != null) {
            visitDeclaracao_local(ctx.declaracao_local());
        } else if (ctx.declaracao_global() != null) {
            visitDeclaracao_global(ctx.declaracao_global());
        }

        return null;
    }

    @Override
    public Void visitCorpo(LAParser.CorpoContext ctx) {
        for (LAParser.Declaracao_localContext decl : ctx.declaracao_local()) {
            visitDeclaracao_local(decl);
        }

        for (LAParser.CmdContext cmd : ctx.cmd()) {
            visitCmd(cmd);
        }

        return null;
    }

    @Override
    public Void visitDeclaracao_local(LAParser.Declaracao_localContext ctx) {
        String primeiro = ctx.getChild(0).getText();

        if (primeiro.equals("declare")) {
            declararVariaveis(ctx.variavel(), Categoria.VARIAVEL);
            return null;
        }

        if (primeiro.equals("constante")) {
            String nome = ctx.IDENT().getText();
            int linha = ctx.IDENT().getSymbol().getLine();

            if (declaradoLocalmente(nome)) {
                erroIdentificadorJaDeclarado(linha, nome);
            } else {
                TipoLA tipo = resolverTipoBasico(ctx.tipo_basico());
                escopoAtual.inserir(new Simbolo(nome, Categoria.CONSTANTE, tipo));
            }

            return null;
        }

        if (primeiro.equals("tipo")) {
            String nome = ctx.IDENT().getText();
            int linha = ctx.IDENT().getSymbol().getLine();

            TipoLA tipoDeclarado = resolverTipo(ctx.tipo(), nome);

            if (declaradoLocalmente(nome)) {
                erroIdentificadorJaDeclarado(linha, nome);
            } else {
                escopoAtual.inserir(new Simbolo(nome, Categoria.TIPO, tipoDeclarado));
            }

            return null;
        }

        return null;
    }

    @Override
    public Void visitDeclaracao_global(LAParser.Declaracao_globalContext ctx) {
        String primeiro = ctx.getChild(0).getText();
        String nome = ctx.IDENT().getText();
        int linha = ctx.IDENT().getSymbol().getLine();

        if (primeiro.equals("procedimento")) {
            if (declaradoLocalmente(nome)) {
                erroIdentificadorJaDeclarado(linha, nome);
            } else {
                escopoAtual.inserir(new Simbolo(nome, Categoria.PROCEDIMENTO, TipoLA.INVALIDO));
            }

            abrirEscopo();

            if (ctx.parametros() != null) {
                declararParametros(ctx.parametros());
            }

            for (LAParser.Declaracao_localContext decl : ctx.declaracao_local()) {
                visitDeclaracao_local(decl);
            }

            for (LAParser.CmdContext cmd : ctx.cmd()) {
                visitCmd(cmd);
            }

            fecharEscopo();
            return null;
        }

        if (primeiro.equals("funcao")) {
            TipoLA retorno = resolverTipoEstendido(ctx.tipo_estendido());

            if (declaradoLocalmente(nome)) {
                erroIdentificadorJaDeclarado(linha, nome);
            } else {
                escopoAtual.inserir(new Simbolo(nome, Categoria.FUNCAO, retorno));
            }

            abrirEscopo();

            if (ctx.parametros() != null) {
                declararParametros(ctx.parametros());
            }

            for (LAParser.Declaracao_localContext decl : ctx.declaracao_local()) {
                visitDeclaracao_local(decl);
            }

            for (LAParser.CmdContext cmd : ctx.cmd()) {
                visitCmd(cmd);
            }

            fecharEscopo();
        }

        return null;
    }

    private void declararParametros(LAParser.ParametrosContext ctx) {
        for (LAParser.ParametroContext parametro : ctx.parametro()) {
            TipoLA tipo = resolverTipoEstendido(parametro.tipo_estendido());

            for (LAParser.IdentificadorContext identificador : parametro.identificador()) {
                String nome = nomeIdentificador(identificador);
                int linha = linhaIdentificador(identificador);

                if (declaradoLocalmente(nome)) {
                    erroIdentificadorJaDeclarado(linha, nome);
                } else {
                    escopoAtual.inserir(new Simbolo(nome, Categoria.PARAMETRO, tipo));
                }
            }
        }
    }

    private void declararVariaveis(LAParser.VariavelContext ctx, Categoria categoria) {
        TipoLA tipo = resolverTipo(ctx.tipo(), null);

        for (LAParser.IdentificadorContext identificador : ctx.identificador()) {
            String nome = nomeIdentificador(identificador);
            int linha = linhaIdentificador(identificador);

            if (declaradoLocalmente(nome)) {
                erroIdentificadorJaDeclarado(linha, nome);
            } else {
                escopoAtual.inserir(new Simbolo(nome, categoria, tipo));
            }
        }
    }

    private TipoLA resolverTipo(LAParser.TipoContext ctx, String nomeRegistro) {
        if (ctx.registro() != null) {
            return resolverRegistro(ctx.registro(), nomeRegistro);
        }

        return resolverTipoEstendido(ctx.tipo_estendido());
    }

    private TipoLA resolverRegistro(LAParser.RegistroContext ctx, String nomeRegistro) {
        Map<String, TipoLA> campos = new LinkedHashMap<>();

        for (LAParser.VariavelContext variavel : ctx.variavel()) {
            TipoLA tipoCampo = resolverTipo(variavel.tipo(), null);

            for (LAParser.IdentificadorContext identificador : variavel.identificador()) {
                String nomeCampo = nomeIdentificador(identificador);
                int linha = linhaIdentificador(identificador);

                if (campos.containsKey(nomeCampo)) {
                    erroIdentificadorJaDeclarado(linha, nomeCampo);
                } else {
                    campos.put(nomeCampo, tipoCampo);
                }
            }
        }

        return TipoLA.registro(nomeRegistro, campos);
    }

    private TipoLA resolverTipoBasico(LAParser.Tipo_basicoContext ctx) {
        switch (ctx.getText()) {
            case "literal":
                return TipoLA.LITERAL;
            case "inteiro":
                return TipoLA.INTEIRO;
            case "real":
                return TipoLA.REAL;
            case "logico":
                return TipoLA.LOGICO;
            default:
                return TipoLA.INVALIDO;
        }
    }

    private TipoLA resolverTipoEstendido(LAParser.Tipo_estendidoContext ctx) {
        boolean ponteiro = ctx.getText().startsWith("^");
        TipoLA base = resolverTipoBasicoIdent(ctx.tipo_basico_ident());

        if (ponteiro) {
            return TipoLA.ponteiro(base);
        }

        return base;
    }

    private TipoLA resolverTipoBasicoIdent(LAParser.Tipo_basico_identContext ctx) {
        if (ctx.tipo_basico() != null) {
            return resolverTipoBasico(ctx.tipo_basico());
        }

        String nomeTipo = ctx.IDENT().getText();
        Simbolo simbolo = escopoAtual.buscar(nomeTipo);

        if (simbolo == null || simbolo.categoria != Categoria.TIPO) {
            erroTipoNaoDeclarado(ctx.IDENT().getSymbol().getLine(), nomeTipo);
            return TipoLA.INVALIDO;
        }

        return simbolo.tipo;
    }

    @Override
    public Void visitCmd(LAParser.CmdContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Void visitCmdLeia(LAParser.CmdLeiaContext ctx) {
        for (LAParser.IdentificadorContext identificador : ctx.identificador()) {
            resolverIdentificador(identificador);
        }

        return null;
    }

    @Override
    public Void visitCmdEscreva(LAParser.CmdEscrevaContext ctx) {
        for (LAParser.ExpressaoContext expressao : ctx.expressao()) {
            avaliarExpressao(expressao);
        }

        return null;
    }

    @Override
    public Void visitCmdSe(LAParser.CmdSeContext ctx) {
        avaliarExpressao(ctx.expressao());

        for (LAParser.CmdContext cmd : ctx.cmd()) {
            visitCmd(cmd);
        }

        return null;
    }

    @Override
    public Void visitCmdCaso(LAParser.CmdCasoContext ctx) {
        avaliarExpAritmetica(ctx.exp_aritmetica());
        visitSelecao(ctx.selecao());

        for (LAParser.CmdContext cmd : ctx.cmd()) {
            visitCmd(cmd);
        }

        return null;
    }

    @Override
    public Void visitCmdPara(LAParser.CmdParaContext ctx) {
        String nome = ctx.IDENT().getText();

        if (escopoAtual.buscar(nome) == null) {
            erroIdentificadorNaoDeclarado(ctx.IDENT().getSymbol().getLine(), nome);
        }

        for (LAParser.Exp_aritmeticaContext exp : ctx.exp_aritmetica()) {
            avaliarExpAritmetica(exp);
        }

        for (LAParser.CmdContext cmd : ctx.cmd()) {
            visitCmd(cmd);
        }

        return null;
    }

    @Override
    public Void visitCmdEnquanto(LAParser.CmdEnquantoContext ctx) {
        avaliarExpressao(ctx.expressao());

        for (LAParser.CmdContext cmd : ctx.cmd()) {
            visitCmd(cmd);
        }

        return null;
    }

    @Override
    public Void visitCmdFaca(LAParser.CmdFacaContext ctx) {
        for (LAParser.CmdContext cmd : ctx.cmd()) {
            visitCmd(cmd);
        }

        avaliarExpressao(ctx.expressao());
        return null;
    }

    @Override
    public Void visitCmdAtribuicao(LAParser.CmdAtribuicaoContext ctx) {
        boolean desreferencia = ctx.getStart().getText().equals("^");

        ResultadoTipo destino = resolverIdentificador(ctx.identificador());

        if (desreferencia && !destino.tipo.isInvalido()) {
            destino = ResultadoTipo.ok(destino.tipo.deref());
        }

        ResultadoTipo origem = avaliarExpressao(ctx.expressao());

        if (!destino.hasErroSemantico
                && !origem.hasErroSemantico
                && !destino.tipo.aceitaAtribuicaoDe(origem.tipo)) {
            erroAtribuicaoNaoCompativel(ctx.getStart().getLine(), nomeIdentificador(ctx.identificador()));
        }

        return null;
    }

    @Override
    public Void visitCmdChamada(LAParser.CmdChamadaContext ctx) {
        String nome = ctx.IDENT().getText();

        if (escopoAtual.buscar(nome) == null) {
            erroIdentificadorNaoDeclarado(ctx.IDENT().getSymbol().getLine(), nome);
        }

        for (LAParser.ExpressaoContext expressao : ctx.expressao()) {
            avaliarExpressao(expressao);
        }

        return null;
    }

    @Override
    public Void visitCmdRetorne(LAParser.CmdRetorneContext ctx) {
        avaliarExpressao(ctx.expressao());
        return null;
    }

    /**
     * Resolve um identificador simples ou qualificado por campos de registro.
     */
    private ResultadoTipo resolverIdentificador(LAParser.IdentificadorContext ctx) {
        List<TerminalNode> partes = ctx.IDENT();

        String primeiroNome = partes.get(0).getText();
        Simbolo simbolo = escopoAtual.buscar(primeiroNome);

        if (simbolo == null) {
            erroIdentificadorNaoDeclarado(partes.get(0).getSymbol().getLine(), nomeIdentificador(ctx));
            return ResultadoTipo.erroSemantico();
        }

        TipoLA tipoAtual = simbolo.tipo;

        for (int i = 1; i < partes.size(); i++) {
            String campo = partes.get(i).getText();

            if (tipoAtual.kind != TipoLA.Kind.REGISTRO || !tipoAtual.campos.containsKey(campo)) {
                erroIdentificadorNaoDeclarado(partes.get(i).getSymbol().getLine(), nomeIdentificador(ctx));
                return ResultadoTipo.erroSemantico();
            }

            tipoAtual = tipoAtual.campos.get(campo);
        }

        for (LAParser.Exp_aritmeticaContext dimensao : ctx.dimensao().exp_aritmetica()) {
            avaliarExpAritmetica(dimensao);
        }

        return ResultadoTipo.ok(tipoAtual);
    }

    private ResultadoTipo avaliarExpressao(LAParser.ExpressaoContext ctx) {
        List<LAParser.Termo_logicoContext> termos = ctx.termo_logico();

        ResultadoTipo atual = avaliarTermoLogico(termos.get(0));

        for (int i = 1; i < termos.size(); i++) {
            ResultadoTipo direita = avaliarTermoLogico(termos.get(i));

            if (atual.hasErroSemantico || direita.hasErroSemantico) {
                atual = atual.combinarErro(direita);
            } else if (atual.tipo.kind == TipoLA.Kind.LOGICO && direita.tipo.kind == TipoLA.Kind.LOGICO) {
                atual = ResultadoTipo.ok(TipoLA.LOGICO);
            } else {
                atual = ResultadoTipo.incompatibilidade();
            }
        }

        return atual;
    }

    private ResultadoTipo avaliarTermoLogico(LAParser.Termo_logicoContext ctx) {
        List<LAParser.Fator_logicoContext> fatores = ctx.fator_logico();

        ResultadoTipo atual = avaliarFatorLogico(fatores.get(0));

        for (int i = 1; i < fatores.size(); i++) {
            ResultadoTipo direita = avaliarFatorLogico(fatores.get(i));

            if (atual.hasErroSemantico || direita.hasErroSemantico) {
                atual = atual.combinarErro(direita);
            } else if (atual.tipo.kind == TipoLA.Kind.LOGICO && direita.tipo.kind == TipoLA.Kind.LOGICO) {
                atual = ResultadoTipo.ok(TipoLA.LOGICO);
            } else {
                atual = ResultadoTipo.incompatibilidade();
            }
        }

        return atual;
    }

    private ResultadoTipo avaliarFatorLogico(LAParser.Fator_logicoContext ctx) {
        ResultadoTipo parcela = avaliarParcelaLogica(ctx.parcela_logica());

        if (ctx.getText().startsWith("nao")) {
            if (parcela.hasErroSemantico) {
                return parcela;
            }

            if (parcela.tipo.kind == TipoLA.Kind.LOGICO) {
                return ResultadoTipo.ok(TipoLA.LOGICO);
            }

            return ResultadoTipo.incompatibilidade();
        }

        return parcela;
    }

    private ResultadoTipo avaliarParcelaLogica(LAParser.Parcela_logicaContext ctx) {
        if (ctx.getText().equals("verdadeiro") || ctx.getText().equals("falso")) {
            return ResultadoTipo.ok(TipoLA.LOGICO);
        }

        return avaliarExpRelacional(ctx.exp_relacional());
    }

    private ResultadoTipo avaliarExpRelacional(LAParser.Exp_relacionalContext ctx) {
        List<LAParser.Exp_aritmeticaContext> exps = ctx.exp_aritmetica();

        ResultadoTipo esquerda = avaliarExpAritmetica(exps.get(0));

        if (exps.size() == 1) {
            return esquerda;
        }

        ResultadoTipo direita = avaliarExpAritmetica(exps.get(1));

        if (esquerda.hasErroSemantico || direita.hasErroSemantico) {
            return esquerda.combinarErro(direita);
        }

        if (saoComparaveis(esquerda.tipo, direita.tipo)) {
            return ResultadoTipo.ok(TipoLA.LOGICO);
        }

        return ResultadoTipo.incompatibilidade();
    }

    private boolean saoComparaveis(TipoLA a, TipoLA b) {
        if (a.isNumerico() && b.isNumerico()) {
            return true;
        }

        if (a.kind == b.kind && (a.kind == TipoLA.Kind.LITERAL || a.kind == TipoLA.Kind.LOGICO)) {
            return true;
        }

        return a.kind == TipoLA.Kind.REGISTRO
                && b.kind == TipoLA.Kind.REGISTRO
                && a.nomeRegistro != null
                && a.nomeRegistro.equals(b.nomeRegistro);
    }

    private ResultadoTipo avaliarExpAritmetica(LAParser.Exp_aritmeticaContext ctx) {
        List<LAParser.TermoContext> termos = ctx.termo();

        ResultadoTipo atual = avaliarTermo(termos.get(0));

        for (int i = 1; i < termos.size(); i++) {
            ResultadoTipo direita = avaliarTermo(termos.get(i));

            if (atual.hasErroSemantico || direita.hasErroSemantico) {
                atual = atual.combinarErro(direita);
            } else if (atual.tipo.isNumerico() && direita.tipo.isNumerico()) {
                atual = ResultadoTipo.ok(promoverNumerico(atual.tipo, direita.tipo));
            } else {
                atual = ResultadoTipo.incompatibilidade();
            }
        }

        return atual;
    }

    private ResultadoTipo avaliarTermo(LAParser.TermoContext ctx) {
        List<LAParser.FatorContext> fatores = ctx.fator();

        ResultadoTipo atual = avaliarFator(fatores.get(0));

        for (int i = 1; i < fatores.size(); i++) {
            ResultadoTipo direita = avaliarFator(fatores.get(i));

            if (atual.hasErroSemantico || direita.hasErroSemantico) {
                atual = atual.combinarErro(direita);
            } else if (atual.tipo.isNumerico() && direita.tipo.isNumerico()) {
                atual = ResultadoTipo.ok(promoverNumerico(atual.tipo, direita.tipo));
            } else {
                atual = ResultadoTipo.incompatibilidade();
            }
        }

        return atual;
    }

    private ResultadoTipo avaliarFator(LAParser.FatorContext ctx) {
        List<LAParser.ParcelaContext> parcelas = ctx.parcela();

        ResultadoTipo atual = avaliarParcela(parcelas.get(0));

        for (int i = 1; i < parcelas.size(); i++) {
            ResultadoTipo direita = avaliarParcela(parcelas.get(i));

            if (atual.hasErroSemantico || direita.hasErroSemantico) {
                atual = atual.combinarErro(direita);
            } else if (atual.tipo.isNumerico() && direita.tipo.isNumerico()) {
                atual = ResultadoTipo.ok(TipoLA.INTEIRO);
            } else {
                atual = ResultadoTipo.incompatibilidade();
            }
        }

        return atual;
    }

    private ResultadoTipo avaliarParcela(LAParser.ParcelaContext ctx) {
        if (ctx.parcela_unario() != null) {
            return avaliarParcelaUnario(ctx.parcela_unario());
        }

        return avaliarParcelaNaoUnario(ctx.parcela_nao_unario());
    }

    private ResultadoTipo avaliarParcelaUnario(LAParser.Parcela_unarioContext ctx) {
        if (ctx.identificador() != null) {
            ResultadoTipo tipo = resolverIdentificador(ctx.identificador());

            if (ctx.getStart().getText().equals("^") && !tipo.tipo.isInvalido()) {
                return ResultadoTipo.ok(tipo.tipo.deref());
            }

            return tipo;
        }

        if (ctx.IDENT() != null) {
            String nome = ctx.IDENT().getText();
            Simbolo simbolo = escopoAtual.buscar(nome);

            if (simbolo == null) {
                erroIdentificadorNaoDeclarado(ctx.IDENT().getSymbol().getLine(), nome);
                return ResultadoTipo.erroSemantico();
            }

            for (LAParser.ExpressaoContext expressao : ctx.expressao()) {
                avaliarExpressao(expressao);
            }

            return ResultadoTipo.ok(simbolo.tipo);
        }

        if (ctx.NUM_INT() != null) {
            return ResultadoTipo.ok(TipoLA.INTEIRO);
        }

        if (ctx.NUM_REAL() != null) {
            return ResultadoTipo.ok(TipoLA.REAL);
        }

        if (!ctx.expressao().isEmpty()) {
            return avaliarExpressao(ctx.expressao(0));
        }

        return ResultadoTipo.incompatibilidade();
    }

    private ResultadoTipo avaliarParcelaNaoUnario(LAParser.Parcela_nao_unarioContext ctx) {
        if (ctx.CADEIA() != null) {
            return ResultadoTipo.ok(TipoLA.LITERAL);
        }

        if (ctx.identificador() != null) {
            ResultadoTipo base = resolverIdentificador(ctx.identificador());

            if (base.hasErroSemantico) {
                return base;
            }

            return ResultadoTipo.ok(TipoLA.ponteiro(base.tipo));
        }

        return ResultadoTipo.incompatibilidade();
    }

    private TipoLA promoverNumerico(TipoLA a, TipoLA b) {
        if (a.kind == TipoLA.Kind.REAL || b.kind == TipoLA.Kind.REAL) {
            return TipoLA.REAL;
        }

        return TipoLA.INTEIRO;
    }

    private boolean declaradoLocalmente(String nome) {
        return escopoAtual.contemLocal(nome);
    }

    private void abrirEscopo() {
        escopoAtual = new Escopo(escopoAtual);
    }

    private void fecharEscopo() {
        escopoAtual = escopoAtual.getPai();
    }

    private String nomeIdentificador(LAParser.IdentificadorContext ctx) {
        List<TerminalNode> partes = ctx.IDENT();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < partes.size(); i++) {
            if (i > 0) {
                sb.append(".");
            }

            sb.append(partes.get(i).getText());
        }

        return sb.toString();
    }

    private int linhaIdentificador(LAParser.IdentificadorContext ctx) {
        return ctx.IDENT(0).getSymbol().getLine();
    }

    private void erroIdentificadorJaDeclarado(int linha, String nome) {
        erros.add("Linha " + linha + ": identificador " + nome + " ja declarado anteriormente");
    }

    private void erroTipoNaoDeclarado(int linha, String nome) {
        erros.add("Linha " + linha + ": tipo " + nome + " nao declarado");
    }

    private void erroIdentificadorNaoDeclarado(int linha, String nome) {
        erros.add("Linha " + linha + ": identificador " + nome + " nao declarado");
    }

    private void erroAtribuicaoNaoCompativel(int linha, String nome) {
        erros.add("Linha " + linha + ": atribuicao nao compativel para " + nome);
    }
}
