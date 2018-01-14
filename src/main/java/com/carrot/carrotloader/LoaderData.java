package com.carrot.carrotloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;


public class LoaderData {
	private static File chunkloadersFile;
	private static ConfigurationNode chunkloadersNode;
	private static ConfigurationLoader<CommentedConfigurationNode> chunkloadersLoader;

	private static Hashtable<UUID, CarrotChunk> chunkloaders = new Hashtable<>();
	private static Hashtable<UUID, Integer> maxChunkloaders = new Hashtable<>();

	public static void init(File rootDir) throws IOException
	{
		rootDir.mkdirs();

		chunkloadersFile = new File(rootDir, "chunkloaders.json");
		chunkloadersFile.createNewFile();

		chunkloadersLoader = HoconConfigurationLoader.builder().setFile(chunkloadersFile).build();
		chunkloadersNode = chunkloadersLoader.load();
	}

	public static void load() {
		boolean hasErrors = false;

		for (Entry<Object, ? extends ConfigurationNode> chunkNode : chunkloadersNode.getNode("players").getChildrenMap().entrySet()) {
			int value = chunkNode.getValue().getInt(0);
			if (value > 0)
				maxChunkloaders.put(UUID.fromString(chunkNode.getKey().toString()), value);
		}

		for (ConfigurationNode chunkNode : chunkloadersNode.getNode("chunks").getChildrenList()) {
			try {
				CarrotChunk chunk = chunkNode.getValue(TypeToken.of(CarrotChunk.class));
				if (!chunkloaders.contains(chunk.getOwner()) && !chunk.cleanup())
					chunkloaders.put(chunk.getOwner(), chunk);
			} catch (Exception e) {
				e.printStackTrace();
				hasErrors = true;
			}
		}
		if (hasErrors)
			CarrotLoader.getLogger().warn("Errors occured while loading CarrotChunks.");
	}

	public static void unload() {
		save();
		chunkloaders.forEach((owner,chunk) -> {
			chunk.unload();
		});
		chunkloaders.clear();
	}

	public static void save() {
		boolean hasErrors = false;

		chunkloadersNode.removeChild("players");
		for (Entry<UUID, Integer> maxValues : maxChunkloaders.entrySet())
			chunkloadersNode.getNode("players").getNode(maxValues.getKey().toString()).setValue(maxValues.getValue().intValue());

		chunkloadersNode.removeChild("chunks");
		for (CarrotChunk chunk : chunkloaders.values()) {
			ConfigurationNode chunkNode = chunkloadersNode.getNode("chunks").getAppendedNode();
			try {
				if (!chunk.cleanup())
					chunkNode.setValue(TypeToken.of(CarrotChunk.class), chunk);
			} catch (ObjectMappingException e) {
				e.printStackTrace();
				hasErrors = true;
			}
		}
		try {
			chunkloadersLoader.save(chunkloadersNode);
		} catch (Exception e) {
			e.printStackTrace();
			hasErrors = true;
		}
		if (hasErrors)
			CarrotLoader.getLogger().error("Unable to save all CarrotChunks");
	}

	public static void chunkLoad(UUID player) {
		if (!chunkloaders.containsKey(player))
			return;
		chunkloaders.get(player).load();
	}

	public static void chunkUnLoad(UUID player) {
		if (!chunkloaders.containsKey(player))
			return;
		chunkloaders.get(player).unload();
	}

	public static boolean addChunk(UUID player, Location<World> pos) {
		if (!chunkloaders.containsKey(player))
			chunkloaders.put(player, new CarrotChunk(player));
		boolean ret = chunkloaders.get(player).addChunk(pos.getExtent().getUniqueId(), pos.getChunkPosition());
		save();
		return ret;
	}

	public static boolean delChunk(UUID player, Location<World> pos) {
		if (!chunkloaders.containsKey(player))
			return false;
		boolean ret = chunkloaders.get(player).delChunk(pos.getExtent().getUniqueId(), pos.getChunkPosition());
		if (chunkloaders.get(player).cleanup())
			chunkloaders.remove(player);
		save();
		return ret;
	}

	public static void clearChunks(UUID player) {
		if (!chunkloaders.containsKey(player))
			return ;
		chunkloaders.get(player).clearChunks();
		if (chunkloaders.get(player).cleanup())
			chunkloaders.remove(player);
		save();
	}

	public static int count(UUID player) {
		if (!chunkloaders.containsKey(player))
			return 0;
		return chunkloaders.get(player).count();
	}

	public static Optional<Map<UUID, List<Vector3i>>> list(UUID player) {
		if (!chunkloaders.containsKey(player))
			return Optional.empty();
		return chunkloaders.get(player).chunks();
	}

	public static int getMaxChunkloaders(UUID uuid) {
		return maxChunkloaders.getOrDefault(uuid, 0);
	}

	public static void setMaxChunkloaders(UUID uuid, int value) {
		if (value <= 0)
			maxChunkloaders.remove(uuid);
		else
			maxChunkloaders.put(uuid, value);
		save();
	}

	public static List<UUID> getOwners(UUID world, Vector3i coord) {
		List<UUID> owners = new ArrayList<>();
		for (Entry<UUID, CarrotChunk> entry : chunkloaders.entrySet()) {
			if (entry.getValue().isLoaded(world, coord).isPresent())
				owners.add(entry.getKey());
		}
		return owners;
	}

	public static boolean isOwner(UUID world, Vector3i coord, UUID player) {
		if (!chunkloaders.containsKey(player))
			return false;
		return chunkloaders.get(player).isLoaded(world, coord).isPresent();
	}

	public static Optional<String> getPlayerName(UUID uuid)
	{
		Optional<Player> optPlayer = Sponge.getServer().getPlayer(uuid);
		if (optPlayer.isPresent())
		{
			return Optional.of(optPlayer.get().getName());
		}
		try
		{
			return Optional.of(Sponge.getServer().getGameProfileManager().get(uuid).get().getName().get());
		}
		catch (Exception e)
		{
			return Optional.empty();
		}
	}

	public static boolean showChunk(UUID target, Player player) {
		if (!chunkloaders.containsKey(target))
			return false;
		chunkloaders.get(target).seeChunks(player);
		return true;
	}

}
