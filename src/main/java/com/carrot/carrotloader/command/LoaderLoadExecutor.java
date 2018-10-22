package com.carrot.carrotloader.command;

import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.carrot.carrotloader.LoaderData;

public class LoaderLoadExecutor implements CommandExecutor{

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

		Optional<User> user = args.<User>getOne("player");
		Optional<Location<World>> loc = args.<Location<World>>getOne("location");

		UUID target = null;
		Location<World> location = null;

		if (src instanceof Player) {
			Player player = (Player) src;
			target = player.getUniqueId();
			location = player.getLocation();
		}

		if (user.isPresent()) {
			if (!src.hasPermission("carrotloader.admin")) {
				src.sendMessage(Text.of(TextColors.DARK_RED, "Missing Permission"));
				return CommandResult.empty();
			}
			target = user.get().getUniqueId();
			Optional<Player> player = user.get().getPlayer();
			if (player.isPresent())
				location = player.get().getLocation();
		}

		if (loc.isPresent()) {
			if (!src.hasPermission("carrotloader.admin")) {
				src.sendMessage(Text.of(TextColors.DARK_RED, "Missing Permission"));
				return CommandResult.empty();
			}
			location = loc.get();
		}
		
		if (target == null || location == null) {
			src.sendMessage(Text.of(TextColors.RED, "From console, you need to specify a player. If the player is not online, you have to give a location as well"));
			return CommandResult.empty();
		}

		if (LoaderData.count(target) >= LoaderData.getMaxChunkloaders(target)) {
			src.sendMessage(Text.of(TextColors.RED, "No chunkloader slot available"));
			return CommandResult.empty();
		}

		if (LoaderData.addChunk(target, location))
			src.sendMessage(Text.of(TextColors.DARK_GREEN, "Chunkloader added. Will load a 3x3 ;)"));
		else
			src.sendMessage(Text.of(TextColors.DARK_RED, "This chunkloader would overlap another chunkloader area"));
		return CommandResult.success();
	}

}
