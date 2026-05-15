package br.ufscar.dc.compiladores.t2manual;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * T2 sem ANTLR: analisador léxico + analisador sintático descendente recursivo.
 *
 * O programa recebe exatamente dois argumentos:
 * args[0] = caminho do arquivo de entrada
 * args[1] = caminho do arquivo de saída
 *
 * Nenhuma saída é impressa no terminal. Tudo é gravado no arquivo indicado.
 */
public class Principal {

    public static void main(String[] args) {
        if (args.length != 2) {
            return;
        }

        Path entrada = Path.of(args[0]);
        Path saida = Path.of(args[1]);
        List<String> linhasSaida = new ArrayList<>();

        try {
            String fonte = Files.readString(entrada, StandardCharsets.UTF_8);
            Lexer lexer = new Lexer(fonte);
            List<Token> tokens = lexer.scan();
            Parser parser = new Parser(tokens);
            parser.programa();
            linhasSaida.add("Fim da compilacao");
        } catch (LexicalException e) {
            linhasSaida.add(e.getMessage());
            linhasSaida.add("Fim da compilacao");
        } catch (SyntaxException e) {
            linhasSaida.add("Linha " + e.line + ": erro sintatico proximo a " + e.lexeme);
            linhasSaida.add("Fim da compilacao");
        } catch (IOException e) {
            return;
        }

        try {
            Path parent = saida.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(saida, String.join(System.lineSeparator(), linhasSaida) + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private enum TokenKind {
        IDENT, NUM_INT, NUM_REAL, CADEIA, KEYWORD, SYMBOL, EOF
    }

    private static class Token {
        final TokenKind kind;
        final String lexeme;
        final int line;

        Token(TokenKind kind, String lexeme, int line) {
            this.kind = kind;
            this.lexeme = lexeme;
            this.line = line;
        }

        boolean isEOF() {
            return kind == TokenKind.EOF;
        }
    }

    private static class LexicalException extends RuntimeException {
        LexicalException(String message) {
            super(message);
        }
    }

    private static class SyntaxException extends RuntimeException {
        final int line;
        final String lexeme;

        SyntaxException(Token token) {
            this.line = token.line;
            this.lexeme = token.isEOF() ? "EOF" : token.lexeme;
        }
    }

    /**
     * Analisador léxico manual da LA.
     */
    private static class Lexer {
        private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
                "algoritmo", "declare", "literal", "inteiro", "real", "logico",
                "leia", "escreva", "fim_algoritmo",
                "tipo", "var", "constante", "registro", "fim_registro",
                "procedimento", "fim_procedimento", "funcao", "fim_funcao", "retorne",
                "se", "entao", "senao", "fim_se",
                "caso", "seja", "fim_caso",
                "para", "ate", "faca", "fim_para",
                "enquanto", "fim_enquanto",
                "verdadeiro", "falso", "e", "ou", "nao"
        ));

        private final String input;
        private final List<Token> tokens = new ArrayList<>();
        private int pos = 0;
        private int line = 1;

        Lexer(String input) {
            this.input = input;
        }

        List<Token> scan() {
            while (!isAtEnd()) {
                char c = peek();

                if (isWhitespace(c)) {
                    consumeWhitespace();
                    continue;
                }

                if (c == '{') {
                    consumeComment();
                    continue;
                }

                if (c == '"') {
                    consumeString();
                    continue;
                }

                if (isLetter(c)) {
                    consumeIdentifierOrKeyword();
                    continue;
                }

                if (isDigit(c)) {
                    consumeNumber();
                    continue;
                }

                if (tryConsumeDoubleSymbol()) {
                    continue;
                }

                if (isSingleSymbol(c)) {
                    add(TokenKind.SYMBOL, String.valueOf(c));
                    pos++;
                    continue;
                }

                throw new LexicalException("Linha " + line + ": " + c + " - simbolo nao identificado");
            }

            tokens.add(new Token(TokenKind.EOF, "EOF", line));
            return tokens;
        }

        private boolean isAtEnd() {
            return pos >= input.length();
        }

        private char peek() {
            return input.charAt(pos);
        }

        private char peekNext() {
            if (pos + 1 >= input.length()) {
                return '\0';
            }
            return input.charAt(pos + 1);
        }

        private void add(TokenKind kind, String lexeme) {
            tokens.add(new Token(kind, lexeme, line));
        }

        private boolean isWhitespace(char c) {
            return c == ' ' || c == '\t' || c == '\f' || c == '\n' || c == '\r';
        }

        private void consumeWhitespace() {
            char c = peek();
            if (c == '\r') {
                line++;
                pos++;
                if (!isAtEnd() && peek() == '\n') {
                    pos++;
                }
                return;
            }
            if (c == '\n') {
                line++;
                pos++;
                return;
            }
            pos++;
        }

        private void consumeComment() {
            int startLine = line;
            pos++;
            while (!isAtEnd()) {
                char c = peek();
                if (c == '}') {
                    pos++;
                    return;
                }
                if (c == '\n' || c == '\r') {
                    throw new LexicalException("Linha " + startLine + ": comentario nao fechado");
                }
                pos++;
            }
            throw new LexicalException("Linha " + startLine + ": comentario nao fechado");
        }

        private void consumeString() {
            int startLine = line;
            StringBuilder sb = new StringBuilder();
            sb.append('"');
            pos++;

            while (!isAtEnd()) {
                char c = peek();
                if (c == '"') {
                    sb.append('"');
                    pos++;
                    add(TokenKind.CADEIA, sb.toString());
                    return;
                }
                if (c == '\n' || c == '\r') {
                    throw new LexicalException("Linha " + startLine + ": cadeia literal nao fechada");
                }
                if (c == '\\') {
                    sb.append(c);
                    pos++;
                    if (!isAtEnd()) {
                        char next = peek();
                        if (next == '\n' || next == '\r') {
                            throw new LexicalException("Linha " + startLine + ": cadeia literal nao fechada");
                        }
                        sb.append(next);
                        pos++;
                    }
                    continue;
                }
                sb.append(c);
                pos++;
            }
            throw new LexicalException("Linha " + startLine + ": cadeia literal nao fechada");
        }

        private void consumeIdentifierOrKeyword() {
            int start = pos;
            pos++;
            while (!isAtEnd()) {
                char c = peek();
                if (isLetter(c) || isDigit(c) || c == '_') {
                    pos++;
                } else {
                    break;
                }
            }
            String lexeme = input.substring(start, pos);
            add(KEYWORDS.contains(lexeme) ? TokenKind.KEYWORD : TokenKind.IDENT, lexeme);
        }

        private void consumeNumber() {
            int start = pos;
            while (!isAtEnd() && isDigit(peek())) {
                pos++;
            }
            if (!isAtEnd() && peek() == '.' && isDigit(peekNext())) {
                pos++;
                while (!isAtEnd() && isDigit(peek())) {
                    pos++;
                }
                add(TokenKind.NUM_REAL, input.substring(start, pos));
                return;
            }
            add(TokenKind.NUM_INT, input.substring(start, pos));
        }

        private boolean tryConsumeDoubleSymbol() {
            if (pos + 1 >= input.length()) {
                return false;
            }
            String two = input.substring(pos, pos + 2);
            if (two.equals("<-") || two.equals("<>") || two.equals("<=") || two.equals(">=") || two.equals("..")) {
                add(TokenKind.SYMBOL, two);
                pos += 2;
                return true;
            }
            return false;
        }

        private boolean isSingleSymbol(char c) {
            return c == ':' || c == '(' || c == ')' || c == '[' || c == ']' || c == ','
                    || c == '+' || c == '-' || c == '*' || c == '/' || c == '%'
                    || c == '<' || c == '>' || c == '=' || c == '^' || c == '&' || c == '.';
        }

        private boolean isLetter(char c) {
            return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
        }

        private boolean isDigit(char c) {
            return c >= '0' && c <= '9';
        }
    }

    /**
     * Parser descendente recursivo da LA.
     * Cada método corresponde a uma regra da gramática.
     */
    private static class Parser {
        private final List<Token> tokens;
        private int current = 0;

        Parser(List<Token> tokens) {
            this.tokens = tokens;
        }

        // programa : declaracoes 'algoritmo' corpo 'fim_algoritmo' EOF ;
        void programa() {
            declaracoes();
            consume("algoritmo");
            corpo();
            consume("fim_algoritmo");
            consumeEOF();
        }

        // declaracoes : decl_local_global* ;
        private void declaracoes() {
            while (startsDeclLocalGlobal()) {
                declLocalGlobal();
            }
        }

        // decl_local_global : declaracao_local | declaracao_global ;
        private void declLocalGlobal() {
            if (startsDeclaracaoLocal()) {
                declaracaoLocal();
                return;
            }
            if (check("procedimento") || check("funcao")) {
                declaracaoGlobal();
                return;
            }
            error();
        }

        // declaracao_local : 'declare' variavel | 'constante' IDENT ':' tipo_basico '=' valor_constante | 'tipo' IDENT ':' tipo ;
        private void declaracaoLocal() {
            if (match("declare")) {
                variavel();
                return;
            }
            if (match("constante")) {
                consumeIdent();
                consume(":");
                tipoBasico();
                consume("=");
                valorConstante();
                return;
            }
            if (match("tipo")) {
                consumeIdent();
                consume(":");
                tipo();
                return;
            }
            error();
        }

        // variavel : identificador (',' identificador)* ':' tipo ;
        private void variavel() {
            identificador();
            while (match(",")) {
                identificador();
            }
            consume(":");
            tipo();
        }

        // identificador : IDENT ('.' IDENT)* dimensao ;
        private void identificador() {
            consumeIdent();
            while (match(".")) {
                consumeIdent();
            }
            dimensao();
        }

        // dimensao : ('[' exp_aritmetica ']')* ;
        private void dimensao() {
            while (match("[")) {
                expAritmetica();
                consume("]");
            }
        }

        // tipo : registro | tipo_estendido ;
        private void tipo() {
            if (check("registro")) {
                registro();
            } else {
                tipoEstendido();
            }
        }

        // tipo_basico : 'literal' | 'inteiro' | 'real' | 'logico' ;
        private void tipoBasico() {
            if (match("literal") || match("inteiro") || match("real") || match("logico")) {
                return;
            }
            error();
        }

        // tipo_basico_ident : tipo_basico | IDENT ;
        private void tipoBasicoIdent() {
            if (check("literal") || check("inteiro") || check("real") || check("logico")) {
                tipoBasico();
                return;
            }
            consumeIdent();
        }

        // tipo_estendido : '^'? tipo_basico_ident ;
        private void tipoEstendido() {
            match("^");
            tipoBasicoIdent();
        }

        // valor_constante : CADEIA | NUM_INT | NUM_REAL | 'verdadeiro' | 'falso' ;
        private void valorConstante() {
            if (matchKind(TokenKind.CADEIA) || matchKind(TokenKind.NUM_INT) || matchKind(TokenKind.NUM_REAL)
                    || match("verdadeiro") || match("falso")) {
                return;
            }
            error();
        }

        // registro : 'registro' variavel* 'fim_registro' ;
        private void registro() {
            consume("registro");
            while (checkKind(TokenKind.IDENT)) {
                variavel();
            }
            consume("fim_registro");
        }

        // declaracao_global : procedimento ... | funcao ... ;
        private void declaracaoGlobal() {
            if (match("procedimento")) {
                consumeIdent();
                consume("(");
                if (startsParametro()) {
                    parametros();
                }
                consume(")");
                while (startsDeclaracaoLocal()) {
                    declaracaoLocal();
                }
                while (startsCmd()) {
                    cmd();
                }
                consume("fim_procedimento");
                return;
            }

            if (match("funcao")) {
                consumeIdent();
                consume("(");
                if (startsParametro()) {
                    parametros();
                }
                consume(")");
                consume(":");
                tipoEstendido();
                while (startsDeclaracaoLocal()) {
                    declaracaoLocal();
                }
                while (startsCmd()) {
                    cmd();
                }
                consume("fim_funcao");
                return;
            }

            error();
        }

        // parametro : 'var'? identificador (',' identificador)* ':' tipo_estendido ;
        private void parametro() {
            match("var");
            identificador();
            while (match(",")) {
                identificador();
            }
            consume(":");
            tipoEstendido();
        }

        // parametros : parametro (',' parametro)* ;
        private void parametros() {
            parametro();
            while (match(",")) {
                parametro();
            }
        }

        // corpo : declaracao_local* cmd* ;
        private void corpo() {
            while (startsDeclaracaoLocal()) {
                declaracaoLocal();
            }
            while (startsCmd()) {
                cmd();
            }
        }

        // cmd : cmdLeia | cmdEscreva | cmdSe | cmdCaso | cmdPara | cmdEnquanto | cmdFaca | cmdAtribuicao | cmdChamada | cmdRetorne ;
        private void cmd() {
            if (check("leia")) {
                cmdLeia();
                return;
            }
            if (check("escreva")) {
                cmdEscreva();
                return;
            }
            if (check("se")) {
                cmdSe();
                return;
            }
            if (check("caso")) {
                cmdCaso();
                return;
            }
            if (check("para")) {
                cmdPara();
                return;
            }
            if (check("enquanto")) {
                cmdEnquanto();
                return;
            }
            if (check("faca")) {
                cmdFaca();
                return;
            }
            if (check("retorne")) {
                cmdRetorne();
                return;
            }
            if (check("^")) {
                cmdAtribuicao();
                return;
            }
            if (checkKind(TokenKind.IDENT)) {
                if (lookaheadLexeme(1).equals("(")) {
                    cmdChamada();
                } else {
                    cmdAtribuicao();
                }
                return;
            }
            error();
        }

        // cmdLeia : 'leia' '(' '^'? identificador (',' '^'? identificador)* ')' ;
        private void cmdLeia() {
            consume("leia");
            consume("(");
            match("^");
            identificador();
            while (match(",")) {
                match("^");
                identificador();
            }
            consume(")");
        }

        // cmdEscreva : 'escreva' '(' expressao (',' expressao)* ')' ;
        private void cmdEscreva() {
            consume("escreva");
            consume("(");
            expressao();
            while (match(",")) {
                expressao();
            }
            consume(")");
        }

        // cmdSe : 'se' expressao 'entao' cmd* ('senao' cmd*)? 'fim_se' ;
        private void cmdSe() {
            consume("se");
            expressao();
            consume("entao");
            while (startsCmd()) {
                cmd();
            }
            if (match("senao")) {
                while (startsCmd()) {
                    cmd();
                }
            }
            consume("fim_se");
        }

        // cmdCaso : 'caso' exp_aritmetica 'seja' selecao ('senao' cmd*)? 'fim_caso' ;
        private void cmdCaso() {
            consume("caso");
            expAritmetica();
            consume("seja");
            selecao();
            if (match("senao")) {
                while (startsCmd()) {
                    cmd();
                }
            }
            consume("fim_caso");
        }

        // cmdPara : 'para' IDENT '<-' exp_aritmetica 'ate' exp_aritmetica 'faca' cmd* 'fim_para' ;
        private void cmdPara() {
            consume("para");
            consumeIdent();
            consume("<-");
            expAritmetica();
            consume("ate");
            expAritmetica();
            consume("faca");
            while (startsCmd()) {
                cmd();
            }
            consume("fim_para");
        }

        // cmdEnquanto : 'enquanto' expressao 'faca' cmd* 'fim_enquanto' ;
        private void cmdEnquanto() {
            consume("enquanto");
            expressao();
            consume("faca");
            while (startsCmd()) {
                cmd();
            }
            consume("fim_enquanto");
        }

        // cmdFaca : 'faca' cmd* 'ate' expressao ;
        private void cmdFaca() {
            consume("faca");
            while (startsCmd()) {
                cmd();
            }
            consume("ate");
            expressao();
        }

        // cmdAtribuicao : '^'? identificador '<-' expressao ;
        private void cmdAtribuicao() {
            match("^");
            identificador();
            consume("<-");
            expressao();
        }

        // cmdChamada : IDENT '(' expressao (',' expressao)* ')' ;
        private void cmdChamada() {
            consumeIdent();
            consume("(");
            expressao();
            while (match(",")) {
                expressao();
            }
            consume(")");
        }

        // cmdRetorne : 'retorne' expressao ;
        private void cmdRetorne() {
            consume("retorne");
            expressao();
        }

        // selecao : item_selecao* ;
        private void selecao() {
            while (startsItemSelecao()) {
                itemSelecao();
            }
        }

        // item_selecao : constantes ':' cmd* ;
        private void itemSelecao() {
            constantes();
            consume(":");
            while (startsCmd()) {
                cmd();
            }
        }

        // constantes : numero_intervalo (',' numero_intervalo)* ;
        private void constantes() {
            numeroIntervalo();
            while (match(",")) {
                numeroIntervalo();
            }
        }

        // numero_intervalo : op_unario? NUM_INT ('..' op_unario? NUM_INT)? ;
        private void numeroIntervalo() {
            opUnarioOptional();
            consumeKind(TokenKind.NUM_INT);
            if (match("..")) {
                opUnarioOptional();
                consumeKind(TokenKind.NUM_INT);
            }
        }

        private void opUnarioOptional() {
            match("-");
        }

        // exp_aritmetica : termo (op1 termo)* ;
        private void expAritmetica() {
            termo();
            while (check("+") || check("-")) {
                op1();
                termo();
            }
        }

        // termo : fator (op2 fator)* ;
        private void termo() {
            fator();
            while (check("*") || check("/")) {
                op2();
                fator();
            }
        }

        // fator : parcela (op3 parcela)* ;
        private void fator() {
            parcela();
            while (check("%")) {
                op3();
                parcela();
            }
        }

        private void op1() {
            if (match("+") || match("-")) {
                return;
            }
            error();
        }

        private void op2() {
            if (match("*") || match("/")) {
                return;
            }
            error();
        }

        private void op3() {
            consume("%");
        }

        // parcela : op_unario? parcela_unario | parcela_nao_unario ;
        private void parcela() {
            if (check("-")) {
                consume("-");
                parcelaUnario();
                return;
            }
            if (startsParcelaUnario()) {
                parcelaUnario();
                return;
            }
            if (startsParcelaNaoUnario()) {
                parcelaNaoUnario();
                return;
            }
            error();
        }

        // parcela_unario : '^'? identificador | IDENT '(' expressao (',' expressao)* ')' | NUM_INT | NUM_REAL | '(' expressao ')' ;
        private void parcelaUnario() {
            if (match("^")) {
                identificador();
                return;
            }
            if (checkKind(TokenKind.IDENT)) {
                if (lookaheadLexeme(1).equals("(")) {
                    consumeIdent();
                    consume("(");
                    expressao();
                    while (match(",")) {
                        expressao();
                    }
                    consume(")");
                } else {
                    identificador();
                }
                return;
            }
            if (matchKind(TokenKind.NUM_INT) || matchKind(TokenKind.NUM_REAL)) {
                return;
            }
            if (match("(")) {
                expressao();
                consume(")");
                return;
            }
            error();
        }

        // parcela_nao_unario : '&' identificador | CADEIA ;
        private void parcelaNaoUnario() {
            if (match("&")) {
                identificador();
                return;
            }
            if (matchKind(TokenKind.CADEIA)) {
                return;
            }
            error();
        }

        // exp_relacional : exp_aritmetica (op_relacional exp_aritmetica)? ;
        private void expRelacional() {
            expAritmetica();
            if (startsOpRelacional()) {
                opRelacional();
                expAritmetica();
            }
        }

        private void opRelacional() {
            if (match("=") || match("<>") || match(">=") || match("<=") || match(">") || match("<")) {
                return;
            }
            error();
        }

        // expressao : termo_logico ('ou' termo_logico)* ;
        private void expressao() {
            termoLogico();
            while (match("ou")) {
                termoLogico();
            }
        }

        // termo_logico : fator_logico ('e' fator_logico)* ;
        private void termoLogico() {
            fatorLogico();
            while (match("e")) {
                fatorLogico();
            }
        }

        // fator_logico : 'nao'? parcela_logica ;
        private void fatorLogico() {
            match("nao");
            parcelaLogica();
        }

        // parcela_logica : 'verdadeiro' | 'falso' | exp_relacional ;
        private void parcelaLogica() {
            if (match("verdadeiro") || match("falso")) {
                return;
            }
            expRelacional();
        }

        private boolean startsDeclLocalGlobal() {
            return startsDeclaracaoLocal() || check("procedimento") || check("funcao");
        }

        private boolean startsDeclaracaoLocal() {
            return check("declare") || check("constante") || check("tipo");
        }

        private boolean startsParametro() {
            return check("var") || checkKind(TokenKind.IDENT);
        }

        private boolean startsCmd() {
            return check("leia") || check("escreva") || check("se") || check("caso")
                    || check("para") || check("enquanto") || check("faca") || check("retorne")
                    || check("^") || checkKind(TokenKind.IDENT);
        }

        private boolean startsItemSelecao() {
            return check("-") || checkKind(TokenKind.NUM_INT);
        }

        private boolean startsParcelaUnario() {
            return check("^") || checkKind(TokenKind.IDENT) || checkKind(TokenKind.NUM_INT)
                    || checkKind(TokenKind.NUM_REAL) || check("(");
        }

        private boolean startsParcelaNaoUnario() {
            return check("&") || checkKind(TokenKind.CADEIA);
        }

        private boolean startsOpRelacional() {
            return check("=") || check("<>") || check(">=") || check("<=") || check(">") || check("<");
        }

        private Token peek() {
            return tokens.get(current);
        }

        private Token lookahead(int distance) {
            int index = current + distance;
            if (index >= tokens.size()) {
                return tokens.get(tokens.size() - 1);
            }
            return tokens.get(index);
        }

        private String lookaheadLexeme(int distance) {
            return lookahead(distance).lexeme;
        }

        private boolean check(String lexeme) {
            if (peek().isEOF()) {
                return false;
            }
            return peek().lexeme.equals(lexeme);
        }

        private boolean checkKind(TokenKind kind) {
            return peek().kind == kind;
        }

        private boolean match(String lexeme) {
            if (check(lexeme)) {
                advance();
                return true;
            }
            return false;
        }

        private boolean matchKind(TokenKind kind) {
            if (checkKind(kind)) {
                advance();
                return true;
            }
            return false;
        }

        private Token consume(String lexeme) {
            if (check(lexeme)) {
                return advance();
            }
            error();
            return null;
        }

        private Token consumeKind(TokenKind kind) {
            if (checkKind(kind)) {
                return advance();
            }
            error();
            return null;
        }

        private Token consumeIdent() {
            return consumeKind(TokenKind.IDENT);
        }

        private void consumeEOF() {
            if (!peek().isEOF()) {
                error();
            }
        }

        private Token advance() {
            if (!peek().isEOF()) {
                current++;
            }
            return tokens.get(current - 1);
        }

        private void error() {
            throw new SyntaxException(peek());
        }
    }
}
