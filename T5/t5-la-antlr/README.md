# T5 - Gerador de Código C para LA

Projeto do Trabalho 5 da disciplina Construção de Compiladores.

Esta versão usa Java + Maven + ANTLR4 e gera código C executável por GCC.

## Integrantes

- Cristian Martins
- RA: 799714


## Requisitos

- Java JDK 11 ou superior
- Apache Maven
- GCC ou MinGW para o corretor automático

## Compilação

```bash
mvn clean package
```

O JAR será criado em:

```txt
target/t5-la-antlr-1.0-SNAPSHOT.jar
```

## Execução manual

```bash
java -jar target/t5-la-antlr-1.0-SNAPSHOT.jar entrada.txt saida.c
```

Se o programa LA não tiver erro, `saida.c` conterá código C. Se houver erro léxico/sintático, a saída terá a mensagem de erro seguida de `Fim da compilacao`.

## Uso com o corretor

Use a opção:

```txt
gerador
```

E configure o compilador assim:

```txt
"java -jar C:\caminho\absoluto\t5-la-antlr\target\t5-la-antlr-1.0-SNAPSHOT.jar"
```

## Arquivos principais

- `src/main/antlr4/LA.g4`: gramática léxica e sintática.
- `Principal.java`: entrada do compilador e tratamento de erros léxicos/sintáticos.
- `GeradorCVisitor.java`: geração de código C.
- `CTipo.java`, `CSimbolo.java`, `CEscopo.java`, `ParametroFormal.java`: estruturas auxiliares do gerador.
