package graphics.clipping;

import model.Line;
import model.Point;
import model.Polygon;

public class CohenSutherland implements LineClipper {

    private final static int INSIDE = 0b0000;
    private final static int LEFT = 0b1000;
    private final static int RIGHT = 0b0100;
    private final static int BOTTOM = 0b0010;
    private final static int TOP = 0b0001;

    @Override
    public Line clip(Line line, Polygon boundary) {
        Line newLine = new Line(line.getPoint1(), line.getPoint2(), line.getColor());

        Point start = line.getPoint1();
        Point end = line.getPoint2();

        int xMin = boundary.getMinX();
        int xMax = boundary.getMaxX();
        int yMin = boundary.getMinY();
        int yMax = boundary.getMaxY();

        int scoreStart = scorePoint(start, xMin, xMax, yMin, yMax);
        int scoreEnd = scorePoint(end, xMin, xMax, yMin, yMax);

        int lineScore = scoreStart | scoreEnd;

        if (lineScore == INSIDE) {
            // trivial accept
            return newLine;
        }
        if ((scoreStart & scoreEnd) != 0) {
            // trivial reject
            return null;
        } else {
            newLine = getClippedLine(newLine, scoreEnd, xMin, xMax, yMin, yMax);
        }

        return newLine;
    }

    private Line getClippedLine(Line line, int pointScore, int xMin, int xMax, int yMin, int yMax) {
        Line clippedLine = line;
        Point intersection = null;

        Point p1 = line.getPoint1();
        Point p2 = line.getPoint2();
        int color = line.getColor();

        if ((pointScore & TOP) != 0) {
            intersection = getIntersectionTop(p1, p2, yMin);

            clippedLine = p1.getY() < p2.getY()
                    ? new Line(intersection, p2, color)
                    : new Line(p1, intersection, color);
        }
        if ((pointScore & BOTTOM) != 0) {
            intersection = getIntersectionBottom(p1, p2, yMax);

            clippedLine = p1.getY() > p2.getY()
                    ? new Line(intersection, p2, color)
                    : new Line(p1, intersection, color);
        }
        if ((pointScore & LEFT) != 0) {
            intersection = getIntersectionLeft(p1, p2, xMin);

            clippedLine = p1.getX() < p2.getX()
                    ? new Line(intersection, p2, color)
                    : new Line(p1, intersection, color);
        }
        if ((pointScore & RIGHT) != 0) {
            intersection = getIntersectionRight(p1, p2, xMax);

            clippedLine = p1.getX() > p2.getX()
                    ? new Line(intersection, p2, color)
                    : new Line(p1, intersection, color);
        }

        return clippedLine;
    }

    private Point getIntersectionLeft(Point p1, Point p2, int xMin) {
        double k = (double) (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());

        Point intersection = new Point(xMin, (int) (p1.getY() + k * (xMin - p1.getX())));
        return intersection;
    }

    private Point getIntersectionRight(Point p1, Point p2, int xMax) {
        double k = (double) (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
        Point intersection = new Point(xMax, (int) (p1.getY() + k * (p1.getX() - xMax)));
        return intersection;
    }

    private Point getIntersectionTop(Point p1, Point p2, int yMin) {
        double h = (double) (p2.getX() - p1.getX()) / (p2.getY() - p1.getY());
        Point intersection = new Point((int) (p1.getX() + h * (p1.getY() - yMin)), yMin);
        return intersection;
    }

    private Point getIntersectionBottom(Point p1, Point p2, int yMax) {
        double h = (double) (p2.getX() - p1.getX()) / (p2.getY() - p1.getY());
        Point intersection = new Point((int) (p1.getX() + h * (yMax - p1.getY())), yMax);
        return intersection;
    }

    // Returns a 4-bit score for a point relative to the clipping boundary
    private int scorePoint(Point p, int xMin, int xMax, int yMin, int yMax) {
        int score = INSIDE;

        if (p.getY() < yMin) {
            score |= TOP;
        }
        if (p.getY() > yMax) {
            score |= BOTTOM;
        }
        if (p.getX() < xMin) {
            score |= LEFT;
        }
        if (p.getX() > xMax) {
            score |= RIGHT;
        }

        return score;
    }
}
