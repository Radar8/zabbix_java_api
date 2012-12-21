package jbbx;

/**
 * Zabbix API for Java (Version API 1.8-2.0)
 * jbbx 0.1 by Radar8
 */

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.UserDataHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import static java.lang.Math.*;

public class ZabbixAPI {

    protected String  server_url;
    protected long    request_id;
    protected String  host_url;
    protected String  auth_hash;
    protected String  last_error;
    protected boolean hasLogin=false;
    protected boolean debug_mode=false;

    public ZabbixAPI(String _host_url){
        host_url = _host_url;
        server_url = host_url +"/api_jsonrpc.php";
    }

    /**
     * Авторизация на сервере API
     * @param username
     * @param passwd
     * @return
     * @throws Exception
     */
    public boolean login(String username, String passwd) throws Exception {

        HashMap params = new HashMap();
        params.put("user", username);
        params.put("password", passwd);
        String result = (String) callRequest("user.authenticate", params);

        if(result != null && result.length()==32){
            auth_hash = result;
            hasLogin = true;
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Запрос данных
     * @param method
     * @param params
     * @return
     * @throws Exception
     */
    public Object query(String method, HashMap params) throws Exception {
        return callRequest(method, params);
    }
    public Object query(String method) throws Exception {
        return callRequest(method, new HashMap());
    }

    /**
     * Подготовка параметров, обработка результата
     * @param method
     * @param params
     * @return
     * @throws Exception
     */
    private Object callRequest(String method, HashMap params) throws Exception {

        if(!hasLogin && !method.equals("user.authenticate")) {
            throw new Exception("Authenticated required!");
        }

        request_id = (long)(random()*100000+1);

        JSONObject jsonParams=new JSONObject();
        jsonParams.put("auth", auth_hash);
        jsonParams.put("method", method);
        jsonParams.put("id", request_id);
        jsonParams.put("params", params);
        jsonParams.put("jsonrpc","2.0");

        String jsonResult = sendRequest(jsonParams);
        JSONObject obj;
        JSONParser parser=new JSONParser();
        try{
            obj = (JSONObject) parser.parse(jsonResult);
            Long answer_id = (Long) obj.get("id");
            Object result = obj.get("result");
            if(answer_id != null && answer_id.longValue() == request_id
                && result != null) {

                return result;
            }
            else {
                JSONObject error = (JSONObject) obj.get("error");
                last_error = error.get("message")+"; "+ error.get("data");
                if(debug_mode){
                    System.out.println("Error: "+ last_error);
                }
                throw new Exception(last_error);
            }
        }
        catch(ParseException pe){
            if(debug_mode) {
                System.out.println(pe);
            }
            throw new Exception(pe);
        }
    }

    /**
     * Отправка запроса на сервер
     * @param data
     * @return
     * @throws Exception
     */
    private String sendRequest(JSONObject data) throws Exception {

        URL u = new URL(this.server_url);
        if(debug_mode){
            System.out.println("Send request:");
            System.out.println(data.toJSONString());
        }
        URLConnection uconn;
        StringBuffer answer = new StringBuffer();
        try{

            uconn = u.openConnection();
            uconn.setRequestProperty("Content-type", "application/json-rpc");
            uconn.setRequestProperty("User-Agent", "Jbbx 0.1");
            uconn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(uconn.getOutputStream());

            writer.write(data.toJSONString());
            writer.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(uconn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                answer.append(line);
            }
            writer.close();
            reader.close();

            int i = uconn.getContentLength();
            if(debug_mode){
                System.out.println("Answer ("+ i+"):");
                System.out.println(answer);
            }
        }
        catch(Exception e) {
            System.out.println("Exception: "+ e.getMessage());
        }
        return answer.toString();
    }

    /**
     * Установить идентификатор сессии.
     * Используется как альтернатива авторизации, когда известен идентификатор.
     * @param session_id
     * @return
     * @throws Exception
     */
    public boolean sessionId(String session_id) throws Exception {
        auth_hash = session_id;
        hasLogin = true;
        return true;
    }

    public String sessionId(){
        return auth_hash;
    }

    /**
     * Get Host URL
     * @return
     */
    public String hostUrl(){
        return host_url;
    }

    /**
     * Получить сообщение последней ошибки
     * @return
     */
    public String lastError(){
        return last_error;
    }

    /**
     * Включить режим отладки
     */
    public void debugEnable(){
        debug_mode=true;
    }

    /**
     * Выключить режим отладки
     */
    public void debugDisable(){
        debug_mode=false;
    }
}
