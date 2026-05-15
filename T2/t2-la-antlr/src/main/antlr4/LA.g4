grammar LA;

/*
 * Gramática sintática da linguagem LA.
 *
 * Esta gramática é usada no T2 para reconhecer programas válidos da LA e
 * sinalizar o primeiro erro sintático encontrado. As regras léxicas do T1
 * permanecem no mesmo arquivo para que erros léxicos continuem sendo
 * detectados antes da análise sintática.
 */

/* -------------------------------------------------------------------------
 * Regras sintáticas
 * ------------------------------------------------------------------------- */

programa
    : declaracoes 'algoritmo' corpo 'fim_algoritmo' EOF
    ;

declaracoes
    : decl_local_global*
    ;

decl_local_global
    : declaracao_local
    | declaracao_global
    ;

declaracao_local
    : 'declare' variavel
    | 'constante' IDENT ':' tipo_basico '=' valor_constante
    | 'tipo' IDENT ':' tipo
    ;

variavel
    : identificador (',' identificador)* ':' tipo
    ;

identificador
    : IDENT ('.' IDENT)* dimensao
    ;

dimensao
    : ('[' exp_aritmetica ']')*
    ;

tipo
    : registro
    | tipo_estendido
    ;

tipo_basico
    : 'literal'
    | 'inteiro'
    | 'real'
    | 'logico'
    ;

tipo_basico_ident
    : tipo_basico
    | IDENT
    ;

tipo_estendido
    : '^'? tipo_basico_ident
    ;

valor_constante
    : CADEIA
    | NUM_INT
    | NUM_REAL
    | 'verdadeiro'
    | 'falso'
    ;

registro
    : 'registro' variavel* 'fim_registro'
    ;

declaracao_global
    : 'procedimento' IDENT '(' parametros? ')' declaracao_local* cmd* 'fim_procedimento'
    | 'funcao' IDENT '(' parametros? ')' ':' tipo_estendido declaracao_local* cmd* 'fim_funcao'
    ;

parametro
    : 'var'? identificador (',' identificador)* ':' tipo_estendido
    ;

parametros
    : parametro (',' parametro)*
    ;

corpo
    : declaracao_local* cmd*
    ;

cmd
    : cmdLeia
    | cmdEscreva
    | cmdSe
    | cmdCaso
    | cmdPara
    | cmdEnquanto
    | cmdFaca
    | cmdAtribuicao
    | cmdChamada
    | cmdRetorne
    ;

cmdLeia
    : 'leia' '(' '^'? identificador (',' '^'? identificador)* ')'
    ;

cmdEscreva
    : 'escreva' '(' expressao (',' expressao)* ')'
    ;

cmdSe
    : 'se' expressao 'entao' cmd* ('senao' cmd*)? 'fim_se'
    ;

cmdCaso
    : 'caso' exp_aritmetica 'seja' selecao ('senao' cmd*)? 'fim_caso'
    ;

cmdPara
    : 'para' IDENT '<-' exp_aritmetica 'ate' exp_aritmetica 'faca' cmd* 'fim_para'
    ;

cmdEnquanto
    : 'enquanto' expressao 'faca' cmd* 'fim_enquanto'
    ;

cmdFaca
    : 'faca' cmd* 'ate' expressao
    ;

cmdAtribuicao
    : '^'? identificador '<-' expressao
    ;

cmdChamada
    : IDENT '(' expressao (',' expressao)* ')'
    ;

cmdRetorne
    : 'retorne' expressao
    ;

selecao
    : item_selecao*
    ;

item_selecao
    : constantes ':' cmd*
    ;

constantes
    : numero_intervalo (',' numero_intervalo)*
    ;

numero_intervalo
    : op_unario? NUM_INT ('..' op_unario? NUM_INT)?
    ;

op_unario
    : '-'
    ;

exp_aritmetica
    : termo (op1 termo)*
    ;

termo
    : fator (op2 fator)*
    ;

fator
    : parcela (op3 parcela)*
    ;

op1
    : '+'
    | '-'
    ;

op2
    : '*'
    | '/'
    ;

op3
    : '%'
    ;

parcela
    : op_unario? parcela_unario
    | parcela_nao_unario
    ;

parcela_unario
    : '^'? identificador
    | IDENT '(' expressao (',' expressao)* ')'
    | NUM_INT
    | NUM_REAL
    | '(' expressao ')'
    ;

parcela_nao_unario
    : '&' identificador
    | CADEIA
    ;

exp_relacional
    : exp_aritmetica (op_relacional exp_aritmetica)?
    ;

op_relacional
    : '='
    | '<>'
    | '>='
    | '<='
    | '>'
    | '<'
    ;

expressao
    : termo_logico ('ou' termo_logico)*
    ;

termo_logico
    : fator_logico ('e' fator_logico)*
    ;

fator_logico
    : 'nao'? parcela_logica
    ;

parcela_logica
    : 'verdadeiro'
    | 'falso'
    | exp_relacional
    ;

/* -------------------------------------------------------------------------
 * Regras léxicas
 * ------------------------------------------------------------------------- */

/*
 * Cadeias válidas não podem atravessar linha. A regra de erro logo abaixo
 * captura a primeira aspas sem fechamento antes de qualquer erro sintático.
 */
CADEIA
    : '"' (ESC_SEQ | ~["\\\r\n])* '"'
    ;

/*
 * Números reais vêm antes de inteiros para que 10.5 seja reconhecido como
 * NUM_REAL, e não como NUM_INT seguido de ponto.
 */
NUM_REAL
    : [0-9]+ '.' [0-9]+
    ;

NUM_INT
    : [0-9]+
    ;

/*
 * Identificadores começam por letra e podem conter letras, dígitos e _.
 * Palavras reservadas aparecem como literais nas regras sintáticas e têm
 * prioridade sobre IDENT no ANTLR.
 */
IDENT
    : [a-zA-Z] [a-zA-Z0-9_]*
    ;

/*
 * Comentários válidos são delimitados por { e } e não atravessam linha.
 */
COMENTARIO
    : '{' ~[}\r\n]* '}' -> skip
    ;

WS
    : [ \t\r\n]+ -> skip
    ;

/*
 * Tokens de erro léxico. O programa Principal percorre a lista de tokens
 * antes de chamar o parser e emite a mensagem correspondente ao primeiro
 * desses tokens.
 */
ERRO_CADEIA
    : '"' (ESC_SEQ | ~["\\\r\n])*
    ;

ERRO_COMENTARIO
    : '{' ~[}\r\n]*
    ;

ERRO
    : .
    ;

fragment ESC_SEQ
    : '\\' [btnr"\\]
    ;
