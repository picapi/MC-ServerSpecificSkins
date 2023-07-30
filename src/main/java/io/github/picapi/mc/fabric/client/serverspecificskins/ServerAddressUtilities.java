package io.github.picapi.mc.fabric.client.serverspecificskins;

import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import static org.apache.commons.codec.digest.DigestUtils.getSha256Digest;

public class ServerAddressUtilities {
    static public String hashServerAddress(String address){
        byte[] server_address_hash = DigestUtils.getSha256Digest().digest(address.getBytes());
        return Hex.encodeHexString(server_address_hash);
    }

    static public String hashServerAddress(ServerInfo server){
        return hashServerAddress(server.name + "|" + server.address);
    }

}
