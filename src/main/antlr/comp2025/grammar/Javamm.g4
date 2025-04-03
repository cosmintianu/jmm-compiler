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
EXTENDS : 'extends';

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
LINE_COMMENT: '//' ~[\r\n]+ -> skip;
MULTI_LINE_COMMENT : '/*' .*? '*/' -> skip;

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
    : nameType=type name=ID ';'
    ;

type locals[ boolean isArray= false, boolean isVarargs= false]
    : name= INT ('[' ']' {$isArray = true;})? #IntType
    | name= INT '...' {$isVarargs = true;} #VarargsType
    | name= BOOLEAN   #BooleanType
    | name= ID    #ClassType
    | name = STRING  ('[' ']' {$isArray = true;})? #StringType
    ;

methodDecl locals[boolean isMain=false, boolean isPublic=false, boolean isStatic=false]
    : (PUBLIC {$isPublic=true;})?
        (STATIC {$isStatic=true;})?
        type nameMethod=ID
            '(' (param (',' param)*)? ')'
        '{' varDecl* stmt* '}'

    | {$isMain=true;}
        (PUBLIC {$isPublic=true;})?
        (STATIC {$isStatic=true;})
         VOID nameMethod=MAIN '(' param ')'
        '{' varDecl * stmt* '}'
    ;

param
    : nameType=type name=ID
    ;

stmt
    : '{' stmt* '}' #BracketStmtexpr
    | IF '(' expr ')' stmt ELSE stmt #IfStmt
    | WHILE '(' expr ')' stmt #WhileStmt
    | expr ';' #ExprStmt
    | name=ID '=' NEW type '[' capacity=expr ']' ';' #ArrayInitStmt // Give priority to the specific grammar
    | name=ID '[' ']' '=' expr ';' #ArrayAssignStmt
    | expr '=' expr ';' #VarAssignStmt
    | RETURN expr ';' #ReturnStmt //
    ;

expr
    : '(' expr ')' #ParenExpr
    | expr '[' expr ']' #IndexAccessExpr
    | '[' ( expr ( ',' expr )* )? ']' #ArrayLiteral
    | NEW INT '[' expr ']' #NewArrayExpr
    | NEW name=ID '(' ')' #NewObjectExpr
    | expr '.' LENGTH #LengthExpr
    | expr '.' name=ID '(' ( expr ( ',' expr )* )? ')' #MethodCallExpr
    | expr op=( '*' | '/' ) expr #BinaryExpr
    | expr op=( '+' | '-') expr #BinaryExpr
    | '!' expr #UnaryExpr
    | expr op=('<' | '>') expr #BinaryExpr
    | expr op=('&&' | '||') expr #BinaryExpr
    | value=INTEGER #IntegerLiteral
    | name=TRUE #BooleanLiteral
    | name=FALSE #BooleanLiteral
    | name=ID #VarRefExpr
    | name=THIS #ThisExpr
    ;

