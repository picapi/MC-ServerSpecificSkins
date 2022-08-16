package io.picapi.mc.fabric.client.serverspecificskins;

import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class ServerAddressUtilities {
    static public String serverAddressToFilename(String address){
        String fixed_address;
        if (address.contains(":")){
            fixed_address = address.replace(':', '_');
        } else {
            fixed_address = address + "_25565";
        }
        return fixed_address;
    }

    static public String stringify(ServerAddress serverAddress){
        return serverAddressToFilename(serverAddress.getAddress() + ":" + serverAddress.getPort());
    }

    static public String stringify(ServerInfo server){
        return serverAddressToFilename(server.address);
    }

}
