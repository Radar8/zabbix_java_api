package jbbx;

import org.json.simple.JSONArray;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Test for ZabbixAPI
 */
public class Test {

    public static void main(String[] args) throws Exception {

        if(args.length !=3) {
            System.out.println("Jbbx test (ZabbixAPI for Java)");
            System.out.println("Use:  ... <Url> <User | -u> <Passwd | -p>");
            System.exit(0);
        }

        String api_url = args[0];
        String api_user = args[1];
        String api_passwd = args[2];

        Scanner scanner;
        if(api_user.equals("-u")) {
            System.out.println("Enter User API:");
            scanner = new Scanner(System.in);
            api_user = scanner.nextLine();
        }
        if(api_passwd.equals("-p")) {
            System.out.println("Enter password API:");
            scanner = new Scanner(System.in);
            api_passwd = scanner.nextLine();
        }

        ZabbixAPI api = new ZabbixAPI(api_url);
        api.debugEnable();
//        if(api.sessionId("ID_here")){
        if(api.login(api_user, api_passwd)){

            System.out.println("Login, session-id:"+ api.sessionId());

            System.out.println("------ Hosts (direct api)---------");

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
