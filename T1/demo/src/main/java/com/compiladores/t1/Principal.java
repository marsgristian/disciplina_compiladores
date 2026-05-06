package com.compiladores.t1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Principal {

    private static final Set<String> PALAVRAS_RESERVADAS = new HashSet<String>(Arrays.asList(
            "algoritmo",
            "declare",
            "literal",
            "inteiro",
            "real",
            "logico",
            "leia",
            "escreva",
            "fim_algoritmo",

            "tipo",
            "var",
            "constante",
            "registro",
            "fim_registro",

            "procedimento",
            "fim_procedimento",
            "funcao",
            "fim_funcao",
            "retorne",

            "se",
            "entao",
            "senao",
            "fim_se",

            "caso",
            "seja",
            "fim_caso",

            "para",
            "ate",
            "faca",
            "fim_para",

            "enquanto",
            "fim_enquanto",

            "verdadeiro",
            "falso",

            "e",
            "ou",
            "nao"
    ));

    public static void main(String[] args) {
        if (args.length != 2) {
            return;
        }

        Path entrada = Paths.get(args[0]);
        Path saida = Paths.get(args[1]);

        try {
            String fonte = new String(Files.readAllBytes(entrada), StandardCharsets.UTF_8);
            List<String> linhasSaida = analisar(fonte);

            Path parent = saida.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            String conteudoSaida = String.join(System.lineSeparator(), linhasSaida);

            if (!linhasSaida.isEmpty()) {
                conteudoSaida += System.lineSeparator();
            }

            Files.write(saida, conteudoSaida.getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {
            /*
             * O corretor normalmente fornece caminhos válidos.
             * Não imprimir no terminal, pois o enunciado exige saída em arquivo.
             */
        }
    }

    private static List<String> analisar(String fonte) {
        List<String> saida = new ArrayList<String>();

        int i = 0;
        int linha = 1;
        int n = fonte.length();

        while (i < n) {
            char c = fonte.charAt(i);

            if (c == '\n') {
                linha++;
                i++;
                continue;
            }

            if (c == '\r') {
                linha++;
                if (i + 1 < n && fonte.charAt(i + 1) == '\n') {
                    i += 2;
                } else {
                    i++;
                }
                continue;
            }

            if (c == ' ' || c == '\t' || c == '\f') {
                i++;
                continue;
            }

            if (c == '{') {
                int linhaComentario = linha;
                i++;

                boolean fechado = false;

                while (i < n) {
                    char atual = fonte.charAt(i);

                    if (atual == '}') {
                        i++;
                        fechado = true;
                        break;
                    }

                    if (atual == '\n' || atual == '\r') {
                        saida.add("Linha " + linhaComentario + ": comentario nao fechado");
                        return saida;
                    }

                    i++;
                }

                if (!fechado) {
                    saida.add("Linha " + linhaComentario + ": comentario nao fechado");
                    return saida;
                }

                continue;
            }

            if (c == '"') {
                int linhaCadeia = linha;
                StringBuilder cadeia = new StringBuilder();

                cadeia.append(c);
                i++;

                boolean fechada = false;

                while (i < n) {
                    char atual = fonte.charAt(i);

                    if (atual == '"') {
                        cadeia.append(atual);
                        i++;
                        fechada = true;
                        break;
                    }

                    if (atual == '\n' || atual == '\r') {
                        saida.add("Linha " + linhaCadeia + ": cadeia literal nao fechada");
                        return saida;
                    }

                    cadeia.append(atual);
                    i++;
                }

                if (!fechada) {
                    saida.add("Linha " + linhaCadeia + ": cadeia literal nao fechada");
                    return saida;
                }

                saida.add("<'" + cadeia.toString() + "',CADEIA>");
                continue;
            }

            if (isLetra(c)) {
                int inicio = i;
                i++;

                while (i < n) {
                    char atual = fonte.charAt(i);

                    if (isLetra(atual) || isDigito(atual) || atual == '_') {
                        i++;
                    } else {
                        break;
                    }
                }

                String lexema = fonte.substring(inicio, i);

                if (PALAVRAS_RESERVADAS.contains(lexema)) {
                    saida.add(tokenLiteral(lexema));
                } else {
                    saida.add("<'" + lexema + "',IDENT>");
                }

                continue;
            }

            if (isDigito(c)) {
                int inicio = i;
                i++;

                while (i < n && isDigito(fonte.charAt(i))) {
                    i++;
                }

                boolean real = false;

                if (i + 1 < n && fonte.charAt(i) == '.' && isDigito(fonte.charAt(i + 1))) {
                    real = true;
                    i++;

                    while (i < n && isDigito(fonte.charAt(i))) {
                        i++;
                    }
                }

                String numero = fonte.substring(inicio, i);

                if (real) {
                    saida.add("<'" + numero + "',NUM_REAL>");
                } else {
                    saida.add("<'" + numero + "',NUM_INT>");
                }

                continue;
            }

            if (i + 1 < n) {
                String dois = fonte.substring(i, i + 2);

                if (isOperadorDuplo(dois)) {
                    saida.add(tokenLiteral(dois));
                    i += 2;
                    continue;
                }
            }

            if (isSimboloSimples(c)) {
                saida.add(tokenLiteral(String.valueOf(c)));
                i++;
                continue;
            }

            saida.add("Linha " + linha + ": " + c + " - simbolo nao identificado");
            return saida;
        }

        return saida;
    }

    private static boolean isLetra(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isDigito(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isOperadorDuplo(String s) {
        return s.equals("<-")
                || s.equals("<>")
                || s.equals("<=")
                || s.equals(">=")
                || s.equals("..");
    }

    private static boolean isSimboloSimples(char c) {
        return c == ':'
                || c == '('
                || c == ')'
                || c == '['
                || c == ']'
                || c == ','
                || c == '+'
                || c == '-'
                || c == '*'
                || c == '/'
                || c == '%'
                || c == '<'
                || c == '>'
                || c == '='
                || c == '^'
                || c == '&'
                || c == '.';
    }

    private static String tokenLiteral(String lexema) {
        return "<'" + lexema + "','" + lexema + "'>";
    }
}