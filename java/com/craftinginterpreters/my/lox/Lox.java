package com.craftinginterpreters.my.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    static Scanner scanner = new Scanner();
    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()), false);

        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70); // 65, 70 뭐지?

        /*
          man sysexits 으로 system code 볼 수 있음
          os 마다 다름
         */
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (; ; ) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            run(line, true);
            hadError = false;
        }
    }

    private static void run(String source, boolean repl) {
        List<Token> tokens = scanner.scanTokens(source);

        // REPL이고 세미콜론이 없으면 → 단일 식으로 보고 결과를 자동 출력.
        if (repl && !hasSemicolon(tokens)) {
            // 빈 줄(EOF 토큰만 있는 경우)은 무시.
            if (tokens.size() <= 1) return;

            Parser parser = new Parser(tokens);
            Expr expr = parser.parseExpression();

            if (hadError) return;

            interpreter.interpret(expr);
            return;
        }

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (hadError) return;

        interpreter.interpret(statements);

        System.out.println();
        System.out.println("----------------------------------");

//        System.out.println(new AstPrinter().print(expression));

        for (Token token : tokens) {
            System.out.println(token);
        }
        System.out.println("----------------------------------");
        System.out.println();
        System.out.println();
    }

    private static boolean hasSemicolon(List<Token> tokens) {
        for (Token token : tokens) {
            if (token.type == TokenType.SEMICOLON) return true;
        }
        return false;
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
