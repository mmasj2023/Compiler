package lexer;

import java.util.HashMap;
import java.util.Map;

public class Keyword {
    private static final Map<String, TokenType> keywordMap = new HashMap<>();

    static {
        // 关键字
        keywordMap.put("const", TokenType.CONSTTK);
        keywordMap.put("int", TokenType.INTTK);
        keywordMap.put("static", TokenType.STATICTK);
        keywordMap.put("break", TokenType.BREAKTK);
        keywordMap.put("continue", TokenType.CONTINUETK);
        keywordMap.put("if", TokenType.IFTK);
        keywordMap.put("else", TokenType.ELSETK);
        keywordMap.put("for", TokenType.FORTK);
        keywordMap.put("return", TokenType.RETURNTK);
        keywordMap.put("void", TokenType.VOIDTK);
        keywordMap.put("main", TokenType.MAINTK);
        keywordMap.put("printf", TokenType.PRINTFTK);

        // 运算符和分隔符
        keywordMap.put("!", TokenType.NOT);
        keywordMap.put("&&", TokenType.AND);
        keywordMap.put("||", TokenType.OR);
        keywordMap.put("+", TokenType.PLUS);
        keywordMap.put("-", TokenType.MINU);
        keywordMap.put("*", TokenType.MULT);
        keywordMap.put("/", TokenType.DIV);
        keywordMap.put("%", TokenType.MOD);
        keywordMap.put("<", TokenType.LSS);
        keywordMap.put("<=", TokenType.LEQ);
        keywordMap.put(">", TokenType.GRE);
        keywordMap.put(">=", TokenType.GEQ);
        keywordMap.put("==", TokenType.EQL);
        keywordMap.put("!=", TokenType.NEQ);
        keywordMap.put("=", TokenType.ASSIGN);
        keywordMap.put(";", TokenType.SEMICN);
        keywordMap.put(",", TokenType.COMMA);
        keywordMap.put("(", TokenType.LPARENT);
        keywordMap.put(")", TokenType.RPARENT);
        keywordMap.put("[", TokenType.LBRACK);
        keywordMap.put("]", TokenType.RBRACK);
        keywordMap.put("{", TokenType.LBRACE);
        keywordMap.put("}", TokenType.RBRACE);
    }

    public static TokenType getTokenType(String str) {
        return keywordMap.get(str);
    }

    public static boolean isKeyword(String str) {
        return keywordMap.containsKey(str);
    }
}