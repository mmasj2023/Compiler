package error;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ErrorHandler {
    private final List<String> errorMessages = new ArrayList<>();

    public void reportError(int lineNumber, String errorCode) {
        errorMessages.add(lineNumber + " " + errorCode);
    }

    public void writeErrorsToFile(String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (String error : errorMessages) {
            writer.write(error);
            writer.newLine();
        }
        writer.close();
    }

    public boolean hasErrors() {
        return !errorMessages.isEmpty();
    }

    public void sortErrorsByLineNumber() {
        errorMessages.sort(new ErrorLineComparator());
    }

    private static class ErrorLineComparator implements Comparator<String> {
        @Override
        public int compare(String error1, String error2) {
            // 提取行号并比较
            int lineNumber1 = extractLineNumber(error1);
            int lineNumber2 = extractLineNumber(error2);
            return Integer.compare(lineNumber1, lineNumber2);
        }

        /**
         * 从错误信息中提取行号
         * 错误信息格式为: "行号 错误代码"
         */
        private int extractLineNumber(String error) {
            try {
                // 提取第一个空格前的数字
                String lineNumberStr = error.split(" ")[0];
                return Integer.parseInt(lineNumberStr);
            } catch (Exception e) {
                // 如果解析失败，返回一个很大的值，让这些错误排在后面
                return Integer.MAX_VALUE;
            }
        }
    }
}
