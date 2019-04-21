import javax.swing.*;
import java.awt.*;

public class Renderer extends JPanel {

    private static final long serialVersionID = 1L;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Main.game.repaint(g);
    }
}
