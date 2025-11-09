package lexer;

import error.ErrorHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private int lineNumber = 1;
    private int currentCharIndex = 0;
    private String currentLine = "";
    private char currentChar = '\0';
    private final ErrorHandler errorHandler;

    public Lexer(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public List<Token> tokenize(BufferedReader reader) throws IOException {
        List<Token> tokens = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null) {
            currentLine = line;
            currentCharIndex = 0;

            while (currentCharIndex < currentLine.length()) {
                currentChar = currentLine.charAt(currentCharIndex);

                // 跳过空白字符
                if (Character.isWhitespace(currentChar)) {
                    currentCharIndex++;
                    continue;
                }

                // 处理标识符或关键字
                if (Character.isLetter(currentChar) || currentChar == '_') {
                    tokens.add(processIdentifier());
                }
                // 处理数字
                else if (Character.isDigit(currentChar)) {
                    tokens.add(processNumber());
                }
                // 处理字符串
                else if (currentChar == '"') {
                    tokens.add(processString());
                }
                // 处理注释
                else if (currentChar == '/') {
                    if (currentCharIndex + 1 < currentLine.length()) {
                        if (currentLine.charAt(currentCharIndex + 1) == '/') {
                            break; // 跳过单行注释
                        } else if (currentLine.charAt(currentCharIndex + 1) == '*') {
                            processMultiLineComment(reader);
                            continue;
                        } else {
                            tokens.add(processOperator());
                        }
                    } else {
                        tokens.add(processOperator());
                    }
                }
                // 处理运算符和分隔符
                else {
                    tokens.add(processOperator());
                }
            }
            lineNumber++;
        }

        return tokens;
    }

    private Token processIdentifier() {
        StringBuilder sb = new StringBuilder();
        sb.append(currentChar);
        currentCharIndex++;

        while (currentCharIndex < currentLine.length()) {
            currentChar = currentLine.charAt(currentCharIndex);
            if (Character.isLetterOrDigit(currentChar) || currentChar == '_') {
                sb.append(currentChar);
                currentCharIndex++;
            } else {
                break;
            }
        }

        String identifier = sb.toString();
        TokenType type = Keyword.isKeyword(identifier) ?
                Keyword.getTokenType(identifier) : TokenType.IDENFR;

        return new Token(type, identifier, lineNumber);
    }

    private Token processNumber() {
        StringBuilder sb = new StringBuilder();
        sb.append(currentChar);
        currentCharIndex++;

        while (currentCharIndex < currentLine.length()) {
            currentChar = currentLine.charAt(currentCharIndex);
            if (Character.isDigit(currentChar)) {
                sb.append(currentChar);
                currentCharIndex++;
            } else {
                break;
            }
        }

        return new Token(TokenType.INTCON, sb.toString(), lineNumber);
    }

    private Token processString() {
        StringBuilder sb = new StringBuilder();
        sb.append(currentChar); // 添加开头的双引号
        currentCharIndex++;

        while (currentCharIndex < currentLine.length()) {
            currentChar = currentLine.charAt(currentCharIndex);
            sb.append(currentChar);
            currentCharIndex++;

            if (currentChar == '"') {
                break; // 字符串结束
            }
        }

        return new Token(TokenType.STRCON, sb.toString(), lineNumber);
    }

    private Token processOperator() {
        // 检查双字符运算符
        if (currentCharIndex + 1 < currentLine.length()) {
            String twoCharOp = currentLine.substring(currentCharIndex, currentCharIndex + 2);
            if (Keyword.isKeyword(twoCharOp)) {
                Token token = new Token(Keyword.getTokenType(twoCharOp), twoCharOp, lineNumber);
                currentCharIndex += 2;
                return token;
            }
        }

        // 检查单字符运算符
        String singleCharOp = Character.toString(currentChar);
        if (Keyword.isKeyword(singleCharOp)) {
            Token token = new Token(Keyword.getTokenType(singleCharOp), singleCharOp, lineNumber);
            currentCharIndex++;
            return token;
        } else {
            Token token = new Token(
                    currentChar == '&' ? TokenType.AND : TokenType.OR,
                    singleCharOp,
                    lineNumber
            );
            currentCharIndex++;
            return token;
        }
    }

    private void processMultiLineComment(BufferedReader reader) throws IOException {
        currentCharIndex += 2; // 跳过/*
        boolean commentEnd = false;

        while (!commentEnd) {
            if (currentCharIndex >= currentLine.length()) {
                if (!reader.ready()) break;
                currentLine = reader.readLine();
                lineNumber++;
                currentCharIndex = 0;
                continue;
            }
            if (currentCharIndex + 1 < currentLine.length() &&
                    currentLine.charAt(currentCharIndex) == '*' &&
                    currentLine.charAt(currentCharIndex + 1) == '/') {
                currentCharIndex += 2;
                commentEnd = true;
            } else {
                currentCharIndex++;
            }
        }
    }
}