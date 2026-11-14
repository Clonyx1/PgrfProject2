package graphics.filler;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Deque;

import graphics.Raster;
import model.Point;

public class DequeSeedFiller implements SeedFiller {

    private final Raster raster;

    public DequeSeedFiller(Raster raster) {
        this.raster = raster;
    }

    @Override
    public boolean fill(int x1, int y1, int targetColor, int fillColor) {
        if (targetColor == fillColor || raster.getPixel(x1, y1) == null || raster.getPixel(x1, y1) != targetColor) {
            return false;
        }

        fillRegion(x1, y1, targetColor, fillColor);
        return true;
    }

    private void fillRegion(int x, int y, int targetColor, int fillColor) {
        Deque<Point> stack = new ArrayDeque<>();
        stack.push(new Point(x, y));

        while (!stack.isEmpty()) {
            Point point = stack.pop();
            int px = point.getX();
            int py = point.getY();

            Integer color = raster.getPixel(px, py);
            if (color == null || color != targetColor) {
                continue;
            }

            int patternColor = Color.WHITE.getRGB();
            raster.setPixel(px, py, getPixelColor(px, py, fillColor, patternColor));

            stack.push(new Point(px - 1, py));
            stack.push(new Point(px + 1, py));
            stack.push(new Point(px, py - 1));
            stack.push(new Point(px, py + 1));

            stack.push(new Point(px - 1, py - 1));
            stack.push(new Point(px - 1, py + 1));
            stack.push(new Point(px + 1, py - 1));
            stack.push(new Point(px + 1, py + 1));
        }
    }

    private int getPixelColor(int x, int y, int fillColor, int patternColor){
        if(y % 12 == 0){
            if(x % 12 == 11 || x % 12 == 0 || x % 12 == 1){
                return patternColor;
            }
        }

        if(x % 12 == 0){
            if(y % 12 == 0 || y % 12 == 11 || y % 12 == 1){
                return patternColor;
            }
        }

        return fillColor;
    }
    
}
