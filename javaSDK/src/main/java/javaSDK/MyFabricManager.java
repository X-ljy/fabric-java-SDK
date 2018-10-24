package javaSDK;

/*
 * @author 罗金宇
 * @date 2018/10/8
 */

import javaSDK.OrgManager;

import java.io.File;

public class MyFabricManager {

    // 获取智能合约对象
    public static FabricManager getFabricManager() throws Exception {


        OrgManager orgManager = new OrgManager();
        orgManager
                .init("Org1")
                .setCA("ca0.org1.blockchain.com", "http://10.10.44.122:7054")
                .setUser("Admin", getCryptoConfigPath(), getChannleArtifactsPath())
                .setPeers("Org1MSP", "org1.blockchain.com")
                .addPeer("peer0.org1.blockchain.com", "peer0.org1.blockchain.com", "grpcs://10.10.44.122:7051", "grpcs://10.10.44.122:7053", false)
                .addPeer("peer1.org1.blockchain.com", "peer1.org1.blockchain.com", "grpcs://10.10.44.123:7051", "grpcs://10.10.44.123:7053", false)
                .setOrderers("blockchain.com")
                .addOrderer("orderer1.blockchain.com", "grpcs://10.10.44.122:7050")
                .setChannel("mychannel")
                .setChainCode("test", "/opt/gopath/", "github.com/hyperledger/fabric/blockchains/chaincode/go/chaincode_example02", "2.0", 90000, 120)
                .openTLS(true)
                .openCATLS(false)
                .add();
        return orgManager.use("Org1");
    }

    private static String getChannleArtifactsPath() {
        String directories = "/opt/gopath/src/github.com/hyperledger/fabric/examples/e2e_cli/";
        File directory = new File(directories);
        return directory.getPath() + "/channel-artifacts/";
    }

    private static String getCryptoConfigPath() {
        String directories = "/opt/gopath/src/github.com/hyperledger/fabric/examples/e2e_cli/";
        File directory = new File(directories);
        return directory.getPath() + "/crypto-config/";
    }
}
