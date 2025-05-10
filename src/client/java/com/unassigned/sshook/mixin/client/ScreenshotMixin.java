package com.unassigned.sshook.mixin.client;

import com.unassigned.sshook.SSHook;
import com.unassigned.sshook.SSHookClient;
import com.unassigned.sshook.SSHookClientConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(ScreenshotRecorder.class)
public class ScreenshotMixin {

    @Inject(
            method = "saveScreenshot(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/client/gl/Framebuffer;Ljava/util/function/Consumer;)V",
            at = @At("TAIL")
    )
    private static void onSaveScreenshot(File gameDirectory, @Nullable String fileName, Framebuffer framebuffer, Consumer<Text> messageReceiver, CallbackInfo ci) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        Map<String, String> webhooks;
        if (!SSHookClientConfig.INSTANCE.enableUploadPrompt || player == null) return;

        if (SSHookClientConfig.INSTANCE.useServerOverride) {
            if (SSHookClient.SERVER_WEBHOOKS.isEmpty()) {
                SSHook.LOGGER.info("Attempting to upload SS using webhook with no available webhooks from the server, exiting!");
                return;
            }
            webhooks = SSHookClient.SERVER_WEBHOOKS;
        } else {
            if (SSHookClientConfig.INSTANCE.clientWebhooks.isEmpty()) {
                SSHook.LOGGER.info("Attempting to upload SS using webhook with no available webhook, exiting!");
                return;
            }
            webhooks = SSHookClientConfig.INSTANCE.clientWebhooks;
        }

        File screenshotDir = new File(gameDirectory, "screenshots");
        screenshotDir.mkdirs();
        File screenshotFile = fileName == null ? getScreenshotFilename(screenshotDir) : new File(screenshotDir, fileName);

        SSHookClient.LAST_SCREENSHOT_FILE = screenshotFile;

        MutableText message = Text.literal("Upload screenshot to: ").formatted(Formatting.GRAY);
        for (Map.Entry<String, String> entry : webhooks.entrySet()) {
            String name = entry.getKey();
            MutableText clickable = Text.literal("[" + name + "]")
                    .styled(style -> style
                            .withColor(Formatting.AQUA)
                            .withClickEvent(new ClickEvent.RunCommand("/upload_screenshot " + name))
                            .withHoverEvent(new HoverEvent.ShowText(Text.literal("Click to upload to " + name)))
                    );
            message.append(clickable).append(Text.literal(" "));
        }

        player.sendMessage(message, false);
    }

    @Unique
    private static File getScreenshotFilename(File directory) {
        String base = Util.getFormattedCurrentTime();
        int index = 1;
        while (true) {
            File file = new File(directory, base + (index == 1 ? "" : "_" + index) + ".png");
            if (!file.exists()) return file;
            index++;
        }
    }

}
