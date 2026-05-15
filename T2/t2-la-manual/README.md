# T2 - Analisador Sintático Manual para LA

Versão sem ANTLR do Trabalho 2 da disciplina Construção de Compiladores.

Este projeto implementa:

- analisador léxico manual;
- analisador sintático descendente recursivo;
- detecção dos erros léxicos do T1;
- detecção do primeiro erro sintático;
- gravação obrigatória da saída em arquivo.

## Integrantes

- Cristian Martins
- RA: 799714

Atualize esta seção se houver mais membros no grupo.

## Requisitos

- Java JDK 11 ou superior
- Maven opcional, apenas para empacotar com facilidade

Verifique:

```bash
java -version
javac -version
mvn -version
```

## Compilando com Maven

Na raiz do projeto:

```bash
mvn clean package
```

O JAR será gerado em:

```txt
target/t2-la-manual-1.0-SNAPSHOT.jar
```

## Executando

O programa recebe exatamente dois argumentos:

1. arquivo de entrada
2. arquivo de saída

Windows:

```bat
java -jar target\t2-la-manual-1.0-SNAPSHOT.jar C:\casos-de-teste\entrada\arquivo.txt C:\temp\saida.txt
```

Linux/macOS:

```bash
java -jar target/t2-la-manual-1.0-SNAPSHOT.jar /tmp/entrada.txt /tmp/saida.txt
```

## Usando no corretor automático

Para o T2, use a opção:

```txt
sintatico
```

No argumento do compilador, use:

```txt
"java -jar C:\caminho\absoluto\t2-la-manual\target\t2-la-manual-1.0-SNAPSHOT.jar"
```

Exemplo:

```bat
java -jar C:\corretor\compiladores-corretor-automatico-1.0-SNAPSHOT-jar-with-dependencies.jar "java -jar C:\caminho\absoluto\t2-la-manual\target\t2-la-manual-1.0-SNAPSHOT.jar" C:\mingw64\bin\gcc.exe C:\temp C:\casos-de-teste "799714" sintatico
```

## Compilando sem Maven

Também é possível compilar usando apenas `javac` e `jar`.

Na raiz do projeto:

```bat
mkdir out
javac -encoding UTF-8 -d out src\main\java\br\ufscar\dc\compiladores\t2manual\Principal.java
jar cfe t2-la-manual.jar br.ufscar.dc.compiladores.t2manual.Principal -C out .
```

Execute:

```bat
java -jar t2-la-manual.jar entrada.txt saida.txt
```

## Saída

Programa correto:

```txt
Fim da compilacao
```

Erro léxico:

```txt
Linha X: cadeia literal nao fechada
Fim da compilacao
```

ou:

```txt
Linha X: comentario nao fechado
Fim da compilacao
```

ou:

```txt
Linha X: @ - simbolo nao identificado
Fim da compilacao
```

Erro sintático:

```txt
Linha X: erro sintatico proximo a TOKEN
Fim da compilacao
```

## Organização

O arquivo principal é:

```txt
src/main/java/br/ufscar/dc/compiladores/t2manual/Principal.java
```

Ele contém:

- `Lexer`: analisador léxico manual;
- `Parser`: parser descendente recursivo;
- `Token`: estrutura de token;
- exceções específicas para erro léxico e sintático.

## Observações

Esta versão foi escrita para ser equivalente à versão com ANTLR do T2, mas sem usar gerador de parser. Cada método do parser corresponde a uma regra da gramática da LA.
