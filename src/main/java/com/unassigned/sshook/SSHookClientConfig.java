package com.unassigned.sshook;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.HashMap;
import java.util.Map;

@Config(name=SSHook.MOD_ID)
public class SSHookClientConfig implements ConfigData {

    @ConfigEntry.Gui.Excluded
    public static SSHookClientConfig INSTANCE;

    public static void initClientConfig()
    {
        AutoConfig.register(SSHookClientConfig.class, JanksonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(SSHookClientConfig.class).getConfig();
    }

    @Comment("Every screenshot, notify the user that they can upload a screenshot using discord webhooks.")
    public boolean enableUploadPrompt = true;

    @Comment("If SSHook is installed and valid hooks are present, use the webhooks (and webhook settings) defined by any server.")
    public boolean useServerOverride = true;

    @Comment("If set to true, the bot name and avatar will be set to the user who uploaded it.")
    public boolean enableBotNameOverride = true;

    @Comment("Current list of discord webhooks that the player will be notified to upload to.")
    @ConfigEntry.Gui.Excluded
    public Map<String, String> clientWebhooks = new HashMap<>() {{
       put("ExampleWebhook", "https://example.com/webhook1");
    }};

}
