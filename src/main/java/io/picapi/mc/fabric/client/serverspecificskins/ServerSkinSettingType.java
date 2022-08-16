package io.picapi.mc.fabric.client.serverspecificskins;

import net.minecraft.text.Text;

public enum ServerSkinSettingType {
        CLASSIC(),
        SLIM();

        public Text getText(){
                return Text.translatable("serverspecificskins.skinType."+this.name());
        }

        public String getRequestValue(){
                if (this.equals(CLASSIC)){
                        return "CLASSIC";
                } else if (this.equals(SLIM)) {
                        return "SLIM";
                }
                // Default to Classic if unset
                return "CLASSIC";
        }

        static public ServerSkinSettingType getFromString(String s){
                if (s.equals("CLASSIC")){
                        return CLASSIC;
                } else if (s.equals("SLIM")){
                        return SLIM;
                }
                return CLASSIC;
        }

}
