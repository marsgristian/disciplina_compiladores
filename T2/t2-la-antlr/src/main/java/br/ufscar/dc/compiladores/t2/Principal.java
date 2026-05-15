package br.ufscar.dc.compiladores.t2;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Classe principal do analisador sintático do T2.
 *
 * Responsabilidades:
 * 1. Ler o arquivo de entrada recebido em args[0].
 * 2. Executar o lexer e detectar primeiro erro léxico, se existir.
 * 3. Executar o parser e detectar primeiro erro sintático, se existir.
 * 4. Gravar a saída obrigatoriamente no arquivo recebido em args[1].
 *
 * O programa não imprime nada no terminal porque o corretor automático compara
 * apenas o arquivo de saída produzido.
 */
public class Principal {

    public static void main(String[] args) {
        if (args.length != 2) {
            return;
        }

        Path arquivoEntrada = Path.of(args[0]);
        Path arquivoSaida = Path.of(args[1]);

        List<String> saida = new ArrayList<>();

        try {
            CharStream input = CharStreams.fromPath(arquivoEntrada, StandardCharsets.UTF_8);

            LALexer lexer = new LALexer(input);
            lexer.removeErrorListeners();

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            Token erroLexico = encontrarPrimeiroErroLexico(tokens.getTokens());

            if (erroLexico != null) {
                saida.add(formatarErroLexico(erroLexico));
                saida.add("Fim da compilacao");
                escreverSaida(arquivoSaida, saida);
                return;
            }

            LAParser parser = new LAParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new PrimeiroErroSintaticoListener(saida));

            try {
                parser.programa();
            } catch (ParseCancellationException ignored) {
                // O listener já registrou a mensagem do primeiro erro sintático.
            }

            saida.add("Fim da compilacao");
            escreverSaida(arquivoSaida, saida);

        } catch (IOException e) {
            // O corretor costuma fornecer caminhos válidos. Não imprimir no terminal.
        }
    }

    /**
     * Percorre todos os tokens antes da análise sintática para preservar a
     * prioridade dos erros léxicos do T1.
     */
    private static Token encontrarPrimeiroErroLexico(List<Token> tokens) {
        for (Token token : tokens) {
            int type = token.getType();

            if (type == LALexer.ERRO_CADEIA
                    || type == LALexer.ERRO_COMENTARIO
                    || type == LALexer.ERRO) {
                return token;
            }
        }

        return null;
    }

    /**
     * Converte o token léxico inválido na mensagem exigida pelo corretor.
     */
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

    /**
     * Grava as linhas no arquivo de saída, criando diretórios intermediários
     * quando necessário.
     */
    private static void escreverSaida(Path arquivoSaida, List<String> linhas) throws IOException {
        Path parent = arquivoSaida.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        String conteudo = String.join(System.lineSeparator(), linhas) + System.lineSeparator();
        Files.writeString(arquivoSaida, conteudo, StandardCharsets.UTF_8);
    }

    /**
     * Listener customizado para substituir a mensagem padrão do ANTLR pela
     * mensagem exata esperada pelo T2:
     *
     * Linha X: erro sintatico proximo a TOKEN
     */
    private static class PrimeiroErroSintaticoListener extends BaseErrorListener {
        private final List<String> saida;

        PrimeiroErroSintaticoListener(List<String> saida) {
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

            saida.add("Linha " + line + ": erro sintatico proximo a " + lexema);

            // Interrompe a análise no primeiro erro, como exigido pelo trabalho.
            throw new ParseCancellationException();
        }
    }
}
