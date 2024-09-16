package itstep.learning.spu221.serv;

public class Calculator {
    private final StringBuilder history;
    private final StringBuilder currentInput;
    private String lastOperation;
    private String result;

    public Calculator() {
        history = new StringBuilder();
        currentInput = new StringBuilder();
        lastOperation = "";
        result = "";
    }

    public void append(String number) {
        currentInput.append(number);
        history.append(number);
    }

    public void setOperation(String operation) {
        if (currentInput.length() > 0 || !lastOperation.isEmpty()) {
            if (!isLastCharacterOperator()) {
                if (currentInput.length() > 0) {
                    result = currentInput.toString();
                }
                history.append(" ").append(operation).append(" ");
                lastOperation = operation;
                currentInput.setLength(0);
            }
        }
    }

    private boolean isLastCharacterOperator() {
        if (history.length() > 0) {
            char lastChar = history.charAt(history.length() - 1);
            return lastChar == '+' || lastChar == '-' || lastChar == '*' || lastChar == '/';
        }
        return false;
    }

    public void calculate() {
        if (!lastOperation.isEmpty() && currentInput.length() > 0) {
            double firstOperand = Double.parseDouble(result);
            double secondOperand = Double.parseDouble(currentInput.toString());
            switch (lastOperation) {
                case "+":
                    result = String.valueOf(firstOperand + secondOperand);
                    break;
                case "-":
                    result = String.valueOf(firstOperand - secondOperand);
                    break;
                case "*":
                    result = String.valueOf(firstOperand * secondOperand);
                    break;
                case "/":
                    if (secondOperand != 0) {
                        result = String.valueOf(firstOperand / secondOperand);
                    } else {
                        result = "Error";
                    }
                    break;
            }
            history.append(" = ").append(result);
            currentInput.setLength(0);
        }
    }

    public void clear() {
        currentInput.setLength(0);
        history.setLength(0);
        result = "";
    }

    public void deleteLast() {
        if (currentInput.length() > 0) {
            currentInput.deleteCharAt(currentInput.length() - 1);
            history.setLength(history.length() - 1);
        }
    }

    public String getResult() {
        return currentInput.length() > 0 ? currentInput.toString() : result;
    }

    public String getHistory() {
        return history.toString();
    }
}
