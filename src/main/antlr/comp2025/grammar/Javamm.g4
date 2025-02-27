grammar Javamm;

@header {
    package pt.up.fe.comp2025;
}

// ###############################################
// essa parte é para reservar o nome das variáveis
// ###############################################
CLASS : 'class' ;
INT : 'int' ;
PUBLIC : 'public' ;
RETURN : 'return' ;
EXTENDS : 'extends' ;

INTEGER : [1-9]*[0-9] ;
BOOLEAN : 'boolean' ;
ID : [a-zA-Z_][a-zA-Z0-9_]* ; //Alterei para aceitar numeros e o caractere _

WS : [ \t\n\r\f]+ -> skip ;

// ###############################################
// ###############################################

program
    : importDecl* classDecl EOF
    ;

importDecl
    : 'import' name=ID ('.' name=ID)* ';'
    ;

classDecl locals[boolean extendsClass=false]
    : CLASS name=ID
        (EXTENDS name=ID {$extendsClass=true;})? //Aqui o ID só é pedido quando EXTENDS for verdadeiro
        '{'
        varDecl*
        methodDecl*
        '}'
    ;

varDecl
    : type name=ID ';'
    ;

type
    : name= INT
    | name= INT '[' ']'
    | name= INT '...'
    | name= BOOLEAN
    | name= ID
    ;

methodDecl locals[boolean isPublic=false]
    : (PUBLIC {$isPublic=true;})?
        type name=ID
            '(' param (',' param)* ')'
        '{' varDecl* stmt* '}'

    | (PUBLIC {$isPublic=true;})?
        'static' 'void ' 'main' '(' name=ID '[' ']' name=ID ')'
        '{' varDecl * stmt* '}'
    ;

param
    : type name=ID
    ;

stmt
    : expr '=' expr ';' #AssignStmt //
    | RETURN expr ';' #ReturnStmt

    | '{' stmt* '}' #To_be_defined
    | 'if' '(' expr ')' stmt 'else' stmt #To_be_defined
    | 'while' '(' expr ')' stmt #To_be_defined
    | expr ';' #To_be_defined
    | name=ID '=' expr ';' #To_be_defined
    | name=ID '[' expr ']' '=' expr ';' #To_be_defined
    ;

expr
    : expr op= '*' expr #BinaryExpr //
    | expr op= '+' expr #BinaryExpr //

    | value=INTEGER #IntegerLiteral
    | name=ID #VarRefExpr //
    | expr ('&&' | '<' | '+' | '-' | '*' | '/' ) expr #Tobedefined
    | expr '[' expr ']' #Tobedefined
    | expr '.' 'length' #Tobedefined
    | expr '.' name=ID '(' ( expr ( ',' expr )* )? ')' #Tobedefined
    | 'new' 'int' '[' expr ']' #Tobedefined
    | 'new' name=ID '(' ')' #Tobedefined
    | '!' expr #Tobedefined
    | '(' expr ')' #Tobedefined
    | 'true' #Tobedefined
    | 'false' #Tobedefined
    | 'this' #Tobedefined
    | '[' ( expr ( ',' expr )* )? ']' #Tobedefined
    ;

