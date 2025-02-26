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

INTEGER : [0-9] ;
BOOLEAN : 'boolean' ;
ID : [a-zA-Z]+ ;

WS : [ \t\n\r\f]+ -> skip ;

// ###############################################
// ###############################################

program
    : importDeclaration* classDecl EOF
    ;

importDeclaration
    : 'import' name=ID ('.' name=ID)* ';'
    ;

classDecl locals[boolean extendsClass=false]
    : CLASS name=ID
        (EXTENDS {$extendsClass=true;})?
            name=ID
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
        '(' param ')'
        '{' varDecl* stmt* '}' //me parece que falta a parte do return expression

    | (PUBLIC {$isPublic=true;})?
        'static' 'void ' 'main' '(' 'String' '[' ']' name=ID ')'
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

    | value=INTEGER (value=INTEGER)? #IntegerLiteral // Pensei em add o (value=INTEGER)* para suportar varios digitos
    | name=ID #VarRefExpr //
    | expr ('&&' | '<' | '+' | '-' | '*' | '/' ) expr #Tobedefined
    | expr '[' expr ']' #Tobedefined
    | expr '.' 'length' #Tobedefined
    | 'new' 'int' '[' expr ']' #Tobedefined //será que aqui o int é mesmo entre parentesis?
    | 'new' name=ID '(' ')' #Tobedefined
    | '!' expr #Tobedefined
    | '(' expr ')' #Tobedefined
    | 'true' #Tobedefined
    | 'false' #Tobedefined
    | 'this' #Tobedefined
    | expr '.' name=ID '(' ( expr ( ',' expr )* )? ')' #Tobedefined
    | '[' ( expr ( ',' expr )* )? ']' #Tobedefined
    ;

