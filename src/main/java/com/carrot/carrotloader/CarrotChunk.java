package com.carrot.carrotloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.ChunkTicketManager.PlayerLoadingTicket;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class CarrotChunk {
	@Setting
	private UUID owner = null;
	@Setting
	private Map<UUID, List<Vector3i>> chunks = null;

	private Map<UUID, PlayerLoadingTicket> tickets = null;

	public CarrotChunk() {

	}

	public CarrotChunk(UUID player) {
		owner = player;
		chunks = new HashMap<>();
	}

	public UUID getOwner() {
		return owner;
	}

	public boolean cleanup() {
		for (UUID worldUUID : chunks.keySet()) {
			Optional<World> world = Sponge.getServer().getWorld(worldUUID);
			if (!world.isPresent()) {
				chunks.remove(worldUUID);
			}
		}
		return chunks.isEmpty();
	}

	public Optional<Vector3i> isLoaded(UUID world, Vector3i coord, int range) {
		if (!chunks.containsKey(world))
			return Optional.empty();
		if (chunks.get(world).contains(coord))
			return Optional.of(coord);
		for (Vector3i c : chunks.get(world)) {
			for (int i = -range; i <= range; ++i)
				for (int j = -range; j <= range; ++j)
					if (coord.equals(c.add(i, 0, j)))
						return Optional.of(c);
		}
		return Optional.empty();
	}

	public Optional<Vector3i> isLoaded(UUID world, Vector3i coord) {
		return isLoaded(world, coord, 1);
	}

	public void load() {
		for (Entry<UUID, List<Vector3i>> chunk : chunks.entrySet()) {			
			Optional<World> world = Sponge.getServer().getWorld(chunk.getKey());
			if (!world.isPresent()) {
				continue ;
			}
			if (tickets == null)
				tickets = new HashMap<>();
			if (!tickets.containsKey(chunk.getKey())) {
				Optional<PlayerLoadingTicket> ticket = Sponge.getServer().getChunkTicketManager().createPlayerTicket(CarrotLoader.getInstance(), world.get(), owner);
				if (ticket.isPresent()) {
					tickets.put(chunk.getKey(), ticket.get());
				}
			}
			if (tickets.containsKey(chunk.getKey())) {
				tickets.get(chunk.getKey()).setNumChunks(chunk.getValue().size() * 9);
				for (Vector3i coord : chunk.getValue()) {
					for (int i = -1; i < 2; ++i)
						for (int j = -1; j < 2; ++j)
							tickets.get(chunk.getKey()).forceChunk(coord.add(i, 0, j));
				}
			}
		}
	}

	public void unload() {
		if (tickets == null)
			return;
		for (PlayerLoadingTicket ticket : tickets.values())
			ticket.release();
		tickets.clear();
	}

	public void reload() {
		unload();
		load();
	}

	public boolean addChunk(UUID world, Vector3i chunk) {
		if (isLoaded(world, chunk, 2).isPresent())
			return false;
		if (!chunks.containsKey(world))
			chunks.put(world, new ArrayList<>());
		chunks.get(world).add(chunk);
		reload();
		return true;
	}

	public boolean delChunk(UUID world, Vector3i chunk) {
		Optional<Vector3i> c = isLoaded(world, chunk);
		if (!c.isPresent())
			return false;
		chunks.get(world).remove(c.get());
		if (chunks.get(world).isEmpty())
			chunks.remove(world);
		reload();
		return true;
	}

	public void clearChunks() {
		for (List<Vector3i> coords : chunks.values())
			coords.clear();
		chunks.clear();
		unload();
	}

	public int count() {
		int ret = 0;
		for (List<Vector3i> chunk : chunks.values()) {
			ret += chunk.size();
		}
		return ret;
	}

	public Optional<Map<UUID, List<Vector3i>>> chunks() {
		if (chunks.isEmpty())
			return Optional.empty();
		return Optional.of(chunks);
	}

	public Optional<List<Vector3i>> chunks(UUID world) {
		if (chunks.isEmpty() || !chunks.containsKey(world))
			return Optional.empty();
		return Optional.of(chunks.get(world));
	}

	public void seeChunks(Player player) {
		ParticleEffect particule = ParticleEffect.builder().type(ParticleTypes.HAPPY_VILLAGER).quantity(1).build();
		if (!chunks.containsKey(player.getWorld().getUniqueId()))
			return ;
		
		for (Vector3i chunk : chunks.get(player.getWorld().getUniqueId())) {
			for (int i = -1; i < 2; ++i) {
				for (int j = -1; j < 2; ++j) {
					Vector3d lchunk = chunk.add(i, 0, j).mul(16).add(0, player.getLocation().getY() + 2, 0).toDouble();
					for (int x = 0; x < 16; ++x) {
						for (int z = 0; z < 16; ++z) {
							for (int y = -3; y < 4; ++y)
								player.spawnParticles(particule, lchunk.add(x, y, z));
							
							// WHY IS THAT CRAP NOT WORKING !?
//							BlockRay<World> blockRay = BlockRay.from(player.getLocation().getExtent(), lchunk.add(x, 0, z)).direction(new Vector3d(0, -1, 0)).stopFilter(BlockRay.onlyAirFilter()).build();
//							Optional<BlockRayHit<World>> block = blockRay.end();
//							if (block.isPresent()) {
//								player.spawnParticles(particule, block.get().getPosition());
//							}
						}
					}
				}
			}
		}
	}

}
