package com.carrot.carrotloader.command;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class LoaderMainExecutor implements CommandExecutor{

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		List<Text> contents = new ArrayList<>();

		contents.add(Text.of(TextColors.GOLD, "/cl help", TextColors.GRAY, " - ", TextColors.YELLOW, "Print a link to the wiki"));
		contents.add(Text.of(TextColors.GOLD, "/cl load", TextColors.GRAY, " - ", TextColors.YELLOW, "Add a chunk to your list of loaded chunks"));
		contents.add(Text.of(TextColors.GOLD, "/cl unload", TextColors.GRAY, " - ", TextColors.YELLOW, "Remove a chunk from your list of loaded chunks"));
		contents.add(Text.of(TextColors.GOLD, "/cl unloadall", TextColors.GRAY, " - ", TextColors.YELLOW, "Remove all chunks from your list of loaded chunks"));
		contents.add(Text.of(TextColors.GOLD, "/cl list", TextColors.GRAY, " - ", TextColors.YELLOW, "List all chunks from your list of loaded chunks"));

		PaginationList.builder()
		.title(Text.of(TextColors.GOLD, "{ ", TextColors.YELLOW, "/carrotloader", TextColors.GOLD, " }"))
		.contents(contents)
		.padding(Text.of("-"))
		.sendTo(src);
		return CommandResult.success();
	}

}
