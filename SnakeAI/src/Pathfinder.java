import java.util.*;

public class Pathfinder {
    private Game game;
    private Snake snake;

    private PriorityQueue<Cell> openSet;
    private ArrayList<Cell> closedSet;
    private HashMap<Cell, Cell> cameFrom;

    //This pathfinder mainly uses the a* path-finding algorithm
    public Pathfinder(Game game, Snake snake) {
        this.game = game;
        this.snake = snake;
        cameFrom = new HashMap<>();

        //creates a new comparator for sorting the openSet priorityQueue
        //priority is lowest fScore. If fScore's are the same, prioritize the lowest gScore.
        //If the gScores are also the same, prioritize the the cell furthest from the snake tail, so that the snake
        //does not accidentally cut its path to the tail off while going for the food.
        Comparator<Cell> fCostComparator = new Comparator<Cell>() {
            @Override
            public int compare(Cell o1, Cell o2) {
                if (o1.getFScore() == o2.getFScore()) {
                    if (o1.getGScore() == o2.getGScore()) {
                        //prioritize whichever is furthest away from snake tail
                        return o2.getDistanceTo(snake.getSnakeTail()) - o1.getDistanceTo(snake.getSnakeTail());
                    }
                    //prioritize whichever is closer to goal
                    //this is a simplified expression of o1.fScore - o1.gScore - (o2.fScore - o2.gScore),
                    //but o1fScore == o2fScore
                    return o2.getGScore() - o1.getGScore();
                }
                //prioritize whichever has smallest sum of distance taken to get to that cell + distance to goal
                return o1.getFScore() - o2.getFScore();
            }
        };
        openSet = new PriorityQueue<>(fCostComparator);
        closedSet = new ArrayList<>();
    }

    //This pathfinding algorithm is an implementation of the A* pathfinding algorithm
    public boolean canFindPath(Cell start, Cell goal) {
        Cell current;

        cameFrom.clear();
        closedSet.clear();
        openSet.clear();
        start.setGScore(0);
        //fScore is gScore + distance to goal
        start.setFScore(start.getGScore() + start.getDistanceTo(goal));
        openSet.add(start);

        while (!openSet.isEmpty()) {
            //set current to the most prioritized cell in the neighbour cells of all already seen cells
            current = openSet.remove();
            if (current == goal) {
                return true;
            }
            closedSet.add(current);
            addNeighbours(current, goal);
        }
        return false;
    }

    public Stack<Cell> reconstructPathFrom(Cell current) {
        //retrace steps found from canFindPath from the goal to 1 cell away from the start
        Stack<Cell> path = new Stack<>();
        while (cameFrom.containsKey(current)) {
            path.add(current);
            current = cameFrom.get(current);
        }
        return path;
    }

    public void addNeighbours(Cell current, Cell goal) {
        int tempGScore;
        Cell[] neighbours = getNeighbours(current);
        for (int i = 0; i < 4; i++) {
            //make sure the neighbour cell is ok to look at. (either is the goal itself, is a legal cell, and make sure
            //it is a new cell that hasn't been accessed yet
            if ((neighbours[i] == goal || game.isInLegalCell(neighbours[i])) && !closedSet.contains(neighbours[i])
                    && neighbours[i] != snake.snakeHead) {
                //gScore to the neighbour is the steps it took to get to the current cell + 1
                tempGScore = current.getGScore() + 1;

                if (openSet.contains(neighbours[i]) && tempGScore >= neighbours[i].getGScore()) {
                    //in this scenario, the neighbouring cell has already been seen (but not accessed) with less steps
                    continue;
                }
                neighbours[i].setGScore(tempGScore);
                neighbours[i].setFScore(tempGScore + neighbours[i].getDistanceTo(goal));
                if (openSet.contains(neighbours[i])) {
                    cameFrom.replace(neighbours[i], current);
                } else {
                    openSet.add(neighbours[i]);
                    cameFrom.put(neighbours[i], current);
                }
            }
        }
    }

    //returns the neighbouring cells
    public Cell[] getNeighbours(Cell current) {
        return new Cell[]{Game.getCell(current.getX() + 1, current.getY()),
                Game.getCell(current.getX() - 1, current.getY()), Game.getCell(current.getX(), current.getY() + 1),
                Game.getCell(current.getX(), current.getY() - 1)};
    }

    //This algorithm finds the shortest path to the goal, and makes sure it can reach it's own tail after each move to
    //stay safe.
    public Cell getNextCell(Cell start, Cell goal) {
        if (canFindPath(start, goal)) {
            //stores current snake info to reset the snake later
            ArrayList<Cell> nextCellCopy = new ArrayList<>(snake.getNextCell());
            Queue<Cell> bodyCopy = new LinkedList<>(snake.getBody());
            Cell snakeHeadCopy = snake.snakeHead;
            Cell snakeTailCopy = snake.getSnakeTail();
            int newTailsCopy = snake.getNewTails();
            //temporarily move the snake to the food and check if it can find its tail after moving
            Stack<Cell> path = reconstructPathFrom(goal);
            while (!path.isEmpty()) {
                snake.addNextCellByAI(path.pop());
            }
            while (snake.getNextCellsSize() > 0) {
                snake.moveAI();
            }

            Cell possibleMove = reconstructPathFrom(goal).pop();
            boolean shouldMove;
            if ((snake.getBodyLength() == 0 || snake.snakeHead.getDistanceTo(snake.getSnakeTail()) > 1) && canFindPath(snake.snakeHead, snake.getSnakeTail())) {
                //the distance from snakeHead to snakeTail has to be greater than 1 after eating so that there is no
                //chance for the only possible next move to be running into its own tail and dying
                shouldMove = true;
            } else {
                if (!canFindPath(snake.snakeHead, snake.getSnakeTail())) System.out.println("cant reach tail after moving to goal");
                else System.out.println("error, distance to tail : " + snake.snakeHead.getDistanceTo(snake.getSnakeTail()));
                shouldMove = false;
            }

            //reset snake back to original position before moving
            snake.setNextCell(nextCellCopy);
            snake.setBody(bodyCopy);
            snake.setSnakeTail(snakeTailCopy);
            snake.snakeHead = snakeHeadCopy;
            snake.setNewTails(newTailsCopy);

            if (shouldMove) {
                return possibleMove;
            }
            //cant reach tail after moving to food
            return getCellFurthestFromGoal(start, goal);
        } else System.out.println("cant find food");

        //if there is no possible move to the food or if after moving, the tail cannot be found, then stall until a
        //path opens up
        return getCellFurthestFromGoal(start, goal);
    }

    public Cell getCellFurthestFromGoal(Cell current, Cell goal) {
        Cell[] neighbours = getNeighbours(current);
        int maxDistance = 0;
        Cell maxDistanceCell = null;

        //find which neighbouring cell is furthest away from the goal. The neighbouring cell should only be considered
        //if the snake can still find its own tail after moving to that cell
        for (int i = 0; i < 4; i++) {
            System.out.println(snake.getSnakeTail());
            System.out.println("neighbour : " + neighbours[i]);

            if (game.isInLegalCell(neighbours[i]) && neighbours[i].getDistanceTo(goal) > maxDistance) {
                if (canFindPath(neighbours[i], snake.getSnakeTail())) {
                    maxDistanceCell = neighbours[i];
                    maxDistance = neighbours[i].getDistanceTo(goal);
                } else {
                    System.out.println("can't find snakeTail");
                }
            } else {
                if (!game.isInLegalCell(neighbours[i])) System.out.println("not legal cell");
                else if (neighbours[i].getDistanceTo(goal) <= maxDistance) System.out.println("not furthest cell");
                System.out.println("neighbour is nono");
            }

        }
        //No cell is found sometimes when the snake is next to its tail. In these cases, the snake should follow its
        //tail. It will never hit itself by following its tail.
        if (maxDistanceCell ==  null) {
            System.out.println("oops");
            return snake.getSnakeTail();
        }
        return maxDistanceCell;
    }

    //This algorithm is based on efficiently filling up space on the game grid so that future food spawns are easy
    //to get. This is achieved by having the snake distance itself from its tail, prioritizing the cell that will take
    //the most amount of moves to get to its tail. The snake needs to be able to see the food at all times too.
    public Cell getNextCellConservatively(Cell start, Cell goal) {
        Cell[] neighbours = getNeighbours(start);
        //maxDistance to Tail
        int maxDistance = 0;
        int distanceToTail;
        Cell maxDistanceCell = null;
        boolean canFindTail, nextCellIsGoal;

        for (int i = 0; i < 4; i++) {
            if (game.isInLegalCell(neighbours[i])) {
                if (neighbours[i] == goal) {
                    nextCellIsGoal = true;
                } else {
                    nextCellIsGoal = false;
                }
                canFindTail = canFindPath(neighbours[i], snake.getSnakeTail());

                if (nextCellIsGoal && canFindTail && neighbours[i].getDistanceTo(snake.getSnakeTail()) > 1) {
                        return neighbours[i];
                }
                //if tail can be found and the length of this path to the tail is longer than any previously calculated
                //and the food can still be found, set this cell to the max distance cell from tail
                distanceToTail = reconstructPathFrom(snake.getSnakeTail()).size();
                System.out.println("Length to Tail : " + distanceToTail);
                if (distanceToTail > maxDistance && canFindTail && canFindPath(neighbours[i], goal)) {
                    maxDistanceCell = neighbours[i];
                    maxDistance = distanceToTail;
                }
            }
        }
        if (maxDistanceCell == null) {
            return getCellFurthestFromGoal(start, goal);
        }
        return maxDistanceCell;
    }
}
