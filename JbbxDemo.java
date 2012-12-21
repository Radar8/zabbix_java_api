import jbbx.ZabbixAPI;
import org.json.simple.JSONArray;
import java.util.HashMap;

public class JbbxDemo {

    public static void main(String[] args) throws Exception {

        ZabbixAPI api = new ZabbixAPI("https://zabbix-server");
        api.debugEnable();
      // if(api.sessionId("ID_here")){
        if(api.login(api_user, api_passwd)){

            System.out.println("Login, session-id:"+ api.sessionId());
            System.out.println("------ Hosts:");

            HashMap params = new HashMap();
            params.put("limit", 10);
            params.put("output", "extend");
            JSONArray hosts = (JSONArray) api.query("host.get", params);
            if(!hosts.isEmpty()) {
                Iterator it = hosts.iterator();
                while (it.hasNext()){
                    HashMap user = (HashMap) it.next();
                    System.out.println("Host:" + user.get("host") + ", hostid:" + user.get("hostid"));
                }
            }
        }
        else{
            System.out.println("Auth error:"+ api.lastError());
        }
    }
}
