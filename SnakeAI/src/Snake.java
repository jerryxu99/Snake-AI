import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Snake {
    private Queue<Cell> body;
    private ArrayList<Cell> nextCell;
    private int newTails = 0;
    public Cell snakeHead;
    private Cell snakeTail;
    private int[] direction;

    //creates a new snake at (x, y)
    public Snake(int x, int y) {
        body = new LinkedList<>();
        snakeHead = Game.getCell(x, y);
        snakeTail = snakeHead;

        //sets the default direction in human controlled snake
        direction = new int[2]; //{x, y}
        direction[0] = 0;
        direction[1] = -1;

        nextCell = new ArrayList<>();
    }

    //this is used when the human controls the snake
    public void move() {
        body.add(snakeHead);

        if (newTails == 0) {
            snakeTail = body.remove();
        } else {
            newTails--;
        }

        //if there is no next move manually queued, just keep heading in the same direction.
        //Otherwise, move to the queued cell
        if (nextCell.isEmpty()) {
            snakeHead = Game.getCell(snakeHead.getX() + direction[0], snakeHead.getY() + direction[1]);
        } else {
            snakeHead = nextCell.remove(0);
        }
    }

    //this is used when the AI controls the snake
    public void moveAI() {
        body.add(snakeHead);

        snakeHead = nextCell.remove(0);

        if (newTails == 0) {
            snakeTail = body.remove();
        } else {
            newTails--;
        }
    }

    public void grow() {
        newTails += 1;
    }

    public void addNextCellByAI(Cell cell) {
        nextCell.add(cell);
    }

    //when human controls
    public void addNextCellByDirection(int x, int y) {
        direction[0] = x;
        direction[1] = y;
        if (!nextCell.isEmpty()) {
            nextCell.add(Game.getCell(nextCell.get(nextCell.size() - 1).getX() + x,
                    nextCell.get(nextCell.size() - 1).getY() + y));
        } else {
            nextCell.add(Game.getCell(snakeHead.getX() + x, snakeHead.getY() + y));
        }
    }

    public Cell getSnakeTail() {
        return snakeTail;
    }

    public int getNewTails() {
        return newTails;
    }

    public int getBodyLength() {
        return body.size();
    }

    public int getNextCellsSize() {
        return nextCell.size();
    }

    public Queue<Cell> getBody() {
        return body;
    }

    public ArrayList<Cell> getNextCell() {
        return nextCell;
    }

    public void setBody(Queue<Cell> body) {
        this.body = body;
    }

    public void setSnakeTail(Cell cell) {
        snakeTail = cell;
    }

    public void setNewTails(int n) {
        newTails = n;
    }

    public void setNextCell(ArrayList<Cell> nextCells) {
        nextCell = nextCells;
    }
}
