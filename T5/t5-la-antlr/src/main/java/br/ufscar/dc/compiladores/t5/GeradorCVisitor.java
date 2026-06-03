package br.ufscar.dc.compiladores.t5;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.ufscar.dc.compiladores.t5.CSimbolo.Categoria;

/**
 * Gerador de código C para programas LA semanticamente válidos.
 *
 * O objetivo do T5 não é produzir C idêntico aos gabaritos, e sim C compilável
 * cuja execução tenha a mesma entrada/saída. Por isso o gerador privilegia
 * código simples: printf/scanf, structs C, funções, procedimentos, laços e
 * condicionais diretos.
 */
public class GeradorCVisitor extends LABaseVisitor<Void> {
    private final StringBuilder globais = new StringBuilder();
    private final StringBuilder prototipos = new StringBuilder();
    private final StringBuilder funcoes = new StringBuilder();
    private final StringBuilder main = new StringBuilder();
    private StringBuilder atual = main;
    private int indent = 1;
    private CEscopo escopoAtual = new CEscopo(null);

    public GeradorCVisitor() {
        registrarTiposBasicos();
    }

    private void registrarTiposBasicos() {
        escopoAtual.inserir(new CSimbolo("inteiro", Categoria.TIPO, CTipo.INTEIRO));
        escopoAtual.inserir(new CSimbolo("real", Categoria.TIPO, CTipo.REAL));
        escopoAtual.inserir(new CSimbolo("literal", Categoria.TIPO, CTipo.LITERAL));
        escopoAtual.inserir(new CSimbolo("logico", Categoria.TIPO, CTipo.LOGICO));
    }

    public String gerar(LAParser.ProgramaContext ctx) {
        primeiraPassagem(ctx.declaracoes());
        segundaPassagemRotinas(ctx.declaracoes());
        gerarCorpoPrincipal(ctx.corpo());

        StringBuilder c = new StringBuilder();
        c.append("#include <stdio.h>\n");
        c.append("#include <stdlib.h>\n");
        c.append("#include <string.h>\n\n");
        c.append(globais);
        if (globais.length() > 0) c.append("\n");
        c.append(prototipos);
        if (prototipos.length() > 0) c.append("\n");
        c.append(funcoes);
        if (funcoes.length() > 0) c.append("\n");
        c.append("int main() {\n");
        c.append(main);
        c.append("\treturn 0;\n");
        c.append("}\n");
        return c.toString();
    }

    /** Primeira passagem: registra tipos, globais e assinaturas das rotinas. */
    private void primeiraPassagem(LAParser.DeclaracoesContext ctx) {
        for (LAParser.Decl_local_globalContext d : ctx.decl_local_global()) {
            if (d.declaracao_local() != null) {
                String old = setAtual(globais, 0);
                gerarDeclaracaoLocal(d.declaracao_local(), true);
                restoreAtual(old);
            } else {
                registrarRotina(d.declaracao_global());
            }
        }
    }

    /** Segunda passagem: emite as definições C das funções/procedimentos. */
    private void segundaPassagemRotinas(LAParser.DeclaracoesContext ctx) {
        for (LAParser.Decl_local_globalContext d : ctx.decl_local_global()) {
            if (d.declaracao_global() != null) gerarRotina(d.declaracao_global());
        }
    }

    private void gerarCorpoPrincipal(LAParser.CorpoContext ctx) {
        atual = main; indent = 1;
        for (LAParser.Declaracao_localContext d : ctx.declaracao_local()) gerarDeclaracaoLocal(d, false);
        for (LAParser.CmdContext c : ctx.cmd()) gerarCmd(c);
    }

    private String setAtual(StringBuilder sb, int ind) {
        String ignored = "";
        atual = sb;
        indent = ind;
        return ignored;
    }
    private void restoreAtual(String ignored) { atual = main; indent = 1; }

    private void abrirEscopo() { escopoAtual = new CEscopo(escopoAtual); }
    private void fecharEscopo() { escopoAtual = escopoAtual.getPai(); }

    private void emit(String s) {
        for (int i = 0; i < indent; i++) atual.append('\t');
        atual.append(s).append('\n');
    }

    private void gerarDeclaracaoLocal(LAParser.Declaracao_localContext ctx, boolean global) {
        String tag = ctx.getChild(0).getText();
        if (tag.equals("declare")) {
            CTipo tipo = resolverTipo(ctx.variavel().tipo(), null);
            for (LAParser.IdentificadorContext id : ctx.variavel().identificador()) {
                String nome = nomeIdentificador(id);
                escopoAtual.inserir(new CSimbolo(nome, Categoria.VARIAVEL, tipo));
                emit(declaracaoVariavel(nome, tipo, dimensoes(id)) + ";");
            }
        } else if (tag.equals("constante")) {
            String nome = ctx.IDENT().getText();
            CTipo tipo = resolverTipoBasico(ctx.tipo_basico());
            String valor = valorConstante(ctx.valor_constante());
            escopoAtual.inserir(new CSimbolo(nome, Categoria.CONSTANTE, tipo));
            if (tipo.kind == CTipo.Kind.LITERAL) emit("const char " + nome + "[] = " + valor + ";");
            else emit("const " + cTipo(tipo) + " " + nome + " = " + valor + ";");
        } else if (tag.equals("tipo")) {
            String nome = ctx.IDENT().getText();
            CTipo tipo = resolverTipo(ctx.tipo(), nome);
            escopoAtual.inserir(new CSimbolo(nome, Categoria.TIPO, tipo));
            if (tipo.kind == CTipo.Kind.REGISTRO) emitirTypedefRegistro(nome, tipo);
        }
    }

    private void emitirTypedefRegistro(String nome, CTipo tipo) {
        emit("typedef struct {");
        indent++;
        for (Map.Entry<String, CTipo> campo : tipo.campos.entrySet()) emit(declaracaoVariavel(campo.getKey(), campo.getValue(), new ArrayList<>()) + ";");
        indent--;
        emit("} " + nome + ";");
    }

    private void registrarRotina(LAParser.Declaracao_globalContext ctx) {
        String nome = ctx.IDENT().getText();
        boolean funcao = ctx.getChild(0).getText().equals("funcao");
        List<ParametroFormal> params = parametros(ctx.parametros());
        CTipo retorno = funcao ? resolverTipoEstendido(ctx.tipo_estendido()) : CTipo.INVALIDO;
        escopoAtual.inserir(new CSimbolo(nome, funcao ? Categoria.FUNCAO : Categoria.PROCEDIMENTO, retorno, false, params));
        prototipos.append(assinaturaRotina(nome, funcao, retorno, params)).append(";\n");
    }

    private void gerarRotina(LAParser.Declaracao_globalContext ctx) {
        String nome = ctx.IDENT().getText();
        boolean funcao = ctx.getChild(0).getText().equals("funcao");
        CSimbolo rotina = escopoAtual.buscar(nome);
        CTipo retorno = funcao ? rotina.tipo : CTipo.INVALIDO;

        atual = funcoes; indent = 0;
        emit(assinaturaRotina(nome, funcao, retorno, rotina.parametros) + " {");
        indent++;
        abrirEscopo();
        for (ParametroFormal p : rotina.parametros) escopoAtual.inserir(new CSimbolo(p.nome, Categoria.PARAMETRO, p.tipo, p.porReferencia));
        for (LAParser.Declaracao_localContext d : ctx.declaracao_local()) gerarDeclaracaoLocal(d, false);
        for (LAParser.CmdContext c : ctx.cmd()) gerarCmd(c);
        indent--;
        fecharEscopo();
        emit("}");
        atual = main; indent = 1;
    }

    private String assinaturaRotina(String nome, boolean funcao, CTipo retorno, List<ParametroFormal> params) {
        StringBuilder sb = new StringBuilder();
        sb.append(funcao ? cTipoRetorno(retorno) : "void").append(" ").append(nome).append("(");
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            ParametroFormal p = params.get(i);
            sb.append(declaracaoParametro(p));
        }
        sb.append(")");
        return sb.toString();
    }

    private List<ParametroFormal> parametros(LAParser.ParametrosContext ctx) {
        List<ParametroFormal> out = new ArrayList<>();
        if (ctx == null) return out;
        for (LAParser.ParametroContext p : ctx.parametro()) {
            boolean porRef = p.getChild(0).getText().equals("var");
            CTipo tipo = resolverTipoEstendido(p.tipo_estendido());
            for (LAParser.IdentificadorContext id : p.identificador()) out.add(new ParametroFormal(nomeIdentificador(id), tipo, porRef));
        }
        return out;
    }

    private String declaracaoParametro(ParametroFormal p) {
        if (p.tipo.kind == CTipo.Kind.LITERAL) return "char " + p.nome + "[80]";
        if (p.porReferencia) return cTipo(p.tipo) + " *" + p.nome;
        return cTipo(p.tipo) + " " + p.nome;
    }

    private void gerarCmd(LAParser.CmdContext ctx) {
        if (ctx.cmdLeia() != null) gerarLeia(ctx.cmdLeia());
        else if (ctx.cmdEscreva() != null) gerarEscreva(ctx.cmdEscreva());
        else if (ctx.cmdSe() != null) gerarSe(ctx.cmdSe());
        else if (ctx.cmdCaso() != null) gerarCaso(ctx.cmdCaso());
        else if (ctx.cmdPara() != null) gerarPara(ctx.cmdPara());
        else if (ctx.cmdEnquanto() != null) gerarEnquanto(ctx.cmdEnquanto());
        else if (ctx.cmdFaca() != null) gerarFaca(ctx.cmdFaca());
        else if (ctx.cmdAtribuicao() != null) gerarAtribuicao(ctx.cmdAtribuicao());
        else if (ctx.cmdChamada() != null) gerarChamada(ctx.cmdChamada());
        else if (ctx.cmdRetorne() != null) gerarRetorne(ctx.cmdRetorne());
    }

    private void gerarLeia(LAParser.CmdLeiaContext ctx) {
        for (LAParser.IdentificadorContext id : ctx.identificador()) {
            CTipo tipo = tipoIdentificador(id);
            String alvo = scanfTarget(id, tipo);
            if (tipo.kind == CTipo.Kind.LITERAL) emit("scanf(\"%79s\", " + alvo + ");");
            else if (tipo.kind == CTipo.Kind.REAL) emit("scanf(\"%f\", " + alvo + ");");
            else emit("scanf(\"%d\", " + alvo + ");");
        }
    }

    private void gerarEscreva(LAParser.CmdEscrevaContext ctx) {
        for (LAParser.ExpressaoContext ectx : ctx.expressao()) {
            Expr e = expr(ectx);
            if (e.tipo.kind == CTipo.Kind.LITERAL) emitirPrintfString(e);
            else if (e.tipo.kind == CTipo.Kind.REAL) emit("printf(\"%f\", " + e.codigo + ");");
            else emit("printf(\"%d\", " + e.codigo + ");");
        }
    }

    private void emitirPrintfString(Expr e) {
        if (e.partesString.size() <= 1) emit("printf(\"%s\", " + e.codigoString() + ");");
        else {
            StringBuilder fmt = new StringBuilder();
            for (int i = 0; i < e.partesString.size(); i++) fmt.append("%s");
            emit("printf(\"" + fmt + "\", " + String.join(", ", e.partesString) + ");");
        }
    }

    private void gerarAtribuicao(LAParser.CmdAtribuicaoContext ctx) {
        boolean deref = ctx.getStart().getText().equals("^");
        CTipo destino = tipoIdentificador(ctx.identificador());
        String lhs = identToC(ctx.identificador());
        if (deref) { lhs = "*(" + lhs + ")"; destino = destino.deref(); }
        Expr e = expr(ctx.expressao());
        if (destino.kind == CTipo.Kind.LITERAL) {
            if (e.partesString.size() > 1) {
                StringBuilder fmt = new StringBuilder();
                for (int i = 0; i < e.partesString.size(); i++) fmt.append("%s");
                emit("snprintf(" + lhs + ", 80, \"" + fmt + "\", " + String.join(", ", e.partesString) + ");");
            } else emit("strcpy(" + lhs + ", " + e.codigoString() + ");");
        } else emit(lhs + " = " + e.codigo + ";");
    }

    private void gerarChamada(LAParser.CmdChamadaContext ctx) {
        emit(ctx.IDENT().getText() + "(" + argumentosChamada(ctx.IDENT().getText(), ctx.expressao()) + ");");
    }

    private void gerarRetorne(LAParser.CmdRetorneContext ctx) { emit("return " + expr(ctx.expressao()).codigo + ";"); }

    private void gerarSe(LAParser.CmdSeContext ctx) {
        emit("if (" + expr(ctx.expressao()).codigo + ") {");
        indent++;
        boolean senao = false;
        List<LAParser.CmdContext> entaoCmds = new ArrayList<>();
        List<LAParser.CmdContext> senaoCmds = new ArrayList<>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child.getText().equals("senao")) { senao = true; continue; }
            if (child instanceof LAParser.CmdContext) {
                if (senao) senaoCmds.add((LAParser.CmdContext) child);
                else entaoCmds.add((LAParser.CmdContext) child);
            }
        }
        for (LAParser.CmdContext c : entaoCmds) gerarCmd(c);
        indent--;
        if (!senaoCmds.isEmpty()) {
            emit("} else {");
            indent++;
            for (LAParser.CmdContext c : senaoCmds) gerarCmd(c);
            indent--;
        }
        emit("}");
    }

    private void gerarEnquanto(LAParser.CmdEnquantoContext ctx) {
        emit("while (" + expr(ctx.expressao()).codigo + ") {");
        indent++;
        for (LAParser.CmdContext c : ctx.cmd()) gerarCmd(c);
        indent--;
        emit("}");
    }

    private void gerarFaca(LAParser.CmdFacaContext ctx) {
        emit("do {");
        indent++;
        for (LAParser.CmdContext c : ctx.cmd()) gerarCmd(c);
        indent--;
        emit("} while (!(" + expr(ctx.expressao()).codigo + ")); ");
    }

    private void gerarPara(LAParser.CmdParaContext ctx) {
        String id = ctx.IDENT().getText();
        String ini = expArit(ctx.exp_aritmetica(0)).codigo;
        String fim = expArit(ctx.exp_aritmetica(1)).codigo;
        emit("for (" + id + " = " + ini + "; " + id + " <= " + fim + "; " + id + "++) {");
        indent++;
        for (LAParser.CmdContext c : ctx.cmd()) gerarCmd(c);
        indent--;
        emit("}");
    }

    private void gerarCaso(LAParser.CmdCasoContext ctx) {
        emit("switch (" + expArit(ctx.exp_aritmetica()).codigo + ") {");
        indent++;
        for (LAParser.Item_selecaoContext item : ctx.selecao().item_selecao()) {
            for (int v : valoresConstantes(item.constantes())) emit("case " + v + ":");
            indent++;
            for (LAParser.CmdContext c : item.cmd()) gerarCmd(c);
            emit("break;");
            indent--;
        }
        if (!ctx.cmd().isEmpty()) {
            emit("default:");
            indent++;
            for (LAParser.CmdContext c : ctx.cmd()) gerarCmd(c);
            emit("break;");
            indent--;
        }
        indent--;
        emit("}");
    }

    private List<Integer> valoresConstantes(LAParser.ConstantesContext ctx) {
        List<Integer> vals = new ArrayList<>();
        for (LAParser.Numero_intervaloContext ni : ctx.numero_intervalo()) {
            String txt = ni.getText();
            if (txt.contains("..")) {
                String[] p = txt.split("\\.\\.");
                int a = Integer.parseInt(p[0]); int b = Integer.parseInt(p[1]);
                for (int v = a; v <= b; v++) vals.add(v);
            } else vals.add(Integer.parseInt(txt));
        }
        return vals;
    }

    private String argumentosChamada(String nome, List<LAParser.ExpressaoContext> args) {
        CSimbolo rot = escopoAtual.buscar(nome);
        List<String> out = new ArrayList<>();
        for (int i = 0; i < args.size(); i++) {
            boolean porRef = rot != null && i < rot.parametros.size() && rot.parametros.get(i).porReferencia;
            if (porRef) out.add(enderecoExpressao(args.get(i)));
            else out.add(expr(args.get(i)).codigoString());
        }
        return String.join(", ", out);
    }

    private String enderecoExpressao(LAParser.ExpressaoContext e) {
        String txt = e.getText();
        CSimbolo s = escopoAtual.buscar(txt);
        if (s != null && s.porReferencia && s.tipo.kind != CTipo.Kind.LITERAL) return txt;
        if (s != null && s.tipo.kind == CTipo.Kind.LITERAL) return txt;
        return "&" + txt;
    }

    private Expr expr(LAParser.ExpressaoContext ctx) {
        Expr e = termoLog(ctx.termo_logico(0));
        for (int i = 1; i < ctx.termo_logico().size(); i++) e = Expr.logico("(" + e.codigo + " || " + termoLog(ctx.termo_logico(i)).codigo + ")");
        return e;
    }

    private Expr termoLog(LAParser.Termo_logicoContext ctx) {
        Expr e = fatorLog(ctx.fator_logico(0));
        for (int i = 1; i < ctx.fator_logico().size(); i++) e = Expr.logico("(" + e.codigo + " && " + fatorLog(ctx.fator_logico(i)).codigo + ")");
        return e;
    }

    private Expr fatorLog(LAParser.Fator_logicoContext ctx) {
        Expr e = parcelaLog(ctx.parcela_logica());
        if (ctx.getText().startsWith("nao")) return Expr.logico("!(" + e.codigo + ")");
        return e;
    }

    private Expr parcelaLog(LAParser.Parcela_logicaContext ctx) {
        if (ctx.getText().equals("verdadeiro")) return Expr.logico("1");
        if (ctx.getText().equals("falso")) return Expr.logico("0");
        return expRel(ctx.exp_relacional());
    }

    private Expr expRel(LAParser.Exp_relacionalContext ctx) {
        Expr left = expArit(ctx.exp_aritmetica(0));
        if (ctx.exp_aritmetica().size() == 1) return left;
        Expr right = expArit(ctx.exp_aritmetica(1));
        String op = ctx.op_relacional().getText();
        if (op.equals("=")) op = "=="; else if (op.equals("<>")) op = "!=";
        return Expr.logico("(" + left.codigo + " " + op + " " + right.codigo + ")");
    }

    private Expr expArit(LAParser.Exp_aritmeticaContext ctx) {
        Expr e = termo(ctx.termo(0));
        for (int i = 1; i < ctx.termo().size(); i++) {
            Expr d = termo(ctx.termo(i));
            String op = ctx.op1(i - 1).getText();
            if (op.equals("+") && e.tipo.kind == CTipo.Kind.LITERAL && d.tipo.kind == CTipo.Kind.LITERAL) e = Expr.concat(e, d);
            else e = Expr.of(tipoNum(e.tipo, d.tipo), "(" + e.codigo + " " + op + " " + d.codigo + ")");
        }
        return e;
    }

    private Expr termo(LAParser.TermoContext ctx) {
        Expr e = fator(ctx.fator(0));
        for (int i = 1; i < ctx.fator().size(); i++) {
            Expr d = fator(ctx.fator(i));
            e = Expr.of(tipoNum(e.tipo, d.tipo), "(" + e.codigo + " " + ctx.op2(i - 1).getText() + " " + d.codigo + ")");
        }
        return e;
    }

    private Expr fator(LAParser.FatorContext ctx) {
        Expr e = parcela(ctx.parcela(0));
        for (int i = 1; i < ctx.parcela().size(); i++) e = Expr.of(CTipo.INTEIRO, "(" + e.codigo + " % " + parcela(ctx.parcela(i)).codigo + ")");
        return e;
    }

    private Expr parcela(LAParser.ParcelaContext ctx) {
        Expr e = ctx.parcela_unario() != null ? parcelaUnario(ctx.parcela_unario()) : parcelaNaoUnario(ctx.parcela_nao_unario());
        if (ctx.getText().startsWith("-") && e.tipo.isNumerico()) return Expr.of(e.tipo, "(-" + e.codigo + ")");
        return e;
    }

    private Expr parcelaUnario(LAParser.Parcela_unarioContext ctx) {
        if (ctx.identificador() != null) {
            CTipo t = tipoIdentificador(ctx.identificador());
            String c = identToC(ctx.identificador());
            if (ctx.getStart().getText().equals("^")) { t = t.deref(); c = "*(" + c + ")"; }
            if (t.kind == CTipo.Kind.LITERAL) return Expr.string(c);
            return Expr.of(t, c);
        }
        if (ctx.IDENT() != null) {
            CSimbolo f = escopoAtual.buscar(ctx.IDENT().getText());
            CTipo t = f == null ? CTipo.INTEIRO : f.tipo;
            return Expr.of(t, ctx.IDENT().getText() + "(" + argumentosChamada(ctx.IDENT().getText(), ctx.expressao()) + ")");
        }
        if (ctx.NUM_INT() != null) return Expr.of(CTipo.INTEIRO, ctx.NUM_INT().getText());
        if (ctx.NUM_REAL() != null) return Expr.of(CTipo.REAL, ctx.NUM_REAL().getText());
        return expr(ctx.expressao(0));
    }

    private Expr parcelaNaoUnario(LAParser.Parcela_nao_unarioContext ctx) {
        if (ctx.CADEIA() != null) return Expr.string(ctx.CADEIA().getText());
        CTipo base = tipoIdentificador(ctx.identificador());
        return Expr.of(CTipo.ponteiro(base), "&" + identToC(ctx.identificador()));
    }

    private CTipo tipoNum(CTipo a, CTipo b) { return a.kind == CTipo.Kind.REAL || b.kind == CTipo.Kind.REAL ? CTipo.REAL : CTipo.INTEIRO; }

    private CTipo resolverTipo(LAParser.TipoContext ctx, String nomeRegistro) {
        if (ctx.registro() != null) return resolverRegistro(ctx.registro(), nomeRegistro);
        return resolverTipoEstendido(ctx.tipo_estendido());
    }
    private CTipo resolverRegistro(LAParser.RegistroContext ctx, String nome) {
        Map<String, CTipo> campos = new LinkedHashMap<>();
        for (LAParser.VariavelContext v : ctx.variavel()) {
            CTipo t = resolverTipo(v.tipo(), null);
            for (LAParser.IdentificadorContext id : v.identificador()) campos.put(nomeIdentificador(id), t);
        }
        return CTipo.registro(nome, campos);
    }
    private CTipo resolverTipoEstendido(LAParser.Tipo_estendidoContext ctx) {
        CTipo base = resolverTipoBasicoIdent(ctx.tipo_basico_ident());
        return ctx.getText().startsWith("^") ? CTipo.ponteiro(base) : base;
    }
    private CTipo resolverTipoBasicoIdent(LAParser.Tipo_basico_identContext ctx) {
        if (ctx.tipo_basico() != null) return resolverTipoBasico(ctx.tipo_basico());
        CSimbolo s = escopoAtual.buscar(ctx.IDENT().getText());
        return s == null ? CTipo.INVALIDO : s.tipo;
    }
    private CTipo resolverTipoBasico(LAParser.Tipo_basicoContext ctx) {
        switch (ctx.getText()) {
            case "inteiro": return CTipo.INTEIRO;
            case "real": return CTipo.REAL;
            case "literal": return CTipo.LITERAL;
            case "logico": return CTipo.LOGICO;
            default: return CTipo.INVALIDO;
        }
    }

    private String cTipo(CTipo t) {
        if (t.kind == CTipo.Kind.INTEIRO || t.kind == CTipo.Kind.LOGICO) return "int";
        if (t.kind == CTipo.Kind.REAL) return "float";
        if (t.kind == CTipo.Kind.LITERAL) return "char*";
        if (t.kind == CTipo.Kind.PONTEIRO) return cTipo(t.apontado) + "*";
        if (t.kind == CTipo.Kind.REGISTRO && t.nomeRegistro != null) return t.nomeRegistro;
        return "int";
    }
    private String cTipoRetorno(CTipo t) { return t.kind == CTipo.Kind.LITERAL ? "char*" : cTipo(t); }

    private String declaracaoVariavel(String nome, CTipo tipo, List<String> dims) {
        String ds = String.join("", dims);
        if (tipo.kind == CTipo.Kind.LITERAL) return "char " + nome + ds + "[80]";
        if (tipo.kind == CTipo.Kind.PONTEIRO) return cTipo(tipo.apontado) + " *" + nome + ds;
        if (tipo.kind == CTipo.Kind.REGISTRO && tipo.nomeRegistro == null) return "struct { } " + nome + ds;
        return cTipo(tipo) + " " + nome + ds;
    }

    private List<String> dimensoes(LAParser.IdentificadorContext id) {
        List<String> out = new ArrayList<>();
        for (LAParser.Exp_aritmeticaContext e : id.dimensao().exp_aritmetica()) out.add("[" + expArit(e).codigo + "]");
        return out;
    }

    private String nomeIdentificador(LAParser.IdentificadorContext ctx) { return ctx.IDENT(0).getText(); }

    private CTipo tipoIdentificador(LAParser.IdentificadorContext ctx) {
        CSimbolo s = escopoAtual.buscar(ctx.IDENT(0).getText());
        if (s == null) return CTipo.INTEIRO;
        CTipo t = s.tipo;
        for (int i = 1; i < ctx.IDENT().size(); i++) if (t.kind == CTipo.Kind.REGISTRO) t = t.campos.get(ctx.IDENT(i).getText());
        return t == null ? CTipo.INTEIRO : t;
    }

    private String identToC(LAParser.IdentificadorContext ctx) {
        String base = ctx.IDENT(0).getText();
        CSimbolo s = escopoAtual.buscar(base);
        String code = (s != null && s.porReferencia && s.tipo.kind != CTipo.Kind.LITERAL) ? "(*" + base + ")" : base;
        for (int i = 1; i < ctx.IDENT().size(); i++) code += "." + ctx.IDENT(i).getText();
        for (LAParser.Exp_aritmeticaContext e : ctx.dimensao().exp_aritmetica()) code += "[" + expArit(e).codigo + "]";
        return code;
    }

    private String scanfTarget(LAParser.IdentificadorContext id, CTipo tipo) {
        String nome = id.IDENT(0).getText();
        CSimbolo s = escopoAtual.buscar(nome);
        if (tipo.kind == CTipo.Kind.LITERAL) return identToC(id);
        if (s != null && s.porReferencia) return nome;
        return "&" + identToC(id);
    }

    private String valorConstante(LAParser.Valor_constanteContext ctx) {
        if (ctx.getText().equals("verdadeiro")) return "1";
        if (ctx.getText().equals("falso")) return "0";
        return ctx.getText();
    }

    private static class Expr {
        final CTipo tipo; final String codigo; final List<String> partesString;
        Expr(CTipo tipo, String codigo, List<String> partesString) { this.tipo = tipo; this.codigo = codigo; this.partesString = partesString; }
        static Expr of(CTipo t, String c) { return new Expr(t, c, new ArrayList<>()); }
        static Expr logico(String c) { return of(CTipo.LOGICO, c); }
        static Expr string(String c) { List<String> p = new ArrayList<>(); p.add(c); return new Expr(CTipo.LITERAL, c, p); }
        static Expr concat(Expr a, Expr b) { List<String> p = new ArrayList<>(); p.addAll(a.partesString); p.addAll(b.partesString); return new Expr(CTipo.LITERAL, p.get(0), p); }
        String codigoString() { return partesString.isEmpty() ? codigo : (partesString.size() == 1 ? partesString.get(0) : codigo); }
    }
}
