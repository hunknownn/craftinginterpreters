# Lox 문법 (11장까지 최종 형상 — 책 표준)

> Crafting Interpreters 책 표준 문법. 11장(Resolving and Binding)은 문법을 추가하지 않고
> 정적 분석(Resolver)만 더하므로, 문법 형상은 10장(함수)까지와 동일하다.
> (클래스/상속은 12~13장이므로 제외)

## 1. 구문 문법 (Statements)

```ebnf
program        → declaration* EOF ;

declaration    → funDecl
               | varDecl
               | statement ;

funDecl        → "fun" function ;
function       → IDENTIFIER "(" parameters? ")" block ;
parameters     → IDENTIFIER ( "," IDENTIFIER )* ;

varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;

statement      → exprStmt
               | forStmt
               | ifStmt
               | printStmt
               | returnStmt
               | whileStmt
               | block ;

exprStmt       → expression ";" ;
forStmt        → "for" "(" ( varDecl | exprStmt | ";" )
                           expression? ";"
                           expression? ")" statement ;
ifStmt         → "if" "(" expression ")" statement
                 ( "else" statement )? ;
printStmt      → "print" expression ";" ;
returnStmt     → "return" expression? ";" ;
whileStmt      → "while" "(" expression ")" statement ;
block          → "{" declaration* "}" ;
```

## 2. 표현식 문법 (Expressions) — 우선순위 낮음 → 높음

```ebnf
expression     → assignment ;

assignment     → IDENTIFIER "=" assignment
               | logic_or ;

logic_or       → logic_and ( "or" logic_and )* ;
logic_and      → equality ( "and" equality )* ;

equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;

unary          → ( "!" | "-" ) unary
               | call ;

call           → primary ( "(" arguments? ")" )* ;
arguments      → expression ( "," expression )* ;

primary        → "true" | "false" | "nil"
               | NUMBER | STRING
               | IDENTIFIER
               | "(" expression ")" ;
```

## 3. 어휘 문법 (Terminals)

```ebnf
NUMBER         → DIGIT+ ( "." DIGIT+ )? ;
STRING         → "\"" <any char except "\"">* "\"" ;
IDENTIFIER     → ALPHA ( ALPHA | DIGIT )* ;
ALPHA          → "a" ... "z" | "A" ... "Z" | "_" ;
DIGIT          → "0" ... "9" ;
```
