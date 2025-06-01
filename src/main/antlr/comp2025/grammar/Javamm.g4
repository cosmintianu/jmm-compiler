grammar Javamm;

@header {
    package pt.up.fe.comp2025;
}

/* ###############################################
   This part is to reserve the name of the variables
   ############################################### */
CLASS : 'class' ;
INT : 'int' ;
BOOLEAN : 'boolean' ;
PUBLIC : 'public' ;
RETURN : 'return' ;
EXTENDS : 'extends';
STATIC : 'static';
VOID :'void';
IF: 'if';
ELSE: 'else';
WHILE: 'while';
NEW : 'new';
THIS: 'this';
IMPORT: 'import';
TRUE: 'true';
FALSE: 'false';

INTEGER : [1-9][0-9]*|[0];
ID : [a-zA-Z_$][a-zA-Z0-9_$]*;

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
    | name= INT '...' {$isVarargs = true; $isArray = true;} #VarargsType
    | name= BOOLEAN ('[' ']' {$isArray = true;})? #BooleanType // Added array option for consistency
    | name= ID ('[' ']' {$isArray = true;})?   #ClassType
    | name= BOOLEAN '...' {$isVarargs = true; $isArray = true;} #BooleanVarargsType // Example
    | name= ID '...' {$isVarargs = true; $isArray = true;} #ClassVarargsType    // Example
    ;

methodDecl locals[boolean isMain=false, boolean isPublic=false, boolean isStatic=false]
    : // General methods
      (PUBLIC {$isPublic=true;})?
      (STATIC {$isStatic=true;})?
      type methodName=ID
      '(' (param (',' param)*)? ')'
      '{'
          varDecl* stmt*
      '}'
    |
      {$isMain=true;}
      (PUBLIC {$isPublic=true;})?
      (STATIC {$isStatic=true;}) // STATIC is mandatory for this form
      VOID methodName=ID // 'methodName' will be an ID token
      '('  param ')'
      '{'
          varDecl* stmt* // Allowing empty body
      '}'
    ;

param
    : nameType=type name=ID
    ;

stmt
    : '{' stmt* '}' #BracketStmt
    | IF '(' expr ')' stmt ELSE stmt #IfStmt
    | WHILE '(' expr ')' stmt #WhileStmt
    | expr ';' #ExprStmt
//    | name=ID '=' NEW type '[' capacity=expr ']' ';' #ArrayInitStmt // Give priority to the specific grammar
    | name=ID '[' expr ']' '=' expr ';' #ArrayAssignStmt
    | expr '=' expr ';' #VarAssignStmt
    | RETURN expr ';' #ReturnStmt //
    ;

expr
    : '(' expr ')' #ParenExpr
    | expr '[' expr ']' #IndexAccessExpr
    | '[' ( expr ( ',' expr )* )? ']' #ArrayLiteral
    | NEW INT '[' capacity=expr ']' #NewArrayExpr
    | NEW name=ID '(' ')' #NewObjectExpr
    | expr '.' name=ID '(' ( expr ( ',' expr )* )? ')' #MethodCallExpr
    | expr op=( '*' | '/' ) expr #BinaryExpr
    | expr op=( '+' | '-') expr #BinaryExpr
    | '!' expr #UnaryExpr
    | expr op=('<' | '>') expr #BinaryExpr
    | expr op=('&&' | '||') expr #BinaryExpr
    | value=INTEGER #IntegerLiteral
    | name=TRUE #BooleanLiteral
    | name=FALSE #BooleanLiteral
    | expr '.' ID #DotAccessExpr
    | name=ID #VarRefExpr
    | name=THIS #ThisExpr
    ;

