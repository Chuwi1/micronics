/**
 * This file is part of Aion-Lightning <aion-lightning.org>.
 *
 *  Aion-Lightning is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Aion-Lightning is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details. *
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Aion-Lightning.
 *  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Credits goes to all Open Source Core Developer Groups listed below
 * Please do not change here something, ragarding the developer credits, except the "developed by XXXX".
 * Even if you edit a lot of files in this source, you still have no rights to call it as "your Core".
 * Everybody knows that this Emulator Core was developed by Aion Lightning 
 * @-Aion-Unique-
 * @-Aion-Lightning
 * @Aion-Engine
 * @Aion-Extreme
 * @Aion-NextGen
 * @Aion-Core Dev.
 */
package com.aionemu.gameserver.model.siege;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeLocationTemplate;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeReward;
import com.aionemu.gameserver.model.templates.zone.ZoneType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOVE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.zone.ZoneInstance;

import java.util.List;

/**
 * @author Source
 */
public class SourceLocation extends SiegeLocation {

    protected List<SiegeReward> siegeRewards;
    private boolean status;

    public SourceLocation() {
    }

    public SourceLocation(SiegeLocationTemplate template) {
        super(template);
        this.siegeRewards = template.getSiegeRewards() != null ? template.getSiegeRewards() : null;
    }

    public List<SiegeReward> getReward() {
        return this.siegeRewards;
    }

    public boolean isPreparations() {
        return status;
    }

    public void setPreparation(boolean status) {
        this.status = status;
    }

    @Override
    public void onEnterZone(Creature creature, ZoneInstance zone) {
        super.onEnterZone(creature, zone);
        if (isVulnerable()) {
            creature.setInsideZoneType(ZoneType.SIEGE);
        }
    }

    @Override
    public void onLeaveZone(Creature creature, ZoneInstance zone) {
        super.onLeaveZone(creature, zone);
        if (isVulnerable()) {
            creature.unsetInsideZoneType(ZoneType.SIEGE);
        }
    }

    /*
     * TODO: move to datapack
     */
    public WorldPosition getEntryPosition() {
        WorldPosition pos = new WorldPosition(getWorldId());
        switch (getLocationId()) {
            case 4011:
                pos.setXYZH(332.14316f, 854.36053f, 313.98f, (byte) 77);
                break;
            case 4021:
                pos.setXYZH(2353.9065f, 378.1945f, 237.8031f, (byte) 113);
                break;
            case 4031:
                pos.setXYZH(879.23627f, 2712.4644f, 254.25073f, (byte) 85);
                break;
            case 4041:
                pos.setXYZH(2901.2354f, 2365.0383f, 339.1469f, (byte) 39);
                break;
            case 2021: //Altar of Avarice
                pos.setXYZH(777.9701f, 1698.6073f, 323.0f, (byte) 20);
                break;
            case 2011: //Temple of Scale
                pos.setXYZH(1910.7449f, 1922.6559f, 290.4681f, (byte) 54);
                break;
            case 3011: //Vorgaltem Citadel
                pos.setXYZH(1173.7444f, 1239.4143f, 265.734f, (byte) 96);
                break;
            case 3021: //Crison Temple
                pos.setXYZH(1749.1307f, 1375.5408f, 322.19568f, (byte) 111);
                break;
            case 5011: //Sillus
                pos.setXYZH(1752.0148f, 1386.0483f, 236.0021f, (byte) 23);
                break;
            case 6011: //Silona
                pos.setXYZH(1517.9255f, 1108.3562f, 54.25f, (byte) 91);
                break;
            case 6021: //Pradeth
                pos.setXYZH(2435.5393f, 2469.033f, 230.25412f, (byte) 19);
                break;
            case 1221: //Krotan
                pos.setXYZH(2582.004f, 1648.8265f, 2879.1743f, (byte) 53);
                break;
            case 1231: //Kysis
                pos.setXYZH(2080.2422f, 2470.489f, 2899.7234f, (byte) 109);
                break;
            case 1241: //Miren
                pos.setXYZH(1451.1864f, 1956.0186f, 2899.7734f, (byte) 2);
                break;
        }

        return pos;
    }

    @Override
    public void clearLocation() {
        for (Player player : getPlayers().values()) {
            WorldPosition pos = getEntryPosition();
            World.getInstance().updatePosition(player, pos.getX(), pos.getY(), pos.getZ(), player.getHeading());
            PacketSendUtility.broadcastPacketAndReceive(player, new SM_MOVE(player));
        }
    }
}
