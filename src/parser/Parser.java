package parser;

import error.ErrorHandler;
import lexer.Token;
import lexer.TokenType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int tokenIndex = 0;
    private final ErrorHandler errorHandler;
    private final BufferedWriter parserWriter;
    private SyntaxNode rootNode;

    public Parser(List<Token> tokens, ErrorHandler errorHandler, BufferedWriter parserWriter) {
        this.tokens = tokens;
        this.errorHandler = errorHandler;
        this.parserWriter = parserWriter;
    }

    public SyntaxNode parse() {
        rootNode = parseCompUnit();
        return rootNode;
    }

    private boolean matchToken(TokenType type) {
        return tokens.get(tokenIndex).getType().equals(type);
    }

    private boolean preMatchToken(TokenType type) {
        if (tokenIndex + 1 >= tokens.size()) return false;
        return tokens.get(tokenIndex + 1).getType().equals(type);
    }

    private boolean prePreMatchToken(TokenType type) {
        if (tokenIndex + 2 >= tokens.size()) return false;
        return tokens.get(tokenIndex + 2).getType().equals(type);
    }

    private Token consumeToken(TokenType expectedType) {
        Token token = tokens.get(tokenIndex);
        tokenIndex++;
        return token;
    }

    private void outputToken(Token token) {
        try {
            parserWriter.write(token.toString());
            parserWriter.newLine();
            // System.out.println(token.toString());
        } catch (IOException e) {
            System.out.println("Error while writing output");
            e.printStackTrace();
        }
    }

    private void outputSyntaxNode(SyntaxNode node) {
        if (node.getSyntaxType().equals("BlockItem") || node.getSyntaxType().equals("Decl") || node.getSyntaxType().equals("BType")) {
            return;
        }
        try {
            parserWriter.write("<" + node.getSyntaxType() + ">");
            parserWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getTokenLine() {
        if (tokenIndex < tokens.size()) return tokens.get(tokenIndex - 1).getLineNumber();
        if (!tokens.isEmpty()) return tokens.get(tokens.size() - 1).getLineNumber();
        return 0;
    }

    private SyntaxNode parseCompUnit() {
        SyntaxNode compUnitNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "CompUnit");

        while (tokenIndex < tokens.size() && (matchToken(TokenType.CONSTTK) || matchToken(TokenType.STATICTK) || (matchToken(TokenType.INTTK) && preMatchToken(TokenType.IDENFR) && (!prePreMatchToken(TokenType.LPARENT))))) {
            SyntaxNode declNode = parseDecl();
            compUnitNode.addChild(declNode);
        }

        while (tokenIndex < tokens.size() && (matchToken(TokenType.VOIDTK) || (matchToken(TokenType.INTTK) && preMatchToken(TokenType.IDENFR) && prePreMatchToken(TokenType.LPARENT)))) {
            SyntaxNode funcDefNode = parseFuncDef();
            compUnitNode.addChild(funcDefNode);
        }

        SyntaxNode mainFuncDefNode = parseMainFuncDef();
        compUnitNode.addChild(mainFuncDefNode);

        outputSyntaxNode(compUnitNode);
        return compUnitNode;
    }

    private SyntaxNode parseDecl() {
        SyntaxNode declNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "Decl");

        if (tokenIndex < tokens.size() && matchToken(TokenType.CONSTTK)) {
            SyntaxNode constDeclNode = parseConstDecl();
            declNode.addChild(constDeclNode);
        } else {
            SyntaxNode varDeclNode = parseVarDecl();
            declNode.addChild(varDeclNode);
        }

        outputSyntaxNode(declNode);
        return declNode;
    }

    private SyntaxNode parseConstDecl() {
        SyntaxNode constDeclNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "ConstDecl");

        Token constToken = consumeToken(TokenType.CONSTTK);
        SyntaxNode constNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, constToken);
        constDeclNode.addChild(constNode);
        outputToken(constToken);

        SyntaxNode bTypeNode = parseBType();
        constDeclNode.addChild(bTypeNode);

        SyntaxNode constDefNode = parseConstDef();
        constDeclNode.addChild(constDefNode);

        while (tokenIndex < tokens.size() && matchToken(TokenType.COMMA)) {
            Token commaToken = consumeToken(TokenType.COMMA);
            SyntaxNode commaNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, commaToken);
            constDeclNode.addChild(commaNode);
            outputToken(commaToken);

            SyntaxNode nextConstDefNode = parseConstDef();
            constDeclNode.addChild(nextConstDefNode);
        }

        if (tokenIndex < tokens.size() && matchToken(TokenType.SEMICN)) {
            Token semicnToken = consumeToken(TokenType.SEMICN);
            SyntaxNode semicnNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, semicnToken);
            constDeclNode.addChild(semicnNode);
            outputToken(semicnToken);
        } else {
            errorHandler.reportError(getTokenLine(), "i");
        }

        outputSyntaxNode(constDeclNode);
        return constDeclNode;
    }

    private SyntaxNode parseBType() {
        SyntaxNode bTypeNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "BType");

        Token intToken = consumeToken(TokenType.INTTK);
        SyntaxNode intNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, intToken);
        bTypeNode.addChild(intNode);
        outputToken(intToken);

        outputSyntaxNode(bTypeNode);
        return bTypeNode;
    }

    private SyntaxNode parseConstDef() {
        SyntaxNode constDefNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "ConstDef");

        Token identToken = consumeToken(TokenType.IDENFR);
        SyntaxNode identNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, identToken);
        constDefNode.addChild(identNode);
        outputToken(identToken);

        if (tokenIndex < tokens.size() && matchToken(TokenType.LBRACK)) {
            Token lBrackToken = consumeToken(TokenType.LBRACK);
            SyntaxNode lBrackNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, lBrackToken);
            constDefNode.addChild(lBrackNode);
            outputToken(lBrackToken);

            SyntaxNode constExpNode = parseConstExp();
            constDefNode.addChild(constExpNode);

            if (matchToken(TokenType.RBRACK)) {
                Token rBrackToken = consumeToken(TokenType.RBRACK);
                SyntaxNode rBrackNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, rBrackToken);
                constDefNode.addChild(rBrackNode);
                outputToken(rBrackToken);
            } else {
                errorHandler.reportError(getTokenLine(), "k");
            }
        }

        Token assignToken = consumeToken(TokenType.ASSIGN);
        SyntaxNode assignNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, assignToken);
        constDefNode.addChild(assignNode);
        outputToken(assignToken);

        SyntaxNode constInitValNode = parseConstInitVal();
        constDefNode.addChild(constInitValNode);

        outputSyntaxNode(constDefNode);
        return constDefNode;
    }

    private SyntaxNode parseConstExp() {
        SyntaxNode constExpNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "ConstExp");

        SyntaxNode addExpNode = parseAddExp();
        constExpNode.addChild(addExpNode);

        outputSyntaxNode(constExpNode);
        return constExpNode;
    }

    private SyntaxNode parseAddExp() {
        SyntaxNode addExpNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "AddExp");

        SyntaxNode mulExpNode = parseMulExp();
        addExpNode.addChild(mulExpNode);
        outputSyntaxNode(addExpNode);

        while (tokenIndex < tokens.size() && (matchToken(TokenType.PLUS) || matchToken(TokenType.MINU))) {
            if (matchToken(TokenType.PLUS)) {
                Token plusToken = consumeToken(TokenType.PLUS);
                SyntaxNode plusNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, plusToken);
                addExpNode.addChild(plusNode);
                outputToken(plusToken);
            } else {
                Token minusToken = consumeToken(TokenType.MINU);
                SyntaxNode minusNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, minusToken);
                addExpNode.addChild(minusNode);
                outputToken(minusToken);
            }
            SyntaxNode nextMulExpNode = parseMulExp();
            addExpNode.addChild(nextMulExpNode);
            outputSyntaxNode(addExpNode);
        }

        return addExpNode;
    }

    private SyntaxNode parseMulExp() {
        SyntaxNode mulExpNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "MulExp");

        SyntaxNode unaryExpNode = parseUnaryExp();
        mulExpNode.addChild(unaryExpNode);
        outputSyntaxNode(mulExpNode);

        while (tokenIndex < tokens.size() && (matchToken(TokenType.MULT) || matchToken(TokenType.DIV) ||  matchToken(TokenType.MOD))) {
            if (matchToken(TokenType.MULT)) {
                Token multToken = consumeToken(TokenType.MULT);
                SyntaxNode multNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, multToken);
                mulExpNode.addChild(multNode);
                outputToken(multToken);
            } else if (matchToken(TokenType.DIV)) {
                Token divToken = consumeToken(TokenType.DIV);
                SyntaxNode divNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, divToken);
                mulExpNode.addChild(divNode);
                outputToken(divToken);
            } else {
                Token modToken = consumeToken(TokenType.MOD);
                SyntaxNode modNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, modToken);
                mulExpNode.addChild(modNode);
                outputToken(modToken);
            }
            SyntaxNode nextMulExpNode = parseUnaryExp();
            mulExpNode.addChild(nextMulExpNode);
            outputSyntaxNode(mulExpNode);
        }

        return mulExpNode;
    }

    private SyntaxNode parseUnaryExp() {
        SyntaxNode unaryExpNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "UnaryExp");

        if (tokenIndex < tokens.size() && (matchToken(TokenType.PLUS) || matchToken(TokenType.MINU) || matchToken(TokenType.NOT))) {
            SyntaxNode unaryOpNode = parseUnaryOp();
            unaryExpNode.addChild(unaryOpNode);

            SyntaxNode nextUnaryExpNode = parseUnaryExp();
            unaryExpNode.addChild(nextUnaryExpNode);
        } else if (matchToken(TokenType.LPARENT) || matchToken(TokenType.INTCON)) {
            SyntaxNode primaryExpNode = parsePrimaryExp();
            unaryExpNode.addChild(primaryExpNode);
        } else if (preMatchToken(TokenType.LPARENT)) {
            Token idenToken = consumeToken(TokenType.IDENFR);
            SyntaxNode idenNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, idenToken);
            unaryExpNode.addChild(idenNode);
            outputToken(idenToken);

            Token lParenToken = consumeToken(TokenType.LPARENT);
            SyntaxNode lParenNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, lParenToken);
            unaryExpNode.addChild(lParenNode);
            outputToken(lParenToken);

            if (tokenIndex < tokens.size() && (matchToken(TokenType.PLUS) || matchToken(TokenType.MINU) || matchToken(TokenType.NOT) || matchToken(TokenType.IDENFR) || matchToken(TokenType.LPARENT) || matchToken(TokenType.INTCON))) {
                SyntaxNode funcRParamsNode = parseFuncRParams();
                unaryExpNode.addChild(funcRParamsNode);
            }

            if (tokenIndex < tokens.size() && matchToken(TokenType.RPARENT)) {
                Token rParenToken = consumeToken(TokenType.RPARENT);
                SyntaxNode rParenNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, rParenToken);
                unaryExpNode.addChild(rParenNode);
                outputToken(rParenToken);
            } else {
                errorHandler.reportError(getTokenLine(), "j");
            }
        } else {
            SyntaxNode primaryExpNode = parsePrimaryExp();
            unaryExpNode.addChild(primaryExpNode);
        }

        outputSyntaxNode(unaryExpNode);
        return unaryExpNode;
    }

    private SyntaxNode parsePrimaryExp() {
        SyntaxNode primaryExpNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "PrimaryExp");
        if (tokenIndex < tokens.size() && matchToken(TokenType.LPARENT)) {
            Token lParentToken = consumeToken(TokenType.LPARENT);
            SyntaxNode lParentNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, lParentToken);
            primaryExpNode.addChild(lParentNode);
            outputToken(lParentToken);

            SyntaxNode expNode = parseExp();
            primaryExpNode.addChild(expNode);

            if (tokenIndex < tokens.size() && matchToken(TokenType.RPARENT)) {
                Token rParentToken = consumeToken(TokenType.RPARENT);
                SyntaxNode rParentNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, rParentToken);
                primaryExpNode.addChild(rParentNode);
                outputToken(rParentToken);
            } else {
                errorHandler.reportError(getTokenLine(), "j");
            }
        } else if (tokenIndex < tokens.size() && matchToken(TokenType.IDENFR)) {
            SyntaxNode lValNode = parseLVal();
            primaryExpNode.addChild(lValNode);
        } else {
            SyntaxNode numberNode = parseNumber();
            primaryExpNode.addChild(numberNode);
        }

        outputSyntaxNode(primaryExpNode);
        return primaryExpNode;
    }

    private SyntaxNode parseExp() {
        SyntaxNode expNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "Exp");

        SyntaxNode addExpNode = parseAddExp();
        expNode.addChild(addExpNode);

        outputSyntaxNode(expNode);
        return expNode;
    }

    private SyntaxNode parseLVal() {
        SyntaxNode lValNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "LVal");

        Token idenToken = consumeToken(TokenType.IDENFR);
        SyntaxNode idenNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, idenToken);
        lValNode.addChild(idenNode);
        outputToken(idenToken);

        if (tokenIndex < tokens.size() && matchToken(TokenType.LBRACK)) {
            Token lBrackToken = consumeToken(TokenType.LBRACK);
            SyntaxNode lBrackNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, lBrackToken);
            lValNode.addChild(lBrackNode);
            outputToken(lBrackToken);

            SyntaxNode expNode = parseExp();
            lValNode.addChild(expNode);

            if (tokenIndex < tokens.size() && matchToken(TokenType.RBRACK)) {
                Token rBrackToken = consumeToken(TokenType.RBRACK);
                SyntaxNode rBrackNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, rBrackToken);
                lValNode.addChild(rBrackNode);
                outputToken(rBrackToken);
            } else {
                errorHandler.reportError(getTokenLine(), "k");
            }
        }

        outputSyntaxNode(lValNode);
        return lValNode;
    }

    private SyntaxNode parseNumber() {
        SyntaxNode numberNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "Number");

        Token intConToken = consumeToken(TokenType.INTCON);
        SyntaxNode intConNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, intConToken);
        numberNode.addChild(intConNode);
        outputToken(intConToken);

        outputSyntaxNode(numberNode);
        return numberNode;
    }

    private SyntaxNode parseFuncRParams() {
        SyntaxNode funcRParamsNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "FuncRParams");

        SyntaxNode expNode = parseExp();
        funcRParamsNode.addChild(expNode);

        while (tokenIndex + 1 < tokens.size() && matchToken(TokenType.COMMA)) {
            Token commaToken = consumeToken(TokenType.COMMA);
            SyntaxNode commaNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, commaToken);
            funcRParamsNode.addChild(commaNode);
            outputToken(commaToken);

            SyntaxNode nextExpNode = parseExp();
            funcRParamsNode.addChild(nextExpNode);
        }

        outputSyntaxNode(funcRParamsNode);
        return funcRParamsNode;
    }

    private SyntaxNode parseUnaryOp() {
        SyntaxNode unaryOpNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "UnaryOp");
        if (tokenIndex < tokens.size()) {
            if (matchToken(TokenType.PLUS)) {
                Token plusToken = consumeToken(TokenType.PLUS);
                SyntaxNode plusNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, plusToken);
                unaryOpNode.addChild(plusNode);
                outputToken(plusToken);
            } else if (matchToken(TokenType.MINU)) {
                Token minuToken = consumeToken(TokenType.MINU);
                SyntaxNode minuNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, minuToken);
                unaryOpNode.addChild(minuNode);
                outputToken(minuToken);
            } else {
                Token notToken = consumeToken(TokenType.NOT);
                SyntaxNode notNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, notToken);
                unaryOpNode.addChild(notNode);
                outputToken(notToken);
            }
        }

        outputSyntaxNode(unaryOpNode);
        return unaryOpNode;
    }

    private SyntaxNode parseConstInitVal() {
        SyntaxNode constInitValNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "ConstInitVal");

        if (tokenIndex < tokens.size() && matchToken(TokenType.LBRACE)) {
            Token lBraceToken = consumeToken(TokenType.LBRACE);
            SyntaxNode lBraceNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, lBraceToken);
            constInitValNode.addChild(lBraceNode);
            outputToken(lBraceToken);

            if (tokenIndex < tokens.size() && matchToken(TokenType.RBRACE)) {
                Token rBraceToken = consumeToken(TokenType.RBRACE);
                SyntaxNode rBraceNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, rBraceToken);
                constInitValNode.addChild(rBraceNode);
                outputToken(rBraceToken);
            } else {
                SyntaxNode constExpNode = parseConstExp();
                constInitValNode.addChild(constExpNode);

                while (tokenIndex < tokens.size() && matchToken(TokenType.COMMA)) {
                    Token commaToken = consumeToken(TokenType.COMMA);
                    SyntaxNode commaNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, commaToken);
                    constInitValNode.addChild(commaNode);
                    outputToken(commaToken);

                    SyntaxNode nextConstExpNode = parseConstExp();
                    constInitValNode.addChild(nextConstExpNode);
                }

                Token rBraceToken = consumeToken(TokenType.RBRACE);
                SyntaxNode rBraceNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, rBraceToken);
                constInitValNode.addChild(rBraceNode);
                outputToken(rBraceToken);
            }
        } else {
            SyntaxNode constExpNode = parseConstExp();
            constInitValNode.addChild(constExpNode);
        }

        outputSyntaxNode(constInitValNode);
        return constInitValNode;
    }

    private SyntaxNode parseVarDecl() {
        SyntaxNode varDeclNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "VarDecl");

        if (tokenIndex < tokens.size() && matchToken(TokenType.STATICTK)) {
            Token staticToken = consumeToken(TokenType.STATICTK);
            SyntaxNode staticNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, staticToken);
            varDeclNode.addChild(staticNode);
            outputToken(staticToken);
        }

        SyntaxNode bTypeNode = parseBType();
        varDeclNode.addChild(bTypeNode);

        SyntaxNode varDefNode = parseVarDef();
        varDeclNode.addChild(varDefNode);

        while (tokenIndex < tokens.size() && matchToken(TokenType.COMMA)) {
            Token commaToken = consumeToken(TokenType.COMMA);
            SyntaxNode commaNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, commaToken);
            varDeclNode.addChild(commaNode);
            outputToken(commaToken);

            SyntaxNode nextVarDefNode = parseVarDef();
            varDeclNode.addChild(nextVarDefNode);
        }

        if (tokenIndex < tokens.size() && matchToken(TokenType.SEMICN)) {
            Token semicnToken = consumeToken(TokenType.SEMICN);
            SyntaxNode semicnNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, semicnToken);
            varDeclNode.addChild(semicnNode);
            outputToken(semicnToken);
        } else {
            errorHandler.reportError(getTokenLine(), "i");
        }

        outputSyntaxNode(varDeclNode);
        return varDeclNode;
    }

    private SyntaxNode parseVarDef() {
        SyntaxNode varDefNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "VarDef");

        Token idenToken = consumeToken(TokenType.IDENFR);
        SyntaxNode idenNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, idenToken);
        varDefNode.addChild(idenNode);
        outputToken(idenToken);

        if (tokenIndex < tokens.size() && matchToken(TokenType.LBRACK)) {
            Token lBrackToken = consumeToken(TokenType.LBRACK);
            SyntaxNode lBrackNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, lBrackToken);
            varDefNode.addChild(lBrackNode);
            outputToken(lBrackToken);

            SyntaxNode constExpNode = parseConstExp();
            varDefNode.addChild(constExpNode);

            if (tokenIndex < tokens.size() && matchToken(TokenType.RBRACK)) {
                Token rBrackToken = consumeToken(TokenType.RBRACK);
                SyntaxNode rBrackNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, rBrackToken);
                varDefNode.addChild(rBrackNode);
                outputToken(rBrackToken);
            } else {
                errorHandler.reportError(getTokenLine(), "k");
            }
        }

        if (tokenIndex < tokens.size() && matchToken(TokenType.ASSIGN)) {
            Token assignToken = consumeToken(TokenType.ASSIGN);
            SyntaxNode assignNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, assignToken);
            varDefNode.addChild(assignNode);
            outputToken(assignToken);

            SyntaxNode initValNode = parseInitVal();
            varDefNode.addChild(initValNode);
        }

        outputSyntaxNode(varDefNode);
        return varDefNode;
    }

    private SyntaxNode parseInitVal() {
        SyntaxNode initValNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "InitVal");

        if (tokenIndex < tokens.size() && matchToken(TokenType.LBRACE)) {
            Token lBraceToken = consumeToken(TokenType.LBRACE);
            SyntaxNode lBraceNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, lBraceToken);
            initValNode.addChild(lBraceNode);
            outputToken(lBraceToken);

            if (tokenIndex < tokens.size() && matchToken(TokenType.RBRACE)) {
                Token rBraceToken = consumeToken(TokenType.RBRACE);
                SyntaxNode rBraceNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, rBraceToken);
                initValNode.addChild(rBraceNode);
                outputToken(rBraceToken);
            } else {
                SyntaxNode expNode = parseExp();
                initValNode.addChild(expNode);

                while (tokenIndex < tokens.size() && matchToken(TokenType.COMMA)) {
                    Token commaToken = consumeToken(TokenType.COMMA);
                    SyntaxNode commaNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, commaToken);
                    initValNode.addChild(commaNode);
                    outputToken(commaToken);

                    SyntaxNode nextExpNode = parseExp();
                    initValNode.addChild(nextExpNode);
                }

                Token rBraceToken = consumeToken(TokenType.RBRACE);
                SyntaxNode rBraceNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, rBraceToken);
                initValNode.addChild(rBraceNode);
                outputToken(rBraceToken);
            }
        } else {
            SyntaxNode expNode = parseExp();
            initValNode.addChild(expNode);
        }

        outputSyntaxNode(initValNode);
        return initValNode;
    }

    private SyntaxNode parseFuncDef() {
        SyntaxNode funcDefNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "FuncDef");

        SyntaxNode funcTypeNode = parseFuncType();
        funcDefNode.addChild(funcTypeNode);

        Token idenToken = consumeToken(TokenType.IDENFR);
        SyntaxNode idenNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, idenToken);
        funcDefNode.addChild(idenNode);
        outputToken(idenToken);

        Token lParentToken = consumeToken(TokenType.LPARENT);
        SyntaxNode lParentNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, lParentToken);
        funcDefNode.addChild(lParentNode);
        outputToken(lParentToken);

        if (tokenIndex < tokens.size() && matchToken(TokenType.INTTK)) {
            SyntaxNode funcFParamsNode = parseFuncFParams();
            funcDefNode.addChild(funcFParamsNode);
        }

        if (tokenIndex < tokens.size() && matchToken(TokenType.RPARENT)) {
            Token rParentToken = consumeToken(TokenType.RPARENT);
            SyntaxNode rParentNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, rParentToken);
            funcDefNode.addChild(rParentNode);
            outputToken(rParentToken);
        } else {
            errorHandler.reportError(getTokenLine(), "j");
        }

        SyntaxNode blockNode = parseBlock();
        funcDefNode.addChild(blockNode);

        outputSyntaxNode(funcDefNode);
        return funcDefNode;
    }

    private SyntaxNode parseFuncType() {
        SyntaxNode funcTypeNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "FuncType");

        if (tokenIndex < tokens.size() && matchToken(TokenType.INTTK)) {
            Token intToken = consumeToken(TokenType.INTTK);
            SyntaxNode intNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, intToken);
            funcTypeNode.addChild(intNode);
            outputToken(intToken);
        } else {
            Token voidToken = consumeToken(TokenType.VOIDTK);
            SyntaxNode voidNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, voidToken);
            funcTypeNode.addChild(voidNode);
            outputToken(voidToken);
        }

        outputSyntaxNode(funcTypeNode);
        return funcTypeNode;
    }

    private SyntaxNode parseFuncFParams() {
        SyntaxNode funcFParamsNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "FuncFParams");

        SyntaxNode funcFParamNode = parseFuncFParam();
        funcFParamsNode.addChild(funcFParamNode);

        while (tokenIndex < tokens.size() && matchToken(TokenType.COMMA)) {
            Token commaToken = consumeToken(TokenType.COMMA);
            SyntaxNode commaNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, commaToken);
            funcFParamsNode.addChild(commaNode);
            outputToken(commaToken);

            SyntaxNode nextFuncFParamNode = parseFuncFParam();
            funcFParamsNode.addChild(nextFuncFParamNode);
        }

        outputSyntaxNode(funcFParamsNode);
        return funcFParamsNode;
    }

    private SyntaxNode parseFuncFParam() {
        SyntaxNode FuncFParamNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "FuncFParam");

        SyntaxNode bTypeNode = parseBType();
        FuncFParamNode.addChild(bTypeNode);

        Token idenToken = consumeToken(TokenType.IDENFR);
        SyntaxNode idenNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, idenToken);
        FuncFParamNode.addChild(idenNode);
        outputToken(idenToken);

        if (tokenIndex < tokens.size() && matchToken(TokenType.LBRACK)) {
            Token lBrackToken = consumeToken(TokenType.LBRACK);
            SyntaxNode lBrackNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, lBrackToken);
            FuncFParamNode.addChild(lBrackNode);
            outputToken(lBrackToken);

            if (tokenIndex < tokens.size() && matchToken(TokenType.RBRACK)) {
                Token rBrackToken = consumeToken(TokenType.RBRACK);
                SyntaxNode rBrackNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, rBrackToken);
                FuncFParamNode.addChild(rBrackNode);
                outputToken(rBrackToken);
            } else {
                errorHandler.reportError(getTokenLine(), "k");
            }
        }

        outputSyntaxNode(FuncFParamNode);
        return FuncFParamNode;
    }

    private SyntaxNode parseBlock() {
        SyntaxNode blockNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "Block");

        Token lBraceToken = consumeToken(TokenType.LBRACE);
        SyntaxNode lBraceNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, lBraceToken);
        blockNode.addChild(lBraceNode);
        outputToken(lBraceToken);

        while (tokenIndex < tokens.size() && !matchToken(TokenType.RBRACE)) {
            SyntaxNode blockItemNode = parseBlockItem();
            blockNode.addChild(blockItemNode);
        }

        Token rBraceToken = consumeToken(TokenType.RBRACE);
        SyntaxNode rBraceNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, rBraceToken);
        blockNode.addChild(rBraceNode);
        outputToken(rBraceToken);

        outputSyntaxNode(blockNode);
        return blockNode;
    }

    private SyntaxNode parseBlockItem() {
        SyntaxNode blockItemNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "BlockItem");

        if (tokenIndex < tokens.size() && (matchToken(TokenType.CONSTTK) || matchToken(TokenType.INTTK) || matchToken(TokenType.STATICTK))) {
            SyntaxNode declNode = parseDecl();
            blockItemNode.addChild(declNode);
        } else {
            SyntaxNode stmtNode = parseStmt();
            blockItemNode.addChild(stmtNode);
        }

        outputSyntaxNode(blockItemNode);
        return blockItemNode;
    }

    private SyntaxNode parseStmt() {
        SyntaxNode stmtNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "Stmt");

        if (tokenIndex < tokens.size() && matchToken(TokenType.IFTK)) {
            Token ifToken = consumeToken(TokenType.IFTK);
            SyntaxNode ifNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, ifToken);
            stmtNode.addChild(ifNode);
            outputToken(ifToken);

            Token lParentToken = consumeToken(TokenType.LPARENT);
            SyntaxNode lParentNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, lParentToken);
            stmtNode.addChild(lParentNode);
            outputToken(lParentToken);

            SyntaxNode condNode = parseCond();
            stmtNode.addChild(condNode);

            if (tokenIndex < tokens.size() && matchToken(TokenType.RPARENT)) {
                Token rParentToken = consumeToken(TokenType.RPARENT);
                SyntaxNode rParentNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, rParentToken);
                stmtNode.addChild(rParentNode);
                outputToken(rParentToken);
            } else {
                errorHandler.reportError(getTokenLine(), "j");
            }

            SyntaxNode nextStmtNode = parseStmt();
            stmtNode.addChild(nextStmtNode);

            if (tokenIndex < tokens.size() && matchToken(TokenType.ELSETK)) {
                Token elseToken = consumeToken(TokenType.ELSETK);
                SyntaxNode elseNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, elseToken);
                stmtNode.addChild(elseNode);
                outputToken(elseToken);

                SyntaxNode anotherStmtNode = parseStmt();
                stmtNode.addChild(anotherStmtNode);
            }
        } else if (tokenIndex < tokens.size() && matchToken(TokenType.FORTK)) {
            Token forToken = consumeToken(TokenType.FORTK);
            SyntaxNode forNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, forToken);
            stmtNode.addChild(forNode);
            outputToken(forToken);

            Token lParentToken = consumeToken(TokenType.LPARENT);
            SyntaxNode lParentNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, lParentToken);
            stmtNode.addChild(lParentNode);
            outputToken(lParentToken);

            if (tokenIndex < tokens.size() && matchToken(TokenType.IDENFR)) {
                SyntaxNode forStmtNode = parseForStmt();
                stmtNode.addChild(forStmtNode);
            }

            Token semicnToken = consumeToken(TokenType.SEMICN);
            SyntaxNode semicNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, semicnToken);
            stmtNode.addChild(semicNode);
            outputToken(semicnToken);

            if (tokenIndex < tokens.size() && !(matchToken(TokenType.SEMICN))) {
                SyntaxNode condNode = parseCond();
                stmtNode.addChild(condNode);
            }

            Token nextsemicnToken = consumeToken(TokenType.SEMICN);
            SyntaxNode nextSemicnNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, nextsemicnToken);
            stmtNode.addChild(nextSemicnNode);
            outputToken(nextsemicnToken);

            if (tokenIndex < tokens.size() && matchToken(TokenType.IDENFR)) {
                SyntaxNode forStmtNode = parseForStmt();
                stmtNode.addChild(forStmtNode);
            }

            if (tokenIndex < tokens.size() && matchToken(TokenType.RPARENT)) {
                Token rParentToken = consumeToken(TokenType.RPARENT);
                SyntaxNode rParentNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, rParentToken);
                stmtNode.addChild(rParentNode);
                outputToken(rParentToken);
            } else {
                errorHandler.reportError(getTokenLine(), "j");
            }

            SyntaxNode StmtNode = parseStmt();
            stmtNode.addChild(StmtNode);
        } else if (tokenIndex < tokens.size() && matchToken(TokenType.BREAKTK)) {
            Token breakToken = consumeToken(TokenType.BREAKTK);
            SyntaxNode breakNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, breakToken);
            stmtNode.addChild(breakNode);
            outputToken(breakToken);

            if (tokenIndex < tokens.size() && matchToken(TokenType.SEMICN)) {
                Token semicnToken = consumeToken(TokenType.SEMICN);
                SyntaxNode semicNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, semicnToken);
                stmtNode.addChild(semicNode);
                outputToken(semicnToken);
            } else {
                errorHandler.reportError(getTokenLine(), "i");
            }
        } else if (tokenIndex < tokens.size() && matchToken(TokenType.CONTINUETK)) {
            Token continueToken = consumeToken(TokenType.CONTINUETK);
            SyntaxNode continueNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, continueToken);
            stmtNode.addChild(continueNode);
            outputToken(continueToken);

            if (tokenIndex < tokens.size() && matchToken(TokenType.SEMICN)) {
                Token semicnToken = consumeToken(TokenType.SEMICN);
                SyntaxNode semicNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, semicnToken);
                stmtNode.addChild(semicNode);
                outputToken(semicnToken);
            } else {
                errorHandler.reportError(getTokenLine(), "i");
            }
        } else if (tokenIndex < tokens.size() && matchToken(TokenType.RETURNTK)) {
            Token returnToken = consumeToken(TokenType.RETURNTK);
            SyntaxNode returnNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, returnToken);
            stmtNode.addChild(returnNode);
            outputToken(returnToken);

            if (tokenIndex < tokens.size() && (matchToken(TokenType.PLUS) || matchToken(TokenType.MINU) || matchToken(TokenType.NOT) || matchToken(TokenType.IDENFR) || matchToken(TokenType.LPARENT) || matchToken(TokenType.INTCON))) {
                SyntaxNode expNode = parseExp();
                stmtNode.addChild(expNode);
            }

            if (tokenIndex < tokens.size() && matchToken(TokenType.SEMICN)) {
                Token semicnToken = consumeToken(TokenType.SEMICN);
                SyntaxNode semicNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, semicnToken);
                stmtNode.addChild(semicNode);
                outputToken(semicnToken);
            } else {
                errorHandler.reportError(getTokenLine(), "i");
            }
        } else if (tokenIndex < tokens.size() && matchToken(TokenType.PRINTFTK)) {
            Token printToken = consumeToken(TokenType.PRINTFTK);
            SyntaxNode printNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, printToken);
            stmtNode.addChild(printNode);
            outputToken(printToken);

            Token lParentToken = consumeToken(TokenType.LPARENT);
            SyntaxNode lParentNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, lParentToken);
            stmtNode.addChild(lParentNode);
            outputToken(lParentToken);

            Token stringConstToken = consumeToken(TokenType.STRCON);
            SyntaxNode stringConstNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, stringConstToken);
            stmtNode.addChild(stringConstNode);
            outputToken(stringConstToken);

            while (tokenIndex < tokens.size() && matchToken(TokenType.COMMA)) {
                Token commaToken = consumeToken(TokenType.COMMA);
                SyntaxNode commaNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, commaToken);
                stmtNode.addChild(commaNode);
                outputToken(commaToken);

                SyntaxNode expNode = parseExp();
                stmtNode.addChild(expNode);
            }

            if (tokenIndex < tokens.size() && matchToken(TokenType.RPARENT)) {
                Token rParentToken = consumeToken(TokenType.RPARENT);
                SyntaxNode rParentNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, rParentToken);
                stmtNode.addChild(rParentNode);
                outputToken(rParentToken);
            } else {
                errorHandler.reportError(getTokenLine(), "j");
            }

            if (tokenIndex < tokens.size() && matchToken(TokenType.SEMICN)) {
                Token semicnToken = consumeToken(TokenType.SEMICN);
                SyntaxNode semicNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, semicnToken);
                stmtNode.addChild(semicNode);
                outputToken(semicnToken);
            } else {
                errorHandler.reportError(getTokenLine(), "i");
            }
        } else if (tokenIndex < tokens.size() && matchToken(TokenType.LBRACE)) {
            SyntaxNode blockNode = parseBlock();
            stmtNode.addChild(blockNode);
        } else if (tokenIndex + 1 < tokens.size() && (preMatchToken(TokenType.LBRACK) || preMatchToken(TokenType.ASSIGN))) {
            SyntaxNode lValNode = parseLVal();
            stmtNode.addChild(lValNode);

            Token assignToken = consumeToken(TokenType.ASSIGN);
            SyntaxNode assignNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, assignToken);
            stmtNode.addChild(assignNode);
            outputToken(assignToken);

            SyntaxNode expNode = parseExp();
            stmtNode.addChild(expNode);

            if (tokenIndex < tokens.size() && matchToken(TokenType.SEMICN)) {
                Token semicnToken = consumeToken(TokenType.SEMICN);
                SyntaxNode semicNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, semicnToken);
                stmtNode.addChild(semicNode);
                outputToken(semicnToken);
            } else {
                errorHandler.reportError(getTokenLine(), "i");
            }
        } else {
            if (tokenIndex < tokens.size() && (matchToken(TokenType.PLUS) || matchToken(TokenType.MINU) || matchToken(TokenType.NOT) || matchToken(TokenType.IDENFR) || matchToken(TokenType.LPARENT) || matchToken(TokenType.INTCON))) {
                SyntaxNode expNode = parseExp();
                stmtNode.addChild(expNode);
            }

            if (tokenIndex < tokens.size() && matchToken(TokenType.SEMICN)) {
                Token semicnToken = consumeToken(TokenType.SEMICN);
                SyntaxNode semicNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, semicnToken);
                stmtNode.addChild(semicNode);
                outputToken(semicnToken);
            } else {
                errorHandler.reportError(getTokenLine(), "i");
            }
        }

        outputSyntaxNode(stmtNode);
        return stmtNode;
    }

    private SyntaxNode parseCond() {
        SyntaxNode condNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "Cond");

        SyntaxNode lOrExpNode = parseLOrExp();
        condNode.addChild(lOrExpNode);

        outputSyntaxNode(condNode);
        return condNode;
    }

    private SyntaxNode parseLOrExp() {
        SyntaxNode lOrExpNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "LOrExp");

        SyntaxNode lAndExpNode = parseLAndExp();
        lOrExpNode.addChild(lAndExpNode);
        outputSyntaxNode(lOrExpNode);

        while (tokenIndex < tokens.size() && matchToken(TokenType.OR)) {
            Token orToken = consumeToken(TokenType.OR);
            SyntaxNode orNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, orToken);
            lOrExpNode.addChild(orNode);
            if (orToken.getValue().equals("|")) {
                errorHandler.reportError(getTokenLine(), "a");
            }
            outputToken(orToken);

            SyntaxNode nextLAndExpNode = parseLAndExp();
            lOrExpNode.addChild(nextLAndExpNode);
            outputSyntaxNode(lOrExpNode);
        }

        return lOrExpNode;
    }

    private SyntaxNode parseLAndExp() {
        SyntaxNode lAndExpNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "LAndExp");

        SyntaxNode eqExpNode = parseEqExp();
        lAndExpNode.addChild(eqExpNode);
        outputSyntaxNode(lAndExpNode);

        while (tokenIndex < tokens.size() && matchToken(TokenType.AND)) {
            Token andToken = consumeToken(TokenType.AND);
            SyntaxNode andNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, andToken);
            lAndExpNode.addChild(andNode);
            if (andToken.getValue().equals("&")) {
                errorHandler.reportError(getTokenLine(), "a");
            }
            outputToken(andToken);

            SyntaxNode nextEaExpNode = parseEqExp();
            lAndExpNode.addChild(nextEaExpNode);
            outputSyntaxNode(lAndExpNode);
        }

        return lAndExpNode;
    }

    private SyntaxNode parseEqExp() {
        SyntaxNode eqExpNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "EqExp");

        SyntaxNode relExpNode = parseRelExp();
        eqExpNode.addChild(relExpNode);
        outputSyntaxNode(eqExpNode);

        while (tokenIndex < tokens.size() && (matchToken(TokenType.EQL) || matchToken(TokenType.NEQ))) {
            if (matchToken(TokenType.EQL)) {
                Token eqlToken = consumeToken(TokenType.EQL);
                SyntaxNode eqlNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, eqlToken);
                eqExpNode.addChild(eqlNode);
                outputToken(eqlToken);
            } else {
                Token neqToken = consumeToken(TokenType.NEQ);
                SyntaxNode neqNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, neqToken);
                eqExpNode.addChild(neqNode);
                outputToken(neqToken);
            }

            SyntaxNode nextRelExpNode = parseRelExp();
            eqExpNode.addChild(nextRelExpNode);
            outputSyntaxNode(eqExpNode);
        }

        return eqExpNode;
    }

    private SyntaxNode parseRelExp() {
        SyntaxNode relExpNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "RelExp");

        SyntaxNode addExpNode = parseAddExp();
        relExpNode.addChild(addExpNode);
        outputSyntaxNode(relExpNode);

        while (tokenIndex < tokens.size() && (matchToken(TokenType.LSS) || matchToken(TokenType.GRE) || matchToken(TokenType.LEQ) || matchToken(TokenType.GEQ))) {
            if (matchToken(TokenType.LSS)) {
                Token lssToken = consumeToken(TokenType.LSS);
                SyntaxNode lssNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, lssToken);
                relExpNode.addChild(lssNode);
                outputToken(lssToken);
            } else if (matchToken(TokenType.GRE)) {
                Token greToken = consumeToken(TokenType.GRE);
                SyntaxNode greNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, greToken);
                relExpNode.addChild(greNode);
                outputToken(greToken);
            } else if (matchToken(TokenType.LEQ)) {
                Token leqToken = consumeToken(TokenType.LEQ);
                SyntaxNode leqNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, leqToken);
                relExpNode.addChild(leqNode);
                outputToken(leqToken);
            } else {
                Token geqToken = consumeToken(TokenType.GEQ);
                SyntaxNode geqNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, geqToken);
                relExpNode.addChild(geqNode);
                outputToken(geqToken);
            }

            SyntaxNode nextAddExpNode = parseAddExp();
            relExpNode.addChild(nextAddExpNode);
            outputSyntaxNode(relExpNode);
        }

        return relExpNode;
    }

    private SyntaxNode parseForStmt() {
        SyntaxNode forStmtNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "ForStmt");

        SyntaxNode lValNode = parseLVal();
        forStmtNode.addChild(lValNode);

        Token assignToken = consumeToken(TokenType.ASSIGN);
        SyntaxNode assignNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, assignToken);
        forStmtNode.addChild(assignNode);
        outputToken(assignToken);

        SyntaxNode expNode = parseExp();
        forStmtNode.addChild(expNode);

        while (tokenIndex < tokens.size() && matchToken(TokenType.COMMA)) {
            Token commaToken = consumeToken(TokenType.COMMA);
            SyntaxNode commaNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, commaToken);
            forStmtNode.addChild(commaNode);
            outputToken(commaToken);

            SyntaxNode nextLValNode = parseLVal();
            forStmtNode.addChild(nextLValNode);

            Token nextAssignToken = consumeToken(TokenType.ASSIGN);
            SyntaxNode nextAssignNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, nextAssignToken);
            forStmtNode.addChild(nextAssignNode);
            outputToken(nextAssignToken);

            SyntaxNode nextExpNode = parseExp();
            forStmtNode.addChild(nextExpNode);
        }

        outputSyntaxNode(forStmtNode);
        return forStmtNode;
    }

    private SyntaxNode parseMainFuncDef() {
        SyntaxNode mainFuncDefNode = new SyntaxNode(SyntaxNode.NodeType.NON_TERMINAL, "MainFuncDef");

        Token intToken = consumeToken(TokenType.INTTK);
        SyntaxNode intNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, intToken);
        mainFuncDefNode.addChild(intNode);
        outputToken(intToken);

        Token mainToken = consumeToken(TokenType.MAINTK);
        SyntaxNode mainNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, mainToken);
        mainFuncDefNode.addChild(mainNode);
        outputToken(mainToken);

        Token lParenToken = consumeToken(TokenType.LPARENT);
        SyntaxNode lParenNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, lParenToken);
        mainFuncDefNode.addChild(lParenNode);
        outputToken(lParenToken);

        if (tokenIndex < tokens.size() && matchToken(TokenType.RPARENT)) {
            Token rParenToken = consumeToken(TokenType.RPARENT);
            SyntaxNode rParenNode = new SyntaxNode(SyntaxNode.NodeType.TERMINAL, rParenToken);
            mainFuncDefNode.addChild(rParenNode);
            outputToken(rParenToken);
        } else {
            errorHandler.reportError(getTokenLine(), "j");
        }

        SyntaxNode blockNode = parseBlock();
        mainFuncDefNode.addChild(blockNode);

        outputSyntaxNode(mainFuncDefNode);
        return mainFuncDefNode;
    }
}