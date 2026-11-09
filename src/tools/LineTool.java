package tools;

import model.Line;
import model.Point;

public class LineTool {
    public Line straightenLine(Line line) {
        Line result = line;

        Point start = line.getPoint1();
        Point end = line.getPoint2();

        int dx = end.getX() - start.getX();
        int dy = end.getY() - start.getY();

        // Threshold for determining whether the line should be diagonal
        double angleDegrees = Math.toDegrees(Math.atan2(Math.abs(dy), Math.abs(dx)));
        double thresholdDegrees = 25.0;

        Point newEnd = line.getPoint2();
        if (angleDegrees <= thresholdDegrees) {
            // Horizontal line
            newEnd = new Point(end.getX(), start.getY());
            result = new Line(start, newEnd, line.getColor());
        } else if (Math.abs(angleDegrees - 90) <= thresholdDegrees) {
            // Vertical line
            newEnd = new Point(start.getX(), end.getY());
            result = new Line(start, newEnd, line.getColor());
        } else if (Math.abs(angleDegrees - 45) <= thresholdDegrees
                || Math.abs(angleDegrees - 135) <= thresholdDegrees) {
            // Diagonal line
            double lenght = Math.hypot(dx, dy);
            int d = (int) Math.round(lenght / Math.sqrt(2));
            // Top left
            if (dx < 0 && dy < 0) {
                newEnd = new Point(start.getX() - d, start.getY() - d);
                result = new Line(start, newEnd, line.getColor());
            }
            // Bottom left
            else if (dx < 0 && dy > 0) {
                newEnd = new Point(start.getX() - d, start.getY() + d);
                result = new Line(start, newEnd, line.getColor());
            }
            // Top right
            else if (dx > 0 && dy < 0) {
                newEnd = new Point(start.getX() + d, start.getY() - d);
                result = new Line(start, newEnd, line.getColor());
            }
            // Bottom right
            else if (dx > 0 && dy > 0) {
                newEnd = new Point(start.getX() + d, start.getY() + d);
                result = new Line(start, newEnd, line.getColor());
            }
        }
        return result;
    }
}
