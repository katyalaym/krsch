import java.util.HashMap;
import java.util.List;

public class UserInterface {
    static FileHelper fileHelper = new FileHelper();

    public static void main(String[] args) throws Exception {
        //wrongInput();
        validInput();
    }

    static void wrongInput() throws Exception {
        fileHelper.testRead("wrong-test.input");
        process("wrong-test.input");
    }
   
    static void validInput() throws Exception {
        fileHelper.testRead("valid-test.input");
        process("valid-test.input");
    }

    static void process(String fileName) throws Exception {
        Lexer lexer = new Lexer();
        /* Преобразовать входной поток символов в список токенов */
        lexer.processInput(fileName);
        List<Token> tokens = lexer.getTokens();

        /* Создать таблицы переменных */
        HashMap<String, Integer> myVarTable = new HashMap<String, Integer>();
        /* Создать экземпляр парсера с имеющимся списком токенов
         *  и таблицей переменных */
        Parser parser = new Parser(tokens, myVarTable);
        /* Провести разбор списка токенов */
        parser.lang();
        /* Получить список постфиксных токенов */
        List<Token> postfixToken = parser.getPostfixToken();
        for (Token val : postfixToken) {
            System.out.print(val.getValue());
        }
        /* Отобразить таблицу переменных */
        System.out.println(myVarTable);
    }
}

