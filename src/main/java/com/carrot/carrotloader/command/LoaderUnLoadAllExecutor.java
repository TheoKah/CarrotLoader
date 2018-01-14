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

import com.carrot.carrotloader.LoaderData;

public class LoaderUnLoadAllExecutor implements CommandExecutor{

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Optional<User> user = args.<User>getOne("player");
		UUID target;
		if (!user.isPresent()) {
			if (!(src instanceof Player)) {
				src.sendMessage(Text.of("Needs to be a player"));
				return CommandResult.empty();
			}
			target = ((Player) src).getUniqueId();
		} else {
			if (!src.hasPermission("carrotloader.admin")) {
				src.sendMessage(Text.of(TextColors.DARK_RED, "Missing Permission"));
				return CommandResult.empty();
			}
				
			target = user.get().getUniqueId();
		}
		
		LoaderData.clearChunks(target);
		src.sendMessage(Text.of(TextColors.DARK_GREEN, "Removed all chunkloaders"));
		return CommandResult.success();
	}

}
