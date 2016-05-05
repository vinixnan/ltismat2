package massim.competition2013.scenario;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import massim.competition2013.GraphSimulationAgentState;
import massim.competition2013.GraphSimulationWorldState;

/**
 * This class in the one in charge of executing the agents actions.
 */
public class ActionExecutor {

	private static int VALID_ACTION = 0;
	private static int WRONG_ROLE = 1;
	private static int INSUFFICIENT_RESOURCES = 2;
	private static int WRONG_STATUS = 3;

	private final static String SUCCESSFUL = "successful";
	private final static String FAILED_OUT_OF_RANGE = "failed_out_of_range";
	private final static String FAILED_IN_RANGE = "failed_in_range";
	private final static String FAILED_WRONG_PARAM = "failed_wrong_param";
	private final static String FAILED_UNREACHABLE = "failed_unreachable";
	private final static String FAILED_RESOURCES = "failed_resources";
	private final static String FAILED_ROLE = "failed_role";
	private final static String FAILED_STATUS = "failed_status";
	private final static String FAILED_ATTACKED = "failed_attacked";
	private final static String FAILED_PARRIED = "failed_parried";
	private final static String FAILED_LIMIT = "failed_limit";
	private final static String FAILED_RANDOM = "failed_random";
	private final static String FAILED = "failed";
	private final static String UNKNOWN_ACTION = "unknownAction";
	private final static String USELESS = "useless";

	private static Random random;

	/**
	 * Executes the actions of all agents.
	 * 
	 * @param world
	 */
	public static void execute(GraphSimulationWorldState world) {
		clearFlags(world);
		executeAttacksAndParrys(world);
		executeRest(world);
		// updateStatus(world);
	}

	/**
	 * Clears the information about the actions executed in the previous step.
	 * Also determines random failure of actions.
	 * 
	 * @param world
	 */
	protected static void clearFlags(GraphSimulationWorldState world) {
		for (GraphSimulationAgentState agentState : world.getAgents()) {
			agentState.lastAction = agentState.action;
			agentState.lastActionParam = agentState.param;
			agentState.lastActionResult = "";
			agentState.attacked = false;
			if (randomFail()) {
				agentState.action = "randomFail";
				agentState.param = "";
			}

		}
	}

	/**
	 * Returns true if the action should fail.
	 * 
	 * @return
	 */
	private static boolean randomFail() {
		if (random == null) {
			random = new Random();
		}
		return random.nextInt(100) < 1; // probability of failure is 1%
	}

	/**
	 * Executes all the <code>attack</code> actions simultaneously, meaning that
	 * all the costs calculations to corroborate that an attack can actually be
	 * performed, are made before updating the internal states of the attacked
	 * agents.<br>
	 * <br>
	 * Parries are taking into account to determine the outcome of an attack,
	 * but the state of the parrying agents is not update regarding the
	 * <code>parry</code> action costs
	 * 
	 * @param world
	 */
	protected static void executeAttacksAndParrys(GraphSimulationWorldState world) {
		Map<GraphSimulationAgentState, Integer> damageMap = new HashMap<>();
		for (GraphSimulationAgentState agentState : world.getAgents()) {
			if (agentState.action != null && agentState.action.equalsIgnoreCase("attack")) {
				int check = checkResources(agentState, "attack", world);
				if (check == VALID_ACTION) {

					String param = agentState.param;
					GraphSimulationAgentState victim = world.getAgent(param);

					if (victim != null) {
						if (validAttack(agentState, victim, world)) {
							if (!("parry".equalsIgnoreCase(victim.action) && checkResources(victim, "parry", world) == VALID_ACTION)) {

								// check effective range
								int effectiveRange = getEffectiveRange(agentState.visRange);
								int distance = agentState.getDistance(victim.node.name);
								if (distance != -1 && distance <= effectiveRange) {
									int damage = calculateDamage(agentState, victim);
									if (damageMap.containsKey(victim)) {
										damageMap.put(victim, damageMap.get(victim) + damage);
									} else {
										damageMap.put(victim, damage);
									}

									agentState.lastActionResult = SUCCESSFUL;
									updateResources(agentState, "attack", world, true);

									// make sure that disabled victims can't be exploited to get the achievement
									if (!victim.team.equals(agentState.team) && !(victim.health == 0)) {
										// Increase counter for achievements										
										world.getTeamState(agentState.team).successfullAttack();
									}

								} else {
									agentState.lastActionResult = FAILED_IN_RANGE;
									updateResources(agentState, "attack", world, false);
								}

							} else {
								agentState.lastActionResult = FAILED_PARRIED;
								updateResources(agentState, "attack", world, true);
							}

							victim.attacked = true;

						} else {
							agentState.lastActionResult = FAILED_OUT_OF_RANGE;
							updateResources(agentState, "attack", world, false);
						}
					} else {
						agentState.lastActionResult = FAILED_WRONG_PARAM;
						updateResources(agentState, "attack", world, false);
					}
				} else {
					agentState.lastActionResult = getResultMessage(check);
					updateResources(agentState, "attack", world, false);
				}
			}
		}
		for (GraphSimulationAgentState victim : damageMap.keySet()) {
			victim.health = Math.max(victim.health - damageMap.get(victim), 0);
		}
	}

	/**
	 * Returns true iff the victim is on a node within the vis.-range of the
	 * attacking agent.
	 * 
	 * @param agent
	 * @param victim
	 * @param world
	 * @return
	 */
	protected static boolean validAttack(GraphSimulationAgentState agent, GraphSimulationAgentState victim, GraphSimulationWorldState world) {
		int dist = -1;
		if (victim != null) {
			dist = agent.getDistance(victim.node.name);
		}
		return dist != -1; // the distance only has an entry, if it is within
							// the v.range
	}

	/**
	 * Executes the rest of the actions (excluding attacks) in a random order.
	 * Random ordering can affect the outcome of the actions, for example when
	 * an agent is trying to <code>repair</code> another one that is trying to
	 * move to a different node, or when two agents in the same team attempt to
	 * <code>buy</code> and the team only has enough resources for one buy.
	 * 
	 * @param world
	 */
	protected static void executeRest(GraphSimulationWorldState world) {

		Vector<GraphSimulationAgentState> agents = new Vector<>(world.getAgents());
		int check;
		Collections.shuffle(agents);

		for (GraphSimulationAgentState agentState : agents) {

			if (agentState.action.equalsIgnoreCase("goto")) {
				handleGotoAction(world, agentState);
			} else if (agentState.action.equalsIgnoreCase("parry")) {
				handleParryAction(world, agentState);
			} else if (agentState.action.equalsIgnoreCase("probe") || agentState.action.equalsIgnoreCase("survey") || agentState.action.equalsIgnoreCase("inspect")) {
				check = checkResources(agentState, agentState.action, world);

				if ((check == VALID_ACTION) && !agentState.attacked) {

					if (agentState.action.equalsIgnoreCase("probe")) {
						handleProbeAction(world, agentState);
					} else if (agentState.action.equalsIgnoreCase("survey")) {
						handleSurveyAction(world, agentState);
					} else if (agentState.action.equalsIgnoreCase("inspect")) {
						handleInspectAction(world, agentState);
					}
				} else {
					if (agentState.attacked) {
						agentState.lastActionResult = FAILED_ATTACKED;
					} else {
						agentState.lastActionResult = getResultMessage(check);
					}
					updateResources(agentState, agentState.action, world, false);
				}
			} else if (agentState.action.equalsIgnoreCase("buy")) {
				handleBuyAction(world, agentState);
			} else if (agentState.action.equalsIgnoreCase("repair")) {
				handleRepair(world, agentState);
			} else if (agentState.action.equalsIgnoreCase("recharge")) {
				updateResourcesRecharge(agentState, world, !agentState.attacked);
				agentState.lastAction = "recharge"; // ?
				agentState.lastActionResult = agentState.attacked ? FAILED_ATTACKED : SUCCESSFUL;
			} else if ("randomFail".equalsIgnoreCase(agentState.action)) {
				agentState.lastActionResult = FAILED_RANDOM;
			} else if ("skip".equalsIgnoreCase(agentState.action)) {
				agentState.lastActionResult = SUCCESSFUL;
			} else if ("noAction".equalsIgnoreCase(agentState.action)) {
				agentState.lastActionResult = FAILED;
			} else if (!"attack".equalsIgnoreCase(agentState.action)) {
				agentState.lastAction = UNKNOWN_ACTION;
				agentState.lastActionResult = FAILED;
			}
			agentState.action = "";
			agentState.param = "";
		}
	}

	/**
	 * This method handles the repair action.
	 * @param world The simulation world state.
	 * @param agentState The current agent state.
	 */
	private static void handleRepair(GraphSimulationWorldState world, GraphSimulationAgentState agentState) {
		int check;
		check = checkResources(agentState, agentState.action, world);
		if (check == VALID_ACTION) {
			try {
				GraphSimulationAgentState receiver = world.getAgent(agentState.param);
				if (receiver != null && !agentState.equals(receiver) && agentState.getDistance(receiver.node.name) != -1) {

					int distance = agentState.getDistance(receiver.node.name);
					int effectiveRange = getEffectiveRange(agentState.visRange);

					if (distance <= effectiveRange) {
						receiver.health += getEffectiveRepairing(agentState, receiver);
						if (receiver.health > receiver.maxHealth) {
							receiver.health = receiver.maxHealth;
						}

						agentState.lastActionResult = SUCCESSFUL;
						updateResources(agentState, agentState.action, world, true);
					} else {
						agentState.lastActionResult = FAILED_IN_RANGE;
						updateResources(agentState, agentState.action, world, false);
					}

				} else {
					if (receiver != null && agentState.getDistance(receiver.node.name) == -1) {
						agentState.lastActionResult = FAILED_OUT_OF_RANGE;
					} else {
						agentState.lastActionResult = FAILED_WRONG_PARAM;
					}
					updateResources(agentState, agentState.action, world, false);
				}
			} catch (Exception e) {
				agentState.lastActionResult = FAILED_WRONG_PARAM;
				updateResources(agentState, agentState.action, world, false);
			}
		} else {
			agentState.lastActionResult = getResultMessage(check);
			updateResources(agentState, agentState.action, world, false);
		}
	}

	/**
	 * This method handles the buy action.
	 * @param world The simulation world state.
	 * @param agentState The current agent state.
	 */
	private static void handleBuyAction(GraphSimulationWorldState world, GraphSimulationAgentState agentState) {
		int check;
		check = checkResources(agentState, agentState.action, world);
		if (check == VALID_ACTION) {
			if ("battery".equalsIgnoreCase(agentState.param) && agentState.maxEnergy + world.getConfig().getRoleConf(agentState.roleName).rateBuyEnergy <= world.getConfig().getRoleConf(agentState.roleName).maxBuyEnergy) {

				agentState.maxEnergy += world.getConfig().getRoleConf(agentState.roleName).rateBuyEnergy;
				agentState.maxEnergyDisabled += world.getConfig().getRoleConf(agentState.roleName).rateBuyEnergyDisabled;
				if (agentState.health > 0) {
					agentState.energy += world.getConfig().getRoleConf(agentState.roleName).rateBuyEnergy;
					;
				} else {
					agentState.energy += world.getConfig().getRoleConf(agentState.roleName).rateBuyEnergyDisabled;
				}
				agentState.lastActionResult = SUCCESSFUL;
				updateResources(agentState, agentState.action, world, true);
			} else if ("sensor".equalsIgnoreCase(agentState.param) && agentState.visRange + world.getConfig().getRoleConf(agentState.roleName).rateBuyVisRange <= world.getConfig().getRoleConf(agentState.roleName).maxBuyVisRange) {

				agentState.visRange += world.getConfig().getRoleConf(agentState.roleName).rateBuyVisRange;
				agentState.lastActionResult = SUCCESSFUL;
				updateResources(agentState, agentState.action, world, true);
			} else if ("shield".equalsIgnoreCase(agentState.param) && agentState.maxHealth + world.getConfig().getRoleConf(agentState.roleName).rateBuyHealth <= world.getConfig().getRoleConf(agentState.roleName).maxBuyHealth) {

				agentState.maxHealth += world.getConfig().getRoleConf(agentState.roleName).rateBuyHealth;
				if (agentState.health > 0) {
					agentState.health += world.getConfig().getRoleConf(agentState.roleName).rateBuyHealth;
				}
				agentState.lastActionResult = SUCCESSFUL;
				updateResources(agentState, agentState.action, world, true);
			} else if ("sabotageDevice".equalsIgnoreCase(agentState.param) && agentState.strength + world.getConfig().getRoleConf(agentState.roleName).rateBuyStrength <= world.getConfig().getRoleConf(agentState.roleName).maxBuyStrength) {

				agentState.strength += world.getConfig().getRoleConf(agentState.roleName).rateBuyStrength;
				agentState.lastActionResult = SUCCESSFUL;
				updateResources(agentState, agentState.action, world, true);
			} else {
				if ("battery".equalsIgnoreCase(agentState.param) || "sensor".equalsIgnoreCase(agentState.param) || "shield".equalsIgnoreCase(agentState.param) || "sabotageDevice".equalsIgnoreCase(agentState.param)) {
					agentState.lastActionResult = FAILED_LIMIT;
				} else {
					agentState.lastActionResult = FAILED_WRONG_PARAM;
				}
				updateResources(agentState, agentState.action, world, false);

			}
		} else {
			agentState.lastActionResult = getResultMessage(check);
			updateResources(agentState, agentState.action, world, false);
		}
	}

	/**
	 * This method handles the inspect action.
	 * @param world The simulation world state.
	 * @param agentState The current agent state.
	 */
	private static void handleInspectAction(GraphSimulationWorldState world, GraphSimulationAgentState agentState) {
		String param = agentState.param;

		if (param.equals("")) {
			// no param given, inspect agents at the same node
			agentState.lastActionResult = SUCCESSFUL;
			updateResources(agentState, agentState.action, world, true);
			for (GraphSimulationAgentState inspectedAgent : agentState.node.agents) {
				if (!agentState.team.equals(inspectedAgent.team)) {
					world.getTeamState(agentState.team).addInspectedAgent(inspectedAgent);
				}
			}
		} else {
			// inspect the given agent
			GraphSimulationAgentState insp = world.getAgent(param);
			if (insp != null && !agentState.team.equals(insp.team)) {

				int distance = agentState.getDistance(insp.node.name);
				int effectiveRange = getEffectiveRange(agentState.visRange);
				agentState.lastEffectiveRange = effectiveRange;

				if (distance != -1) {
					if (distance <= effectiveRange) {
						agentState.lastActionResult = SUCCESSFUL;
						updateResources(agentState, agentState.action, world, true);
						world.getTeamState(agentState.team).addInspectedAgent(insp);
					} else {
						agentState.lastActionResult = FAILED_IN_RANGE;
						updateResources(agentState, agentState.action, world, false);
					}
				} else {
					agentState.lastActionResult = FAILED_OUT_OF_RANGE;
					updateResources(agentState, agentState.action, world, false);
				}
			} else {
				updateResources(agentState, agentState.action, world, false);
				agentState.lastActionResult = FAILED_WRONG_PARAM;
			}
		}
	}

	/**
	 * This method handles the survey action.
	 * @param world The simulation world state.
	 * @param agentState The current agent state.
	 */
	private static void handleSurveyAction(GraphSimulationWorldState world, GraphSimulationAgentState agentState) {
		// success does not depend on range
		agentState.lastActionResult = SUCCESSFUL;
		updateResources(agentState, agentState.action, world, true);
		// add all edges in effective range
		int effectiveRange = getEffectiveSurveyRange(agentState.visRange);
		agentState.lastEffectiveRange = effectiveRange;
		Set<GraphNode> nodes = new HashSet<>();
		nodes.add(agentState.node);
		Set<GraphNode> previousNodes = new HashSet<>();
		previousNodes.add(agentState.node);
		for (int i = 0; i < effectiveRange; i++) {
			Set<GraphNode> nextNodes = new HashSet<>();
			for (GraphNode node : previousNodes) {
				for (GraphEdge edge : world.getConnectedEdges(node)) {
					world.getTeamState(agentState.team).addSurveyedEdge(edge);
					if (nodes.add(edge.node1)) {
						nextNodes.add(edge.node1);
					}
					if (nodes.add(edge.node2)) {
						nextNodes.add(edge.node2);
					}
				}
			}
			previousNodes = nextNodes;
		}
	}

	/**
	 * This method handles the probe action.
	 * 
	 * @param world
	 *            The simulation world state.
	 * @param agentState
	 *            The current agent state.
	 */
	private static void handleProbeAction(GraphSimulationWorldState world, GraphSimulationAgentState agentState) {
		String param = agentState.param;

		if (param.equals("")) {
			// no param given, probe node of the agent
			world.getTeamState(agentState.team).addProbedNodes(agentState.node);
			agentState.lastActionResult = SUCCESSFUL;
		} else {
			GraphNode node = world.getNode(param);
			if (node == null) {
				agentState.lastActionResult = FAILED_WRONG_PARAM;
			} else {

				int distance = agentState.getDistance(node.name);
				int effectiveRange = getEffectiveRange(agentState.visRange);
				agentState.lastEffectiveRange = effectiveRange;

				if (distance != -1) {
					if (distance <= effectiveRange) {
						agentState.lastActionResult = SUCCESSFUL;
						updateResources(agentState, agentState.action, world, true);
						world.getTeamState(agentState.team).addProbedNodes(node);
					} else {
						agentState.lastActionResult = FAILED_IN_RANGE;
						updateResources(agentState, agentState.action, world, false);
					}

				} else {
					agentState.lastActionResult = FAILED_OUT_OF_RANGE;
					updateResources(agentState, agentState.action, world, false);
				}

			}
		}
	}

	/**
	 * This method handles the parry action.
	 * 
	 * @param world
	 *            The simulation world state.
	 * @param agentState
	 *            The current agentstate.
	 */
	private static void handleParryAction(GraphSimulationWorldState world, GraphSimulationAgentState agentState) {
		int check;
		check = checkResources(agentState, agentState.action, world);
		if (check == VALID_ACTION) {
			if (agentState.attacked) {
				agentState.lastActionResult = SUCCESSFUL;
				world.getTeamState(agentState.team).successfullParry();
			} else {
				agentState.lastActionResult = USELESS;
			}
			updateResources(agentState, agentState.action, world, true);
		} else {
			agentState.lastActionResult = getResultMessage(check);
			updateResources(agentState, agentState.action, world, false);
		}
	}

	/**
	 * This method handles the goto action.
	 * 
	 * @param world
	 *            The simulation world sate.
	 * @param agentState
	 *            The current agentstate.
	 */
	private static void handleGotoAction(GraphSimulationWorldState world, GraphSimulationAgentState agentState) {
		int check;
		try {
			GraphNode nodeTo = null;
			int edgeCost = Integer.MAX_VALUE;

			if (agentState.node.name.equals(agentState.param)) {
				throw new Exception("Invalid Move");
			}
			for (GraphEdge edge : world.getConnectedEdges(agentState.node)) {
				if (edge.node1.name.equals(agentState.param)) {
					edgeCost = edge.weight;
					nodeTo = edge.node1;
					break;
				} else if (edge.node2.name.equals(agentState.param)) {
					edgeCost = edge.weight;
					nodeTo = edge.node2;
					break;
				}
			}
			if (nodeTo == null) {
				throw new Exception("Invalid Move");
			}

			check = checkResourcesGoto(agentState, edgeCost, world);
			if (check == VALID_ACTION) {
				agentState.node.agents.remove(agentState);
				agentState.setNode(nodeTo);
				nodeTo.agents.add(agentState);
				updateGotoResources(agentState, edgeCost, world, true);
			} else {
				updateGotoResources(agentState, edgeCost, world, false);
			}
			agentState.lastActionResult = getResultMessage(check);

		} catch (Exception e) {
			updateGotoResources(agentState, 1, world, false);
			if (world.getNode(agentState.param) != null) {
				agentState.lastActionResult = FAILED_UNREACHABLE;
			} else {
				agentState.lastActionResult = FAILED_WRONG_PARAM;
			}
		}
	}

	private static String getResultMessage(int code) {
		if (code == VALID_ACTION) {
			return SUCCESSFUL;
		} else if (code == INSUFFICIENT_RESOURCES) {
			return FAILED_RESOURCES;
		} else if (code == WRONG_ROLE) {
			return FAILED_ROLE;
		} else if (code == WRONG_STATUS) {
			return FAILED_STATUS;
		}
		return null;
	}

	/**
	 * Updates the internal status of an agent after performing an action.
	 * 
	 * @param agent
	 * @param action
	 * @param world
	 * @param successful
	 */
	private static void updateResources(GraphSimulationAgentState agent, String action, GraphSimulationWorldState world, boolean successful) {
		ActionConfiguration ac = world.getConfig().getActionConf(action);
		if (successful) {
			if (agent.health > 0) {
				agent.energy -= getEffectiveCost(ac, agent, world, false);
				agent.health -= ac.healthCost;
				world.getTeamState(agent.team).currAchievementPoints -= ac.pointsCost;
				world.getTeamState(agent.team).usedAchievementPoints += ac.pointsCost;

				if (agent.energy > agent.maxEnergy) {
					agent.energy = agent.maxEnergy;
				}
			} else {
				agent.energy -= getEffectiveCost(ac, agent, world, false);
				agent.health -= ac.healthCostDisabled;
				world.getTeamState(agent.team).currAchievementPoints -= ac.pointsCostDisabled;
				world.getTeamState(agent.team).usedAchievementPoints += ac.pointsCostDisabled;

				if (agent.energy > agent.maxEnergyDisabled) {
					agent.energy = agent.maxEnergyDisabled;
				}
			}
		} else {

			if (agent.health > 0) {
				agent.energy -= getEffectiveCost(ac, agent, world, true);
				agent.health -= ac.healthCostFailed;
				world.getTeamState(agent.team).currAchievementPoints -= ac.pointsCostFailed;
				world.getTeamState(agent.team).usedAchievementPoints += ac.pointsCostFailed;

				if (agent.energy > agent.maxEnergy) {
					agent.energy = agent.maxEnergy;
				}
			} else {

				agent.energy -= getEffectiveCost(ac, agent, world, true);
				agent.health -= ac.healthCostFailedDisabled;
				world.getTeamState(agent.team).currAchievementPoints -= ac.pointsCostFailedDisabled;
				world.getTeamState(agent.team).usedAchievementPoints += ac.pointsCostFailedDisabled;
				if (agent.energy > agent.maxEnergyDisabled) {
					agent.energy = agent.maxEnergyDisabled;
				}
			}

		}
		if (agent.health > agent.maxHealth) {
			agent.health = agent.maxHealth;
		}
		if (agent.energy < 0) {
			agent.energy = 0;
		}
		if (agent.health < 0) {
			agent.health = 0;
		}

		// store successful attacks in the newBuyTransaction
		if (action.equalsIgnoreCase("buy")) {
			world.getTeamState(agent.team).newBuyTransactions.put(agent.name, agent.param);
		}

	}

	/**
	 * Updates the internal status of an agent after performing the
	 * <code>recharge</code> action.
	 * 
	 * @param agent
	 * @param world
	 * @param successful
	 */
	private static void updateResourcesRecharge(GraphSimulationAgentState agent, GraphSimulationWorldState world, boolean successful) {
		ActionConfiguration ac = world.getConfig().getActionConf("recharge");
		if (agent.health > 0) {
			if (successful) {
				agent.energy += Math.round(((float) (ac.energyCost * agent.maxEnergy)) / 100f);
				agent.health += Math.round(((float) (ac.healthCost * agent.maxHealth)) / 100f);
			} else {
				agent.energy += Math.round(((float) (ac.energyCostFailed * agent.maxEnergy)) / 100f);
				agent.health += Math.round(((float) (ac.healthCostFailed * agent.maxHealth)) / 100f);
			}
			if (agent.energy > agent.maxEnergy) {
				agent.energy = agent.maxEnergy;
			}
		} else {
			if (successful) {
				agent.energy += Math.round(((float) (ac.energyCostDisabled * agent.maxEnergyDisabled)) / 100f);
			} else {
				agent.energy += Math.round(((float) (ac.energyCostFailedDisabled * agent.maxEnergyDisabled)) / 100f);
			}
			if (agent.energy > agent.maxEnergyDisabled) {
				agent.energy = agent.maxEnergyDisabled;
			}
		}
		if (agent.health > agent.maxHealth) {
			agent.health = agent.maxHealth;
		}
		if (agent.energy < 0) {
			agent.energy = 0;
		}
		if (agent.health < 0) {
			agent.health = 0;
		}
	}

	/**
	 * Updates the internal status of an agent after performing the
	 * <code>goto</code> action.
	 * 
	 * @param agent
	 * @param action
	 * @param world
	 * @param successful
	 */
	private static void updateGotoResources(GraphSimulationAgentState agent, int edgeCost, GraphSimulationWorldState world, boolean successful) {
		ActionConfiguration ac = world.getConfig().getActionConf("goto");
		if (agent.health > 0) {
			if (successful) {
				agent.energy -= ac.energyCost * edgeCost;
				agent.health -= ac.healthCost;
				world.getTeamState(agent.team).currAchievementPoints -= ac.pointsCost;
				world.getTeamState(agent.team).usedAchievementPoints += ac.pointsCost;
			} else {
				agent.energy -= ac.energyCostFailed;
				agent.health -= ac.healthCostFailed;
				world.getTeamState(agent.team).currAchievementPoints -= ac.pointsCostFailed;
				world.getTeamState(agent.team).usedAchievementPoints += ac.pointsCostFailed;
			}
			if (agent.energy > agent.maxEnergy) {
				agent.energy = agent.maxEnergy;
			}
		} else {
			if (successful) {
				agent.energy -= ac.energyCostDisabled * edgeCost;
				agent.health -= ac.healthCostDisabled;
				world.getTeamState(agent.team).currAchievementPoints -= ac.pointsCostDisabled;
				world.getTeamState(agent.team).usedAchievementPoints += ac.pointsCostDisabled;
			} else {
				agent.energy -= ac.energyCostFailedDisabled;
				agent.health -= ac.healthCostFailedDisabled;
				world.getTeamState(agent.team).currAchievementPoints -= ac.pointsCostFailedDisabled;
				world.getTeamState(agent.team).usedAchievementPoints += ac.pointsCostFailedDisabled;
			}
			if (agent.energy > agent.maxEnergyDisabled) {
				agent.energy = agent.maxEnergyDisabled;
			}
		}
		if (agent.health > agent.maxHealth) {
			agent.health = agent.maxHealth;
		}
		if (agent.energy < 0) {
			agent.energy = 0;
		}
		if (agent.health < 0) {
			agent.health = 0;
		}
	}

	/**
	 * Checks if the <code>agent</code> can perform the <code>action</code>
	 * according to its role and current status
	 * 
	 * @param agent
	 * @param action
	 * @param world
	 * @return ACTION_ALLOWED if the action is allowed, WRONG_ROLE if action is
	 *         not part of the role, UNSUFFICIENT_RESOURCES if resources are
	 *         unsufficient, WRONG_STATUS if action belongs to role when
	 *         enabled.
	 */
	private static int checkResources(GraphSimulationAgentState agent, String action, GraphSimulationWorldState world) {
		if (agent.health > 0) {
			if (!world.getConfig().getRoleConf(agent.roleName).actions.contains(action)) {
				return WRONG_ROLE;
			}
			ActionConfiguration ac = world.getConfig().getActionConf(action);

			int effCost = getEffectiveCost(ac, agent, world, false);

			if ((agent.energy >= effCost) && (agent.health >= ac.healthCost) && (world.getTeamState(agent.team).currAchievementPoints >= ac.pointsCost)) {
				return VALID_ACTION;
			}
			return INSUFFICIENT_RESOURCES;
		}
		if (!world.getConfig().getRoleConf(agent.roleName).actionsDisable.contains(action)) {
			if (!world.getConfig().getRoleConf(agent.roleName).actions.contains(action)) {
				return WRONG_ROLE;
			}
			return WRONG_STATUS;
		}
		ActionConfiguration ac = world.getConfig().getActionConf(action);
		if ((agent.energy >= getEffectiveCost(ac, agent, world, false)) && (agent.health >= ac.healthCostDisabled) && (world.getTeamState(agent.team).currAchievementPoints >= ac.pointsCostDisabled)) {
			return VALID_ACTION;
		}
		return INSUFFICIENT_RESOURCES;
	}

	/**
	 * Checks if the agent can perform the <code>goto</code> action according to
	 * its role and current status
	 * 
	 * @param agent
	 * @param action
	 * @param world
	 * @return
	 */
	private static int checkResourcesGoto(GraphSimulationAgentState agent, int edgeCost, GraphSimulationWorldState world) {
		if (agent.health > 0) {
			if (!world.getConfig().getRoleConf(agent.roleName).actions.contains("goto")) {
				return WRONG_ROLE;
			}
			ActionConfiguration ac = world.getConfig().getActionConf("goto");
			if ((agent.energy >= ac.energyCost * edgeCost) && (agent.health >= ac.healthCost) && (world.getTeamState(agent.team).currAchievementPoints >= ac.pointsCost)) {
				return VALID_ACTION;
			}
			return INSUFFICIENT_RESOURCES;
		}
		if (!world.getConfig().getRoleConf(agent.roleName).actionsDisable.contains("goto")) {
			if (!world.getConfig().getRoleConf(agent.roleName).actions.contains("goto")) {
				return WRONG_ROLE;
			}
			return WRONG_STATUS;
		}
		ActionConfiguration ac = world.getConfig().getActionConf("goto");
		if ((agent.energy >= ac.energyCostDisabled * edgeCost) && (agent.health >= ac.healthCostDisabled) && (world.getTeamState(agent.team).currAchievementPoints >= ac.pointsCostDisabled)) {
			return VALID_ACTION;
		}
		return INSUFFICIENT_RESOURCES;
	}

	/**
	 * Calculates the damage of an attack depending on the attacker's strength
	 * and the distance between attacker and victim.
	 * 
	 * @param attacker
	 *            The attacking agent
	 * @param victim
	 *            The attacked agent
	 * @return The resulting damage from an attack
	 */
	private static int calculateDamage(GraphSimulationAgentState attacker, GraphSimulationAgentState victim) {

		double maxVal = attacker.strength;
		double distance = attacker.getDistance(victim.node.name);
		double vRange = attacker.visRange;

		double damage = ((maxVal - 1.00) / Math.pow(vRange, 2.00)) * Math.pow(distance - vRange, 2.00) + 1.;

		return new Double(damage).intValue();
	}

	/**
	 * Calculates the effective cost based on the distance. Takes into account,
	 * whether the agent is disabled or the action failed. Attention:
	 * wrong_param has to be detected separately outside of this method again
	 * 
	 * @param ac
	 *            the action to execute
	 * @param agent
	 *            the executing agent
	 * @param world
	 *            the world
	 * @return the effective cost <br>
	 *         or -1 iff the target is outside the vis.-range and failed is
	 *         false - <b>note: this is the answer for the checkResources-method - the updateResources method already knows whether
	 *         the action failed and will get a corresponding result - TODO refactoring 2014 </b><br>
	 *         or the action's basecost iff the target does not exist (wrong
	 *         param) <br>
	 *         If the action failed, the cost are the baseCost + the
	 *         maxVisRange, if the target was outside that range <br>
	 */
	private static int getEffectiveCost(ActionConfiguration ac, GraphSimulationAgentState agent, GraphSimulationWorldState world, boolean failed) {
		String param = agent.param;

		int baseCost = 0;
		if (failed) {
			if (agent.health > 0) {
				baseCost = ac.energyCostFailed;
			} else {
				baseCost = ac.energyCostFailedDisabled;
			}
		} else {
			if (agent.health > 0) {
				baseCost = ac.energyCost;
			} else {
				baseCost = ac.energyCostDisabled;
			}
		}

		if (ac.name.equalsIgnoreCase("attack")) {
			GraphSimulationAgentState target = world.getAgent(param);
			if (target == null) {
				return baseCost;
			}
			int distance = agent.getDistance(target.node.name);
			if (distance == -1) {
				if (!failed) {
					return -1;
				}
				return baseCost + agent.visRange;
			}
			return baseCost + distance;
		} else if (ac.name.equalsIgnoreCase("probe")) {

			GraphNode target = world.getNode(param);
			if (target == null) {
				return baseCost;
			}
			int distance = agent.getDistance(target.name);
			if (distance == -1) {
				if (!failed) {
					return -1;
				}
				return baseCost + agent.visRange;
			}
			return baseCost + distance;
		} else if (ac.name.equalsIgnoreCase("inspect")) {
			if (param.equals("")) {
				return baseCost;
			}
			GraphSimulationAgentState target = world.getAgent(param);
			if (target == null) {
				return baseCost;
			}
			int distance = agent.getDistance(target.node.name);
			if (distance == -1) {
				if (!failed) {
					return -1;
				}
				return baseCost + agent.visRange;
			}
			return baseCost + distance;
		} else if (ac.name.equalsIgnoreCase("repair")) {
			if (param.equals("")) {
				return baseCost;
			}
			GraphSimulationAgentState target = world.getAgent(param);
			if (target == null) {
				return baseCost;
			}
			int distance = agent.getDistance(target.node.name);
			if (distance == -1) {
				if (!failed) {
					return -1;
				}
				return baseCost + agent.visRange;
			}
			return baseCost + distance;
		}

		// return 'normal' cost for other actions
		return baseCost;
	}

	/**
	 * Calculates an effective range based on the maximum visibility range
	 * 
	 * @param range
	 * @return
	 */
	private static int getEffectiveRange(int range) {

		if (random == null) {
			random = new Random();
		}
		int rand = random.nextInt(100);
		double scaleRand = ((double) rand) / 100.;

		double d = ((double) range) * Math.pow(scaleRand, 2.);

		return (int) Math.round(d);
	}

	/**
	 * Calculates an effective range for the survey action (minimum value 1)
	 * based on the maximum visibility range
	 * 
	 * @param range
	 * @return
	 */
	private static int getEffectiveSurveyRange(int range) {

		if (random == null) {
			random = new Random();
		}
		int rand = random.nextInt(100);
		double scaleRand = ((double) rand) / 100.;

		double d = ((double) range - 1.) * Math.pow(scaleRand, 2.) + 1.;

		return (int) Math.round(d);
	}

	/**
	 * Calculates the actual amount that an agent is able to repair
	 * 
	 * @param repairer
	 *            the repairing agent
	 * @param receiver
	 *            the agent to be repaired
	 * @return
	 */
	private static int getEffectiveRepairing(GraphSimulationAgentState repairer, GraphSimulationAgentState receiver) {

		double maxVal = receiver.maxHealth;
		double distance = repairer.getDistance(receiver.node.name);
		double vRange = repairer.visRange;

		double repairing = ((maxVal - 1.00) / Math.pow(vRange, 2.00)) * Math.pow(distance - vRange, 2.00) + 1;

		// System.out.println("vRange: "+vRange+" distance: "+distance+" maxVal: "+maxVal+" repairing: "+repairing);

		return new Double(repairing).intValue();
	}

}
