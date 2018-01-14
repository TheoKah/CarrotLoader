package com.carrot.carrotloader.command;

import java.net.MalformedURLException;
import java.net.URL;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

public class LoaderWikiExecutor implements CommandExecutor{

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		String wikiURL = "https://github.com/TheoKah/CarrotLoader/wiki/User-Guide";

		try {
			src.sendMessage(Text.of(TextColors.DARK_PURPLE, "Link to the wiki: ", Text.builder(wikiURL)
					.color(TextColors.DARK_AQUA)
					.onClick(TextActions.openUrl(new URL(wikiURL))).build()));
		} catch (MalformedURLException e) {
			src.sendMessage(Text.of(TextColors.DARK_PURPLE, "Link to the wiki: ", TextColors.DARK_AQUA, wikiURL));
		}
		return CommandResult.success();
	}

}
