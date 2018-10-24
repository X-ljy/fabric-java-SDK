package javaSDK;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 夕 on 2018/10/24 14:18
 */
public class TestManagerMain {

    public static void main(String[] args) throws Exception {
        FabricManager fabricManager = MyFabricManager.getFabricManager();

        Map<String,String> map = new HashMap<String, String>();

        System.out.println("获取区块信息");
        map = fabricManager.getBlockchainInfo();
        for(String k : map.keySet()){
            System.out.println(k + " "+ map.get(k));
        }

        map = fabricManager.query("query",new String[]{"a"});
        for(String k : map.keySet()){
            System.out.println(k + " "+ map.get(k));
        }








    }
}
