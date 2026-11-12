package graphics.clipping;

import model.Line;
import model.Point;
import model.Polygon;

public class CohenSutherlandClipper implements LineClipper {

    private static final int INSIDE = 0b0000;
    private static final int LEFT = 0b1000;
    private static final int RIGHT = 0b0100;
    private static final int BOTTOM = 0b0010;
    private static final int TOP = 0b0001;

    @Override
    public Line clip(Line line, Polygon boundary) {
        if (boundary.getLines().isEmpty()) {
            return null;
        }

        int xMin = boundary.getMinX();
        int xMax = boundary.getMaxX();
        int yMin = boundary.getMinY();
        int yMax = boundary.getMaxY();

        double x1 = line.getPoint1().getX();
        double y1 = line.getPoint1().getY();
        double x2 = line.getPoint2().getX();
        double y2 = line.getPoint2().getY();

        while (true) {
            int scoreStart = scorePoint(x1, y1, xMin, xMax, yMin, yMax);
            int scoreEnd = scorePoint(x2, y2, xMin, xMax, yMin, yMax);
            
            //accept
            if ((scoreStart | scoreEnd) == INSIDE) {
                Point p1 = new Point((int) Math.round(x1), (int) Math.round(y1));
                Point p2 = new Point((int) Math.round(x2), (int) Math.round(y2));
                int color = line.getColor();

                return new Line(p1, p2, color);
            }
            //reject
            if ((scoreStart & scoreEnd) != 0) {
                return null;
            }

            //score for point outside of boundary
            int outsideScore = scoreStart != INSIDE ? scoreStart : scoreEnd;
            double x;
            double y;

            if ((outsideScore & TOP) != 0) {
                if (y2 == y1) {
                    return null;
                }
                y = yMin;
                x = x1 + (x2 - x1) * (yMin - y1) / (y2 - y1);
            } else if ((outsideScore & BOTTOM) != 0) {
                if (y2 == y1) {
                    return null;
                }
                y = yMax;
                x = x1 + (x2 - x1) * (yMax - y1) / (y2 - y1);
            } else if ((outsideScore & RIGHT) != 0) {
                if (x2 == x1) {
                    return null;
                }
                x = xMax;
                y = y1 + (y2 - y1) * (xMax - x1) / (x2 - x1);
            } else {
                if (x2 == x1) {
                    return null;
                }
                x = xMin;
                y = y1 + (y2 - y1) * (xMin - x1) / (x2 - x1);
            }

            //if start outside
            if (outsideScore == scoreStart) {
                x1 = x;
                y1 = y;
            
            //if end outside
            } else {
                x2 = x;
                y2 = y;
            }
        }
    }

    private int scorePoint(double x, double y, int xMin, int xMax, int yMin, int yMax) {
        int score = INSIDE;

        if (y < yMin) {
            score |= TOP;
        }
        if (y > yMax) {
            score |= BOTTOM;
        }
        if (x < xMin) {
            score |= LEFT;
        }
        if (x > xMax) {
            score |= RIGHT;
        }

        return score;
    }
}
