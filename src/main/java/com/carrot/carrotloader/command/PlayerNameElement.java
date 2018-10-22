package com.carrot.carrotloader.command;

import java.util.Optional;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.PatternMatchingCommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

public class PlayerNameElement extends PatternMatchingCommandElement
{
	public PlayerNameElement(Text key)
	{
		super(key);
	}
	
	@Override
	protected Iterable<String> getChoices(CommandSource src)
	{
		return Sponge.getServer().getGameProfileManager().getCache().getProfiles().stream().filter(p -> p.getName().isPresent()).map(p -> p.getName().get()).collect(Collectors.toList());
	}

	@Override
	protected Object getValue(String choice) throws IllegalArgumentException
	{
		Optional<Player> op = Sponge.getServer().getPlayer(choice);
		if (op.isPresent())
			return Optional.of(op.get().getUniqueId());
		Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
		if (userStorage.isPresent()){
			Optional<User> p = userStorage.get().get(choice);
			if (p.isPresent())
				return Optional.of(p.get().getUniqueId());
		}
		return Optional.empty();
	}

	public Text getUsage(CommandSource src)
	{
		return Text.of("Player Name");
	}
}
