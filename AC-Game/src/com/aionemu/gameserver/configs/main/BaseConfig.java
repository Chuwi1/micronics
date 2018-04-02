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
 *  You should have received a copy of the GNU General Public License
 *  along with Aion-Lightning.
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

/**
 * @Author Lyras
 */
public class BaseConfig {
    /**
     * Base Configs
     */
    @Property(key="gameserver.base.buff.enable", defaultValue = "false")
    public static boolean BASE_BUFF_ENABLED;
    
    @Property(key="gameserver.base.reward.enable", defaultValue = "false")
    public static boolean BASE_REWARD_ENABLED;
    
    @Property(key="gameserver.base.assault.min.delay", defaultValue="15") // 15 = 15min
    public static int ASSAULT_MIN_DELAY;
    
    @Property(key="gameserver.base.assault.min.delay", defaultValue="20") // 20 = 20min
    public static int ASSAULT_MAX_DELAY;
    
    @Property(key="gameserver.base.boss.spawn.min.delay", defaultValue="30") // 30 = 30min
    public static int BOSS_SPAWN_MIN_DELAY;
    
    @Property(key="gameserver.base.boss.spawn.min.delay", defaultValue="45") // 240 = 4 hours
    public static int BOSS_SPAWN_MAX_DELAY;
    
    @Property(key="gameserver.base.buff.elyo.enable", defaultValue = "false")
    public static boolean BASE_BUFF_EYLO_ENABLED;    
    @Property(key="gameserver.base.buff.elyo.code", defaultValue="12115") // 15 = 15min
    public static int BASE_BUFF_CODEID_ELYO;
    
    @Property(key="gameserver.base.buff.asmo.enable", defaultValue = "false")
    public static boolean BASE_BUFF_ASMO_ENABLED;
    @Property(key="gameserver.base.buff.asmo.code", defaultValue="12115") // 15 = 15min
    public static int BASE_BUFF_CODEID_ASMO;
}
