# T1 - Analisador Léxico para a Linguagem LA

Trabalho 1 da disciplina **Construção de Compiladores**, ministrada pelo professor Daniel Lucrédio.

Este projeto implementa um **analisador léxico** para a linguagem **LA - Linguagem Algorítmica**, desenvolvida pelo professor Jander, no âmbito do DC/UFSCar.

O analisador lê um programa-fonte em LA e produz uma lista de tokens reconhecidos. Em caso de erro léxico, a execução é interrompida e o primeiro erro encontrado é reportado.

---

## Integrantes

- Nome: Cristian Martins
- RA: 799714

Atualize esta seção caso o trabalho tenha sido desenvolvido em grupo.

---

## Requisitos

Para compilar e executar este projeto, é necessário ter instalado:

- Java JDK 11 ou superior
- Apache Maven

Verifique a instalação do Java:

```bash
java -version
````

Verifique a instalação do Maven:

```bash
mvn -version
```

---

## Estrutura do projeto

A estrutura principal do projeto é:

```txt
T1/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── com/
                └── compiladores/
                    └── t1/
                        └── Principal.java
```

O arquivo principal é:

```txt
src/main/java/com/compiladores/t1/Principal.java
```

---

## Compilação

Na pasta raiz do projeto, execute:

```bash
mvn clean package
```

Após a compilação, o arquivo `.jar` será gerado na pasta:

```txt
target/
```

Exemplo:

```txt
target/t1-lexico-1.0-SNAPSHOT.jar
```

O nome exato do arquivo pode variar de acordo com o `artifactId` configurado no `pom.xml`.

---

## Execução

O analisador deve ser executado com exatamente **dois argumentos**:

1. caminho absoluto do arquivo de entrada;
2. caminho absoluto do arquivo de saída.

Formato:

```bash
java -jar caminho/do/analisador.jar caminho/entrada.txt caminho/saida.txt
```

Exemplo no Windows:

```bat
java -jar target\t1-lexico-1.0-SNAPSHOT.jar C:\casos-de-teste\entrada\arquivo1.txt C:\temp\saida.txt
```

Exemplo no Linux/macOS:

```bash
java -jar target/t1-lexico-1.0-SNAPSHOT.jar /tmp/entrada.txt /tmp/saida.txt
```

O programa **não imprime a saída no terminal**. A saída é gravada obrigatoriamente no arquivo informado no segundo argumento.

---

## Uso com o corretor automático

Para usar este projeto com o corretor automático da disciplina, o argumento do compilador deve ser informado como comando completo.

Exemplo:

```txt
"java -jar C:\caminho\absoluto\T1\target\t1-lexico-1.0-SNAPSHOT.jar"
```

Exemplo de comando completo do corretor:

```bat
java -jar C:\corretor\compiladores-corretor-automatico-1.0-SNAPSHOT-jar-with-dependencies.jar "java -jar C:\caminho\absoluto\T1\target\t1-lexico-1.0-SNAPSHOT.jar" C:\mingw64\bin\gcc.exe C:\temp C:\casos-de-teste "799714" lexico
```

Para o T1, a opção de correção deve ser:

```txt
lexico
```

---

## Funcionamento

O analisador léxico percorre o arquivo de entrada caractere por caractere e reconhece os seguintes elementos da linguagem LA:

* palavras reservadas;
* identificadores;
* números inteiros;
* números reais;
* cadeias literais;
* operadores;
* delimitadores;
* comentários;
* símbolos inválidos.

Espaços em branco, quebras de linha e comentários válidos são ignorados.

---

## Tokens reconhecidos

### Palavras reservadas

Exemplos:

```txt
algoritmo
declare
literal
inteiro
real
logico
leia
escreva
fim_algoritmo
se
entao
senao
fim_se
para
ate
faca
fim_para
enquanto
fim_enquanto
procedimento
fim_procedimento
funcao
fim_funcao
retorne
```

Palavras reservadas são emitidas no formato:

```txt
<'algoritmo','algoritmo'>
```

---

### Identificadores

Identificadores começam com uma letra e podem conter letras, dígitos e `_`.

Exemplo:

```txt
nome
idade
valor_total
```

Saída:

```txt
<'nome',IDENT>
```

---

### Números

Números inteiros:

```txt
10
25
100
```

Saída:

```txt
<'10',NUM_INT>
```

Números reais:

```txt
10.5
3.14
0.25
```

Saída:

```txt
<'10.5',NUM_REAL>
```

---

### Cadeias literais

Cadeias são delimitadas por aspas duplas:

```txt
"ola mundo"
```

Saída:

```txt
<'"ola mundo"',CADEIA>
```

Cadeias não podem atravessar linha.

---

### Comentários

Comentários são delimitados por `{` e `}`:

```txt
{ isto é um comentário }
```

Comentários válidos são ignorados e não geram tokens.

Comentários não podem atravessar linha sem fechamento.

---

## Formato da saída

Para cada token reconhecido, o analisador escreve uma linha no arquivo de saída.

Exemplo de entrada:

```txt
algoritmo
    declare
        nome: literal
fim_algoritmo
```

Saída:

```txt
<'algoritmo','algoritmo'>
<'declare','declare'>
<'nome',IDENT>
<':',':'>
<'literal','literal'>
<'fim_algoritmo','fim_algoritmo'>
```

---

## Tratamento de erros léxicos

Ao encontrar um erro léxico, o analisador interrompe a execução e escreve o erro no arquivo de saída.

### Símbolo não identificado

Entrada:

```txt
nome~ literal
```

Saída:

```txt
Linha 1: ~ - simbolo nao identificado
```

---

### Comentário não fechado

Entrada:

```txt
{ comentario nao fechado
```

Saída:

```txt
Linha 1: comentario nao fechado
```

---

### Cadeia literal não fechada

Entrada:

```txt
"texto sem fechamento
```

Saída:

```txt
Linha 1: cadeia literal nao fechada
```

---

## Exemplo completo

Entrada:

```txt
{ leitura de nome e idade com escrita de mensagem usando estes dados }
algoritmo
    declare
        nome: literal
    declare
        idade: inteiro

    leia(nome)
    leia(idade)

    escreva(nome, " tem ", idade, " anos.")
fim_algoritmo
```

Saída:

```txt
<'algoritmo','algoritmo'>
<'declare','declare'>
<'nome',IDENT>
<':',':'>
<'literal','literal'>
<'declare','declare'>
<'idade',IDENT>
<':',':'>
<'inteiro','inteiro'>
<'leia','leia'>
<'(','('>
<'nome',IDENT>
<')',')'>
<'leia','leia'>
<'(','('>
<'idade',IDENT>
<')',')'>
<'escreva','escreva'>
<'(','('>
<'nome',IDENT>
<',',','>
<'" tem "',CADEIA>
<',',','>
<'idade',IDENT>
<',',','>
<'" anos."',CADEIA>
<')',')'>
<'fim_algoritmo','fim_algoritmo'>
```

---

## Observações importantes

* O programa deve ser executado sempre com dois argumentos.
* A saída deve ser gravada em arquivo.
* O programa não deve imprimir a lista de tokens no terminal.
* O caminho usado no corretor automático deve ser absoluto.
* Caso esteja usando Java, o comando do compilador no corretor deve incluir `java -jar`.

Errado:

```txt
C:\caminho\T1\target\t1-lexico-1.0-SNAPSHOT.jar
```

Certo:

```txt
java -jar C:\caminho\T1\target\t1-lexico-1.0-SNAPSHOT.jar
```

---

## Compilação sem Maven

Também é possível compilar manualmente usando apenas `javac` e `jar`.

Na raiz do projeto:

```bat
mkdir out
javac -encoding UTF-8 -d out src\main\java\br\ufscar\dc\compiladores\t1\Principal.java
jar cfe t1-lexico.jar br.ufscar.dc.compiladores.t1.Principal -C out .
```

Executar:

```bat
java -jar t1-lexico.jar entrada.txt saida.txt
```
