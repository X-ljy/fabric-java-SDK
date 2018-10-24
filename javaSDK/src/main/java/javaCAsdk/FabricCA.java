package javaCAsdk;

/**
 * Created by 夕 on 2018/10/22 17:34
 */
import javaCAsdk.FabricOrg;
import javaCAsdk.FabricStore;
import javaCAsdk.FabricUser;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.Attribute;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.HFCAInfo;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

import java.io.File;

public class FabricCA {


    /**
     * 登记（注册）用户 register
     *
     *只有已经注册的用户才能发起登记请求，发起登记请求的用户称为登记员。
     * 这里使用admin作为登记员，首先注册初始化的管理员用户，获取admin注册证书后，才能
     *进行下一步登记用户的操作
     *
     * @param orgName
     * @param mspId
     * @param calocation
     * @param adminName
     * @param adminPasswd
     * @param newUserName
     * @param newUserPasswd
     * @param affiliation   //用户从属关系 org1.department1
     * @throws Exception
     */
    public static boolean registerUser(String orgName, String mspId, String calocation,
                                         String caName, String adminName, String adminPasswd,
                                         String newUserName, String newUserPasswd, String affiliation)
                                         throws Exception {

        System.out.println("初始化管理员用户，获取管理员的用户证书与私钥，以便进行下一步操作");

        //构建FabricOrg
        FabricOrg FabricOrg = new FabricOrg(orgName, mspId);

        //组织一管理员的私钥路径
        String kstore = "/opt/gopath/src/github.com/hyperledger/fabric/examples/e2e_cli/crypto-config/" +
                        "peerOrganizations/org1.blockchain.com/users/Admin@org1.blockchain.com/msp/keystore";

        //构建FabricStore实例，获取的身份证书与私钥将会存在这里
        FabricStore FabricStore = new FabricStore(new File(kstore));

        //创建CAclient实例
        HFCAClient ca = HFCAClient.createNewInstance(caName, calocation, null);
        ca.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());


        HFCAInfo info = ca.info();
        String  CAName = info.getCAName();
        String  CACertificateChain = info.getCACertificateChain();
        String  Version  = info.getVersion();
        //输出info
        System.out.println("CAName"+CAName+
                            "CACertificateChain"+CACertificateChain+
                            "Version"+Version);

        //登录管理员用户
        FabricUser admin = FabricStore.getMember(adminName, orgName);
        if (!admin.isEnrolled()) {
            Enrollment enrollment = ca.enroll(admin.getName(), adminPasswd);
            System.out.println(enrollment.getKey());
            System.out.println(enrollment.getCert());
            admin.setEnrollment(enrollment);
            admin.setMspId(mspId);

        }

        //构建将要注册的用户实例
        FabricUser user = FabricStore.getMember(newUserName, orgName);


        if (!user.isRegistered()) {

            //注册新用户，构建RegistrationRequest请求
            RegistrationRequest rr = new RegistrationRequest(user.getName(), affiliation);
            rr.setSecret(newUserPasswd);

            //添加hf.Revoker,该用户的身份认证证书允许被撤销
            Attribute revoker = new Attribute("hf.Revoker", "true");
            rr.addAttribute(revoker);

            //添加hf.Registrar.Roles属性，如果添加该属性，则可以使用新注册的用户来添加新的用户类型
            Attribute roles = new Attribute("hf.Registrar.Roles", "user,client,peer");
            rr.addAttribute(roles);

            //赋予用户创建其他用户的功能，同时指定用户在创建其他用户是可以指定的属性列表，
            // 不在此列表内的属性，不允许用户为其他用户添加
            Attribute attrs = new Attribute("hf.Registrar.Attributes", "hf.Revoker,hf.Registrar.Roles");
            rr.addAttribute(attrs);

            String secret = ca.register(rr, admin);

            if (secret != null) {
                user.setEnrollmentSecret(secret);
                FabricOrg.addUser(user);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * 登录用户  enroll
     *
     * @param orgName
     * @param mspId
     * @param caName
     * @param calocation
     * @param userName
     * @param userpasswd
     * @return
     * @throws Exception
     */

    public static boolean EnrollUser(String orgName, String mspId, String caName, String calocation,
                                       String userName, String userpasswd
                                      ) throws Exception {

        //用户登录状态
        Boolean state = false;

        FabricOrg FabricOrg = new FabricOrg(orgName, mspId);
        String kstore = "/opt/gopath/src/github.com/hyperledger/fabric/examples/e2e_cli/crypto-config/" +
                        "peerOrganizations/org1.blockchain.com/users/Admin@org1.blockchain.com/msp/keystore";
        FabricStore FabricStore = new FabricStore(new File(kstore));

        //创建CAclient实例
        HFCAClient ca = HFCAClient.createNewInstance(caName, calocation, null);
        ca.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());


        HFCAInfo info = ca.info();
        String  CAName = info.getCAName();
        String  CACertificateChain = info.getCACertificateChain();
        String  Version  = info.getVersion();
        //输出info
        System.out.println("CAName"+CAName+
                "CACertificateChain"+CACertificateChain+
                "Version"+Version);


        FabricUser CAuser = FabricStore.getMember(userName, orgName);

        if (!CAuser.isEnrolled()) {
            Enrollment enrollment = ca.enroll(CAuser.getName(), userpasswd);
            if (enrollment != null) {

                CAuser.setEnrollment(enrollment);
                CAuser.setMspId(mspId);
                FabricOrg.addUser(CAuser);

                state = true;

            } else {

                state = false;

            }

        }

        return state;

    }

    /**
     *获取用户证书与私钥
     * @param orgName
     * @param mspId
     * @param caName
     * @param calocation
     * @param userName
     * @param userpasswd
     * @return 用户证书与私钥
     * @throws Exception
     */
    public static String getCertAndKeyStore(String orgName, String mspId, String caName, String calocation,
                                            String userName, String userpasswd
                                            ) throws Exception {


        FabricOrg FabricOrg = new FabricOrg(orgName, mspId);
        String kstore = "/opt/gopath/src/github.com/hyperledger/fabric/examples/e2e_cli/crypto-config/" +
                        "peerOrganizations/org1.blockchain.com/users/Admin@org1.blockchain.com/msp/keystore";
        FabricStore FabricStore = new FabricStore(new File(kstore));


        //创建CAclient实例
        HFCAClient ca = HFCAClient.createNewInstance(caName, calocation, null);
        ca.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());


        FabricUser CAuser = FabricStore.getMember(userName, orgName);
        Enrollment enrollment = ca.enroll(CAuser.getName(), userpasswd);

        return  "用户证书"  + enrollment.getCert() + "\n用户私钥" + enrollment.getKey();


    }

    public  static  Boolean  RevokeUser(String caName,String calocation ,String userName,
                                         String orgName, String mspId,String userpasswd)
                                         throws Exception {

        FabricOrg FabricOrg = new FabricOrg(orgName, mspId);
        String kstore = "/opt/gopath/src/github.com/hyperledger/fabric/examples/e2e_cli/crypto-config/" +
                "peerOrganizations/org1.blockchain.com/users/Admin@org1.blockchain.com/msp/keystore";
        FabricStore FabricStore = new FabricStore(new File(kstore));


        HFCAClient ca = HFCAClient.createNewInstance(caName, calocation, null);
        ca.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

        FabricUser user = FabricStore.getMember(userName, orgName);

        if(user == null){
            System.out.println("返回user实例为null");
            return false;
        }

        try {
            Enrollment enrollment = ca.enroll(user.getName(), userpasswd);
            user.setEnrollment(enrollment);
            System.out.println(enrollment.getCert());
            System.out.println(enrollment.getKey());
            ca.revoke(user,user.getEnrollment(),"reason");

        }catch (Exception e){

            e.printStackTrace();

        }

        return true;
    }

}
