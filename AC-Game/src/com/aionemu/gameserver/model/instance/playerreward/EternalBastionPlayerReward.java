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
package com.aionemu.gameserver.model.instance.playerreward;

/****/
/** Author Rinzler (Encom)
/****/

public class EternalBastionPlayerReward extends InstancePlayerReward
{
	private int scoreAP;
	private int ceramium;
	private int highGradeMaterialBox;
	private int highestGradeMaterialBox;
	private int lowGradeMaterialSupportBundle;
	private int highGradeMaterialSupportBundle;
	private int highestGradeMaterialSupportBundle;
	private boolean isRewarded = false;
	
	public EternalBastionPlayerReward(Integer object) {
		super(object);
	}
	
	public boolean isRewarded() {
		return isRewarded;
	}
	public void setRewarded() {
		isRewarded = true;
	}
	
	public int getScoreAP() {
		return scoreAP;
	}
	public void setScoreAP(int ap) {
		this.scoreAP = ap;
	}
	public int getCeramium() {
		return ceramium;
	}
	public int getHighGradeMaterialBox() {
        return highGradeMaterialBox;
    }
	public int getHighestGradeMaterialBox() {
        return highestGradeMaterialBox;
    }
	public int getLowGradeMaterialSupportBundle() {
        return lowGradeMaterialSupportBundle;
    }
	public int getHighGradeMaterialSupportBundle() {
        return highGradeMaterialSupportBundle;
    }
    public int getHighestGradeMaterialSupportBundle() {
        return highestGradeMaterialSupportBundle;
    }
	
	public void setCeramium(int ceramium) {
		this.ceramium = ceramium;
	}
	public void setHighGradeMaterialBox(int highGradeMaterialBox) {
        this.highGradeMaterialBox = highGradeMaterialBox;
    }
    public void setHighestGradeMaterialBox(int highestGradeMaterialBox) {
        this.highestGradeMaterialBox = highestGradeMaterialBox;
    }
	public void setLowGradeMaterialSupportBundle(int lowGradeMaterialSupportBundle) {
        this.lowGradeMaterialSupportBundle = lowGradeMaterialSupportBundle;
    }
    public void setHighGradeMaterialSupportBundle(int highGradeMaterialSupportBundle) {
        this.highGradeMaterialSupportBundle = highGradeMaterialSupportBundle;
    }
	public void setHighestGradeMaterialSupportBundle(int highestGradeMaterialSupportBundle) {
        this.highestGradeMaterialSupportBundle = highestGradeMaterialSupportBundle;
    }
}
