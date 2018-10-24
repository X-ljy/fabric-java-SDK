package javaCAsdk;

/**
 * Created by 夕 on 2018/10/22 19:07
 */
import javaCAsdk.FabricCA;
public class TestCAMain {

    static String orgName ="Org1MSP" ;
    static String mspId="Org1MSP";
    static String calocation="http://10.10.44.122:7054";
    static String adminName="admin";
    static String adminPasswd="adminpw";
    static String newUserName="xxx";
    static String newUserPasswd="xxx";
    static String affiliation="org1.department1";
    static  String caName = "ca0.org1.blockchain.com";

    public static void main(String[] args) throws Exception {


        System.out.println(FabricCA.registerUser(orgName,mspId,calocation,caName,adminName,adminPasswd,newUserName,newUserPasswd,affiliation));

        System.out.println(FabricCA.EnrollUser(orgName,mspId,caName,calocation,newUserName,newUserPasswd));

        System.out.println(FabricCA.getCertAndKeyStore(orgName,mspId,caName,calocation,newUserName,newUserPasswd));

        FabricCA.RevokeUser(caName,calocation,newUserName,orgName,mspId,newUserPasswd);


    }




}
