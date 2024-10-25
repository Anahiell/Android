package itstep.learning.spu221;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    private final static String chatUrl = "https://chat.momentfor.fun/";
    private LinearLayout chatContainer;
    private ScrollView chatScroller;
    private EditText etAuthor;
    private EditText etMessage;
    private final List<ChatMessage> messages = new ArrayList<>();
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        chatContainer = findViewById(R.id.chat_ll_container);

        chatContainer.post(() -> new Thread(this::loadChat).start());
        chatScroller = findViewById(R.id.chat_sv_container);
        findViewById(R.id.chat_btn_send).setOnClickListener(this::onSendMessageClick);
        etAuthor = findViewById(R.id.chat_et_nik);
        loadNickname();
        etMessage = findViewById(R.id.chat_et_message);
        handler.post(this::reloadChat);

    }

    private void reloadChat()
    {
        new Thread(this::loadChat).start();
        handler.postDelayed(this::reloadChat,3000);
    }
    private void onSendMessageClick(View view) {
        String author = etAuthor.getText().toString();
        if (author.isBlank()) {
            Toast.makeText(this, "Enter nickname", Toast.LENGTH_SHORT).show();
            return;
        }
        String message = etMessage.getText().toString();
        if (message.isBlank()) {
            Toast.makeText(this, "Enter the Message", Toast.LENGTH_SHORT).show();
            return;
        }
        ChatMessage chatMessage = new ChatMessage(author,message,true);
        new Thread(() -> sendMessage(chatMessage)).start();
        etMessage.setText("");
        lockAuthorNickname(author);
    }

    private void sendMessage(ChatMessage chatMessage) {
/*
* для тогго что бы добавить новое сообщение, необзодимо отправить запрос методом POST
* с данными формами, которые иметируют отправления формы с параметрами author и  message
* - method POST
* - заглавие формы Content-TYpe: application/x-www-form-urlencoded
* тоесть с телом
* author=The%20Author&msg=Hello%20All
* где данные пришли URL-кодирование (например %20 - пробел)
* */

        try {
            URL url = new URL(chatUrl);
            //открываем соединение
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //настройка соединения
            connection.setDoInput(true); //с соединения можно читать(ожидаеются ответы)
            connection.setDoOutput(true); //запмчт - добавления тела
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded"); //заглавия
            connection.setRequestProperty("Accept","application/json");
            connection.setChunkedStreamingMode(0); //не делить на части (chunk)
            //заполняем тело
            OutputStream bodyStream = connection.getOutputStream();
            String body = String.format(
                    "author=%s&msg=%s",
                    URLEncoder.encode(chatMessage.getAuthor(), StandardCharsets.UTF_8.name()),
                    URLEncoder.encode(chatMessage.getText(),StandardCharsets.UTF_8.name())
            );
            bodyStream.write(body.getBytes(StandardCharsets.UTF_8));
            bodyStream.flush(); //отправление - выаизивание ьуффера в канал
            bodyStream.close(); //освобождаем ресурс
            //получаем ответ
            int statusCode = connection.getResponseCode();
            if(statusCode ==201){
                //запускаем обновления сообшений
                loadChat();
            }
            else{
                //проучаем тело ответа, но с ерор канала
                InputStream errorStream = connection.getErrorStream();
                String errorMessage = readAsString(errorStream);
                runOnUiThread(()->
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show());
                errorStream.close();
            }
            //разрываем соединение
            connection.disconnect();
        }
        catch (Exception ex){
            Log.e("sendMessage",ex.getMessage());
        }
    }
    private void lockAuthorNickname(String author)
    {
        etAuthor.setEnabled(false);
        try(FileOutputStream fos = openFileOutput("nickname.txt",MODE_PRIVATE)){
            fos.write(author.getBytes());
        }catch (IOException e)
        {
            Log.e("SaveNickname",e.getMessage());
        }
    }
    private void loadNickname(){
        try(FileInputStream fis = openFileInput("nickname.txt")){
            byte[] bytes = new byte[fis.available()];
            fis.read(bytes);
            String author = new String(bytes);
            etAuthor.setText(author);
            etAuthor.setEnabled(false);
        }catch (IOException ex){
            Log.e("LoadNickname", ex.getMessage());
        }
    }

    private void showChat(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            //chatContainer.removeAllViews();
            boolean wasNewMessage = false;
            for (int i = 0; i < jsonArray.length(); i++) {
               ChatMessage chatMessage= new ChatMessage(jsonArray.getJSONObject(i));
               if(this.messages.stream().noneMatch(m->m.getId().equals(chatMessage.getId()))) {
                   //новое сообщение
                   messages.add(chatMessage);
                   wasNewMessage = true;
               }
            }
            if(wasNewMessage){
                messages.sort((m1,m2)->m1.getMoment().compareTo(m2.getMoment()));
               // chatContainer.removeAllViews();
                for(ChatMessage chatMessage:messages){
                    if(chatMessage.getView()==null){
                        chatMessage.setView(messageView(chatMessage));
                        chatContainer.addView(chatMessage.getView());
                    }

                }
                chatScroller.post(()->chatScroller.fullScroll(View.FOCUS_DOWN));
            }
        } catch (Exception ex) {
            Log.e("showChat", ex.getMessage());
        }
    }

    private View messageView(ChatMessage chatMessage) {
        LinearLayout box = new LinearLayout(ChatActivity.this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(15,15,15,15);
        chatMessage.isMine = chatMessage.getAuthor().equals(etAuthor.getText().toString());
        box.setBackground(AppCompatResources.getDrawable(this,chatMessage.isMine()? R.drawable.chat_msg_mine : R.drawable.chat_msg));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(5,7,7,20);
        layoutParams.gravity = chatMessage.isMine() ? Gravity.END : Gravity.START;
        box.setLayoutParams(layoutParams);

        TextView tvAuthor =new TextView(this);
        tvAuthor.setText(chatMessage.isMine() ? "ME" : chatMessage.getAuthor());
        box.addView(tvAuthor);

        TextView tvText = new TextView(this);
        tvText.setText(chatMessage.getText());
        box.addView(tvText);
        TextView tvMoment = new TextView(this);
        tvMoment.setText(chatMessage.getFormattedMoment());
        box.addView(tvMoment);

        //сохраняем связь с обьектом-сообщение через тег
        box.setTag(chatMessage);
        //
        box.setOnClickListener(this::messageClick);

        //animation
        box.setAlpha(0f);
        box.animate().alpha(1f).setDuration(300).start();
        return box;
    }

    private void messageClick(View view){
        ChatMessage chatMessage = (ChatMessage) view.getTag();
        Toast.makeText(this, chatMessage.getText(), Toast.LENGTH_SHORT).show();
    }

    private void loadChat() {
        try {
            URL url = new URL(chatUrl);
            InputStream urlStream = url.openStream();
            String jsonString = readAsString(urlStream);

            runOnUiThread(() -> showChat(jsonString));

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

    private String readAsString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = inputStream.read(buffer)) > 0) {
            byteBuilder.write(buffer, 0, len);
        }
        return byteBuilder.toString();
    }

    static class ChatMessage {
        private String id;
        private String author;
        private String text;
        private Date moment;
        private View view;
        private boolean isMine;
        private int avatarResId;

        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;
        }

        private final static SimpleDateFormat momentFormat =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

        public ChatMessage(String author, String text,boolean isMine) {
            this.setAuthor(author);
            this.setText(text);
            this.isMine = isMine;
            this.avatarResId = isMine ? R.mipmap.avatar_lizard : getRandomAvatarResId();

        }

        public ChatMessage(JSONObject jsonObject) throws Exception {
            this.setId(jsonObject.getString("id"));
            this.setAuthor(jsonObject.getString("author"));
            this.setText(jsonObject.getString("text"));
            this.setMoment(momentFormat.parse(jsonObject.getString("moment")));
        }
        public boolean isMine() {
            return isMine;
        }
        public int getAvatarResId() {
            return avatarResId;
        }
        private int getRandomAvatarResId() {
            int[] avatars = {
                    R.mipmap.avatr3_lem,
                    R.mipmap.mem
            };
            int randomIndex = (int) (Math.random() * avatars.length);
            return avatars[randomIndex];
        }
        public String getFormattedMoment() {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ROOT).format(moment);
        }
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public Date getMoment() {
            return moment;
        }

        public void setMoment(Date moment) {
            this.moment = moment;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}