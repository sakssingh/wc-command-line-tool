import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class UnixToolCcwc {
    public static void main(String[] args) throws IOException {
        if (isPipedInput()) {
            processPipedInput(args);
        } else {
            processInteractiveInput(args);
        }
    }
    private static boolean isPipedInput() {
        try {
            if (System.console() != null) {
                return false;
            }
            return System.in.available() <= 0;
        } catch (IOException e) {
            return false;
        }
    }

    private static void processPipedInput(String[] args) {
        String option = args.length == 1 ? args[0] : "";
        printResults(processCommand(option, System.in), "");
    }

    private static void processInteractiveInput(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: ccwc [option] [filename]");
            return;
        }

        String option = args.length == 2 ? args[0] : "";
        String fileName = args[args.length - 1];
        InputStream inputStream;

        File file = new File(fileName);

        if (!file.exists() || !file.isFile()) {
            System.err.println("File not found: " + fileName);
            return;
        }

        try {
            inputStream = new FileInputStream(file);
        } catch (IOException e) {
            System.err.println("Error opening file: " + fileName);
            return;
        }

        long[] results = processCommand(option, inputStream);
        printResults(results, file.getName());
    }

    private static void printResults(long[] result, String fileName) {
        boolean hasError = false;
        StringBuilder countsToBePrinted = new StringBuilder();

        for (int i= 0; i < result.length; i++) {
            if (result[i] == -1) {
                hasError = true;
                break;
            }
            if (i != 0) {
                countsToBePrinted.append(" ");
            }
            countsToBePrinted.append(result[i]);
        }

        if (hasError) {
            System.err.println("Cannot read counts from input stream");
        } else {
            String textToBePrinted = "";
            if (fileName != null) {
                textToBePrinted += " " + fileName;
            }
            System.out.println(countsToBePrinted + textToBePrinted);
        }
    }

    private static long[] processCommand(String option, InputStream inputStream) {
        long[] result;

        if (option.isEmpty()) {
            result = getResultAll(inputStream);
        } else {
            result = getResultFromOption(option, inputStream);
        }
        return result;
    }

    private static long[] getResultFromOption(String option, InputStream inputStream) {
        long[] count = new long[1];

        switch (option) {
            case "-c":
                count[0] = getCountByte(inputStream);
                break;
            case "-l":
                count[0] = getCountLine(inputStream);
                break;
            case "-w":
                count[0] = getCountWord(inputStream);
                break;
            case "-m":
                count[0] = getCountCharacter(inputStream);
                break;
            default:
                count[0] = -1;
                break;
        }

        return count;
    }

    private static long[] getResultAll(InputStream inputStream) {
        Charset charset = StandardCharsets.UTF_8;
        long[] counts = { 0, 0, 0};

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            int c;
            boolean inWord = false;
            boolean isNewLine = true;

            while ((c = reader.read()) != -1) {
                byte[] bytes = Character.toString((char) c).getBytes(charset);
                counts[2] += bytes.length;

                if (Character.isWhitespace(c)) {
                    if (inWord) {
                        inWord = false;
                    }
                    if (c == '\n') {
                        counts[0]++;
                        isNewLine = true;
                    }
                } else {
                    if (!inWord) {
                        inWord = true;
                        counts[1]++;
                    }
                    isNewLine = false;
                }
            }

            if (!isNewLine) {
                counts[0]++;
            }
        } catch (IOException e) {
            counts[0] = -1;
        }

        return counts;
    }

    private static long getCountByte(InputStream inputStream) {
        try {
            long byteCount = 0;
            while (inputStream.read() != -1) {
                byteCount++;
            }
            return byteCount;
        } catch (IOException e) {
            return -1;
        }
    }

    private static long getCountLine(InputStream inputStream) {
        Charset charset = StandardCharsets.UTF_8;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            long lineCount = 0;

            while(reader.readLine() != null) {
                lineCount++;
            }

            return lineCount;
        } catch (IOException e) {
            return -1;
        }
    }

    private static long getCountWord(InputStream inputStream) {
        Charset charset = StandardCharsets.UTF_8;
        long wordCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            String currentLine = reader.readLine();
            while (currentLine != null) {
                currentLine = currentLine.trim();
                if (!currentLine.isEmpty()) {
                    String[] words = currentLine.split("\\s+");
                    wordCount += words.length;
                }
                currentLine = reader.readLine();
            }
            return wordCount;
        } catch (IOException e) {
            return -1;
        }
    }

    private static long getCountCharacter(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            long charCount = 0;
            while ((reader.read()) != -1) {
                charCount++;
            }

            return charCount;
        } catch (IOException e) {
            return -1;
        }
    }
}
