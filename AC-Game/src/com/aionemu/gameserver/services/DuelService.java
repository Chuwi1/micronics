package com.aionemu.gameserver.services;

import java.util.concurrent.Future;

import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.DuelResult;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.summons.SummonMode;
import com.aionemu.gameserver.model.summons.UnsummonType;
import com.aionemu.gameserver.model.templates.zone.ZoneType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DUEL;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.summons.SummonsService;
import com.aionemu.gameserver.skillengine.model.SkillTargetSlot;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;
import com.aionemu.gameserver.world.zone.ZoneInstance;

public class DuelService {

    private static Logger log = LoggerFactory.getLogger(DuelService.class);
    
    private FastMap<Integer, Integer> duels;
    private FastMap<Integer, Future<?>> drawTasks;

    public static final DuelService getInstance() {
        return SingletonHolder.instance;
    }
    
    private DuelService() {
        this.duels = new FastMap<Integer, Integer>().shared();
        this.drawTasks = new FastMap<Integer, Future<?>>().shared();
        log.info("[Duel Service] Has Started !");
    }

    /**
     * Send the duel request to the owner
     *
     * @param requester the player who requested the duel
     * @param responder the player who respond to duel request
     */
    public void onDuelRequest(Player requester, Player responder) {
    	if (requester.isInsideZoneType(ZoneType.PVP) || responder.isInsideZoneType(ZoneType.PVP)) {
			PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_DUEL_PARTNER_INVALID(responder.getName()));
			return;
		} if (isDueling(requester.getObjectId()) || isDueling(responder.getObjectId())) {
			PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_DUEL_HE_REJECT_DUEL(responder.getName()));
			return;
		} for (ZoneInstance zone : responder.getPosition().getMapRegion().getZones(responder)) {
			if (((!zone.isOtherRaceDuelsAllowed()) && (!responder.getRace().equals(requester.getRace()))) || ((!zone.isSameRaceDuelsAllowed()) && (responder.getRace().equals(requester.getRace())))) {
				PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_MSG_DUEL_CANT_IN_THIS_ZONE);
				return;
			}
		}        
		RequestResponseHandler rrh = new RequestResponseHandler(requester) {
			@Override
			public void denyRequest(Creature requester, Player responder) {
				rejectDuelRequest((Player) requester, responder);
			}
			@Override
			public void acceptRequest(Creature requester, Player responder) {
				startDuel((Player) requester, responder);
			}
		};
        responder.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_DUEL_DO_YOU_ACCEPT_REQUEST, rrh);
        PacketSendUtility.sendPacket(responder, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_DUEL_DO_YOU_ACCEPT_REQUEST, 0, 0, requester.getName()));
        PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_DUEL_REQUESTED(requester.getName()));
    }

    /**
     * Asks confirmation for the duel request
     *
     * @param requester the player whose the duel was requested
     * @param responder the player whose the duel was responded
     */
    public void confirmDuelWith(Player requester, Player responder) {
    	if (requester.isEnemy(responder)) {
			return;
		}
		RequestResponseHandler rrh = new RequestResponseHandler(responder) {
			@Override
			public void denyRequest(Creature requester, Player responder) {
			}
			@Override
			public void acceptRequest(Creature requester, Player responder) {
				cancelDuelRequest(responder, (Player) requester);
			}
		};
		requester.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_DUEL_DO_YOU_WITHDRAW_REQUEST, rrh);
		PacketSendUtility.sendPacket(requester, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_DUEL_DO_YOU_WITHDRAW_REQUEST, 0, 0, responder.getName()));
		PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_DUEL_REQUEST_TO_PARTNER(responder.getName()));
    }

    /**
     * Rejects the duel request
     *
     * @param requester the duel requester
     * @param responder the duel responder
     */
    private void rejectDuelRequest(Player requester, Player responder) {
        log.debug("[Duel] Player " + responder.getName() + " rejected duel request from " + requester.getName());
        PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_DUEL_HE_REJECT_DUEL(responder.getName()));
        PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_DUEL_REJECT_DUEL(requester.getName()));
    }

    /**
     * Cancels the duel request
     *
     * @param target    the duel target
     * @param requester
     */
    private void cancelDuelRequest(Player owner, Player target) {
        log.debug("[Duel] Player " + owner.getName() + " cancelled his duel request with " + target.getName());
        PacketSendUtility.sendPacket(target, SM_SYSTEM_MESSAGE.STR_DUEL_REQUESTER_WITHDRAW_REQUEST(owner.getName()));
        PacketSendUtility.sendPacket(owner, SM_SYSTEM_MESSAGE.STR_DUEL_WITHDRAW_REQUEST(target.getName()));
    }

    /**
     * Starts the duel
     *
     * @param requester the player to start duel with
     * @param responder the other player
     */
    private void startDuel(final Player requester, final Player responder) {
		PacketSendUtility.sendPacket(requester, SM_DUEL.SM_DUEL_STARTED(responder.getObjectId()));
		PacketSendUtility.sendPacket(responder, SM_DUEL.SM_DUEL_STARTED(requester.getObjectId()));
		startDuelMsg(requester, responder);
		createDuel(requester.getObjectId(), responder.getObjectId());
		createTask(requester, responder);
	}
	
	private void startDuelMsg(final Player player1, final Player player2) {
        World.getInstance().doOnAllPlayers(new Visitor<Player>() {
            @Override
            public void visit(Player object) {
                if (MathUtil.isInRange(player1, object, 100)) {
				    //A duel between %0 and %1 has started.
				    PacketSendUtility.sendPacket(object, SM_SYSTEM_MESSAGE.STR_DUEL_START_BROADCAST(player2.getName(), player1.getName()));
				}
            }
        });
    }
	
	private void loseDuelMsg(final Player player1, final Player player2) {
        World.getInstance().doOnAllPlayers(new Visitor<Player>() {
            @Override
            public void visit(Player object) {
                if (MathUtil.isInRange(player1, object, 100)) {
				    //%0 defeated %1 in a duel.
				    PacketSendUtility.sendPacket(object, SM_SYSTEM_MESSAGE.STR_DUEL_STOP_BROADCAST(player2.getName(), player1.getName()));
				}
            }
        });
    }
	
	private void drawDuelMsg(final Player player1, final Player player2) {
        World.getInstance().doOnAllPlayers(new Visitor<Player>() {
            @Override
            public void visit(Player object) {
                if (MathUtil.isInRange(player1, object, 100)) {
				    //The duel between %0 and %1 was a draw.
				    PacketSendUtility.sendPacket(object, SM_SYSTEM_MESSAGE.STR_DUEL_TIMEOUT_BROADCAST(player2.getName(), player1.getName()));
				}	
            }
        });
    }

    /**
     * This method will make the selected player lose the duel
     *
     * @param player
     */
    public void loseDuel(final Player player) {
        if (!isDueling(player.getObjectId())) {
            return;
        }
        int opponnentId = duels.get(player.getObjectId());

        player.getAggroList().clear();

        final Player opponent = World.getInstance().findPlayer(opponnentId);

        if (opponent != null) {
            /**
             * all debuffs are removed from winner, but buffs will remain Stop
             * casting or skill use
             */
            opponent.getEffectController().removeAbnormalEffectsByTargetSlot(SkillTargetSlot.DEBUFF);
            opponent.getController().cancelCurrentSkill();
            opponent.getAggroList().clear();

            /**
             * cancel attacking winner by summon
             */
            if (player.getSummon() != null) {
                //if (player.getSummon().getTarget().isTargeting(opponnentId))
                SummonsService.doMode(SummonMode.GUARD, player.getSummon(), UnsummonType.UNSPECIFIED);
            }

            /**
             * cancel attacking loser by summon
             */
            if (opponent.getSummon() != null) {
                //if (opponent.getSummon().getTarget().isTargeting(player.getObjectId()))
                SummonsService.doMode(SummonMode.GUARD, opponent.getSummon(), UnsummonType.UNSPECIFIED);
            }

            /**
             * cancel attacking winner by summoned object
             */
            if (player.getSummonedObj() != null) {
                player.getSummonedObj().getController().cancelCurrentSkill();
            }

            /**
             * cancel attacking loser by summoned object
             */
            if (opponent.getSummonedObj() != null) {
                opponent.getSummonedObj().getController().cancelCurrentSkill();
            }
            loseDuelMsg(player, opponent);
			PacketSendUtility.sendPacket(opponent, new SM_QUEST_ACTION(0, 0));
            PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(0, 0));
            PacketSendUtility.sendPacket(opponent, SM_DUEL.SM_DUEL_RESULT(DuelResult.DUEL_WON, player.getName()));
            PacketSendUtility.sendPacket(player, SM_DUEL.SM_DUEL_RESULT(DuelResult.DUEL_LOST, opponent.getName()));
        } else {
            log.warn("CHECKPOINT : duel opponent is already out of world");
        }

        removeDuel(player.getObjectId(), opponnentId);
    }

    public void loseArenaDuel(Player player) {
        if (!isDueling(player.getObjectId())) {
            return;
        }

        /**
         * all debuffs are removed from loser Stop casting or skill use
         */
        player.getEffectController().removeAbnormalEffectsByTargetSlot(SkillTargetSlot.DEBUFF);
        player.getController().cancelCurrentSkill();

        int opponnentId = duels.get(player.getObjectId());
        Player opponent = World.getInstance().findPlayer(opponnentId);

        if (opponent != null) {
            /**
             * all debuffs are removed from winner, but buffs will remain Stop
             * casting or skill use
             */
            opponent.getEffectController().removeAbnormalEffectsByTargetSlot(SkillTargetSlot.DEBUFF);
            opponent.getController().cancelCurrentSkill();
        } else {
            log.warn("CHECKPOINT : duel opponent is already out of world");
        }

        removeDuel(player.getObjectId(), opponnentId);
    }

    private void createTask(final Player requester, final Player responder) {
        // Schedule for draw
        Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (isDueling(requester.getObjectId(), responder.getObjectId())) {
                	drawDuelMsg(requester, responder);
                    PacketSendUtility.sendPacket(requester, SM_DUEL.SM_DUEL_RESULT(DuelResult.DUEL_DRAW, requester.getName()));
                    PacketSendUtility.sendPacket(responder, SM_DUEL.SM_DUEL_RESULT(DuelResult.DUEL_DRAW, responder.getName()));
                    removeDuel(requester.getObjectId(), responder.getObjectId());
                }
            }
        }, 5 * 60 * 1000); // 5 minutes battle retail like
        PacketSendUtility.sendPacket(requester, new SM_QUEST_ACTION(0, 300));
        PacketSendUtility.sendPacket(responder, new SM_QUEST_ACTION(0, 300));
        drawTasks.put(requester.getObjectId(), task);
        drawTasks.put(responder.getObjectId(), task);
    }

    /**
     * @param playerObjId
     * @return true of player is dueling
     */
    public boolean isDueling(int playerObjId) {
        return (duels.containsKey(playerObjId) && duels.containsValue(playerObjId));
    }

    /**
     * @param playerObjId
     * @param targetObjId
     * @return true of player is dueling
     */
    public boolean isDueling(int playerObjId, int targetObjId) {
        return duels.containsKey(playerObjId) && duels.get(playerObjId) == targetObjId;
    }

    /**
     * @param requesterObjId
     * @param responderObjId
     */
    public void createDuel(int requesterObjId, int responderObjId) {
        duels.put(requesterObjId, responderObjId);
        duels.put(responderObjId, requesterObjId);
    }

    /**
     * @param requesterObjId
     * @param responderObjId
     */
    private void removeDuel(int requesterObjId, int responderObjId) {
        duels.remove(requesterObjId);
        duels.remove(responderObjId);
        removeTask(requesterObjId);
        removeTask(responderObjId);
        Player requester = World.getInstance().findPlayer(requesterObjId);
        Player responder = World.getInstance().findPlayer(responderObjId);
        PacketSendUtility.sendPacket(requester, new SM_QUEST_ACTION(4, (0)));
        PacketSendUtility.sendPacket(responder, new SM_QUEST_ACTION(4, (0)));
    }

    private void removeTask(int playerId) {
        Future<?> task = drawTasks.get(playerId);
        if (task != null && !task.isDone()) {
            task.cancel(true);
            drawTasks.remove(playerId);
        }
    }

    @SuppressWarnings("synthetic-access")
    private static class SingletonHolder {

        protected static final DuelService instance = new DuelService();
    }
}
