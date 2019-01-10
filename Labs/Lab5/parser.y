%{
#include <stdio.h>
#include <stdlib.h>

#define YYDEBUG 1
%}

%token IDENTIFIER
%token CONSTANT
%token MAGIE
%token NEATA
%token NB
%token INFIDELE
%token BATUTE_IN_CUI
%token LANT
%token DE
%token IN_CAZ_CA
%token APAI
%token ALTCUM
%token CAT_TIMP
%token MUNCESTE
%token INGHITE
%token SCUIPA
%token STRING
%token BUCATICA
%token NUMAR
%token COMBINATIE
%token GATA
%token PLUS_CA
%token ORI
%token NU
%token COLON
%token SEMI_COLON
%token COMA
%token DOT
%token PLUS
%token MINUS
%token MULTIPLY
%token DIVISION
%token LEFT_ROUND_PARENTHESIS
%token RIGHT_ROUND_PARENTHESIS
%token LEFT_SQUARE_PARENTHESIS
%token RIGHT_SQUARE_PARENTHESIS
%token LESS_THAN
%token GREATER_THAN
%token LESS_OR_EQUAL_THAN
%token GREATER_OR_EQUAL_THAN
%token DIFFERENT
%token EQUAL
%token ASSIGNMENT
%token BAR

%start program

%%

program : INFIDELE declarationList BAR compoundStatement DOT ;
declarationList : declaration SEMI_COLON declarationList | declaration ;
declaration : IDENTIFIER COLON type | combinationDeclaration ;
combinationDeclaration : COMBINATIE IDENTIFIER declarationList GATA ;
type : primitiveType | arrayDeclaration ;
primitiveType : STRING | BUCATICA | NUMAR ;
arrayDeclaration : LANT LEFT_SQUARE_PARENTHESIS CONSTANT RIGHT_SQUARE_PARENTHESIS DE primitiveType ;
compoundStatement : NEATA statementList NB ;
statementList : statement SEMI_COLON statementList | statement ;
statement : simpleStatement | structStatement ;
simpleStatement : assignemntStatement | ioStatement ;
assignemntStatement : IDENTIFIER ASSIGNMENT expression ;
expression : term | term PLUS expression | term MINUS expression | term MULTIPLY expression | term DIVISION expression | LEFT_ROUND_PARENTHESIS expression RIGHT_SQUARE_PARENTHESIS ;
term : IDENTIFIER | CONSTANT ;
ioStatement : INGHITE LEFT_ROUND_PARENTHESIS IDENTIFIER RIGHT_SQUARE_PARENTHESIS | SCUIPA LEFT_ROUND_PARENTHESIS IDENTIFIER RIGHT_SQUARE_PARENTHESIS ;
structStatement : compoundStatement | ifStatement | whileStatement ;
ifStatement : IN_CAZ_CA condition APAI statement ALTCUM statement | IN_CAZ_CA condition APAI statement ;
whileStatement : CAT_TIMP condition MUNCESTE statement ;
condition : expression relation expression ;
relation : LESS_THAN | GREATER_THAN | LESS_OR_EQUAL_THAN | GREATER_OR_EQUAL_THAN | DIFFERENT | EQUAL ;

%%

yyerror(char *s)
{
  printf("%s\n", s);
}

extern FILE *yyin;

main(int argc, char **argv)
{
  if (argc > 1) 
    yyin = fopen(argv[1], "r");
  if ( (argc > 2) && ( !strcmp(argv[2], "-d") ) ) 
    yydebug = 1;
  if ( !yyparse() ) 
    fprintf(stderr,"\t U GOOOOOD !!!\n");
}

