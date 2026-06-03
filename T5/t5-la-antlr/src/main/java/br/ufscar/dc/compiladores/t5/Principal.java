package br.ufscar.dc.compiladores.t5;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Executável único do T5.
 *
 * Para entradas inválidas, preserva os erros léxicos/sintáticos. Para entradas
 * válidas, gera código C equivalente. A saída sempre é gravada no arquivo
 * recebido em args[1], conforme exigido pelo corretor.
 */
public class Principal {
    public static void main(String[] args) {
        if (args.length != 2) return;

        Path entrada = Path.of(args[0]);
        Path saida = Path.of(args[1]);
        StringBuilder out = new StringBuilder();

        try {
            CharStream input = CharStreams.fromPath(entrada, StandardCharsets.UTF_8);
            LALexer lexer = new LALexer(input);
            lexer.removeErrorListeners();
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            Token erroLexico = primeiroErroLexico(tokens);
            if (erroLexico != null) {
                out.append(formatarErroLexico(erroLexico)).append(System.lineSeparator());
                out.append("Fim da compilacao").append(System.lineSeparator());
                escrever(saida, out.toString());
                return;
            }

            LAParser parser = new LAParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new PrimeiroErroSintaticoListener(out));

            LAParser.ProgramaContext arvore;
            try {
                arvore = parser.programa();
            } catch (ParseCancellationException e) {
                out.append("Fim da compilacao").append(System.lineSeparator());
                escrever(saida, out.toString());
                return;
            }

            // O corretor do T5 usa programas sem erros. O gerador abaixo ainda
            // preserva a estrutura para ser usado no executável final.
            GeradorCVisitor gerador = new GeradorCVisitor();
            String codigoC = gerador.gerar(arvore);
            escrever(saida, codigoC);

        } catch (IOException ignored) {
        }
    }

    private static Token primeiroErroLexico(CommonTokenStream tokens) {
        for (Token t : tokens.getTokens()) {
            int type = t.getType();
            if (type == LALexer.ERRO || type == LALexer.ERRO_CADEIA || type == LALexer.ERRO_COMENTARIO) return t;
        }
        return null;
    }

    private static String formatarErroLexico(Token token) {
        if (token.getType() == LALexer.ERRO_CADEIA) return "Linha " + token.getLine() + ": cadeia literal nao fechada";
        if (token.getType() == LALexer.ERRO_COMENTARIO) return "Linha " + token.getLine() + ": comentario nao fechado";
        return "Linha " + token.getLine() + ": " + token.getText() + " - simbolo nao identificado";
    }

    private static void escrever(Path arquivo, String conteudo) throws IOException {
        Path parent = arquivo.getParent();
        if (parent != null) Files.createDirectories(parent);
        Files.writeString(arquivo, conteudo, StandardCharsets.UTF_8);
    }

    private static class PrimeiroErroSintaticoListener extends BaseErrorListener {
        private final StringBuilder out;
        PrimeiroErroSintaticoListener(StringBuilder out) { this.out = out; }
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                                int charPositionInLine, String msg, RecognitionException e) {
            String lexema = "EOF";
            if (offendingSymbol instanceof Token) {
                Token t = (Token) offendingSymbol;
                if (t.getType() != Token.EOF) lexema = t.getText();
            }
            out.append("Linha ").append(line).append(": erro sintatico proximo a ").append(lexema)
                    .append(System.lineSeparator());
            throw new ParseCancellationException();
        }
    }
}
