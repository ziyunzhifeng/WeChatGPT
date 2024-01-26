package com.ziy.cmpt.demo.test;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class BigModelNew extends WebSocketListener {
    // 地址与鉴权信息  https://spark-api.xf-yun.com/v1.1/chat   1.5地址  domain参数为general
    // 地址与鉴权信息  https://spark-api.xf-yun.com/v2.1/chat   2.0地址  domain参数为generalv2
    public static final String hostUrl = "https://spark-api.xf-yun.com/v3.1/chat";
    public static final String appid = "";
    public static final String apiSecret = "";
    public static final String apiKey = "";
    public static Map<String, List<RoleContent>> talkerHistory = new HashMap<>(); // 对话历史存储集合

    public static String totalAnswer = ""; // 大模型的答案汇总

    // 环境治理的重要性  环保  人口老龄化  我爱我的祖国
    public static String NewQuestion = "";

    public static final Gson gson = new Gson();

    // 个性化参数
    private String userId;
    private Boolean wsCloseFlag;

    private WebSocket webSocket;

    private static Boolean totalFlag = true; // 控制提示用户是否输入

    private IMessageCallback messageCallback;
    // 构造函数
    public BigModelNew() {
    }

    public interface IMessageCallback {
        void onMessage(String msg);
    }

    public void sendMsg(String msg) {
        try {
            String authUrl = getAuthUrl(hostUrl, apiKey, apiSecret);
            OkHttpClient client = new OkHttpClient.Builder().build();
            String url = authUrl.toString().replace("http://", "ws://").replace("https://", "wss://");
            Request request = new Request.Builder().url(url).build();
            webSocket = client.newWebSocket(request, this);
            sendWebSocket(msg);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private void sendWebSocket(String msg) {
        try {
            JSONObject requestJson = new JSONObject();

            JSONObject header = new JSONObject();  // header参数
            header.put("app_id", appid);
            header.put("uid", userId);

            JSONObject parameter = new JSONObject(); // parameter参数
            JSONObject chat = new JSONObject();
            chat.put("domain", "generalv3");
            chat.put("temperature", 0.5);
            chat.put("max_tokens", 4096);
            parameter.put("chat", chat);

            JSONObject payload = new JSONObject(); // payload参数
            JSONObject message = new JSONObject();
            JSONArray text = new JSONArray();
            List<RoleContent> historyList = getHistoryList(userId);
            // 历史问题获取
            if (historyList.size() > 0) {
                for (RoleContent tempRoleContent : historyList) {
                    text.add(JSON.toJSON(tempRoleContent));
                }
            }

            // 最新问题
            RoleContent roleContent = new RoleContent();
            roleContent.role = "user";
            roleContent.content = msg;
            text.add(JSON.toJSON(roleContent));
            historyList.add(roleContent);

            message.put("text", text);
            payload.put("message", message);


            requestJson.put("header", header);
            requestJson.put("parameter", parameter);
            requestJson.put("payload", payload);
            // System.err.println(requestJson); // 可以打印看每次的传参明细
            webSocket.send(requestJson.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 主函数
    public void init(String talkerId, IMessageCallback callback) throws Exception {

        this.userId = talkerId;
        this.messageCallback = callback;
        if (totalFlag) {
            totalFlag = false;
        }
    }

    public static boolean canAddHistory(String talkerId) {  // 由于历史记录最大上线1.2W左右，需要判断是能能加入历史
        int history_length = 0;
        List<RoleContent> historyList = getHistoryList(talkerId);
        for (RoleContent temp : historyList) {
            history_length = history_length + temp.content.length();
        }
        if (history_length > 1000) {
            historyList.remove(0);
            historyList.remove(1);
            historyList.remove(2);
            historyList.remove(3);
            historyList.remove(4);
            return false;
        } else {
            return true;
        }
    }

    private static List<RoleContent> getHistoryList(String talkerId) {
        if (talkerHistory.containsKey(talkerId)) {
            return talkerHistory.get(talkerId);
        } else {
            List<RoleContent> historyList = new ArrayList<>(); // 对话历史存储集合
            talkerHistory.put(talkerId, historyList);
            return talkerHistory.get(talkerId);
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
//        Log.e("SparkChat", userId + "用来区分那个用户的结果" + text);
        JsonParse myJsonParse = gson.fromJson(text, JsonParse.class);
        if (myJsonParse.header.code != 0) {
            Log.e("SparkChat","发生错误，错误码为：" + myJsonParse.header.code);
            Log.e("SparkChat","本次请求的sid为：" + myJsonParse.header.sid);
            webSocket.close(1000, "");
        }
        Log.d("SparkChat","*************************************************************************************");
        List<Text> textList = myJsonParse.payload.choices.text;
        for (Text temp : textList) {
            Log.d("SparkChatx", temp.content);
            totalAnswer=totalAnswer+temp.content;
        }
        Log.d("SparkChat","*************************************************************************************");
        if (myJsonParse.header.status == 2) {
            // 可以关闭连接，释放资源
            List<RoleContent> historyList = getHistoryList(userId);
            if(canAddHistory(userId)){
                RoleContent roleContent=new RoleContent();
                roleContent.setRole("assistant");
                roleContent.setContent(totalAnswer);
                historyList.add(roleContent);
            }else{
                historyList.remove(0);
                RoleContent roleContent=new RoleContent();
                roleContent.setRole("assistant");
                roleContent.setContent(totalAnswer);
                historyList.add(roleContent);
            }
            totalFlag = true;
            webSocket.close(1000, null);
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        try {
            if (null != response) {
                int code = response.code();
                Log.e("SparkChat", "onFailure code:" + code);
                Log.e("SparkChat", "onFailure body:" + response.body().string());
                if (101 != code) {
                    Log.e("SparkChat", "connection failed");
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        super.onClosed(webSocket, code, reason);
        if (null != messageCallback) {
            messageCallback.onMessage(totalAnswer);
            totalAnswer = "";
        }
    }

    // 鉴权方法
    public static String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        // 时间
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        // 拼接
        String preStr = "host: " + url.getHost() + "\n" +
                "date: " + date + "\n" +
                "GET " + url.getPath() + " HTTP/1.1";
        // System.err.println(preStr);
        // SHA256加密
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        // Base64加密
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        // System.err.println(sha);
        // 拼接
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        // 拼接地址
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath())).newBuilder().//
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8))).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();

        // System.err.println(httpUrl.toString());
        return httpUrl.toString();
    }

        //返回的json结果拆解
    class JsonParse {
        Header header;
        Payload payload;
    }

    class Header {
        int code;
        int status;
        String sid;
    }

    class Payload {
        Choices choices;
    }

    class Choices {
        List<Text> text;
    }

    class Text {
        String role;
        String content;
    }

    static class RoleContent {
        String role;
        String content;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

}

