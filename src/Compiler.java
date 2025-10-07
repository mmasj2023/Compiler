import java.io.*;
import java.util.*;

public class Compiler {
    // 定义单词类别码
    private static final Map<String, String> tokenMap = new HashMap<String, String>() {{
        put("const", "CONSTTK");
        put("int", "INTTK");
        put("static", "STATICTK");
        put("break", "BREAKTK");
        put("continue", "CONTINUETK");
        put("if", "IFTK");
        put("else", "ELSETK");
        put("for", "FORTK");
        put("return", "RETURNTK");
        put("void", "VOIDTK");
        put("main", "MAINTK");
        put("printf", "PRINTFTK");
        put("!", "NOT");
        put("&&", "AND");
        put("||", "OR");
        put("+", "PLUS");
        put("-", "MINU");
        put("*", "MULT");
        put("/", "DIV");
        put("%", "MOD");
        put("<", "LSS");
        put("<=", "LEQ");
        put(">", "GRE");
        put(">=", "GEQ");
        put("==", "EQL");
        put("!=", "NEQ");
        put("=", "ASSIGN");
        put(";", "SEMICN");
        put(",", "COMMA");
        put("(", "LPARENT");
        put(")", "RPARENT");
        put("[", "LBRACK");
        put("]", "RBRACK");
        put("{", "LBRACE");
        put("}", "RBRACE");
    }};

    private static List<String> outputTokens = new ArrayList<>();
    private static List<String> errorMessages = new ArrayList<>();
    private static int lineNumber = 1;
    private static int currentCharIndex = 0;
    private static String currentLine = "";
    private static char currentChar = '\0';

    public static void main(String[] args) {
        try {
            // 读取输入文件
            BufferedReader reader = new BufferedReader(new FileReader("testfile.txt"));
            BufferedWriter lexerWriter = new BufferedWriter(new FileWriter("lexer.txt"));
            BufferedWriter errorWriter = new BufferedWriter(new FileWriter("error.txt"));

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
                        processIdentifier();
                    }
                    // 处理数字
                    else if (Character.isDigit(currentChar)) {
                        processNumber();
                    }
                    // 处理字符串
                    else if (currentChar == '"') {
                        processString();
                    }
                    // 处理注释
                    else if (currentChar == '/') {
                        if (currentCharIndex + 1 < currentLine.length()) {
                            if (currentLine.charAt(currentCharIndex + 1) == '/') {
                                break; // 跳过单行注释
                            } else if (currentLine.charAt(currentCharIndex + 1) == '*') {
                                // 处理多行注释
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
                                    if (currentLine.charAt(currentCharIndex) == '*' &&
                                            currentCharIndex + 1 < currentLine.length() &&
                                            currentLine.charAt(currentCharIndex + 1) == '/') {
                                        currentCharIndex += 2;
                                        commentEnd = true;
                                    } else {
                                        currentCharIndex++;
                                    }
                                }
                                continue;
                            } else {
                                processOperator();
                            }
                        } else {
                            processOperator();
                        }
                    }
                    // 处理运算符和分隔符
                    else {
                        processOperator();
                    }
                }
                lineNumber++;
            }
            reader.close();

            // 输出词法分析结果
            for (String token : outputTokens) {
                lexerWriter.write(token);
                lexerWriter.newLine();
            }
            lexerWriter.close();

            // 输出错误信息
            for (String error : errorMessages) {
                errorWriter.write(error);
                errorWriter.newLine();
            }
            errorWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processIdentifier() {
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
        // 检查是否是关键字
        if (tokenMap.containsKey(identifier)) {
            outputTokens.add(tokenMap.get(identifier) + " " + identifier);
        } else {
            outputTokens.add("IDENFR " + identifier);
        }
    }

    private static void processNumber() {
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

        outputTokens.add("INTCON " + sb.toString());
    }

    private static void processString() {
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

        outputTokens.add("STRCON " + sb.toString());
    }

    private static void processOperator() {
        // 检查双字符运算符
        if (currentCharIndex + 1 < currentLine.length()) {
            String twoCharOp = currentLine.substring(currentCharIndex, currentCharIndex + 2);
            if (tokenMap.containsKey(twoCharOp)) {
                outputTokens.add(tokenMap.get(twoCharOp) + " " + twoCharOp);
                currentCharIndex += 2;
                return;
            }
        }

        // 检查单字符运算符
        String singleCharOp = Character.toString(currentChar);
        if (tokenMap.containsKey(singleCharOp)) {
            outputTokens.add(tokenMap.get(singleCharOp) + " " + singleCharOp);
            currentCharIndex++;
        } else if (currentChar == '&' || currentChar == '|') {
            // 处理非法符号错误
            errorMessages.add(lineNumber + " a");
            currentCharIndex++;
        } else {
            // 报告非法字符错误
            if (currentChar == '&' || currentChar == '|') {
                errorMessages.add(lineNumber + " a");
            }
            currentCharIndex++;
        }
    }
}