package itstep.learning.spu221;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class RatesActivity extends AppCompatActivity {

    private final static String nbuUrl = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
    private LinearLayout ratesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rates);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ratesContainer = findViewById(R.id.rates_ll_container);
        ratesContainer.post(() -> new Thread(this::loadRates).start());
    }


    private void loadRates() {
        try {
            URL url = new URL(nbuUrl); //аналогично файлу обьект не делает ничего, кроме создания
            InputStream urlStream = url.openStream();
            String jsonString = readAsString(urlStream);
            /* подключение может сопровождаться следующими проблемами:
             * - android.os.NetworkOnMainThreadException - в Андроиде подключение к сети не может
             * происходить в этом потоке, в котором работает UI
             *
             * - java.lang.SecurityException: Permission denied (missing INTERNET permission?)
             * для доступа к интернету необходимо заявить о манифесте
             *
             * - Can't toast on a thread that has not called Looper.prepare()
             *  запушеные в другом потоке, кроме UI, действия не могут иметь доступ к UI,
             *   в том числе Тосты. Запуск таких действий следует делегировать UI потоку
             * */
            runOnUiThread(() -> showRates(jsonString));

            urlStream.close();
        } catch (MalformedURLException ex) {
            Log.d("loadRates", "MalformedURLException" + ex.getMessage());
        } catch (IOException ex) {
            Log.d("loadRates", "IOException" + ex.getMessage());
        } catch (android.os.NetworkOnMainThreadException ex) {
            Log.d("loadRates", "NetworkOnMainThreadException" + ex.getMessage());
        } catch (SecurityException ex) {
            Log.d("loadRates", "SecurityException" + ex.getMessage());
        }
    }

    private void showRates(String jsonString) {
        JSONArray rates;
        try {
            rates = new JSONArray(jsonString);
            for (int i = 0; i < rates.length(); i++) {
                JSONObject rate = rates.getJSONObject(i);

                // создаем новую строку таблицы
                TableRow row = new TableRow(this);

                //  TextView для страны
                TextView tvCountry = new TextView(this);
                tvCountry.setText(rate.getString("txt"));
                tvCountry.setBackground(AppCompatResources.getDrawable(this, R.drawable.game_table));
                tvCountry.setTextAppearance(this,R.style.TableRowTextStyle);
                row.addView(tvCountry);

                //  TextView для кода валюты
                TextView tvCurrency = new TextView(this);
                tvCurrency.setText(rate.getString("cc"));
                tvCurrency.setBackground(AppCompatResources.getDrawable(this,R.drawable.game_table));
                tvCurrency.setTextAppearance(this,R.style.TableRowTextStyle);
                row.addView(tvCurrency);

                //  TextView для курса
                TextView tvRate = new TextView(this);
                tvRate.setText(rate.getString("rate"));
                tvRate.setBackground(AppCompatResources.getDrawable(this,R.drawable.game_table));
                tvRate.setTextAppearance(this,R.style.TableRowTextStyle);
                row.addView(tvRate);

                //  строку в TableLayout
                ratesContainer.addView(row);
            }
        } catch (JSONException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private String readAsString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = inputStream.read(buffer)) > 0) {
            byteBuilder.write(buffer, 0, len);
        }
        return byteBuilder.toString();
    }
}
/*
 * Работа с инетом
 * Задача: получить курс валют из апи банка и вывести их
 *bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json
 *
 * програмное формирование елементов
 * - отобразить длинных скролов
 *  */