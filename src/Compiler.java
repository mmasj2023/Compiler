import lexer.Lexer;
import lexer.Token;
import error.ErrorHandler;
import parser.Parser;
import parser.SyntaxNode;

import java.io.*;
import java.util.List;

public class Compiler {
    public static void main(String[] args) {
        ErrorHandler errorHandler = new ErrorHandler();
        Lexer lexer = new Lexer(errorHandler);
        try (BufferedReader reader = new BufferedReader(new FileReader("testfile.txt"));
            BufferedWriter parserWriter = new BufferedWriter(new FileWriter("parser.txt"))) {

            List<Token> tokens = lexer.tokenize(reader);

            Parser parser = new Parser(tokens, errorHandler, parserWriter);
            SyntaxNode syntaxTree = parser.parse();

            // 输出错误信息
            if (errorHandler.hasErrors()) {
                errorHandler.writeErrorsToFile("error.txt");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}