package error;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
}