package com.carrot.carrotloader;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.ChunkTicketManager.Callback;
import org.spongepowered.api.world.ChunkTicketManager.LoadingTicket;
import org.spongepowered.api.world.World;

import com.carrot.carrotloader.command.LoaderAdminExecutor;
import com.carrot.carrotloader.command.LoaderListExecutor;
import com.carrot.carrotloader.command.LoaderLoadExecutor;
import com.carrot.carrotloader.command.LoaderMainExecutor;
import com.carrot.carrotloader.command.LoaderManagerExecutor;
import com.carrot.carrotloader.command.LoaderSeeExecutor;
import com.carrot.carrotloader.command.LoaderUnLoadAllExecutor;
import com.carrot.carrotloader.command.LoaderUnLoadExecutor;
import com.carrot.carrotloader.command.LoaderWikiExecutor;
import com.carrot.carrotloader.command.PlayerNameElement;
import com.carrot.carrotloader.listener.PlayerConnexionListener;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

@Plugin(id = "carrotloader", name = "CarrotLoader", authors={"Carrot"}, url="https://github.com/TheoKah/CarrotLoader")
public class CarrotLoader {
	private File rootDir;

	private static CarrotLoader plugin;

	@Inject
	private Logger logger;

	@Inject
	@ConfigDir(sharedRoot = true)
	private File defaultConfigDir;

	@Listener
	public void onInit(GameInitializationEvent event) throws IOException
	{
		plugin = this;

		rootDir = new File(defaultConfigDir, "carrotloader");

		LoaderData.init(rootDir);
	}

	@Listener
	public void onStart(GameStartedServerEvent event)
	{
		LoaderData.load();

		CommandSpec loaderWiki = CommandSpec.builder()
				.description(Text.of("Displays a link to the loader wiki"))
				.executor(new LoaderWikiExecutor())
				.build();

		CommandSpec loaderLoad = CommandSpec.builder()
				.description(Text.of("Add a chunk to your list of loaded chunks"))
				.arguments(GenericArguments.optional(GenericArguments.user(Text.of("player"))))
				.executor(new LoaderLoadExecutor())
				.build();

		CommandSpec loaderUnload = CommandSpec.builder()
				.description(Text.of("Remove a chunk from your list of loaded chunks"))
				.arguments(GenericArguments.optional(GenericArguments.user(Text.of("player"))))
				.executor(new LoaderUnLoadExecutor())
				.build();
		
		CommandSpec loaderSee = CommandSpec.builder()
				.description(Text.of("Spawn particles in the area being loaded"))
				.arguments(GenericArguments.optional(GenericArguments.user(Text.of("player"))))
				.executor(new LoaderSeeExecutor())
				.build();

		CommandSpec loaderUnloadAll = CommandSpec.builder()
				.description(Text.of("Remove all chunks from your list of loaded chunks"))
				.arguments(GenericArguments.optional(GenericArguments.user(Text.of("player"))))
				.executor(new LoaderUnLoadAllExecutor())
				.build();

		CommandSpec loaderList = CommandSpec.builder()
				.description(Text.of("List all chunks from your list of loaded chunks"))
				.arguments(GenericArguments.optional(GenericArguments.user(Text.of("player"))))
				.executor(new LoaderListExecutor())
				.build();

		CommandSpec loaderAdmin = CommandSpec.builder()
				.permission("carrotloader.admin")
				.description(Text.of("List all loaded chunks"))
				.arguments(GenericArguments.optional(GenericArguments.user(Text.of("player"))))
				.executor(new LoaderAdminExecutor())
				.build();
		
		CommandSpec loaderManager = CommandSpec.builder()
				.permission("carrotloader.admin")
				.description(Text.of("Manage chunkloader allowed"))
				.arguments(GenericArguments.choices(Text.of("give|take|set"),
						ImmutableMap.<String, String> builder()
						.put("give", "give")
						.put("take", "take")
						.put("set", "set")
						.build()),
						new PlayerNameElement(Text.of("player")),
						GenericArguments.integer(Text.of("amount")))
				.executor(new LoaderManagerExecutor())
				.build();

		CommandSpec loaderMain = CommandSpec.builder()
				.description(Text.of("Main CarrotLoader command"))
				.executor(new LoaderMainExecutor())
				.child(loaderWiki, "help", "?", "wiki", "how", "howto", "h")
				.child(loaderLoad, "load", "add")
				.child(loaderSee, "see", "mark", "show", "visual")
				.child(loaderUnload, "unload", "delete", "remove", "del")
				.child(loaderUnloadAll, "unloadall", "deleteall", "removeall", "clear")
				.child(loaderList, "list", "l")
				.child(loaderAdmin, "admin", "adm")
				.child(loaderManager, "manager", "shop")
				.build();

		Sponge.getCommandManager().register(plugin, loaderAdmin, "chunkadmin", "chunkadm");
		Sponge.getCommandManager().register(plugin, loaderLoad, "chunkload", "carrotload");
		Sponge.getCommandManager().register(plugin, loaderSee, "chunksee", "carrotsee");
		Sponge.getCommandManager().register(plugin, loaderUnload, "chunkunload", "carrotunload");
		Sponge.getCommandManager().register(plugin, loaderUnloadAll, "chunkclear", "chunkunloadall", "carrotunloadall");
		Sponge.getCommandManager().register(plugin, loaderList, "chunklist");
		Sponge.getCommandManager().register(plugin, loaderMain, "chunkloader", "cl", "carrotloader", "carrotchunk");

		Sponge.getEventManager().registerListeners(this, new PlayerConnexionListener());
				
		Sponge.getServer().getChunkTicketManager().registerCallback(this, new Callback() {
			
			@Override
			public void onLoaded(ImmutableList<LoadingTicket> tickets, World world) {
				System.out.println("CB world: " + world.getName());
				for (LoadingTicket ticket : tickets) {
					System.out.println("getNumChunks" + ticket.getNumChunks());
					System.out.println("getMaxNumChunks" + ticket.getMaxNumChunks());
					System.out.println("getPlugin" + ticket.getPlugin());
					System.out.println("chunks: ");
					for (Vector3i coord : ticket.getChunkList()) {
						System.out.println("- " + coord.getX() + " " + coord.getZ());
						
					}
				}
			}
		});
	}

	@Listener
	public void onStop(GameStoppingServerEvent event) {
		LoaderData.unload();
	}

	public static CarrotLoader getInstance()
	{
		return plugin;
	}

	public static Logger getLogger()
	{
		return getInstance().logger;
	}

	public static Cause getCause()
	{
		return Cause.source(Sponge.getPluginManager().fromInstance(CarrotLoader.getInstance()).get()).build();
	}
}
