package com.pengu.islands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import com.pengu.hammercore.HammerCore;
import com.pengu.hammercore.HammerCore.HCAuthor;
import com.pengu.hammercore.common.utils.BlankTeleporter;
import com.pengu.hammercore.common.utils.IOUtils;
import com.pengu.islands.commands.CommandIC;
import com.pengu.islands.proxy.CommonProxy;
import com.pengu.islands.world.WorldTypeIslands;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = InfoIC.MOD_ID, name = InfoIC.MOD_NAME, version = InfoIC.MOD_VERSION, dependencies = "required-after:hammercore")
public class IslandCraft
{
	public static WorldTypeIslands islandWorldType;
	
	@Instance
	public static IslandCraft instance;
	
	@SidedProxy(serverSide = InfoIC.PROXY_SERVER, clientSide = InfoIC.PROXY_CLIENT)
	public static CommonProxy proxy;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent e)
	{
		islandWorldType = new WorldTypeIslands();
		
		ModMetadata meta = e.getModMetadata();
		meta.autogenerated = false;
		meta.authorList = HammerCore.getHCAuthorsArray();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent e)
	{
		File ics = new File("config", InfoIC.MOD_ID + File.separator + "island.ics");
		
		if(!ics.isFile())
		{
			try(InputStream in = IslandCraft.class.getResourceAsStream("/island.ics");FileOutputStream fos = new FileOutputStream(ics))
			{
				IOUtils.pipeData(in, fos);
			} catch(Throwable err)
			{
				err.printStackTrace();
			}
		}
	}
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent e)
	{
		IslandData.data = null;
		e.registerServerCommand(new CommandIC());
	}
	
	@EventHandler
	public void serverStopping(FMLServerStoppingEvent e)
	{
		CommandIC.pending.clear();
	}
	
	@EventHandler
	public void serverStopped(FMLServerStoppedEvent e)
	{
		IslandData.data = null;
	}
	
	public static void teleportPlayer(EntityPlayerMP player, double x, double y, double z, int targetDim)
	{
		int from = player.dimension;
		
		if(from != targetDim)
		{
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			WorldServer fromDim = server.getWorld(from);
			WorldServer toDim = server.getWorld(targetDim);
			Teleporter teleporter = new BlankTeleporter(toDim);
			if(player != null)
			{
				server.getPlayerList().transferPlayerToDimension(player, targetDim, teleporter);
				if((from == 1) && (player.isEntityAlive()))
				{
					toDim.spawnEntity(player);
					toDim.updateEntityWithOptionalForce(player, false);
				}
			}
		}
		
		BlockPos pos = new BlockPos(x, y, z);
		if(!player.world.isBlockLoaded(pos))
			player.world.getChunkFromBlockCoords(pos);
		
		player.connection.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
		player.addExperienceLevel(0);
		player.fallDistance = 0.0F;
	}
}