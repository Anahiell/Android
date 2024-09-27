package itstep.learning.spu221;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Stack;

import itstep.learning.spu221.serv.Calculator;
import itstep.learning.spu221.R;


public class CalcActivity extends AppCompatActivity {

    private final int maxDigits = 11;
    private Calculator calculator;
    private TextView tvHistory;
    private TextView tvResult;
    private String zeroSign;
    private String dotSign;
    private String minusSign;
    private String plusSign;
    private String dividSign;
    private String multiSign;
    private String percentSign;
    private boolean needClearResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calc);
        calculator = new Calculator();
        tvHistory = findViewById(R.id.calc_tv_history);
        tvResult = findViewById(R.id.calc_tv_result);
        zeroSign = getString(R.string.calc_btn_digit_0);
        minusSign = getString(R.string.calc_btn_minus);
        dotSign = getString(R.string.calc_btn_dot);
        plusSign = getString( R.string.calc_btn_plus);
        dividSign = getString(R.string.calc_btn_division);
        multiSign = getString(R.string.calc_btn_multipli);
        percentSign = getString(R.string.calc_btn_percent);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.calc_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.button_clear).setOnClickListener(this::clearClick);
        findViewById(R.id.button_dot).setOnClickListener(this::dotClick);
        findViewById(R.id.button_sogn_toggle).setOnClickListener(this::pmClick);
        findViewById(R.id.button_delete).setOnClickListener(this::backspaceClick);
        findViewById(R.id.button_squer).setOnClickListener(this::squareClick);
        findViewById(R.id.button_sqrt).setOnClickListener(this::sqrtClick);
        findViewById(R.id.button_plus).setOnClickListener(this::operatorClick);
        findViewById(R.id.button_minus).setOnClickListener(this::operatorClick);
        findViewById(R.id.button_multiply).setOnClickListener(this::operatorClick);
        findViewById(R.id.button_divide).setOnClickListener(this::operatorClick);
        findViewById(R.id.button_percent).setOnClickListener(this::operatorClick);
        findViewById(R.id.button_equals).setOnClickListener(this::equalClick);


        for (int i = 0; i < 10; i++) {
            findViewById(
                    getResources()
                            .getIdentifier(
                                    "button_" + i,
                                    "id",
                                    getPackageName()
                            )
            ).setOnClickListener(this::digitClick);
        }
        if (savedInstanceState == null) {
            this.clearClick(null);
            needClearResult = false;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {/*for saving before rebuilding*/
        super.onSaveInstanceState(outState);
        //outStated - Map, который сохраняет разные типы данных за принципом ключ, значение
        outState.putCharSequence("savedResult", tvResult.getText());
        outState.putBoolean("needClearResult", needClearResult);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //savedInstanceState - посылание на outSate
        tvResult.setText(savedInstanceState.getCharSequence("savedResult"));
        needClearResult = savedInstanceState.getBoolean("needClearResult");

    }

    private void showResult(double x) {
        if (x >= 1e11 || x <= -1e11) {
            /*перенести к ресурсам*/
            tvResult.setText("Overflow");
            return;
        }
        if (tvResult.equals("Overflow"))   /*исправить*/ {
            return;
        }
        String res = x == (int) x
                ? String.valueOf((int) x)
                : String.valueOf(x);
        res = res.replace("0", zeroSign)
                .replace("-", minusSign)
                .replace(",", dotSign);
        int limit = maxDigits;
        if (res.startsWith(minusSign)) {
            limit += 1;
        }
        if (res.contains(dotSign)) {
            limit += 1;
        }
        if (res.length() > limit) {
            res = res.substring(0, limit);
        }
        tvResult.setText(res);
    }

    private void squareClick(View view) {
        String res = tvResult.getText().toString();
        res = res.replace(zeroSign, "0")
                .replace(minusSign, "-")
                .replace(dotSign, ".");

        double x = Double.parseDouble(res);
        needClearResult = true;
        showResult(x * x);
    }

    private void sqrtClick(View view) {
        String res = tvResult.getText().toString();
        res = res.replace(zeroSign, "0")
                .replace(minusSign, "-")
                .replace(dotSign, ".");

        double x = Double.parseDouble(res);
        if (x < 0) {
            Toast.makeText(this, "Cannot be sqrt '-'", Toast.LENGTH_SHORT).show();
        } else {
            needClearResult = true;
            showResult(Math.sqrt(x));
        }
    }

    private void operatorClick(View view) {
        String res = tvResult.getText().toString();
        res = res.replace(zeroSign, "0")
                .replace(minusSign, "-")
                .replace(dotSign, ".");
        String sign = "";
        if (view.getId() == R.id.button_plus) {
            sign = "+";
            if (res.endsWith("+")) {
                Toast.makeText(this, R.string.calc_msg_two_sign, Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (view.getId() == R.id.button_minus) {
            sign = "-";
            if (res.endsWith("-")) {
                Toast.makeText(this, R.string.calc_msg_two_sign, Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (view.getId() == R.id.button_multiply) {
            sign = "x";
            if (res.endsWith("x")) {
                Toast.makeText(this, R.string.calc_msg_two_sign, Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (view.getId() == R.id.button_divide) {
            sign = "/";
            if (res.endsWith("/")) {
                Toast.makeText(this, R.string.calc_msg_two_sign, Toast.LENGTH_SHORT).show();
                return;
            }
        }else if (view.getId() == R.id.button_percent) {
            sign = "%";
            if (res.endsWith("%")) {
                Toast.makeText(this, R.string.calc_msg_two_sign, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (res.isEmpty()) {
            res = "0" + sign;
        } else {
            if (res.endsWith("+") || res.endsWith("-") || res.endsWith("/") || res.endsWith("x")|| res.endsWith("%")) {
                res = res.substring(0, res.length() - 1) + sign;
            } else {
                res = res + sign;
            }
        }

        tvResult.setText(res);
    }




    private void clearClick(View view) {
        tvHistory.setText("");
        tvResult.setText(zeroSign);
    }

    /*
     * при смене конфигурации устройства происходит пересборка активности,
     * из за чего создается перезапуск и исчезают данные*/
    private void digitClick(View view) {
        String res = needClearResult ? "" : tvResult.getText().toString();
        needClearResult = false;
        if (digitLength(res) >= maxDigits) {
            Toast.makeText(this, R.string.calc_msg_max_digits, Toast.LENGTH_SHORT).show();
            return;
        }
        if (zeroSign.equals(res)) {
            res = "";
        }
        res += ((Button) view).getText();
        tvResult.setText(res);
    }

    private int digitLength(String input) {
        int ret = 0;
        ret = input.length();
        if (input.startsWith(minusSign)) {
            ret -= 1;
        }
        if (input.contains(dotSign)||
                input.contains(minusSign)||
                input.contains(plusSign)||
                input.contains(percentSign)||
                input.contains(multiSign)||
                input.contains(dividSign)) {
            ret -= 1;
        }
        return ret;
        //длинна результата в цифрах без знака минус и точки

    }
    private void equalClick(View view) {
        String res = tvResult.getText().toString();
        String history = res + "=";
        res = res.replace(",", ".");

        if (res.isEmpty()) {
            Toast.makeText(this, "Please enter an expression", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double result = evaluate(res);
            showResult(result);
            tvHistory.setText(history+result);

        } catch (Exception e) {
            Toast.makeText(this, "Error in expression: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private double evaluate(String expression) throws Exception {

        expression = expression.replace(zeroSign, "0")
                .replace(minusSign, "-")
                .replace(plusSign, "+")
                .replace(dotSign, ".")
                .replace(multiSign, "*")
                .replace(dividSign, "/");


        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();


        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);


            if (Character.isDigit(ch) || ch == '.') {
                StringBuilder num = new StringBuilder();
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    num.append(expression.charAt(i++));
                }
                i--;
                numbers.push(Double.parseDouble(num.toString()));
            }

            else if ("+-*/".indexOf(ch) >= 0) {

                while (!operators.isEmpty() && precedence(ch) <= precedence(operators.peek())) {
                    double b = numbers.pop();
                    double a = numbers.pop();
                    char op = operators.pop();
                    numbers.push(applyOperation(a, b, op));
                }
                operators.push(ch);
            }
        }


        while (!operators.isEmpty()) {
            double b = numbers.pop();
            double a = numbers.pop();
            char op = operators.pop();
            numbers.push(applyOperation(a, b, op));
        }


        return numbers.pop();
    }


    private int precedence(char operator) {
        switch (operator) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
            default:
                return 0;
        }
    }

    private double applyOperation(double a, double b, char operator) throws Exception {
        switch (operator) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) throw new Exception("Cannot divide by zero");
                return a / b;
        }
        return 0;
    }


    private void dotClick(View view) {
        String res = tvResult.getText().toString();
        if (res.contains(dotSign)) {
            if (res.endsWith(dotSign)) {
                res = res.substring(0, res.length() - 1);
                tvResult.setText(res);
            } else {
                Toast.makeText(this, R.string.calc_msg_two_dots, Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            res += dotSign;
        }
        tvResult.setText(res);
    }

    private void pmClick(View view) {
        String res = tvResult.getText().toString();
        if (zeroSign.equals(res)) {
            Toast.makeText(this, R.string.calc_msg_minus_zero, Toast.LENGTH_SHORT).show();
            return;
        }
        if (res.startsWith(minusSign)) {
            res = res.substring(minusSign.length());

        } else {
            res = minusSign + res;
        }
        tvResult.setText(res);
    }

    private void backspaceClick(View view) {
        String res = tvResult.getText().toString();
        int len = res.length();
        if (res.length() > 1) {
            res = res.substring(0, len - 1);
            if (minusSign.equals(res)) {
                res = zeroSign;
            }
        } else {
            res = zeroSign;
        }
        tvResult.setText(res);
    }

}
