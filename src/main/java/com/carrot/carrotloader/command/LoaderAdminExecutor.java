package com.carrot.carrotloader.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.ChunkTicketManager.LoadingTicket;
import org.spongepowered.api.world.World;

import com.carrot.carrotloader.LoaderData;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableSetMultimap;

public class LoaderAdminExecutor implements CommandExecutor{

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!src.hasPermission("carrotloader.admin")) {
			src.sendMessage(Text.of(TextColors.DARK_RED, "Missing Permission"));
			return CommandResult.success();
		}

		Optional<User> user = args.<User>getOne("player");

		List<Text> contents = new ArrayList<>();
		String title = "Master list of loaded chunks";

		if (user.isPresent()) {
			title = "List of forced chunks for " + user.get().getName();
		}

		for (World world : Sponge.getServer().getWorlds()) {
			ImmutableSetMultimap<Vector3i, LoadingTicket> chunks = Sponge.getServer().getChunkTicketManager().getForcedChunks(world);
			for (Entry<Vector3i, LoadingTicket> chunk : chunks.entries()) {
				if (user.isPresent() && !LoaderData.isOwner(world.getUniqueId(), chunk.getKey(), user.get().getUniqueId()))
					continue;
				Builder text = Text.builder();
				text.append(Text.builder(chunk.getKey().getX() + " " + chunk.getKey().getZ() + " (" + world.getName() + ")")
						.color(TextColors.DARK_GREEN)
						.onHover(TextActions.showText(Text.of(TextColors.YELLOW, (chunk.getKey().getX() * 16), " 0 ", (chunk.getKey().getZ() * 16), TextColors.GRAY, " -> ", TextColors.YELLOW, (chunk.getKey().getX() * 16 + 16), " 255 ", (chunk.getKey().getZ() * 16 + 16))))
						.onClick(TextActions.runCommand("/tppos " + world.getName() + " " + (chunk.getKey().getX() * 16 + 8) + " 160 " + (chunk.getKey().getZ() * 16 + 8)))
						.build());

				text.append(Text.of(TextColors.GRAY, " by ", TextColors.YELLOW, chunk.getValue().getPlugin()));
				if (!user.isPresent()) {
					boolean first = true;
					for (UUID owner : LoaderData.getOwners(world.getUniqueId(), chunk.getKey())) {
						Optional<String> name = LoaderData.getPlayerName(owner);
						if (!name.isPresent())
							continue;
						if (first)
							text.append(Text.of(TextColors.GRAY, " for "));
						else
							text.append(Text.of(TextColors.GRAY, ", "));
						first = false;
						text.append(Text.of(TextColors.DARK_GREEN, Text.builder(name.get())
								.color(TextColors.DARK_GREEN)
								.onHover(TextActions.showText(Text.of("filter")))
								.onClick(TextActions.runCommand("/chunkloader admin " + name.get()))
								.build()));
					}
				}

				contents.add(text.build());
			}
		}

		PaginationList.builder()
		.title(Text.of(TextColors.GOLD, "{ ", TextColors.YELLOW, title, TextColors.GOLD, " }"))
		.contents(contents)
		.padding(Text.of("-"))
		.sendTo(src);
		return CommandResult.success();
	}

}
