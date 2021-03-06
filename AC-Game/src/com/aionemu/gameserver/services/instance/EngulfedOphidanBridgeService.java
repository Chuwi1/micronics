/*
 * This file is part of Encom. **ENCOM FUCK OTHER SVN**
 *
 *  Encom is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Encom is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser Public License
 *  along with Encom.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.services.instance;

import com.aionemu.commons.network.util.ThreadPoolManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import javolution.util.FastList;

import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.AutoGroupService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/****/
/** Author Rinzler (Encom)
/****/

public class EngulfedOphidanBridgeService
{
	private static final Logger log = LoggerFactory.getLogger(EngulfedOphidanBridgeService.class);
    private boolean registerAvailable;
    private final FastList<Integer> playersWithCooldown = FastList.newInstance();
    public static final byte minLevel = 60, capLevel = 66;
    public static final int maskId = 108;
    public static final int InstanceMapId = 301310000;
    
	public void initEngulfedOphidan() {
		String[] times = AutoGroupConfig.OPHIDAN_TIMES.split("\\|");
        for (String cron : times) {
            CronService.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    startOphidanRegistration();
                }
            }, cron);
            log.info("Scheduled Engulfed Ophidan Bridge based on cron expression: " + cron + " Duration: " + AutoGroupConfig.OPHIDAN_TIMER + " in minutes");
        }
	}
	
	private void startUregisterOphidanTask() {
        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                registerAvailable = false;
                playersWithCooldown.clear();
                AutoGroupService.getInstance().unRegisterInstance(maskId);
                Iterator<Player> iter = World.getInstance().getPlayersIterator();
                while (iter.hasNext()) {
                    Player player = iter.next();
                    if (player.getLevel() > minLevel) {
                         PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon, true));
                    }
                }
            }
        }, AutoGroupConfig.OPHIDAN_TIMER * 60 * 1000);
    }
	
	private void startOphidanRegistration() {
        this.registerAvailable = true;
        startUregisterOphidanTask();
        Iterator<Player> iter = World.getInstance().getPlayersIterator();
        while (iter.hasNext()) {
            Player player = iter.next();
            if (player.getLevel() > minLevel && player.getLevel() < capLevel) {
                PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon));
				//You can now participate in the Ophidan Bridge battle.
                PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_OPHIDAN_WAR);
            }
        }
    }
	
	public boolean isOphidanAvailable() {
		return this.registerAvailable;
	}
	
	public byte getInstanceMaskId(Player player) {
        int level = player.getLevel();
        if (level < minLevel || level >= capLevel) {
            return 0;
        }
        return maskId;
    }
	
    public void addCoolDown(Player player) {
        this.playersWithCooldown.add(player.getObjectId());
    }
	
    public boolean hasCoolDown(Player player) {
        return this.playersWithCooldown.contains(player.getObjectId());
    }
	
    public void showWindow(Player player, byte instanceMaskId) {
    	if (!playersWithCooldown.contains(player.getObjectId())) {
            PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId));
        }
    }
	
	private static class SingletonHolder {
		protected static final EngulfedOphidanBridgeService instance = new EngulfedOphidanBridgeService();
	}
	
	public static EngulfedOphidanBridgeService getInstance() {
		return SingletonHolder.instance;
	}
	
	private boolean isInInstance(Player player) {
    	if (player.isInInstance()) {
    		return true;
    	}
        return false;
    }
	
	public boolean canPlayerJoin(Player player) {
		if (registerAvailable && player.getLevel() > minLevel && player.getLevel() < capLevel && !hasCoolDown(player) && !isInInstance(player)) {
			 return true;
		}
		return false;
    }
}