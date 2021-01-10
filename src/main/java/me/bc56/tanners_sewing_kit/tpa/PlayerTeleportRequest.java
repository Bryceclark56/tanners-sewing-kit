package me.bc56.tanners_sewing_kit.tpa;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import me.bc56.tanners_sewing_kit.TannersSewingKit;
import me.bc56.tanners_sewing_kit.command.BackCommand;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class PlayerTeleportRequest {
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(20);

    public UUID from;
    public UUID to;

    LocalDateTime dateExpires;

    public PlayerTeleportRequest(ServerPlayerEntity from, ServerPlayerEntity to, Duration timeout) {
        this.from = from.getUuid();
        this.to = to.getUuid();

        dateExpires = LocalDateTime.now().plus(timeout);
    }

    public static void newTpaHere(ServerPlayerEntity caller, ServerPlayerEntity target) {
        PlayerTeleportRequest request = new PlayerTeleportRequest(target, caller, DEFAULT_TIMEOUT);

        ((TeleportMixinAccess)caller).setOutgoingRequest(request);
        ((TeleportMixinAccess)target).setIncomingRequest(request);

        sendConfirmation(caller, target);
        sendTpaHereNotice(caller, target);
    }

    public static void newTpa(ServerPlayerEntity caller, ServerPlayerEntity target) {
        PlayerTeleportRequest request = new PlayerTeleportRequest(target, caller, DEFAULT_TIMEOUT);

        ((TeleportMixinAccess)caller).setOutgoingRequest(request);
        ((TeleportMixinAccess)target).setIncomingRequest(request);

        sendConfirmation(caller, target);
        sendTpaNotice(caller, target);
    }

    public void execute() {
        if (LocalDateTime.now().isAfter(dateExpires)) return; //TODO: Better way of expiration

        PlayerManager playerManager = TannersSewingKit.server.getPlayerManager();
        ServerPlayerEntity fromPlayer = playerManager.getPlayer(from);
        ServerPlayerEntity toPlayer = playerManager.getPlayer(to);

        BackCommand.setLastLocation(fromPlayer);

        fromPlayer.sendMessage(new LiteralText("Teleporting...").formatted(Formatting.GOLD), false);

        fromPlayer.teleport(toPlayer.getServerWorld(), toPlayer.getX(), toPlayer.getY(), toPlayer.getZ(), toPlayer.getYaw(1.0F), toPlayer.getPitch(1.0F));
    }

    private static void sendConfirmation(ServerPlayerEntity caller, ServerPlayerEntity target) {
        // Confirm with caller
        MutableText requestSent = new LiteralText("Sending request to ").formatted(Formatting.GOLD);
        MutableText targetText = new LiteralText(target.getName().getString()).formatted(Formatting.AQUA);

        caller.sendMessage(requestSent.append(targetText), false);
    }

    private static void sendTpaNotice(ServerPlayerEntity caller, ServerPlayerEntity target) {
        // Notify target
        MutableText callerText = new LiteralText(caller.getName().getString()).formatted(Formatting.AQUA);
        MutableText requestReceived = new LiteralText(" wishes to teleport to you").formatted(Formatting.GOLD);

        target.sendMessage(callerText.append(requestReceived), false);
        sendAcceptPrompt(target);
    }

    private static void sendTpaHereNotice(ServerPlayerEntity caller, ServerPlayerEntity target) {
        // Notify target
        MutableText callerText = new LiteralText(caller.getName().getString()).formatted(Formatting.AQUA);
        MutableText requestReceived = new LiteralText(" wishes for you to teleport to them").formatted(Formatting.GOLD);

        target.sendMessage(callerText.append(requestReceived), false);
        sendAcceptPrompt(target);
    }

    private static void sendAcceptPrompt(ServerPlayerEntity target) {
        ClickEvent acceptClickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept");
        HoverEvent acceptHoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click here to accept"));

        MutableText acceptCommandText = new LiteralText("/tpaccept").setStyle(Style.EMPTY.withClickEvent(acceptClickEvent).withHoverEvent(acceptHoverEvent).withColor(Formatting.LIGHT_PURPLE));
        MutableText acceptPromptText = new LiteralText("Type ").formatted(Formatting.GOLD).append(acceptCommandText).append(new LiteralText(" to accept the request").formatted(Formatting.GOLD));

        target.sendMessage(acceptPromptText, false);
    }
}
