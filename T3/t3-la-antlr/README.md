# T3 - Analisador Semântico para a Linguagem LA

Projeto do Trabalho 3 da disciplina Construção de Compiladores.

Este projeto implementa parte do analisador semântico da linguagem LA usando Java, Maven e ANTLR4.

## Integrantes

- Cristian Martins
- RA: 799714


## Requisitos

- Java JDK 11 ou superior
- Apache Maven
- GCC/MinGW apenas para uso do corretor automático

Verifique:

```bash
java -version
mvn -version
```

## Compilação

Na raiz do projeto:

```bash
mvn clean package
```

O JAR final será gerado em:

```txt
target/t3-la-antlr-1.0-SNAPSHOT.jar
```

## Execução manual

O analisador recebe exatamente dois argumentos:

1. caminho do arquivo de entrada
2. caminho do arquivo de saída

Windows:

```bat
java -jar target\t3-la-antlr-1.0-SNAPSHOT.jar C:\casos-de-teste\entrada\arquivo.txt C:\temp\saida.txt
```

Linux/macOS:

```bash
java -jar target/t3-la-antlr-1.0-SNAPSHOT.jar /tmp/entrada.txt /tmp/saida.txt
```

A saída é gravada obrigatoriamente no arquivo passado como segundo argumento.

## Uso com o corretor automático

Use a opção:

```txt
semantico
```

No argumento do compilador, passe o comando completo:

```txt
"java -jar C:\caminho\absoluto\t3-la-antlr\target\t3-la-antlr-1.0-SNAPSHOT.jar"
```

Exemplo:

```bat
java -jar C:\corretor\compiladores-corretor-automatico-1.0-SNAPSHOT-jar-with-dependencies.jar "java -jar C:\caminho\absoluto\t3-la-antlr\target\t3-la-antlr-1.0-SNAPSHOT.jar" C:\mingw64\bin\gcc.exe C:\temp C:\casos-de-teste "799714" semantico
```

## Erros detectados

O analisador detecta:

1. identificador já declarado anteriormente no mesmo escopo;
2. tipo não declarado;
3. identificador não declarado;
4. atribuição incompatível.

Ao contrário do T2, o T3 não para no primeiro erro semântico. Ele acumula os erros e escreve todos antes de:

```txt
Fim da compilacao
```

## Estrutura do projeto

```txt
src/main/antlr4/LA.g4
src/main/java/br/ufscar/dc/compiladores/t3/Principal.java
src/main/java/br/ufscar/dc/compiladores/t3/SemanticoVisitor.java
src/main/java/br/ufscar/dc/compiladores/t3/Escopo.java
src/main/java/br/ufscar/dc/compiladores/t3/Simbolo.java
src/main/java/br/ufscar/dc/compiladores/t3/TipoLA.java
src/main/java/br/ufscar/dc/compiladores/t3/ResultadoTipo.java
```

## Como funciona

- `LA.g4`: gramática léxica e sintática.
- `Principal.java`: entrada do programa, tratamento de erros léxicos/sintáticos e escrita da saída.
- `SemanticoVisitor.java`: percorre a árvore sintática e executa as verificações semânticas.
- `Escopo.java`: representa escopos encadeados.
- `Simbolo.java`: representa entradas da tabela de símbolos.
- `TipoLA.java`: representa tipos básicos, registros, ponteiros e tipo inválido.
- `ResultadoTipo.java`: representa o resultado da inferência de tipo de expressões.
