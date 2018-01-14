package com.carrot.carrotloader.listener;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import com.carrot.carrotloader.LoaderData;

public class PlayerConnexionListener {

	@Listener(order=Order.LATE)
	public void onPlayerJoin(ClientConnectionEvent.Join event)
	{
		LoaderData.chunkLoad(event.getTargetEntity().getUniqueId());
	}
	
	@Listener(order=Order.LATE)
	public void onPlayerLeft(ClientConnectionEvent.Disconnect event)
	{
		LoaderData.chunkUnLoad(event.getTargetEntity().getUniqueId());
	}
}
