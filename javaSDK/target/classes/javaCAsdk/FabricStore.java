package javaCAsdk;

/**
 * Created by 夕 on 2018/10/24 14:03
 */
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.encoders.Hex;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A local file-based key value store.
 */
public class FabricStore {

    private String file;
    private Log logger = LogFactory.getLog(FabricStore.class);

    public FabricStore(File file) {

        this.file = file.getAbsolutePath();
    }

    /**
     * Get the value associated with name.
     *
     * @param name
     * @return value associated with the name
     */
    public String getValue(String name) {
        Properties properties = loadProperties();
        return properties.getProperty(name);
    }

    /**
     * Has the value present.
     *
     * @param name
     * @return true if it's present.
     */
    public boolean hasValue(String name) {
        Properties properties = loadProperties();
        return properties.containsKey(name);
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(file)) {
            properties.load(input);
            input.close();
        } catch (FileNotFoundException e) {
            logger.warn(String.format("Could not find the file \"%s\"", file));
        } catch (IOException e) {
            logger.warn(String.format("Could not load keyvalue store from file \"%s\", reason:%s",
                    file, e.getMessage()));
        }

        return properties;
    }

    /**
     * Set the value associated with name.
     *
     * @param name  The name of the parameter
     * @param value Value for the parameter
     */
    public void setValue(String name, String value) {
        Properties properties = loadProperties();
        try (
                OutputStream output = new FileOutputStream(file)
        ) {
            properties.setProperty(name, value);
            properties.store(output, "");
            output.close();

        } catch (IOException e) {
            logger.warn(String.format("Could not save the keyvalue store, reason:%s", e.getMessage()));
        }
    }

    private final Map<String, FabricUser> members = new HashMap<>();

    /**
     * Get the user with a given name
     *
     * @param name
     * @param org
     * @return user
     */
    public FabricUser getMember(String name, String org) {

        // Try to get the FabricUser state from the cache
        FabricUser FabricUser = members.get(javaCAsdk.FabricUser.toKeyValStoreName(name, org));
        if (null != FabricUser) {
            return FabricUser;
        }

        // Create the FabricUser and try to restore it's state from the key value store (if found).
        FabricUser = new FabricUser(name, org, this);

        return FabricUser;

    }

    /**
     * Check if store has user.
     *
     * @param name
     * @param org
     * @return true if the user exists.
     */
    public boolean hasMember(String name, String org) {

        // Try to get the FabricUser state from the cache

        if (members.containsKey(FabricUser.toKeyValStoreName(name, org))) {
            return true;
        }
        return FabricUser.isStored(name, org, this);

    }

    /**
     * Get the user with a given name
     *
     * @param name
     * @param org
     * @param mspId
     * @param privateKeyFile
     * @param certificateFile
     * @return user
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeySpecException
     */
    public FabricUser getMember(String name, String org, String mspId, File privateKeyFile,
                                File certificateFile) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {

        try {
            // Try to get the FabricUser state from the cache

            FabricUser FabricUser = members.get(javaCAsdk.FabricUser.toKeyValStoreName(name, org));
            if (null != FabricUser) {

                return FabricUser;

            }

            // Create the FabricUser and try to restore it's state from the key value store (if found).
            FabricUser = new FabricUser(name, org, this);
            FabricUser.setMspId(mspId);

            String certificate = new String(IOUtils.toByteArray(new FileInputStream(certificateFile)), "UTF-8");

            PrivateKey privateKey = getPrivateKeyFromBytes(IOUtils.toByteArray(new FileInputStream(privateKeyFile)));

            FabricUser.setEnrollment(new FabricStoreEnrollement(privateKey, certificate));

            FabricUser.saveState();

            return FabricUser;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw e;
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            throw e;
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            throw e;
        } catch (ClassCastException e) {
            e.printStackTrace();
            throw e;
        }

    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    static PrivateKey getPrivateKeyFromBytes(byte[] data) throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        final Reader pemReader = new StringReader(new String(data));

        final PrivateKeyInfo pemPair;
        try (PEMParser pemParser = new PEMParser(pemReader)) {
            pemPair = (PrivateKeyInfo) pemParser.readObject();
        }

        PrivateKey privateKey = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getPrivateKey(pemPair);

        return privateKey;
    }

    static final class FabricStoreEnrollement implements Enrollment, Serializable {

        private static final long serialVersionUID = -2784835212445309006L;
        private final PrivateKey privateKey;
        private final String certificate;

        FabricStoreEnrollement(PrivateKey privateKey, String certificate) {

            this.certificate = certificate;

            this.privateKey = privateKey;
        }

        @Override
        public PrivateKey getKey() {

            return privateKey;
        }

        @Override
        public String getCert() {
            return certificate;
        }

    }

    public void saveChannel(Channel channel) throws IOException, InvalidArgumentException {

        setValue("channel." + channel.getName(), Hex.toHexString(channel.serializeChannel()));

    }

    Channel getChannel(HFClient client, String name) throws IOException, ClassNotFoundException, InvalidArgumentException {
        Channel ret = null;

        String channelHex = getValue("channel." + name);
        if (channelHex != null) {

            ret = client.deSerializeChannel(Hex.decode(channelHex));

        }
        return ret;
    }

}