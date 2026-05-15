package br.ufscar.dc.compiladores.t4;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Ponto de entrada do T4.
 *
 * Mantém o comportamento dos trabalhos anteriores:
 * - erro léxico interrompe antes da análise sintática;
 * - erro sintático interrompe antes da análise semântica;
 * - erros semânticos são acumulados até o fim do arquivo.
 */
public class Principal {

    public static void main(String[] args) {
        if (args.length != 2) {
            return;
        }

        Path arquivoEntrada = Path.of(args[0]);
        Path arquivoSaida = Path.of(args[1]);

        StringBuilder saida = new StringBuilder();

        try {
            CharStream input = CharStreams.fromPath(arquivoEntrada, StandardCharsets.UTF_8);

            LALexer lexer = new LALexer(input);
            lexer.removeErrorListeners();

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            Token erroLexico = encontrarPrimeiroErroLexico(tokens);

            if (erroLexico != null) {
                saida.append(formatarErroLexico(erroLexico)).append(System.lineSeparator());
                saida.append("Fim da compilacao").append(System.lineSeparator());
                escreverSaida(arquivoSaida, saida.toString());
                return;
            }

            LAParser parser = new LAParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new PrimeiroErroSintaticoListener(saida));

            LAParser.ProgramaContext arvore;

            try {
                arvore = parser.programa();
            } catch (ParseCancellationException ignored) {
                saida.append("Fim da compilacao").append(System.lineSeparator());
                escreverSaida(arquivoSaida, saida.toString());
                return;
            }

            SemanticoVisitor semantico = new SemanticoVisitor();
            semantico.visitPrograma(arvore);

            for (String erro : semantico.getErros()) {
                saida.append(erro).append(System.lineSeparator());
            }

            saida.append("Fim da compilacao").append(System.lineSeparator());
            escreverSaida(arquivoSaida, saida.toString());

        } catch (IOException e) {
            // O corretor fornece caminhos válidos. Não imprimir no terminal.
        }
    }

    private static Token encontrarPrimeiroErroLexico(CommonTokenStream tokens) {
        for (Token token : tokens.getTokens()) {
            int type = token.getType();

            if (type == LALexer.ERRO_CADEIA
                    || type == LALexer.ERRO_COMENTARIO
                    || type == LALexer.ERRO) {
                return token;
            }
        }

        return null;
    }

    private static String formatarErroLexico(Token token) {
        int linha = token.getLine();

        if (token.getType() == LALexer.ERRO_CADEIA) {
            return "Linha " + linha + ": cadeia literal nao fechada";
        }

        if (token.getType() == LALexer.ERRO_COMENTARIO) {
            return "Linha " + linha + ": comentario nao fechado";
        }

        return "Linha " + linha + ": " + token.getText() + " - simbolo nao identificado";
    }

    private static void escreverSaida(Path arquivoSaida, String conteudo) throws IOException {
        Path parent = arquivoSaida.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.writeString(arquivoSaida, conteudo, StandardCharsets.UTF_8);
    }

    /**
     * Listener que preserva a mensagem exata de erro sintático do T2.
     */
    private static class PrimeiroErroSintaticoListener extends BaseErrorListener {
        private final StringBuilder saida;

        PrimeiroErroSintaticoListener(StringBuilder saida) {
            this.saida = saida;
        }

        @Override
        public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg,
                RecognitionException e
        ) {
            String lexema = "EOF";

            if (offendingSymbol instanceof Token) {
                Token token = (Token) offendingSymbol;

                if (token.getType() != Token.EOF) {
                    lexema = token.getText();
                }
            }

            saida.append("Linha ")
                    .append(line)
                    .append(": erro sintatico proximo a ")
                    .append(lexema)
                    .append(System.lineSeparator());

            throw new ParseCancellationException();
        }
    }
}
