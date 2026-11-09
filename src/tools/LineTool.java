package tools;

import model.Line;
import model.Point;

public class LineTool {
    public Line straightenLine(Line line){
        Line result = line;

        Point start = line.getPoint1();
        Point end = line.getPoint2();

        int deltaX = end.getX() - start.getX();
        int deltaY = end.getY() - start.getY();

        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            // Horizontal line
            result = new Line(start, new Point(end.getX(), start.getY()), line.getColor());
        } else {
            // Vertical line
            result = new Line(start, new Point(start.getX(), end.getY()), line.getColor());
        }
        return result;
    }
}
