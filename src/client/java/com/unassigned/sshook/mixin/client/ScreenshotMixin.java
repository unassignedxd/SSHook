package com.unassigned.sshook.mixin.client;

import com.unassigned.sshook.SSHook;
import com.unassigned.sshook.SSHookClient;
import com.unassigned.sshook.SSHookClientConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(ScreenshotRecorder.class)
public class ScreenshotMixin {

    @Inject(method="saveScreenshotInner", at=@At("TAIL"), locals=LocalCapture.CAPTURE_FAILSOFT)
    private static void saveScreenshotInner(File gameDirectory, String fileName, Framebuffer framebuffer, Consumer<Text> messageReceiver, CallbackInfo ci, NativeImage nativeImage, File file, File file2)
    {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        Map<String, String> webhooks;
        if(!SSHookClientConfig.INSTANCE.enableUploadPrompt || player == null) return;

        if(SSHookClientConfig.INSTANCE.useServerOverride)
        {
            if(SSHookClient.SERVER_WEBHOOKS.isEmpty()){
                SSHook.LOGGER.info("Attempting to upload SS using webhook with no available webhooks from the server, exiting!");
                return;
            }
            webhooks = SSHookClient.SERVER_WEBHOOKS;
        }
        else {
            if(SSHookClientConfig.INSTANCE.clientWebhooks.isEmpty()){
                SSHook.LOGGER.info("Attempting to upload SS using webhook with no available webhook, exiting!");
                return;
            }
            webhooks = SSHookClientConfig.INSTANCE.clientWebhooks;
        }

        MutableText message = Text.literal("Upload screenshot to: ").formatted(Formatting.GRAY);
        SSHookClient.LAST_SCREENSHOT_FILE = file2;

        for (Map.Entry<String, String> entry : webhooks.entrySet()) {
            String name = entry.getKey();

            // Create a clickable text component for all available webhooks
            MutableText clickable = Text.literal("[" + name + "]")
                    .styled(style -> style
                            .withColor(Formatting.AQUA)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/upload_screenshot " + name))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to upload to " + name)))
                    );

            message.append(clickable).append(Text.literal(" "));
        }

        player.sendMessage(message, false);

    }

}
