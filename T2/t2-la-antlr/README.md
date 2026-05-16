# T2 - Analisador Sintático para a Linguagem LA

Projeto do Trabalho 2 da disciplina Construção de Compiladores.

Este projeto implementa um analisador sintático para a linguagem LA usando Java, Maven e ANTLR4.

## Integrantes

- Nome: Cristian Martins
- RA: 799714


## Requisitos

- Java JDK 11 ou superior
- Apache Maven
- GCC ou MinGW configurado para uso do corretor automático

Verifique o Java:

```bash
java -version
```

Verifique o Maven:

```bash
mvn -version
```

## Compilação

Na pasta raiz do projeto:

```bash
mvn clean package
```

O arquivo executável será gerado em:

```txt
target/t2-la-antlr-1.0-SNAPSHOT.jar
```

## Execução manual

O analisador deve receber exatamente dois argumentos:

1. caminho absoluto do arquivo de entrada
2. caminho absoluto do arquivo de saída

Exemplo no Windows:

```bat
java -jar target\t2-la-antlr-1.0-SNAPSHOT.jar C:\casos-de-teste\entrada\arquivo.txt C:\temp\saida.txt
```

Exemplo no Linux/macOS:

```bash
java -jar target/t2-la-antlr-1.0-SNAPSHOT.jar /tmp/entrada.txt /tmp/saida.txt
```

O programa não imprime a análise no terminal. A saída é gravada no arquivo informado no segundo argumento.

## Uso com o corretor automático

No corretor, o argumento do compilador deve ser o comando completo:

```txt
"java -jar C:\caminho\absoluto\t2-la-antlr\target\t2-la-antlr-1.0-SNAPSHOT.jar"
```

Para o T2, use a opção:

```txt
sintatico
```

Exemplo geral:

```bat
java -jar C:\corretor\compiladores-corretor-automatico-1.0-SNAPSHOT-jar-with-dependencies.jar "java -jar C:\caminho\absoluto\t2-la-antlr\target\t2-la-antlr-1.0-SNAPSHOT.jar" C:\mingw64\bin\gcc.exe C:\temp C:\casos-de-teste "799714" sintatico
```

## Saída esperada

Se não houver erro:

```txt
Fim da compilacao
```

Se houver erro léxico:

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

Se houver erro sintático:

```txt
Linha X: erro sintatico proximo a TOKEN
Fim da compilacao
```

## Organização do código

- `src/main/antlr4/LA.g4`: gramática léxica e sintática da linguagem LA.
- `src/main/java/br/ufscar/dc/compiladores/t2/Principal.java`: ponto de entrada do analisador, tratamento de erros e gravação da saída.
- `pom.xml`: configuração Maven, geração ANTLR e empacotamento do JAR executável com dependências.

## Observações de implementação

A classe `Principal` primeiro executa o lexer e percorre todos os tokens para detectar erros léxicos. Isso garante que erros como comentário não fechado, cadeia não fechada e símbolo inválido tenham prioridade sobre erros sintáticos.

Somente se não houver erro léxico o parser é executado. O listener sintático customizado substitui a mensagem padrão do ANTLR pela mensagem exigida pelo corretor.
