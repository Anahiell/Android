package itstep.learning.spu221;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import itstep.learning.spu221.serv.Calculator;

public class CalcActivity extends AppCompatActivity {

    private Calculator calculator;
    private TextView tvHistory;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calc);
        calculator = new Calculator();
        tvHistory = findViewById(R.id.calc_tv_history);
        tvResult = findViewById(R.id.calc_tv_result);
        setupButtons();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.calc_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupButtons() {
        findViewById(R.id.button_0).setOnClickListener(v -> onNumberButtonClicked("0"));
        findViewById(R.id.button_1).setOnClickListener(v -> onNumberButtonClicked("1"));
        findViewById(R.id.button_2).setOnClickListener(v -> onNumberButtonClicked("2"));
        findViewById(R.id.button_3).setOnClickListener(v -> onNumberButtonClicked("3"));
        findViewById(R.id.button_4).setOnClickListener(v -> onNumberButtonClicked("4"));
        findViewById(R.id.button_5).setOnClickListener(v -> onNumberButtonClicked("5"));
        findViewById(R.id.button_6).setOnClickListener(v -> onNumberButtonClicked("6"));
        findViewById(R.id.button_7).setOnClickListener(v -> onNumberButtonClicked("7"));
        findViewById(R.id.button_8).setOnClickListener(v -> onNumberButtonClicked("8"));
        findViewById(R.id.button_9).setOnClickListener(v -> onNumberButtonClicked("9"));

        findViewById(R.id.button_add).setOnClickListener(v -> onOperationButtonClicked("+"));
        findViewById(R.id.button_subtract).setOnClickListener(v -> onOperationButtonClicked("-"));
        findViewById(R.id.button_multiply).setOnClickListener(v -> onOperationButtonClicked("*"));
        findViewById(R.id.button_divide).setOnClickListener(v -> onOperationButtonClicked("/"));

        findViewById(R.id.button_equals).setOnClickListener(v -> onEqualsButtonClicked());
        findViewById(R.id.button_clear).setOnClickListener(v -> onClearButtonClicked());
        findViewById(R.id.button_delete).setOnClickListener(v -> onDeleteButtonClicked());
    }

    private void onNumberButtonClicked(String number) {
        calculator.append(number);
        updateDisplay();
    }

    private void onOperationButtonClicked(String operation) {
        calculator.setOperation(operation);
        updateDisplay();
    }

    private void onEqualsButtonClicked() {
        calculator.calculate();
        updateDisplay();
    }

    private void onClearButtonClicked() {
        calculator.clear();
        updateDisplay();
    }

    private void onDeleteButtonClicked() {
        calculator.deleteLast();
        updateDisplay();
    }

    private void updateDisplay() {
        tvResult.setText(calculator.getResult());
        tvHistory.setText(calculator.getHistory());
    }
}
