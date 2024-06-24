import java.awt.*;
import java.awt.geom.*;
public class PlayerSprite {
    private double x,y,size;
    private Color color;

    public PlayerSprite(double x, double y, double size, Color color) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.color = color;
    }
    public void drawSprite(Graphics2D graphics2D){
        Rectangle2D.Double square = new Rectangle2D.Double(x,y,size,size);
        graphics2D.setColor(color);
        graphics2D.fill(square);
    }

    public void moveH(double n){
        x+=n;
    }
    public void moveV(double n){
        y+=n;
    }


    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
