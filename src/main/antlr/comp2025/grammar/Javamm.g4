grammar Javamm;

@header {
    package pt.up.fe.comp2025;
}

/* ###############################################
   This part is to reserve the name of the variables
   ############################################### */
CLASS : 'class' ;
INT : 'int' ;
PUBLIC : 'public' ;
RETURN : 'return' ;
EXTENDS : 'extends' ;

INTEGER : [1-9]*[0-9] ;
BOOLEAN : 'boolean' ;
ID : [a-zA-Z_][a-zA-Z0-9_]* ;

WS : [ \t\n\r\f]+ -> skip ;

/* ###############################################
 ############################################### */

program
    : importDecl* classDecl EOF
    ;

importDecl
    : 'import' name=ID ('.' name=ID)* ';'
    ;

classDecl locals[boolean extendsClass=false]
    : CLASS name=ID
        (EXTENDS name=ID {$extendsClass=true;})?
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
    | RETURN expr ';' #ReturnStmt //
    | '{' stmt* '}' #BracketStmt
    | 'if' '(' expr ')' stmt 'else' stmt #IfStmt
    | 'while' '(' expr ')' stmt #WhileStmt
    | expr ';' #ExprStmt
    | name=ID '=' expr ';' #VarAssignStmt
    | name=ID '[' expr ']' '=' expr ';' #ArrayAssignStmt
    ;

expr
    : expr op= '*' expr #BinaryExpr //
    | expr op= '+' expr #BinaryExpr //
    | expr ('&&' | '<' | '+' | '-' | '*' | '/' ) expr #BinaryExpr
    | expr '[' expr ']' #ArrayAccessExpr
    | expr '.' 'length' #LengthExpr
    | expr '.' name=ID '(' ( expr ( ',' expr )* )? ')' #MethodCallExpr
    | 'new' 'int' '[' expr ']' #NewArrayExpr
    | 'new' name=ID '(' ')' #NewObjectExpr
    | '!' expr #UnaryExpr
    | '(' expr ')' #ParenExpr
    | '[' ( expr ( ',' expr )* )? ']' #ArrayLiteral
    | value=INTEGER #IntegerLiteral
    | 'true' #BooleanLiteral
    | 'false' #BooleanLiteral
    | name=ID #VarRefExpr //
    | 'this' #ThisExpr
    ;

