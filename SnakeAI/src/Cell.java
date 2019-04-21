public class Cell {
    private int x, y;
    //gScore is amount of moves taken to get to that cell from another cell(used in pathfinding).
    //fScore is the absolute distance from the cell to the food
    private int gScore, fScore;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setGScore(int g) {
        gScore = g;
    }

    public void setFScore(int f) {
        fScore = f;
    }

    public int getGScore() {
        return gScore;
    }

    public int getFScore() {
        return fScore;
    }

    public int getDistanceTo(Cell goal) {
        return Math.abs(goal.getX() - x) + Math.abs(goal.getY() - y);
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
