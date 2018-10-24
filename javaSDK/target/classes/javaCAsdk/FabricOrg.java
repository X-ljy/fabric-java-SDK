package javaCAsdk;

import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.util.*;

public class FabricOrg {

    //基于SampleOrg添加的部分变量




    //SampleOrg部分

    final String name;
    final String mspid;
    HFCAClient caClient;

    Map<String, User> userMap = new HashMap<>();
    Map<String, String> peerLocations = new HashMap<>();
    Map<String, String> ordererLocations = new HashMap<>();
    Map<String, String> eventHubLocations = new HashMap<>();
    private FabricUser admin;
    private String caLocation;
    private Properties caProperties = null;

    private FabricUser peerAdmin;


    private String domainName;

    public String getCAName() {
        return caName;
    }

    private String caName;

    public FabricOrg(String name, String mspid) {
        this.name = name;
        this.mspid = mspid;
    }

    public FabricUser getAdmin() {
        return admin;
    }

    public void setAdmin(FabricUser admin) {
        this.admin = admin;
    }

    public String getMSPID() {
        return mspid;
    }

    public String getCALocation() {
        return this.caLocation;
    }

    public void setCALocation(String caLocation) {
        this.caLocation = caLocation;
    }

    public void addPeerLocation(String name, String location) {

        peerLocations.put(name, location);
    }

    public void addOrdererLocation(String name, String location) {

        ordererLocations.put(name, location);
    }

    public void addEventHubLocation(String name, String location) {

        eventHubLocations.put(name, location);
    }

    public String getPeerLocation(String name) {
        return peerLocations.get(name);

    }

    public String getOrdererLocation(String name) {
        return ordererLocations.get(name);

    }

    public String getEventHubLocation(String name) {
        return eventHubLocations.get(name);

    }

    public Set<String> getPeerNames() {

        return Collections.unmodifiableSet(peerLocations.keySet());
    }


    public Set<String> getOrdererNames() {

        return Collections.unmodifiableSet(ordererLocations.keySet());
    }

    public Set<String> getEventHubNames() {

        return Collections.unmodifiableSet(eventHubLocations.keySet());
    }

    public HFCAClient getCAClient() {

        return caClient;
    }

    public void setCAClient(HFCAClient caClient) {

        this.caClient = caClient;
    }

    public String getName() {
        return name;
    }

    public void addUser(FabricUser user) {
        userMap.put(user.getName(), user);
    }

    public User getUser(String name) {
        return userMap.get(name);
    }

    public Collection<String> getOrdererLocations() {
        return Collections.unmodifiableCollection(ordererLocations.values());
    }

    public Collection<String> getEventHubLocations() {
        return Collections.unmodifiableCollection(eventHubLocations.values());
    }


    public void setCAProperties(Properties caProperties) {
        this.caProperties = caProperties;
    }

    public Properties getCAProperties() {
        return caProperties;
    }


    public FabricUser getPeerAdmin() {
        return peerAdmin;
    }

    public void setPeerAdmin(FabricUser peerAdmin) {
        this.peerAdmin = peerAdmin;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setCAName(String caName) {
        this.caName = caName;
    }

    //基于SampleOrg添加的方法




}
