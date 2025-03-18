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

STATIC : 'static';
VOID :'void';
MAIN : 'main';
IF: 'if';
ELSE: 'else';
WHILE: 'while';
NEW : 'new';
LENGTH : 'length';
TRUE: 'true';
FALSE: 'false';
THIS: 'this';
IMPORT: 'import';

INTEGER : [1-9][0-9]*|[0];
BOOLEAN : 'boolean' ;
STRING: 'String';
ID : [a-zA-Z_][a-zA-Z0-9_]* ;

WS : [ \t\n\r\f]+ -> skip ;
LINE_COMMENT: '//' ~[\r\n]* -> skip;

/* ###############################################
 ############################################### */

program
    : importDecl* classDecl EOF
    ;

importDecl
    : IMPORT nameImport+=ID ('.' nameImport+=ID)* ';'
    ;

classDecl
    : CLASS name=ID
        (EXTENDS nameExtendClass=ID)?
        '{'
        varDecl*
        methodDecl*
        '}'
    ;


varDecl
    : type name=ID ';'
    ;

type
    : name= INT  #IntType
    | name= INT '[' ']' #ArrayType
    | name= INT '...'  #VarargsType
    | name= BOOLEAN   #BooleanType
    | name= ID    #ClassType
    | name = STRING  #StringType
    ;

methodDecl locals[boolean isMain=false, boolean isPublic=false]
    : (PUBLIC {$isPublic=true;})?
        type nameMethod=ID
            '(' (param (',' param)*)? ')'
        '{' varDecl* stmt* '}'

    | {$isMain=true;} (PUBLIC {$isPublic=true;})?
         STATIC VOID nameMethod=MAIN '(' STRING '[' ']' name=ID ')'
        '{' varDecl * stmt* '}'
    ;

returnType
    : name= INT
    | name= BOOLEAN
    | name= ID
    ;


param
    : type name=ID
    ;

stmt
    : expr '=' expr ';' #AssignStmt //
    | RETURN expr ';' #ReturnStmt //
    | '{' stmt* '}' #BracketStmt
    | IF '(' expr ')' stmt ELSE stmt #IfStmt
    | WHILE '(' expr ')' stmt #WhileStmt
    | expr ';' #ExprStmt
    | name=ID '=' expr ';' #VarAssignStmt
    | name=ID '[' expr ']' '=' expr ';' #ArrayAssignStmt
    ;

expr
    : expr op=('&&' | '<' | '+' | '-' | '*' | '/' ) expr #BinaryExpr
    | expr '[' expr ']' #ArrayAccessExpr
    | expr '.' LENGTH #LengthExpr
    | expr '.' name=ID '(' ( expr ( ',' expr )* )? ')' #MethodCallExpr
    | NEW INT '[' expr ']' #NewArrayExpr
    | NEW name=ID '(' ')' #NewObjectExpr
    | '!' expr #UnaryExpr
    | '(' expr ')' #ParenExpr
    | '[' ( expr ( ',' expr )* )? ']' #ArrayLiteral
    | value=INTEGER #IntegerLiteral
    | TRUE #BooleanLiteral
    | FALSE #BooleanLiteral
    | name=ID #VarRefExpr //
    | THIS #ThisExpr
    ;

