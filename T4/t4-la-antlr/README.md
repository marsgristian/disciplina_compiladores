# T4 - Analisador Semântico para a Linguagem LA

Projeto do Trabalho 4 da disciplina Construção de Compiladores.

Este projeto implementa a segunda parte do analisador semântico da linguagem LA usando Java, Maven e ANTLR4.

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
target/t4-la-antlr-1.0-SNAPSHOT.jar
```

## Execução manual

O analisador recebe exatamente dois argumentos:

1. caminho do arquivo de entrada
2. caminho do arquivo de saída

Windows:

```bat
java -jar target\t4-la-antlr-1.0-SNAPSHOT.jar C:\casos-de-teste\entrada\arquivo.txt C:\temp\saida.txt
```

Linux/macOS:

```bash
java -jar target/t4-la-antlr-1.0-SNAPSHOT.jar /tmp/entrada.txt /tmp/saida.txt
```

A saída é gravada obrigatoriamente no arquivo passado como segundo argumento.

## Uso com o corretor automático

Use a opção:

```txt
semantico
```

No argumento do compilador, passe o comando completo:

```txt
"java -jar C:\caminho\absoluto\t4-la-antlr\target\t4-la-antlr-1.0-SNAPSHOT.jar"
```

Exemplo:

```bat
java -jar C:\corretor\compiladores-corretor-automatico-1.0-SNAPSHOT-jar-with-dependencies.jar "java -jar C:\caminho\absoluto\t4-la-antlr\target\t4-la-antlr-1.0-SNAPSHOT.jar" C:\mingw64\bin\gcc.exe C:\temp C:\casos-de-teste "799714" semantico
```

## Erros detectados

O analisador detecta:

1. identificador já declarado anteriormente no mesmo escopo;
2. tipo não declarado;
3. identificador não declarado;
4. incompatibilidade de argumentos e parâmetros em chamadas de procedimentos e funções;
5. atribuição incompatível envolvendo tipos básicos, ponteiros e registros;
6. uso do comando `retorne` fora de função.

A análise semântica não para no primeiro erro. Todos os erros encontrados são emitidos antes de:

```txt
Fim da compilacao
```

## Estrutura do projeto

```txt
src/main/antlr4/LA.g4
src/main/java/br/ufscar/dc/compiladores/t4/Principal.java
src/main/java/br/ufscar/dc/compiladores/t4/SemanticoVisitor.java
src/main/java/br/ufscar/dc/compiladores/t4/Escopo.java
src/main/java/br/ufscar/dc/compiladores/t4/Simbolo.java
src/main/java/br/ufscar/dc/compiladores/t4/TipoLA.java
src/main/java/br/ufscar/dc/compiladores/t4/ResultadoTipo.java
src/main/java/br/ufscar/dc/compiladores/t4/ParametroInfo.java
```

## Observações

- `Simbolo` armazena categoria, tipo e assinatura de parâmetros.
- `TipoLA` separa compatibilidade de atribuição e compatibilidade de parâmetro.
- `SemanticoVisitor` acumula erros semânticos e evita cascatas quando uma expressão já contém identificador não declarado.
