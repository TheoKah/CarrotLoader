package com.carrot.carrotloader.command;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.carrot.carrotloader.LoaderData;

public class LoaderManagerExecutor implements CommandExecutor{

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Optional<String> action = args.<String>getOne("give|take|set");
		Optional<User> user = args.<User>getOne("player");
		Optional<Integer> amount = args.<Integer>getOne("amount");


		if (!action.isPresent() || !user.isPresent() || !amount.isPresent()) {
			src.sendMessage(Text.of(TextColors.DARK_RED, "/cl manager <give|take|set> <user> <amount>"));
			return CommandResult.empty();
		}
		
		int total = LoaderData.getMaxChunkloaders(user.get().getUniqueId());
		
		if (action.get().equals("set")) {
			total = amount.get().intValue();
		} else if (action.get().equals("give")) {
			total += amount.get().intValue();
		} else if (action.get().equals("take")) {
			total -= amount.get().intValue();
		} else {
			src.sendMessage(Text.of(TextColors.DARK_RED, "/cl manager <give|take|set> <user> <amount>"));
			return CommandResult.empty();
		}

		Optional<ProviderRegistration<PermissionService>> permService = Sponge.getServiceManager().getRegistration(PermissionService.class);
		if (!permService.isPresent()) {
			src.sendMessage(Text.of(TextColors.DARK_RED, "Permission manager missing :/"));
			return CommandResult.empty();
		}
		
		LoaderData.setMaxChunkloaders(user.get().getUniqueId(), total);
		
		src.sendMessage(Text.of(TextColors.DARK_GREEN, "Success"));
		return CommandResult.success();
	}

}
