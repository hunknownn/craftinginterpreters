# Lox 문법 (11장까지 최종 형상)

> `com/craftinginterpreters/my/lox/Parser.java` 기준 (직접 구현 버전).
> 11장(Resolving and Binding)은 문법을 추가하지 않고 정적 분석(Resolver)만 더하므로,
> 문법 형상은 10장(함수)까지 + 퀴즈 확장(콤마 연산자, 삼항 연산자, 에러 production)과 동일하다.

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
expression     → comma ;

comma          → assignment ( "," assignment )* ;                 // [퀴즈] 콤마 연산자

assignment     → IDENTIFIER "=" assignment
               | logic_or ;

logic_or       → logic_and ( "or" logic_and )* ;
logic_and      → conditional ( "and" conditional )* ;

conditional    → equality ( "?" expression ":" conditional )? ;   // [퀴즈] 삼항 연산자

equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;

unary          → ( "!" | "-" ) unary
               | call ;

call           → primary ( "(" arguments? ")" )* ;
arguments      → assignment ( "," assignment )* ;

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

## 참고 사항

- **퀴즈 확장 3가지** (책 원본엔 없음):
  - `comma` (콤마 연산자): `expression`의 최상위
  - `conditional` (삼항 `?:`): `logic_and`와 `equality` 사이에 위치
  - **에러 production**: `primary`에서 좌측 피연산자 없이 이항 연산자(`,` `!= ==` `> >= < <=` `+` `/ *`)가
    나오면 에러를 보고하고 우측을 우선순위에 맞게 파싱 후 버린다.
    (문법 규칙이 아닌 에러 복구용이라 위 표엔 production으로 넣지 않음)

- **`and`가 `conditional`을 자식으로 둠**: 이 구현에서는 `logic_and → conditional`,
  즉 삼항 연산자가 `and`보다 우선순위가 높다. 책 원본 순서(`and → equality`)와 다르다.

- **`arguments`와 콤마 연산자**: 콤마 연산자가 인자 구분자를 삼키는 것을 막기 위해, `finishCall`은
  인자를 `expression()`(= `comma`)이 아니라 한 단계 위인 `assignment()`로 파싱한다
  (`arguments → assignment ( "," assignment )*`). 책에서 콤마 연산자 퀴즈의 처리 방식과 동일하다.
