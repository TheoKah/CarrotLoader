package com.carrot.carrotloader.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import com.carrot.carrotloader.LoaderData;
import com.flowpowered.math.vector.Vector3i;

public class LoaderListExecutor implements CommandExecutor{

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

		List<Text> contents = new ArrayList<>();

		contents.add(Text.of(TextColors.GOLD, LoaderData.count(target), TextColors.GRAY, " / ", TextColors.GOLD, LoaderData.getMaxChunkloaders(target), TextColors.YELLOW, " chunkloaders in use"));

		Optional<Map<UUID, List<Vector3i>>> chunks = LoaderData.list(target);

		if (chunks.isPresent()) {
			for (Entry<UUID, List<Vector3i>> entry : chunks.get().entrySet()) {
				Optional<World> world = Sponge.getServer().getWorld(entry.getKey());
				for (Vector3i coord : entry.getValue()) {
					
					contents.add(Text.of(Text.builder(coord.getX() + " " + coord.getZ())
							.color(TextColors.DARK_GREEN)
							.onHover(TextActions.showText(Text.of(TextColors.YELLOW, ((coord.getX() - 1) * 16), " 0 ", ((coord.getZ() - 1) * 16), TextColors.GRAY, " -> ", TextColors.YELLOW, ((coord.getX() + 1) * 16 + 16), " 255 ", ((coord.getZ() + 1) * 16 + 16))))
							.build(),
							TextColors.GRAY, " - ", TextColors.DARK_PURPLE, (world.isPresent() ?  world.get().getName() : entry.getKey()),
							TextColors.GRAY, " - ", TextColors.YELLOW, "3x3"));
				}
			}
		} else {
			contents.add(Text.of(TextColors.YELLOW, "No chunks loaded"));
		}

		PaginationList.builder()
		.title(Text.of(TextColors.GOLD, "{ ", TextColors.YELLOW, "List of loaded chunks", TextColors.GOLD, " }"))
		.contents(contents)
		.padding(Text.of("-"))
		.sendTo(src);
		return CommandResult.success();
	}

}
