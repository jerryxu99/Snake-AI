import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Game implements ActionListener, KeyListener {

    private static Cell[][] gameMap;
    private static Renderer renderer;
    private Pathfinder pathFinder;
    private Timer timer;
    private Snake snake;
    private Cell food;

    private static final int WIDTH = 1200, HEIGHT = 800, CELLSIZE = 80;

    //xLength and yLength is the number of cells per row/column
    private int xLength = WIDTH / CELLSIZE;
    private int yLength = HEIGHT / CELLSIZE;
    //ticks is used as a counter
    private int ticks = 1;

    private boolean humanControls;
    private static boolean gameOver = false;

    public Game() {
        JFrame jframe = new JFrame();
        renderer = new Renderer();
        timer = new Timer(10, this);


        jframe.add(renderer);
        jframe.addKeyListener(this);
        jframe.setSize(WIDTH, HEIGHT);
        jframe.setResizable(false);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setVisible(true);

        //initializes the game grid of Cells
        gameMap = new Cell[xLength][yLength];

        for (int x = 0; x < xLength; x++) {
            for (int y = 0; y < yLength; y++) {
                gameMap[x][y] = new Cell(x, y);
            }
        }

        snake = new Snake(xLength / 2, yLength / 2);
        pathFinder = new Pathfinder(this, snake);

        moveFood();

        //switch to true to have human controls
        humanControls = false;

        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            if (ticks % 5 == 0) {
                if (humanControls) {
                    snake.move();
                } else {
                    //AI controlled
                    //When the snake's length is less than half the total amount of cells in the game grid, use a
                    //greedy pathfinding algorithm which finds the best path directly to the food.
                    if (snake.getBodyLength() < (xLength - 2) * (yLength - 2) / 2) {
                        snake.addNextCellByAI(pathFinder.getNextCell(snake.snakeHead, food));
                    } else {
                        //when the snake's length is more than half the total amount of cells in the game grid,
                        //the AI uses a space-conserving pathfinding algorithm which tries to fill up space before
                        //eating the food to prepare for the future
                        snake.addNextCellByAI(pathFinder.getNextCellConservatively(snake.snakeHead, food));
                    }
                    snake.moveAI();
                }
            }

            if (food == snake.snakeHead) {
                moveFood();
                snake.grow();
            }

            if (!isInLegalCell(snake.snakeHead) && snake.getBodyLength() > 0) {
                gameOver = true;
            }

            ticks++;
        }

        renderer.repaint();
    }

    public void repaint(Graphics g) {
        //paints background
        g.setColor(Color.darkGray.darker().darker());
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(Color.black);
        g.fillRect(CELLSIZE, CELLSIZE, WIDTH - 2 * CELLSIZE, HEIGHT - 2 * CELLSIZE);

        //paints the snake
        g.setColor(Color.green);
        for (Cell body : snake.getBody()) {
            paintCell(body, g);
        }
        g.setColor(Color.orange);
        paintCell(snake.snakeHead, g);

        g.setColor(Color.red.darker());
        paintCell(snake.getSnakeTail(), g);

        //paints food
        g.setColor(Color.red);
        paintCell(food, g);

        //prints game over screen
        if (gameOver) {
            g.setColor(Color.gray);
            paintCell(snake.snakeHead, g);

            g.setFont(new Font("Arial", 1, 100));
            g.setColor(Color.red);
            g.drawString("Game Over", WIDTH / 4, HEIGHT / 2 - 50);
        }
    }

    public void paintCell(Cell cell, Graphics g) {
        //fills a cell as a rectangle with padding
        g.fillRect(cell.getX() * CELLSIZE + 1, cell.getY() * CELLSIZE + 1, CELLSIZE - 2, CELLSIZE - 2);
    }

    public void moveFood() {
        //repeatedly moves the food's location until its valid. The food cannot spawn on the snake.
        do {
            food = getCell((int) (Math.random() * (xLength - 1)), (int) (Math.random() * (yLength - 1)));
        } while (!isInLegalCell(food) && food != snake.snakeHead);
    }

    public boolean isInLegalCell(Cell cell) {
        //cannot touch the snake's body and tail
        for (Cell body : snake.getBody()) {
            if (cell == body) {
                return false;
            }
        }
        if (cell == snake.getSnakeTail()) {
            return false;
        }
        //cannot be out of bounds
        if (cell.getX() == 0 || cell.getX() == xLength - 1 || cell.getY() == 0 || cell.getY() == yLength - 1) {
            return false;
        }
        return true;
    }

    public static Cell getCell(int x, int y) {
        return gameMap[x][y];
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameOver && humanControls) {
            switch (e.getKeyCode()) {
                case (KeyEvent.VK_UP):
                    snake.addNextCellByDirection(0, -1);
                    break;
                case (KeyEvent.VK_RIGHT):
                    snake.addNextCellByDirection(1, 0);
                    break;
                case (KeyEvent.VK_DOWN):
                    snake.addNextCellByDirection(0, 1);
                    break;
                case (KeyEvent.VK_LEFT):
                    snake.addNextCellByDirection(-1, 0);
                    break;
            }
        }
        //stop the game whenever
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            gameOver = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
