import java.util.ArrayList;

import energium.*;

public class Bot {
    public static void main(final String[] args) throws Exception {
        Agent agent = new Agent();
        agent.initialize();
        while (true) {
            // wait for updates
            agent.update();

            ArrayList<String> commands = new ArrayList<>();
            /** Your player object */
            Player player = agent.players.get(agent.id);
            /** Opponent player object */
            Player opponent = agent.players.get((agent.id + 1) % 2);

            /** All of your bases */
            ArrayList<Base> bases = player.bases;
            /** All of your collector units */
            ArrayList<Unit> units = player.units;

            // Use System.err.println to print messages to the terminal or your error log.
            // System.out is reserved for the match engine. Uncomment the lines below to log
            // something

            // System.err.println("My ID: " + agent.id + " | Turn " + agent.turn + " | " +
            // player.units.size()
            // + " collector units | " + player.bases.size() + " bases | " + player.energium
            // + " energium");

            /** AI Code Goes here */
            /**
             * Let your creativity go wild. Feel free to change this however you want and
             * submit it as many times as you want to the servers
             */

            // spawn unit until we have 4 units
            if (units.size() < 4 && player.energium >= GameConstants.UNIT_COST) {
                commands.add(bases.get(0).spawnUnit());
            }

            // iterate over all of our collectors and make them do something
            for (int i = 0; i < units.size(); i++) {
                Unit unit = units.get(i);

                // move back to base if unit is going to break down
                if (unit.getBreakdownLevel() >= GameConstants.BREAKDOWN_MAX - 2) {
                    Direction directionBackToBase = unit.pos.directionTo(bases.get(0).pos);
                    commands.add(unit.move(directionBackToBase));
                    continue;
                }

                int unitAge = agent.turn - unit.lastRepairTurn;

                // move randomly for first 20 turns after visit to base (or spawn)

                Position currentPos = unit.pos;
                int currPosStat = agent.map.getTileByPos(currentPos).energium;

                // null if nowhere is better than here
                Direction toMove = null;
                Position destination = currentPos;


                if (unitAge <= 20) {
                    toMove = Direction.values()[(int) Math.floor(Math.random() * 4)];
                    destination = currentPos.translate(toMove, 1);
                    // if destination out of bounds, do nothing for this turn
                    if (destination.x < 0 || destination.x >= agent.mapWidth || destination.y < 0 || destination.y >= agent.mapHeight) {
                        toMove = null;
                    }
                }
                // removing the "random move every 20 turns" provision seems to give better performance.
                // else if (unitAge % 20 == 0) { // random move every 20 turns
                //     toMove = Direction.values()[(int) Math.floor(Math.random() * 4)];
                //     destination = currentPos.translate(toMove, 1);
                //     // if destination out of bounds, do nothing for this turn
                //     if (destination.x < 0 || destination.x >= agent.mapWidth || destination.y < 0 || destination.y >= agent.mapHeight) {
                //         toMove = null;
                //     }
                // }
                else {
                    // get tiles to north, south, east, west
                    Position n = currentPos.y - 1 >= 0 ? currentPos.translate(Direction.NORTH, 1) : null;
                    Position s = currentPos.y + 1 < agent.mapHeight ? currentPos.translate(Direction.SOUTH, 1) : null;
                    Position e = currentPos.x + 1 < agent.mapWidth ? currentPos.translate(Direction.EAST, 1) : null;
                    Position w = currentPos.x - 1 >= 0 ?  currentPos.translate(Direction.WEST, 1) : null;

                    // get current position stats
                    int max = Integer.MIN_VALUE;

                    // figure out which out of nsew has the most energium
                    if (n != null && max <= agent.map.getTileByPos(n).energium) {
                        max = agent.map.getTileByPos(n).energium;
                        toMove = Direction.NORTH;
                        destination = n;
                    }
                    if (s != null && max <= agent.map.getTileByPos(s).energium) {
                        max = agent.map.getTileByPos(s).energium;
                        toMove = Direction.SOUTH;
                        destination = s;
                    }
                    if (e != null && max <= agent.map.getTileByPos(e).energium) {
                        max = agent.map.getTileByPos(e).energium;
                        toMove = Direction.EAST;
                        destination = e;
                    }
                    if (w != null && max <= agent.map.getTileByPos(w).energium) {
                        max = agent.map.getTileByPos(w).energium;
                        toMove = Direction.WEST;
                        destination = w;
                    }
                    
                }

                if (toMove == null || agent.map.getTileByPos(destination).energium < 0) { // if negative stay put
                    
                }
                else if (agent.map.getTileByPos(destination).energium < currPosStat && !destination.equals(bases.get(0).pos)) {
                    // if the destination is a downgrade, do nothing
                }
                else { // add move to commands
                    commands.add(unit.move(toMove));
                }
                
                // first we check the breakdown level, if unit is about to break down, lets make
                // it move towards a random friendly base

                /*

                System.err.println(unit.getBreakdownLevel());
                if (unit.getBreakdownLevel() >= GameConstants.BREAKDOWN_MAX - 2) {
                    Direction directionBackToBase = unit.pos.directionTo(bases.get(0).pos);
                    commands.add(unit.move(directionBackToBase));
                } else {
                    // otherwise lets try to collect our energium
                    // choose a random direction to move in
                    // food for thought - is this optimal to do?
                    Direction randomDirection = Direction.values()[(int) Math.floor(Math.random() * 4)];

                    // move in that direction if the tile the unit would move towards is not
                    // negative in energium and is on the map
                    Position newPos = unit.pos.translate(randomDirection, 1);
                    if (newPos.x >= 0 && newPos.x < agent.mapWidth && newPos.y >= 0 && newPos.y < agent.mapHeight) {
                        if (agent.map.getTileByPos(newPos).energium < 0) {
                            // stay put instead of randomly going to a bad tile
                        } else {
                            commands.add(unit.move(randomDirection));
                        }
                    }
                }

                */
            }

            /** AI Code ends here */

            // send commands to engine
            System.out.println(String.join(",", commands));
            // end turn
            agent.endTurn();

        }
    }
}
