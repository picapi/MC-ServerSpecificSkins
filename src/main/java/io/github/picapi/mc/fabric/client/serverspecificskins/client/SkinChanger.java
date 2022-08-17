package io.github.picapi.mc.fabric.client.serverspecificskins.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.picapi.mc.fabric.client.serverspecificskins.ServerSkinSettingType;
import io.github.picapi.mc.fabric.client.serverspecificskins.ServerSpecificSkins;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;

public class SkinChanger {

    public static class SkinInfo{
        byte[] skinData;
        ServerSkinSettingType skinType;

         SkinInfo(byte[] data, ServerSkinSettingType type){
            this.skinData = data;
            this.skinType = type;
        }

        public byte[] getSkinData() {
            return skinData;
        }

        public ServerSkinSettingType getSkinType() {
            return skinType;
        }
    }
    private static Session getSession() {
        return MinecraftClient.getInstance().getSession();
    }

    public static boolean setSkin(File skin_file, ServerSkinSettingType type) throws IOException {
        ServerSpecificSkins.LOGGER.debug("Getting relevant session");
        Session session = getSession();

        // create a request
        ServerSpecificSkins.LOGGER.debug("Making HTTP request");
        var request = new HttpPost("https://api.minecraftservices.com/minecraft/profile/skins");
        var variant = new StringBody(type.getRequestValue(), ContentType.MULTIPART_FORM_DATA);
        var skin_file_part = new FileBody(skin_file);

        var requestBody = MultipartEntityBuilder.create()
                .addPart("variant",variant)
                .addPart("file",skin_file_part)
                .build();
        request.setEntity(requestBody);
        request.addHeader("Authorization", "Bearer " + session.getAccessToken());
        CloseableHttpClient httpclient = HttpClients.createDefault();
        ServerSpecificSkins.LOGGER.debug("Sending HTTP Request");
        CloseableHttpResponse response = httpclient.execute(request);
        HttpEntity resEntity = response.getEntity();
        if(response.getStatusLine().getStatusCode() == 200){
            ServerSpecificSkins.LOGGER.debug("200 response - success!");
            return true;
        } else {
            ServerSpecificSkins.LOGGER.debug("Non-200 response.");
            return false;
        }
    }

    public static SkinInfo getCurrentSkin() throws IOException {
        Session session = getSession();
        var request = new HttpGet("https://api.minecraftservices.com/minecraft/profile");
        request.addHeader("Authorization", "Bearer " + session.getAccessToken());
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(request);
        JsonObject jsonObject = JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
        var skinURL = jsonObject.getAsJsonArray("skins").get(0).getAsJsonObject().get("url").getAsString();
        var skinType = jsonObject.getAsJsonArray("skins").get(0).getAsJsonObject().get("variant").getAsString();
        var skinRequest = new HttpGet(skinURL);
        var skinResponse = httpclient.execute(skinRequest);
        return new SkinInfo(EntityUtils.toByteArray(skinResponse.getEntity()),ServerSkinSettingType.getFromString(skinType));
    }

    public static byte[] getCurrentSkinSHA256Hash() throws IOException {
        return DigestUtils.getSha256Digest().digest(getCurrentSkin().skinData);

    }
}
