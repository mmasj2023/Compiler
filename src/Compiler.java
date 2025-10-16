import lexer.Lexer;
import lexer.Token;
import error.ErrorHandler;
import java.io.*;
import java.util.List;

public class Compiler {
    public static void main(String[] args) {
        try {
            ErrorHandler errorHandler = new ErrorHandler();
            Lexer lexer = new Lexer(errorHandler);

            // 读取输入文件
            BufferedReader reader = new BufferedReader(new FileReader("testfile.txt"));
            List<Token> tokens = lexer.tokenize(reader);
            reader.close();

            // 检查词法分析阶段是否有错误
            boolean hasLexicalErrors = errorHandler.hasErrors();

            // 输出词法分析结果
            if (!hasLexicalErrors) {
                BufferedWriter lexerWriter = new BufferedWriter(new FileWriter("lexer.txt"));
                for (Token token : tokens) {
                    lexerWriter.write(token.toString());
                    lexerWriter.newLine();
                }
                lexerWriter.close();
            }

            // 输出错误信息
            if (hasLexicalErrors) {
                errorHandler.writeErrorsToFile("error.txt");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}