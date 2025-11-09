package parser;

import lexer.Token;

import java.util.ArrayList;
import java.util.List;

public class SyntaxNode {
    public enum NodeType { TERMINAL, NON_TERMINAL }

    private final NodeType type;
    private final String syntaxType;
    private int lineNumber;
    private final Token token;
    private final List<SyntaxNode> children = new ArrayList<>();

    public SyntaxNode(NodeType type, String SyntaxType) {
        this.type = type;
        this.syntaxType = SyntaxType;
        this.token = null;
        this.lineNumber = -1;
    }

    public SyntaxNode(NodeType type, Token token) {
        this.type = type;
        this.syntaxType = null;
        this.token = token;
        this.lineNumber = token.getLineNumber();
    }

    public void addChild(SyntaxNode node) {
        children.add(node);
        if (lineNumber == -1) {
            lineNumber = children.get(0).lineNumber;
        }
    }

    public NodeType getType() { return type; }
    public String getSyntaxType() { return syntaxType; }
    public Token getToken() { return token; }
    public List<SyntaxNode> getChildren() { return children; }
    public int getLineNumber() {
        if (lineNumber != -1) return lineNumber;
        if (type == NodeType.TERMINAL && token != null) {
            return token.getLineNumber();
        }
        if (!children.isEmpty()) {
            return children.get(0).lineNumber;
        }
        return -1;
    }

    @Override
    public String toString() {
        return type == NodeType.NON_TERMINAL ? syntaxType : token.toString();
    }
}
