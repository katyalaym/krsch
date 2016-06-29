import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class Parser {
    /* Приоритеты операций */
    private final int BRACKET_PR = 6;
    private final int MULT_DIV_PR = 5;
    private final int ADD_SUB_PR = 4;
    private final int LOGIC_PR = 3;
    private final int ASSIGN_OP_PR = 1;
    /* Список инфиксных токенов (входная последовательность парсера) */
    private List<Token> tokens;
    /* Список постфиксных токенов (выходная последовательность парсера)*/
    private ArrayList<Token> postfixTokenList;
    /* Таблица переменных */
    private HashMap<String, Integer> varTable;
    private Token currentToken; //текущий токен
    /* Номер текущего токена. Инкрементируется при соответствии ожидаемого токена
     * в проверяемом выражении текущему и декрементируетсяв обратном случае
     * - таким образом производится шаг назад по списку токена до посленего
     * верного токена. */
    private int currentTokenNumber = 0;
    private int closeBracketCounter;    //счётчик скобок
    private Stack<Token> operators;     //стек с операторами
    /* Полиз процессор */
    private PolizProcessor poliz;

    /* Инициализация списков инфиксиных и постфиксных токенов,
     * а также стека операторов в конструкторе Парсера. Также
     * инициализируются таблицы переменных и структур и создаётся
     * экземпляр полиз процессора. */
    public Parser(List<Token> tokens, HashMap<String, Integer> vT) {
        this.tokens = tokens;
        this.varTable = vT;
        postfixTokenList = new ArrayList<Token>();
        operators = new Stack<Token>();
        poliz = new PolizProcessor(postfixTokenList, vT);
    }

    /* Обработка входной последовательности */
    public void lang() throws Exception {
        boolean exist = false;
        /* Повторять до тех пор, пока не будет найдена ошибка
         * в выражении или пока не будет достигнут конец списка токенов */
        while (currentTokenNumber < tokens.size() && expr()) {
            exist = true;
        }
        /* Отобразить сообщение об ошибке при чтении списка токенов */
        if (!exist) {
            throw new Exception("error in lang " + currentTokenNumber);
        }
        /* Если все токены были обработаны и если выходной список токенов не пуст,
         * то отобразить выходной список токенов, вычислить последнее оставшееся
         * в списке токенов для процессора выражение и очистить список токенов. */
        /* Отобразить сообщение об успешном прочтении списка токенов */
        if (currentTokenNumber == tokens.size()) {
            if (!postfixTokenList.isEmpty()) {
                System.out.println(postfixTokenList);
                setPoliz();
                poliz.go();
                postfixTokenList.clear();
            }
            System.out.println("Success");
        }
    }

    public boolean expr() throws Exception {
        if (declare() || assign()) {
            /* При корректности одного из выражения, если список
             * выходных токенов не пуст, то провести его обработку
             * полиз процессором */
            if (!postfixTokenList.isEmpty()) {
                setPoliz();
                poliz.go();
                postfixTokenList.clear();
            }
            return true;
        } else {
            throw new Exception("declare or assign or while expected, but "
                    + currentToken + "found. " + currentTokenNumber);
        }
    }

    private boolean declare() throws Exception {
        if (varKw()) {
            /* Добавить текущий токен в выходной список
             с соответствующим приоритетом  */
            if (ws()) {
                if (!varName()) {
                    currentTokenNumber--;
                    return false;
                }
            } else {
                currentTokenNumber--;
                return false;
            }
        } else {
            currentTokenNumber--;
            return false;
        }
        if (!sm()) {
            currentTokenNumber--;
            return false;
        } else {
            /* Если выражение объявления написано правильно, то
             * добавить в таблицу переменных токен перед ; */
            addNewToVarTable(tokens.get(currentTokenNumber - 2));
            return true;
        }
    }

    private void addNewToVarTable(Token declaredToken) {
        /* Добавить в таблицу переменных новую переменную
         * с именем токена и нулевым значением */
        varTable.put(declaredToken.getValue(), 0);
    }

    /* Проверка корректности присваивания переменной значения */
    private boolean assign() throws Exception {
        /* Выдаёт ошибку при несоответствии текущего токена ожидаемому.
         * Выражение проверяется на корректность применения скобок. */
        if (varName()) {
            postfixTokenList.add(currentToken);
            /* Пропустить пробел */
            ignoreWhitespace();
            if (assignOp()) {
                /* Если токен является оператором присваивания, то
                 * добваить его в стек операторов с соответствующим приоритетом. */
                currentToken.setPriority(ASSIGN_OP_PR);
                operators.push(currentToken);
                ignoreWhitespace();
                if (!stmt()) {
                    throw new Exception("stmt  expected, but "
                            + currentToken + "found. " + currentTokenNumber);
                }
            } else {
                postfixTokenList.clear();
                currentTokenNumber -= 2;
                return false;
            }

        } else {
            currentTokenNumber--;
        }

        if (closeBracketCounter == 0) {
            if (sm()) {
                /* Когда выражение обработано, добавить все оставшиеся
                 операторы в выходную последовательность */
                while (!operators.empty()) {
                    postfixTokenList.add(operators.pop());
                }
                return true;
            } else {
                currentTokenNumber--;
                return false;
            }
            /* В случае неверного количества скобок
             отобразить сообщение об ошибке */
        } else {
            throw new Exception("illegal number of brackets");
        }
    }

    /*  при присваивании переменной нового значения */
    private boolean stmt() throws Exception {
        /* Обнуление счётчика скобок при каждом новом выражении. */
        closeBracketCounter = 0;
        /* Проверка на скобки - выражение может начинаться с
         * бесконечного количества скобок. */
        ignoreWhitespace();
        checkBrackets();
        ignoreWhitespace();
        /* Независимо от количества скобок, выражение должно начинаться
         * с имени другой переменной или числа (или должно продолжаться ими
         * после оператора) */
        if (stmtUnit()) {
            /* Если токен является операндом, то добавить его в конец
             * выходной последовательности */
            ignoreWhitespace();
            checkBrackets();
            ignoreWhitespace();
            /* Логическая переменная продолжения цикла.
             * Цикл прекращается, если оператор из списка токенов не был
             * найден или если за ним не следовало имя другой переменной или число. */
            boolean goOn = true;
            while (goOn) {
                if (assignAdd()) {
                    /* Если токен является оператором, поместить его в
                     * стек операторов, но перед этим вытолкнуть любой
                     * из операторов, уже находящихся в стеке, если он
                     * имеет больший или равный приоритет, и добавить его
                     * в результирующий список */
                    currentToken.setPriority(ADD_SUB_PR);
                    checkPriority(ADD_SUB_PR);
                    operators.push(currentToken);
                    ignoreWhitespace();
                    checkBrackets();
                    ignoreWhitespace();
                    /* Если следующий токен не операнд, то отобразить
                     сообщение об ошибке. Иначе добавить токен в выходную
                     последовательность */
                    if (!stmtUnit()) {
                        throw new Exception("stmt_unit  expected, but "
                                + currentToken + "found. " + currentTokenNumber);
                    }
                } else {
                    currentTokenNumber--;
                    /* Аналогично предыдущему блоку */
                    if (assignSub()) {
                        currentToken.setPriority(ADD_SUB_PR);
                        checkPriority(ADD_SUB_PR);
                        operators.push(currentToken);
                        /* Пропустить пробел */
                        ignoreWhitespace();
                        /* Пропустить пробел */
                        ignoreWhitespace();
                        checkBrackets();
                        /* Пропустить пробел */
                        ignoreWhitespace();
                        if (!stmtUnit()) {
                            throw new Exception("stmt_unit  expected, but "
                                    + currentToken + "found. " + currentTokenNumber);
                        }
                    } else {
                        currentTokenNumber--;
                        if (assignMult()) {
                            currentToken.setPriority(MULT_DIV_PR);
                            checkPriority(MULT_DIV_PR);
                            operators.push(currentToken);
                            /* Пропустить пробел */
                            ignoreWhitespace();
                            checkBrackets();
                            /* Пропустить пробел */
                            ignoreWhitespace();
                            if (!stmtUnit()) {
                                throw new Exception("stmt_unit  expected, but "
                                        + currentToken + "found. " + currentTokenNumber);
                            }
                        } else {
                            currentTokenNumber--;
                            if (assignDiv()) {
                                currentToken.setPriority(MULT_DIV_PR);
                                checkPriority(MULT_DIV_PR);
                                operators.push(currentToken);
                                /* Пропустить пробел */
                                ignoreWhitespace();
                                checkBrackets();
                                /* Пропустить пробел */
                                ignoreWhitespace();
                                if (!stmtUnit()) {
                                    throw new Exception("stmt_unit  expected, but "
                                            + currentToken + "found. " + currentTokenNumber);
                                }
                            } else {
                                /* В случае, если не было найдено ни одного
                                 * оператора, то закончить цикл */
                                currentTokenNumber--;
                                goOn = false;
                            }
                        }
                    }
                }
                /* Пропустить пробел */
                ignoreWhitespace();
                /* Проверить скобки */
                checkBrackets();
                /* Пропустить пробел */
                ignoreWhitespace();
            }
            return true;
        } else {
            /* Если выражение началось не с имени переменной или числа, то
             * отобразить сообщение об ошибке. */
            throw new Exception("stmt_unit  expected, but "
                    + currentToken + "found. " + currentTokenNumber);
        }
    }

    /* Проверка на наличие имени переменной или числа
     * (используется при присваивании переменной нового значения) */
    private boolean stmtUnit() throws Exception {
        boolean badTry = true;
        if (!digit()) {
            currentTokenNumber--;
            badTry = false;
            if (varName() && !tokens.get(currentTokenNumber).getName().equals("DOT") && !badTry) {
                badTry = true;
                postfixTokenList.add(tokens.get(currentTokenNumber - 1));
            } else {
                currentTokenNumber--;
                badTry = false;
            }
        } else {
            postfixTokenList.add(tokens.get(currentTokenNumber - 1));
        }
        return badTry;
    }
    /* Проверка бесконечного количества скобок.
     * Переменная closeBracketCounter хранит количество введённых
     * левых скобок (эту величину можно воспринимать и как количество
     * ожидаемых правых скобок). Переменная инкрементируется при
     * поступлении левой скобки и декрементируется при поступлении правой.*/
    private void checkBrackets() {
        while (bracketOpen()) {
            closeBracketCounter++;
            /* Если токен является левой скобкой, то
             * добавить его в стек операторов */
            currentToken.setPriority(BRACKET_PR);
            operators.push(currentToken);
            ignoreWhitespace();
        }
        currentTokenNumber--;
        while (bracketClose()) {
            closeBracketCounter--;
            /* Если токен является правой скобкой, то выталкивать
             * элементы из стека операторов, пока не будет найдена
             * соответствующая левая скобка. Каждый оператор добавлять
             * в конец списка выходной последовательности */
            while (!operators.peek().getName().equals("BRACKET_OPEN")) {
                postfixTokenList.add(operators.pop());
            }
            /* Удаление левой скобки из стека операторов */
            operators.pop();
            /* Пропустить пробел */
            ignoreWhitespace();
        }
        currentTokenNumber--;
    }

    /* Проверка приоритетов при добавлении нового оператора */
    private void checkPriority(int priority) {
        /* Повторять, до первой скобки, пока стек операторов не пуст и приоритет
         * предыдущего оператора больше или равен приоритету текущего */
        while (!operators.empty() && operators.peek().getPriority() != BRACKET_PR &&
                (operators.peek().getPriority() >= priority)) {
            /* Если приоритет последнего оператора из стека больше или равен
             * приоритету текущего оператора, то извлечь его из стека и поместить
              * в выходную последовательность */
            if (operators.peek().getPriority() >= priority) {
                postfixTokenList.add(operators.pop());
            }
        }
    }

    /* Обновить для экземпляра полиз процессора список токенов,
     *  и таблицу переменных  */
    private void setPoliz() {
        poliz.setPostfixToken(postfixTokenList);
        poliz.setVarTable(varTable);
    }

    /* Ниже приведены методы для проверки соответствия текущего
     * токена следующему из списка токенов. */
    private boolean sm() {
        match();
        return currentToken.getName().equals("SM");
    }

    private boolean varKw() {
        match();
        return currentToken.getName().equals("VAR_KW");
    }

    private boolean assignOp() {
        match();
        return currentToken.getName().equals("ASSIGN_OP");
    }

    private boolean assignAdd() {
        match();
        return currentToken.getName().equals("ASSIGN_ADD");
    }

    private boolean assignSub() {
        match();
        return currentToken.getName().equals("ASSIGN_SUB");
    }

    private boolean assignMult() {
        match();
        return currentToken.getName().equals("ASSIGN_MULT");
    }

    private boolean assignDiv() {
        match();
        return currentToken.getName().equals("ASSIGN_DIV");
    }

    private boolean digit() {
        match();
        return currentToken.getName().equals("DIGIT");
    }

    private boolean varName() {
        match();
        return currentToken.getName().equals("VAR_NAME");
    }

    private boolean ws() {
        match();
        return currentToken.getName().equals("WS");
    }

    private boolean bracketOpen() {
        match();
        return currentToken.getName().equals("BRACKET_OPEN");
    }

    private boolean bracketClose() {
        match();
        return currentToken.getName().equals("BRACKET_CLOSE");
    }

    private boolean whileKw() {
        match();
        return currentToken.getName().equals("WHILE_KW");
    }

    private boolean curlyBracketOpen() {
        match();
        return currentToken.getName().equals("C_BRACKET_OPEN");
    }

    private boolean curlyBracketClose() {
        match();
        return currentToken.getName().equals("C_BRACKET_CLOSE");
    }

    private boolean equals() {
        match();
        return currentToken.getName().equals("EQUALS");
    }

    private boolean more() {
        match();
        return currentToken.getName().equals("MORE");
    }

    private boolean less() {
        match();
        return currentToken.getName().equals("LESS");
    }

    private boolean moreEquals() {
        match();
        return currentToken.getName().equals("MORE_EQUALS");
    }

    private boolean lessEquals() {
        match();
        return currentToken.getName().equals("LESS_EQUALS");
    }

    private boolean notEquals() {
        match();
        return currentToken.getName().equals("NOT_EQUALS");
    }

    private boolean dot() {
        match();
        return currentToken.getName().equals("DOT");
    }

    /* Пропустить пробел */
    private void ignoreWhitespace() {
        do {
            match();
        } while (currentToken.getName().equals("WS"));
        currentTokenNumber--;
    }

    /* Для проведения проверки на соответствие присваиваем
     * текущему токену значение следующего токена из списка токенов */
    private boolean match() {
        /* Присвоение допустимо только в случае, если ещё не достигнут
         * конец списка токенов */
        if (currentTokenNumber < tokens.size()) {
            currentToken = tokens.get(currentTokenNumber);
            currentTokenNumber++;
            return true;
        } else {
            return false;
        }
    }

    /* Возвращает выходную последовательность парсера -
     * список постфиксных токенов */
    public List<Token> getPostfixToken() {
        return postfixTokenList;
    }
}
