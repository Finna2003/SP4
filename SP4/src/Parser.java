import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    static String[] keywords = {
            "and","and_eq", "auto", "bitand","bitor","bool","break","case","catch","char",
            "class","compl","const", "const_cast","continue","default", "delete","do","double",
            "dynamic_cast", "else","enum", "explicit", "export", "extern", "false","float","for",
            "friend","goto","if", "inline","int","long", "mutable","namespace", "new", "not", "not_eq",
            "operator", "or", "or_eq","private","protected","public", "register", "reinterpret_cast",
            "return","short", "signed","sizeof","static", "static_cast", "struct", "switch", "template",
            "this","throw", "true","try", "typedef", "typeid", "typename", "union", "unsigned", "using",
            "virtual","void","volatile", "wchar_t","while","xor", "xor_eq"
    };

    static String[] operators = {
            "\\+", "-", "\\*", "/", "%", "\\*\\*", "//",
            "\\=\\=", "!\\=", "<>", ">", "<", ">\\=", "<\\=",
            "\\+\\=", "-\\=", "\\*\\=", "/\\=", "%\\=", "\\*\\*=", "//\\=",
            "&", "\\|", "\\^", "~", "<<", ">>", "\\=",
            "\\band\\b", "\\bor\\b", "\\bnot\\b"
    };

    enum Type {
        NUMBER,
        STRING_CONST,
        RESERVED_WORD,
        OPERATOR,
        PUNCTUATION,
        IDENTIFIER,
        UNDEFINED,

        PREPROCESSOR_DIRECTIVE
    }

    static class Node {
        public String lexeme;
        public Integer match;
        public Type type;

        public Node(String lexeme, Integer match, Type type) {
            this.lexeme = lexeme;
            this.match = match;
            this.type = type;
        }
    }
    static public void writeLexeme(Node lexemes, FileOutputStream writer)
            throws IOException
    {
        String line = lexemes.lexeme + " - " + lexemes.type.name() + '\n';
        if (line.isEmpty() || line.isBlank())
            return;

        writer.write(line.getBytes());
    }

    static String removeComment(String line) {
        String regex = "[//]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            String newLine = line.substring(0, matcher.start());
            return newLine;
        }
        return line;
    }

    static String addReservedWord(Vector<Node> lexemes, String line) {
        for (String keyword : keywords) {
            String regex = "(\\b" + keyword + "\\b)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(line);

            while (matcher.find()) {
                Node obj = new Node(keyword, matcher.start(), Type.RESERVED_WORD);
                lexemes.add(obj);
            }
            matcher = pattern.matcher(line);
            if (matcher.find())
                line = matcher.replaceAll(" ".repeat(matcher.group().length()));
        }
        return line;
    }

    static String addOperator(Vector<Node> lexemes, String line) {
        for (String regex : operators) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(line);

            while (matcher.find()) {
                Node obj = new Node(regex, matcher.start(), Type.OPERATOR);
                lexemes.add(obj);
            }
            matcher = pattern.matcher(line);
            if (matcher.find())
                line = matcher.replaceAll(" ".repeat(matcher.group().length()));
        }
        return line;
    }

    static String addNumber(Vector<Node> lexemes, String line) {
        String regex = "(([-+]?([1-9][0-9]*)(\\.[0-9]+)?)|([+-]?0x[0-f]+))";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            Node obj = new Node(matcher.group(1), matcher.start(), Type.NUMBER);
            lexemes.add(obj);
        }
        matcher = pattern.matcher(line);
        if (matcher.find())
            line = matcher.replaceAll(" ".repeat(matcher.group().length()));
        return line;
    }
    static String addPreprocessorDirective(Vector<Node> lexemes, String line) {
        String regex = "([#]{1}([\\w_]+))";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            Node obj = new Node(matcher.group(1), matcher.start(), Type.PREPROCESSOR_DIRECTIVE);
            lexemes.add(obj);
        }
        matcher = pattern.matcher(line);
        if (matcher.find())
            line = matcher.replaceAll(" ".repeat(matcher.group().length()));
        return line;
    }

    static String addString(Vector<Node> lexemes, String line) {
        String regex = "(\"(.*?)\"|\'(.*?)\')";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            Node obj = new Node(matcher.group(1), matcher.start(), Type.STRING_CONST);
            lexemes.add(obj);
        }
        matcher = pattern.matcher(line);
        if (matcher.find())
            line = matcher.replaceAll(" ".repeat(matcher.group().length()));
        return line;
    }

    static String addPunctuation(Vector<Node> lexemes, String line) {
        String regex = "([(),:;\\[\\]@.{}])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            Node obj = new Node(matcher.group(1), matcher.start(), Type.PUNCTUATION);
            lexemes.add(obj);
        }
        matcher = pattern.matcher(line);
        if (matcher.find())
            line = matcher.replaceAll(" ".repeat(matcher.group().length()));
        return line;
    }

    static String addIdentifier(Vector<Node> lexemes, String line) {
        String regex = "([\\w_]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);


        while (matcher.find()) {
            Node obj = new Node(matcher.group(), matcher.start(), Type.IDENTIFIER);
            lexemes.add(obj);
        }
        matcher = pattern.matcher(line);
        if (matcher.find())
            line = matcher.replaceAll(" ".repeat(matcher.group().length()));
        return line;
    }

    static String addUndefined(Vector<Node> lexemes, String line) {
        String regex = "([\\^ ]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            Node obj = new Node(matcher.group(), matcher.start(), Type.UNDEFINED);
            lexemes.add(obj);
        }
        matcher = pattern.matcher(line);
        if (matcher.find())
            line = matcher.replaceAll(" ".repeat(matcher.group().length()));
        return line;
    }

    static void fill(Vector<Node> lexemes, String line) {
        line = line.stripLeading();
        line = line.stripTrailing();

        line = removeComment(line);

        System.out.println("\nOriginal line: " + line);
        if (line.isBlank())
            return;

        line = addPreprocessorDirective(lexemes, line);
        System.out.println("Removed preprocessor directive: " + line);
        if (line.trim().isBlank())
            return;

        line = addString(lexemes, line);
        System.out.println("Removed string: " + line);
        if (line.isBlank())
            return;

        line = addNumber(lexemes, line);
        System.out.println("Removed number: " + line);
        if (line.isBlank())
            return;

        line = addReservedWord(lexemes, line);
        System.out.println("Removed reserved_word: " + line);
        if (line.isBlank())
            return;

        line = addOperator(lexemes, line);
        System.out.println("Removed operator: "  + line);
        if (line.isBlank())
            return;

        line = addPunctuation(lexemes, line);
        System.out.println("Removed punctuation: " + line);
        if (line.isBlank())
            return;

        line = addIdentifier(lexemes, line);
        System.out.println("Removed indentifiers: " + line);
        if (line.trim().isBlank())
            return;

        line = addUndefined(lexemes, line);
        if (line.isBlank() == false)
            System.out.println("End: " + line.length());
    }

    static void sort(Vector<Node> container) {
        for (int i = 0; i < container.size(); i++)
            for (int j = 0; j < container.size() - i - 1; j++)
                if (container.get(j).match > container.get(j + 1).match) {
                    var temp = container.get(j);
                    container.set(j, container.get(j + 1));
                    container.set(j + 1, temp);
                }
    }

    static public void main(String[] args) {
        String inputFilename = "input.txt";
        String outputFilename = "output.txt";

        File iFile = new File(inputFilename);
        File oFile = new File(outputFilename);

        try {
            Scanner scan = new Scanner(iFile);
            FileOutputStream writer = new FileOutputStream(oFile, false);

            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                Vector<Node> lexemes = new Vector<Node>();
                fill(lexemes, line);
              //  sort(lexemes);

                for (var el : lexemes)
                    writeLexeme(el, writer);
            }
        }
        catch (IOException e) {
            System.out.print(e.getMessage());
        }
    }
}
