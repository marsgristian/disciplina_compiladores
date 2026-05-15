# Trabalhos de Construção de Compiladores - Linguagem LA

Repositório com os trabalhos da disciplina **Construção de Compiladores**, ministrada pelo professor **Daniel Lucrédio**, no contexto da linguagem **LA - Linguagem Algorítmica**, desenvolvida pelo professor Jander no DC/UFSCar.

Este repositório contém as implementações dos trabalhos **T1 ao T4**, cada um em sua própria pasta e com documentação específica.

---

## Integrantes

- Cristian Martins
- RA: 799714

Atualize esta seção caso o trabalho tenha sido desenvolvido em grupo.

---

## Estrutura do repositório

```txt
.
├── T1/
│   ├── README.md
│   ├── pom.xml
│   └── src/
│
├── T2/
│   ├── README.md
│   ├── pom.xml
│   └── src/
│
├── T3/
│   ├── README.md
│   ├── pom.xml
│   └── src/
│
├── T4/
│   ├── README.md
│   ├── pom.xml
│   └── src/
│
└── corretor_t1_helper.ipynb
````

Cada trabalho possui um `README.md` próprio com detalhes de implementação, compilação e execução.

---

## Requisitos gerais

Para compilar e executar os trabalhos, é necessário ter instalado:

* Java JDK 11 ou superior
* Apache Maven
* GCC ou MinGW, apenas para uso do corretor automático
* Python/Jupyter, caso deseje usar o notebook auxiliar do corretor

Verifique o Java:

```bash
java -version
```

Verifique o Maven:

```bash
mvn -version
```

Verifique o GCC:

```bash
gcc --version
```

No Windows, caso esteja usando MinGW diretamente, o caminho pode ser algo como:

```txt
C:\mingw64\bin\gcc.exe
```

---

## Compilação dos trabalhos

Cada trabalho deve ser compilado separadamente dentro de sua respectiva pasta.

### T1 - Analisador Léxico

```bash
cd T1
mvn clean package
```

JAR gerado:

```txt
T1/target/*.jar
```

---

### T2 - Analisador Sintático

```bash
cd T2
mvn clean package
```

JAR gerado:

```txt
T2/target/*.jar
```

---

### T3 - Analisador Semântico - Parte 1

```bash
cd T3
mvn clean package
```

JAR gerado:

```txt
T3/target/*.jar
```

---

### T4 - Analisador Semântico - Parte 2

```bash
cd T4
mvn clean package
```

JAR gerado:

```txt
T4/target/*.jar
```

---

## Execução manual

Todos os trabalhos seguem o mesmo padrão de execução exigido pela disciplina:

```bash
java -jar caminho/do/trabalho.jar caminho/entrada.txt caminho/saida.txt
```

O programa recebe obrigatoriamente dois argumentos:

1. caminho completo do arquivo de entrada;
2. caminho completo do arquivo de saída.

Exemplo no Windows:

```bat
java -jar T1\target\t1-lexico.jar C:\casos-de-teste\entrada\arquivo.txt C:\temp\saida.txt
```

Exemplo no Linux/macOS:

```bash
java -jar T1/target/t1-lexico.jar /tmp/entrada.txt /tmp/saida.txt
```

A saída é sempre gravada no arquivo informado no segundo argumento.

Os programas não devem imprimir o resultado da análise no terminal.

---

## Execução com o corretor automático

Os trabalhos podem ser executados pelo corretor automático da disciplina usando o formato:

```bash
java -jar ARG1 ARG2 ARG3 ARG4 ARG5 ARG6 ARG7
```

Onde:

```txt
ARG1 = caminho do JAR do corretor automático
ARG2 = comando executável do trabalho
ARG3 = caminho do GCC
ARG4 = pasta temporária
ARG5 = pasta dos casos de teste
ARG6 = RA(s) do grupo
ARG7 = opção de correção
```

Exemplo geral:

```bat
java -jar C:\corretor\compiladores-corretor-automatico-1.0-SNAPSHOT-jar-with-dependencies.jar "java -jar C:\caminho\do\repositorio\T1\target\t1-lexico.jar" C:\mingw64\bin\gcc.exe C:\temp C:\casos-de-teste "799714" lexico
```

O argumento do compilador deve ser passado como comando completo.

Correto:

```txt
"java -jar C:\caminho\do\repositorio\T1\target\t1-lexico.jar"
```

Incorreto:

```txt
"C:\caminho\do\repositorio\T1\target\t1-lexico.jar"
```

Um arquivo `.jar` não deve ser executado diretamente pelo corretor. Ele deve ser chamado por `java -jar`.

---

## Opções do corretor por trabalho

Use a opção correspondente ao trabalho que deseja corrigir:

| Trabalho | Opção do corretor | Descrição                      |
| -------- | ----------------- | ------------------------------ |
| T1       | `lexico`          | Analisador léxico              |
| T2       | `sintatico`       | Analisador sintático           |
| T3       | `semantico`       | Analisador semântico - parte 1 |
| T4       | `semantico`       | Analisador semântico - parte 2 |

Exemplos:

### Corrigir T1

```txt
ARG2 = "java -jar C:\caminho\do\repositorio\T1\target\t1-lexico.jar"
ARG7 = lexico
```

### Corrigir T2

```txt
ARG2 = "java -jar C:\caminho\do\repositorio\T2\target\t2-sintatico.jar"
ARG7 = sintatico
```

### Corrigir T3

```txt
ARG2 = "java -jar C:\caminho\do\repositorio\T3\target\t3-semantico.jar"
ARG7 = semantico
```

### Corrigir T4

```txt
ARG2 = "java -jar C:\caminho\do\repositorio\T4\target\t4-semantico.jar"
ARG7 = semantico
```

---

## Uso do corretor-helper

Este repositório também pode conter um notebook auxiliar:

```txt
corretor_t1_helper.ipynb
```

Apesar do nome, ele pode ser usado para rodar o corretor de todos os trabalhos, bastando alterar os campos de configuração.

Campos principais a alterar:

```python
CORRETOR_JAR = r"C:\caminho\para\compiladores-corretor-automatico-1.0-SNAPSHOT-jar-with-dependencies.jar"

COMPILADOR_CMD = r"java -jar C:\caminho\do\repositorio\T1\target\t1-lexico.jar"

GCC_CMD = r"C:\mingw64\bin\gcc.exe"

TEMP_DIR = r"C:\temp"

CASOS_TESTE_DIR = r"C:\casos-de-teste"

RAS = "799714"

OPCAO = "t1"
```

Para cada trabalho, altere principalmente:

```python
COMPILADOR_CMD
OPCAO
```

Exemplo para o T2:

```python
COMPILADOR_CMD = r"java -jar C:\caminho\do\repositorio\T2\target\t2-sintatico.jar"
OPCAO = "t2"
```

Exemplo para o T3:

```python
COMPILADOR_CMD = r"java -jar C:\caminho\do\repositorio\T3\target\t3-semantico.jar"
OPCAO = "t3"
```

Exemplo para o T4:

```python
COMPILADOR_CMD = r"java -jar C:\caminho\do\repositorio\T4\target\t4-semantico.jar"
OPCAO = "t4"
```

O notebook auxilia na montagem do comando, validação de caminhos e visualização das saídas produzidas pelo corretor.

---

## Observações sobre cada trabalho

### T1

Implementa o analisador léxico da linguagem LA.

Produz uma lista de tokens reconhecidos e reporta erros léxicos como:

```txt
Linha X: cadeia literal nao fechada
Linha X: comentario nao fechado
Linha X: @ - simbolo nao identificado
```

Consulte:

```txt
T1/README.md
```

---

### T2

Implementa o analisador sintático da linguagem LA.

Reporta o primeiro erro sintático encontrado no formato:

```txt
Linha X: erro sintatico proximo a TOKEN
Fim da compilacao
```

Consulte:

```txt
T2/README.md
```

---

### T3

Implementa a primeira parte do analisador semântico.

Detecta erros como:

```txt
Linha X: identificador nome ja declarado anteriormente
Linha X: tipo tipo_nome nao declarado
Linha X: identificador nome nao declarado
Linha X: atribuicao nao compativel para nome
```

Consulte:

```txt
T3/README.md
```

---

### T4

Implementa a segunda parte do analisador semântico.

Além dos erros do T3, detecta:

```txt
Linha X: incompatibilidade de parametros na chamada de nome
Linha X: comando retorne nao permitido nesse escopo
```

Também amplia o tratamento semântico de registros, ponteiros, funções e procedimentos.

Consulte:

```txt
T4/README.md
```

---

## Organização recomendada para o corretor

Uma organização possível de pastas é:

```txt
C:\compiladores\
├── corretor\
│   └── compiladores-corretor-automatico-1.0-SNAPSHOT-jar-with-dependencies.jar
│
├── casos-de-teste\
│   ├── 1.casos_teste_t1\
│   ├── 2.casos_teste_t2\
│   ├── 3.casos_teste_t3\
│   └── 4.casos_teste_t4\
│
├── temp\
│
└── trabalhos-la\
    ├── T1\
    ├── T2\
    ├── T3\
    └── T4\
```

Evite caminhos com espaços em branco ou muito longos ao usar o corretor automático.

---

## Limpeza dos arquivos gerados

Para limpar os arquivos compilados de um trabalho:

```bash
mvn clean
```

Para recompilar:

```bash
mvn clean package
```

---

## Status dos trabalhos

| Trabalho | Implementação                  | Corretor    |
| -------- | ------------------------------ | ----------- |
| T1       | Analisador léxico              | `lexico`    |
| T2       | Analisador sintático           | `sintatico` |
| T3       | Analisador semântico - parte 1 | `semantico` |
| T4       | Analisador semântico - parte 2 | `semantico` |

---

## Referências

* Disciplina: Construção de Compiladores
* Professor: Daniel Lucrédio
* Linguagem: LA - Linguagem Algorítmica
* Ferramenta de parser utilizada nos trabalhos com ANTLR: ANTLR4

```

Substitua os nomes dos `.jar` nos exemplos pelos nomes reais gerados no seu `target/`, caso estejam diferentes.
```
