import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 * Процессор постфиксных токенов - составляет таблицу переменных.
 */
public class PolizProcessor {

    /* Таблица переменных */
    private HashMap<String, Integer> varTable;
    /* Входной список постфиксных токенов */
    private List<Token> postfixToken;
    /* Номер текущего токена в списке */
    private int currentTokenNumber;
    /* Стек токенов - хранит числа и токены со значениями
     * - операнды арифметических операций */
    private Stack<Token> tokenStack;
    /* Текущий токен */
    private Token current;

    private static int iteration = 0;

    /* Инициализация полей в конструкторе */
    public PolizProcessor(List<Token> postfixToken, HashMap<String, Integer> vT) {
        varTable = vT;
        this.postfixToken = postfixToken;
        currentTokenNumber = 0;
        tokenStack = new Stack<Token>();
    }

    /* Установить значение входного списка токенов */
    public void setPostfixToken(List<Token> postfix) {
        this.postfixToken = postfix;
    }

    /* Запуск процессора */
    public void go() {
        currentTokenNumber = 0;
        /* Повторять до конца файла */
        while (currentTokenNumber < postfixToken.size()) {
            step();
            /* Перейти к следующему токену из списка токенов */
            currentTokenNumber++;
        }
    }

    /* Обработка текущего токена */
    private void step() {
            /* Текущий токен из списка токенов */
        current = postfixToken.get(currentTokenNumber);
        /* Установить последовательность действий для поступившего токена */
        switch (current.getName()) {
                /* Токен со значением */
            case "VAR_NAME":
                    /* Добавить в стек токенов текущий токен для
                     * дальнейших операций */
                tokenStack.push(current);
                break;
                /* Число */
            case "DIGIT":
                    /* Добавить в стек токенов */
                tokenStack.push(current);
                break;
                /* Присвоить */
            case "ASSIGN_OP":
                    /* Присвоить значение последнего токена из
                     стека токенов предпоследнему */
                Token what = tokenStack.pop();
                Token where = tokenStack.pop();
                if(!what.getName().equals("ITER")) {
                    System.out.println("t" + iteration + " = " + what.getValue());
                }
                System.out.println(where.getValue() + " = " + "t" + iteration);
                break;
                /* Сложение */
            case "ASSIGN_ADD":
                iteration++;
                Token secAdd = tokenStack.pop();
                Token firAdd = tokenStack.pop();
                System.out.println("t" + iteration + " = " + firAdd.getValue() + " + " + secAdd.getValue());
                tokenStack.push(new Token("ITER", "t" + iteration));
                break;
                /* Вычитание */
            case "ASSIGN_SUB":
                iteration++;
                Token secSub = tokenStack.pop();
                Token firSub = tokenStack.pop();
                System.out.println("t" + iteration + " = " + firSub.getValue() + " - " + secSub.getValue());
                tokenStack.push(new Token("ITER", "t" + iteration));
                break;
                /* Произведение */
            case "ASSIGN_MULT":
                iteration++;
                Token secMult = tokenStack.pop();
                Token firMult = tokenStack.pop();
                System.out.println("t" + iteration + " = " + firMult.getValue() + " * " + secMult.getValue());
                tokenStack.push(new Token("ITER", "t" + iteration));
                break;
                /* Деление */
            case "ASSIGN_DIV":
                iteration++;
                Token secDiv = tokenStack.pop();
                Token firDiv = tokenStack.pop();
                System.out.println("t" + iteration + " = " + firDiv.getValue() + " / " + secDiv.getValue());
                tokenStack.push(new Token("ITER", "t" + iteration));
                break;
        }
    }
    /* Установить таблицу переменных: полиз процессору требуется обновление таблицы */
    public void setVarTable(HashMap<String, Integer> varTable) {
        this.varTable = varTable;
    }

}
