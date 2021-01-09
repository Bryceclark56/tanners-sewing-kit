package me.bc56.tanners_sewing_kit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.bc56.tanners_sewing_kit.homes.HomeManager;
import me.bc56.tanners_sewing_kit.homes.HomeMixinAccess;
import me.bc56.tanners_sewing_kit.homes.PlayerHome;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;

public class HomeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("sethome")
            .then(argument("name", word())
                .executes(HomeCommand::setNamedHome)
            )
            .executes(HomeCommand::setHome)
        );

        dispatcher.register(literal("delhome")
            .then(argument("name", word())
                .executes(HomeCommand::removeNamedHome)
            )
            .executes(HomeCommand::removeHome)
        );

        dispatcher.register(literal("home")
            .then(argument("name", word())
                .executes(HomeCommand::goToNamedHome)
            )
            .executes(HomeCommand::goToHome)
        );

        dispatcher.register(literal("listhomes")
            .executes(HomeCommand::listHomes)
        );
    }

    public static int listHomes(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();

        Map<String, PlayerHome> homes = ((HomeMixinAccess)player).getHomes();

        if (homes == null || homes.isEmpty()) {
            player.sendMessage(new LiteralText("You don't have any homes").formatted(Formatting.RED), false);
            return 0;
        }

        MutableText finalMessage = new LiteralText("Homes: ").formatted(Formatting.GOLD);

        List<MutableText> homesList = homes.keySet().stream().map(homeName -> 
            new LiteralText(homeName)
                .setStyle(Style.EMPTY
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Teleport to: " + homeName)))
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home " + homeName))
                    .withColor(Formatting.WHITE)
                )
        ).collect(Collectors.toList());

        Iterator<MutableText> homesIter = homesList.iterator();

        Text separator = new LiteralText(", ");
        while(homesIter.hasNext()) {
            MutableText homeText = homesIter.next();
            finalMessage.append(homeText);

            if (homesIter.hasNext()) {
                finalMessage.append(separator);
            }
        }

        player.sendMessage(finalMessage, false);

        return 0;
    }

    public static int goToHome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();

        HomeManager.sendToHome(player);

        return 0;
    }

    public static int goToNamedHome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String homeName = getString(context, "name").toLowerCase();

        HomeManager.sendToHome(player, homeName);

        return 0;
    }

    public static int setHome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();

        HomeManager.setNewHome(player);

        return 0;
    }

    public static int setNamedHome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String homeName = getString(context, "name");

        HomeManager.setNewHome(player, homeName);

        return 0;
    }

    public static int removeHome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();

        HomeManager.removeHome(player);

        return 0;
    }

    public static int removeNamedHome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String homeName = getString(context, "name");

        HomeManager.removeNamedHome(player, homeName);

        return 0;
    }
}
