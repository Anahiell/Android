package itstep.learning.spu221;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private final List<int[][]> previousStates = new ArrayList<>();
    private final List<Long> previousScores = new ArrayList<>();

    private long score;
    private final int N = 4;
    private final String bestScoreFileName ="best_score.dat";
    private final Random random = new Random();
    private Animation fadeInAnimation;
    private final int[][] cells = new int[N][N];
    private final TextView[][] tvCells = new TextView[N][N];
    private TextView tvScore;
    private TextView tvBestScore;
    Animation mergeAnimation;
    private long bestScore;


    @SuppressLint("DiscouragedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        GridLayout gameField = findViewById(R.id.game_board);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnNewGame = findViewById(R.id.btn_new);

        btnNewGame.setOnClickListener(v -> {
            resetGame();
        });

        mergeAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_2);
         tvScore= findViewById(R.id.score);
         tvBestScore = findViewById(R.id.game_Best_score);
        findViewById(R.id.game_btn_undo).setOnClickListener(this::undoClick);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tvCells[i][j] = findViewById(
                        getResources()
                                .getIdentifier(
                                        "game_view_" + i + "x" + j,
                                        "id",
                                        getPackageName()
                                )
                );
            }
        }

        findViewById(R.id.game_btn_undo).setOnClickListener(v -> undo());
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeInAnimation.reset();
        startGame();

        findViewById(R.id.game_board).setOnTouchListener(
                new SwipeTouchListener(this) {
                    @Override
                    public void onSwipeLeft() {
                        if (moveLeft()) {
                            spawnCell();
                        } else {
                            Toast.makeText(GameActivity.this, "NO STEP", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSwipeRight() {
                        if (moveRight()) {
                            spawnCell();
                        } else {
                            Toast.makeText(GameActivity.this, "NO STEP", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSwipeBottom() {
                        if (moveDown()) {
                            spawnCell();
                        }
                        else {
                            Toast.makeText(GameActivity.this, "NO STEP", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSwipeTop() {
                        if (moveUp())
                            spawnCell();
                        else {
                            Toast.makeText(GameActivity.this, "NO STEP", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void undoClick(View view)
    {//диалоги
        new AlertDialog.Builder(this,
                com.google.android.material.R.style.Theme_MaterialComponents_DayNight_Dialog_FixedSize)
                .setTitle("Dialog example")
                .setMessage("Cheking language")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setPositiveButton("Close",(dialog, which)->{})
                .setNegativeButton("Exit",(dialog, which)->this.finish())
                .setNeutralButton("New game",(dialog, which)->this.startGame())
                .show();

    }
    private void saveState() {
        int[][] stateCopy = new int[N][N];
        for (int i = 0; i < N; i++) {
            System.arraycopy(cells[i], 0, stateCopy[i], 0, N);
        }
        previousStates.add(stateCopy);
        previousScores.add(score);
    }

    private void saveMaxScore()
    {
        /*работа с файлами
        * В Андроид файлы делится на две категории
        * - "приватные" файлы - файлы с репозитория приложения,
        * которую удаляют при удаление прниложения.*/
        /*- обшие файлы - с других репозиториев "фото/галерея", "загрузки" тощо
        * С такими файцлпми необходимор разрешение*/

        try(FileOutputStream fos = openFileOutput(bestScoreFileName,
                Context.MODE_PRIVATE);
            DataOutputStream writer = new DataOutputStream(fos)) {
            writer.writeLong(bestScore);
            writer.flush();

        }
        catch (IOException ex)
        {
            Log.e("saveMaxScore", "fos"+ex.getMessage());
        }

    }
    private void loadMaxScore()
    {
        try(FileInputStream fis = openFileInput(bestScoreFileName);
            DataInputStream reader = new DataInputStream(fis)
        ){
            bestScore = reader.readLong();
        }
        catch (IOException ex)
        {
            Log.e("loadMaxScore","fis: "+ex.getMessage());
        }
    }

    private void showField() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tvCells[i][j].setText(String.valueOf(cells[i][j]));
                updateCellBackground(tvCells[i][j], cells[i][j]);

                adjustTextSize(tvCells[i][j], cells[i][j]);
            }
        }
    }

    private void adjustTextSize(TextView cellView, int value) {
        float textSize;
        if (value == 0) {
            cellView.setText("");
            textSize = 30;
        } else if (value < 100) {
            textSize = 30;
        } else if (value < 1000) {
            textSize = 26;
        } else if (value < 10000) {
            textSize = 22;
        } else {
            textSize = 18;
        }
        cellView.setTextSize(textSize);
    }

    private void updateCellBackground(TextView cellView, int value) {
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(20);

        int color;
        switch (value) {
            case 0:
                color = getResources().getColor(R.color.game_table_row_views);
                break;
            case 2:
                color = getResources().getColor(R.color.game_bg_2);
                break;
            case 4:
                color = getResources().getColor(R.color.game_bg_4);
                break;
            case 8:
                color = getResources().getColor(R.color.game_bg_8);
                break;
            case 16:
                color = getResources().getColor(R.color.game_bg_16);
                break;
            case 32:
                color = getResources().getColor(R.color.game_bg_32);
                break;
            case 64:
                color = getResources().getColor(R.color.game_bg_64);
                break;
            case 128:
                color = getResources().getColor(R.color.game_bg_128);
                break;
            case 256:
                color = getResources().getColor(R.color.game_bg_256);
                break;
            case 512:
                color = getResources().getColor(R.color.game_bg_512);
                break;
            case 1024:
                color = getResources().getColor(R.color.game_bg_1024);
                break;
            case 2048:
                color = getResources().getColor(R.color.game_bg_2048);
                break;

            default:
                color = getResources().getColor(R.color.game_bg_4096);
                break;
        }

        background.setColor(color);
        cellView.setBackground(background);
    }

    private boolean spawnCell() {
        List<Coord> coords = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (cells[i][j] == 0) {
                    coords.add(new Coord(i, j));
                }
            }
        }
        if (coords.isEmpty()) {
            return false;
        }
        int randomIndex = random.nextInt(coords.size());
        Coord randomCoord = coords.get(randomIndex);

        cells[randomCoord.i][randomCoord.j] = random.nextInt(10) == 0 ? 4 : 2;


        tvCells[randomCoord.i][randomCoord.j].startAnimation(fadeInAnimation);
        showField();

        return true;
    }
private void startGame()
{
    addScore(-score);
    loadMaxScore();
    showMaxScore();
    previousStates.clear();  // Очистка стека состояний
    previousScores.clear();
    spawnCell();
}
    private void undo() {
        if (!previousStates.isEmpty()) {
            int[][] lastState = previousStates.remove(previousStates.size() - 1);
            for (int i = 0; i < N; i++) {
                System.arraycopy(lastState[i], 0, cells[i], 0, N);
            }
            score = previousScores.remove(previousScores.size() - 1);
            showField();
            tvScore.setText(String.valueOf(score));
        } else {
            Toast.makeText(this, "Нет действий для отмены", Toast.LENGTH_SHORT).show();
        }
    }

private boolean moveLeft() {
    saveState();
    boolean wasMove = false;

    for (int i = 0; i < N; i++) {
        int pos1 = 0; // позиция для перемещения
        boolean merged = false; // флаг, что произошло слияние

        for (int j = 0; j < N; j++) {
            if (cells[i][j] != 0) {
                if (pos1 != j) {
                    cells[i][pos1] = cells[i][j];
                    cells[i][j] = 0;
                    wasMove = true;
                }


                if (pos1 > 0 && cells[i][pos1] == cells[i][pos1 - 1] && !merged) {
                    cells[i][pos1 - 1] += cells[i][pos1];
                    cells[i][pos1] = 0;
                    tvCells[i][pos1 - 1].startAnimation(mergeAnimation);
                    addScore(cells[i][pos1 - 1]);
                    merged = true;
                    wasMove = true;
                } else {
                    pos1++;
                    merged = false;
                }
            }
        }
    }

    return wasMove;
}

    private boolean moveRight() {
        saveState();
        boolean wasMove = false;

        for (int i = 0; i < N; i++) {
            int pos1 = N - 1; // позиция для перемещения
            boolean merged = false; // флаг, что произошло слияние

            for (int j = N - 1; j >= 0; j--) {
                if (cells[i][j] != 0) {
                    if (pos1 != j) {
                        cells[i][pos1] = cells[i][j];
                        cells[i][j] = 0;
                        wasMove = true;
                    }


                    if (pos1 < N - 1 && cells[i][pos1] == cells[i][pos1 + 1] && !merged) {
                        cells[i][pos1 + 1] += cells[i][pos1];
                        cells[i][pos1] = 0;
                        tvCells[i][pos1 + 1].startAnimation(mergeAnimation);
                        addScore(cells[i][pos1 + 1]);
                        merged = true;
                        wasMove = true;
                    } else {
                        pos1--;
                        merged = false;
                    }
                }
            }
        }

        return wasMove;
    }

    private boolean moveUp() {
        saveState();
        boolean wasMove = false;

        for (int j = 0; j < N; j++) {
            int pos1 = 0; // позиция для перемещения
            boolean merged = false; // флаг, что произошло слияние

            for (int i = 0; i < N; i++) {
                if (cells[i][j] != 0) {
                    if (pos1 != i) {
                        cells[pos1][j] = cells[i][j];
                        cells[i][j] = 0;
                        wasMove = true;
                    }


                    if (pos1 > 0 && cells[pos1][j] == cells[pos1 - 1][j] && !merged) {
                        cells[pos1 - 1][j] += cells[pos1][j];
                        cells[pos1][j] = 0;
                        tvCells[pos1 - 1][j].startAnimation(mergeAnimation);
                        addScore(cells[pos1 - 1][j]);
                        merged = true;
                        wasMove = true;
                    } else {
                        pos1++;
                        merged = false;
                    }
                }
            }
        }

        return wasMove;
    }

    private boolean moveDown() {
        saveState();
        boolean wasMove = false;

        for (int j = 0; j < N; j++) {
            int pos1 = N - 1; // позиция для перемещения
            boolean merged = false; // флаг, что произошло слияние

            for (int i = N - 1; i >= 0; i--) {
                if (cells[i][j] != 0) {
                    if (pos1 != i) {
                        cells[pos1][j] = cells[i][j];
                        cells[i][j] = 0;
                        wasMove = true;
                    }

                    // чек слияние
                    if (pos1 < N - 1 && cells[pos1][j] == cells[pos1 + 1][j] && !merged) {
                        cells[pos1 + 1][j] += cells[pos1][j];
                        cells[pos1][j] = 0;
                        tvCells[pos1 + 1][j].startAnimation(mergeAnimation); // Анимация
                        addScore(cells[pos1 + 1][j]);
                        merged = true;
                        wasMove = true;
                    } else {
                        pos1--; //  к следующей позиции
                        merged = false; //  следующее слияние
                    }
                }
            }
        }

        return wasMove;
    }

    private void showMaxScore()
    {
        tvBestScore.setText(
                getString(R.string.game_best, bestScore));
    }
    private void addScore(long value)
    {
        score+=value;
        tvScore.setText(getString(R.string.game_score, score));
        if(score>bestScore){
            bestScore =score;
            saveMaxScore();
            showMaxScore();
        }
    }
    class Coord {
        int i;
        int j;

        public Coord(int i, int j) {
            this.i = i;
            this.j = j;
        }
    }

    private void resetGame() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                cells[i][j] = 0;
            }
        }

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tvCells[i][j].setText(cells[i][j] == 0 ? "" : String.valueOf(cells[i][j]));
            }
        }

        score = 0;
        tvScore.setText(String.valueOf(score));

        startGame();
    }

}