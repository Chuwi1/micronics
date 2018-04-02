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
package com.aionemu.gameserver.services.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.ai2.eventcallback.OnDieEventCallback;
import com.aionemu.gameserver.configs.main.BaseConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.TemporaryPlayerTeam;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.BaseService;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 *
 * @author Source
 */
@SuppressWarnings("rawtypes")
public class BossDeathListener extends OnDieEventCallback {

    private static final Logger log = LoggerFactory.getLogger(BossDeathListener.class);

    private final Base<?> base;

    public BossDeathListener(Base base) {
        this.base = base;
    }

    @Override
    public void onBeforeDie(AbstractAI obj) {
		Race race = null;
		Npc boss = base.getBoss();
		AionObject winner = base.getBoss().getAggroList().getMostDamage();
		if (winner instanceof Creature) {
			final Creature kill = (Creature) winner;
			if (BaseConfig.BASE_BUFF_ENABLED) {
				applyBaseBuff();
			}
			if (BaseConfig.BASE_REWARD_ENABLED) {
				giveBaseRewardsToPlayers((Player) kill); 
			}
			if (kill.getRace().isPlayerRace()) {
				base.setRace(kill.getRace());
				race = kill.getRace();
			}
			announceCapture(null, kill);
		} else if (winner instanceof TemporaryPlayerTeam) {
			final TemporaryPlayerTeam team = (TemporaryPlayerTeam) winner;
			if (BaseConfig.BASE_BUFF_ENABLED) {
				applyBaseBuff();
			}
			if (BaseConfig.BASE_REWARD_ENABLED) {
				giveBaseRewardsToPlayers((Player) winner);
			}
			if (team.getRace().isPlayerRace()) {
				base.setRace(team.getRace());
				race = team.getRace();
			}
			announceCapture(team, null);
		} else {
		   base.setRace(Race.NPC);
		}
		BaseService.getInstance().capture(base.getId(), base.getRace());
		log.info("Legat kill ! BOSS: " + boss + " in BaseId: " + base.getBaseLocation().getId() + "killed by RACE: " + race);
	}
    
    @Override
    public void onAfterDie(AbstractAI obj) {
    }
    
    public void announceCapture(final TemporaryPlayerTeam team, final Creature kill) {
        final String baseName = base.getBaseLocation().getName();
        World.getInstance().doOnAllPlayers(new Visitor<Player>() {
            @Override
            public void visit(Player player) {
                if (team != null && kill == null) {
					//%0 succeeded in conquering %1.
                	//1403136 Custom String Client
                    PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1403136, team.getRace().getRaceDescriptionId(), baseName));
                } else {
					//%0 succeeded in conquering %1.
                	//1403136 Custom String Client
                    PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1403136, kill.getRace().getRaceDescriptionId(), baseName));
                }
            }
        });
    }
    
    public void applyBaseBuff() {
		World.getInstance().doOnAllPlayers(new Visitor<Player>() {
			@Override
			public void visit(Player player) {
				if (player.getCommonData().getRace() == Race.ELYOS) {
					if (BaseConfig.BASE_BUFF_EYLO_ENABLED) {
					SkillEngine.getInstance().applyEffectDirectly(BaseConfig.BASE_BUFF_CODEID_ELYO, player, player, 0); //Kaisinel's Bane.
					//The power of Kaisinel's Protection surrounds you.
					PacketSendUtility.playerSendPacketTime(player, SM_SYSTEM_MESSAGE.STR_MSG_WEAK_RACE_BUFF_LIGHT_GAIN, 10000);
					}
				} else if (player.getCommonData().getRace() == Race.ASMODIANS) {
					if (BaseConfig.BASE_BUFF_ASMO_ENABLED) {
					SkillEngine.getInstance().applyEffectDirectly(BaseConfig.BASE_BUFF_CODEID_ASMO, player, player, 0); //Marchutan's Bane.
					//The power of Marchutan's Protection surrounds you.
					PacketSendUtility.playerSendPacketTime(player, SM_SYSTEM_MESSAGE.STR_MSG_WEAK_RACE_BUFF_DARK_GAIN, 10000);
					}
				}
			}
		});
	}
    
    protected void giveBaseRewardsToPlayers(Player player) {
		switch (player.getWorldId()) {
			case 210020000: //Eltnen.
			case 210040000: //Heiron.
			case 220020000: //Morheim.
			case 220040000: //Beluslan.
		        //HTMLService.sendGuideHtml(player, "adventurers_base1");
				ItemService.addItem(player, 186000242, 1);
				ItemService.addItem(player, 166030009, 1);
				AbyssPointsService.addGp(player, 200);
			break;
			case 600090000: //Kaldor.
			case 600100000: //Levinshor.
			case 600050000: //katalam
			case 600060000: //Danaria
		        //HTMLService.sendGuideHtml(player, "adventurers_base2");
				ItemService.addItem(player, 186000242, 1);
				ItemService.addItem(player, 166030009, 1);
				AbyssPointsService.addGp(player, 200);
			break;
			case 400020000: //Belus.
			case 400040000: //Aspida.
			case 400050000: //Atanatos.
			case 400060000: //Disillon.
		        //HTMLService.sendGuideHtml(player, "adventurers_base3");
				ItemService.addItem(player, 186000242, 1);
				ItemService.addItem(player, 166030009, 1);
				AbyssPointsService.addGp(player, 200);
			break;
		}
	}

}
