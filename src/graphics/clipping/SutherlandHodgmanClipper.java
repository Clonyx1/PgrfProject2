package graphics.clipping;

import model.Line;
import model.Point;
import model.Polygon;

import java.util.ArrayList;
import java.util.List;

public final class SutherlandHodgmanClipper implements PolygonClipper {

    /**
     * Clips a polygon against a convex clipping polygon
     * using the Sutherland-Hodgman algorithm.
     */
    /* subject = náš polygon, boundary = polygon, kterým ořezáváme */
    public Polygon clip(Polygon subject, Polygon boundary) {

        List<Line> subjectEdges = subject.getLines();

        List<Point> subjectVertices = vertices(subject);
        List<Point> boundaryVertices = vertices(boundary);

        if (subjectVertices.size() < 3) {
            return new Polygon();
        }
        if (boundaryVertices.size() < 3 || area(boundaryVertices) == 0) {
            throw new IllegalArgumentException("Clipping polygon must have at least 3 vertices and non-zero area.");
        }

        boolean ccw = area(boundaryVertices) > 0;

        List<Point> currentVertices = new ArrayList<>(subjectVertices);

        for (int i = 0; i < boundaryVertices.size(); i++) {
            Point a = boundaryVertices.get(i);
            Point b = boundaryVertices.get((i + 1) % boundaryVertices.size());

            currentVertices = clipEdge(currentVertices, a, b, ccw);
            if (currentVertices.size() < 3) {
                return new Polygon();
            }
        }

        return toPolygon(currentVertices, subjectEdges.get(0).getColor());
    }

    /**
     * Clips a polygon against a single edge of the clipping polygon.
     * Returns the vertices that remain inside the clipping half-plane.
     */
    private List<Point> clipEdge(List<Point> polygon, Point a, Point b, boolean ccw) {
        List<Point> result = new ArrayList<>();

        Point previous = polygon.getLast();

        for(Point current : polygon) {
            boolean currentInside = isInside(current, a, b, ccw);
            boolean previousInside = isInside(previous, a, b, ccw);

            if(currentInside && previousInside) result.add(current);
            else if(currentInside && !previousInside) {
                Point intersection = intersection(previous, current, a, b);
                result.add(intersection);
                result.add(current);
            }
            else if(!currentInside && previousInside) {
                Point intersection = intersection(previous, current, a, b);
                result.add(intersection);
            }

            previous = current;
        }

        return result;
    }

    /**
     * Checks whether a point lies inside the clipping half-plane.
     * The inside direction depends on the polygon orientation (CW or CCW).
     * 
     * // - CCW: inside if cross >= 0.
     * // - CW: inside if cross <= 0.
     * // Point on the boundary is considered inside.
     */
    public boolean isInside(Point p, Point a, Point b, boolean ccw) {
        // side that point p lies on
        double side = cross(a, b, p);

        return ccw ? side >= 0 : side <= 0;
    }

    /**
     * Calculates the intersection of segment PQ with the line AB.
     * The resulting coordinates are rounded to integers.
     */
    public Point intersection(Point p, Point q, Point a, Point b) {
        double d1 = cross(a, b, p);
        double d2 = cross(a, b, q);

        if (d1 == d2)
            throw new IllegalArgumentException("Parallel lines");

        // determines the position of the intersection on the PQ segment
        double t = d1 / (d1 - d2);

        // intersection coordinates
        int x = (int) Math.round(p.getX() + t * (q.getX() - p.getX()));
        int y = (int) Math.round(p.getY() + t * (q.getY() - p.getY()));

        return new Point(x, y);
    }

    /*
     * -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- Utilities
     * -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
     */

    /**
     * Calculates the signed area of a polygon using the shoelace formula.
     * The sign determines the orientation of its vertices:
     *
     * > 0 - Counterclockwise (CCW).
     * < 0 - Clockwise (CW).
     * = 0 - Zero area (degenerate polygon).
     *
     * The last vertex is automatically connected to the first.
     */
    /*
     * Determining inside / outside - i.e., whether the polygon is oriented clockwise or counterclockwise
     * relative to the hour hand
     */
    private double area(List<Point> polygon) {
        double sum = 0;
        for (int i = 0; i < polygon.size(); i++) {
            Point a = polygon.get(i);
            Point b = polygon.get((i + 1) % polygon.size());
            sum += (double) a.getX() * b.getY() - (double) b.getX() * a.getY();
        }
        return sum / 2;
    }

    /**
     * Calculates the cross product of vectors AB and AP.
     * The sign determines the position of point P relative to the directed line AB:
     *
     * > 0 - Point is to the left of the line.
     * < 0 - Point is to the right of the line.
     * = 0 - Point lies on the line.
     *
     * Used to determine whether a point is inside the clipping region.
     */
    private double cross(Point a, Point b, Point p) {
        return ((double) b.getX() - a.getX()) * (p.getY() - a.getY())
                - ((double) b.getY() - a.getY()) * (p.getX() - a.getX());
    }

    /*
     * -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-=-=-=-=-=
     * -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
     */

    /**
     * Converts a polygon represented by edges into a list of vertices.
     * Adds the final endpoint only if it differs from the first vertex,
     * avoiding a duplicate vertex in closed polygons.
     */
    private List<Point> vertices(Polygon polygon) {
        List<Line> lines = polygon.getLines();
        List<Point> result = new ArrayList<>();
        for (Line line : lines) {
            result.add(line.getPoint1());
        }

        if (!lines.isEmpty()) {
            Point first = lines.getFirst().getPoint1();
            Point last = lines.getLast().getPoint2();
            if (first.getX() != last.getX() || first.getY() != last.getY()) {
                result.add(last);
            }
        }
        return result;
    }

    /**
     * Converts a list of vertices back into a polygon represented by edges.
     * Returns an empty polygon if fewer than three vertices remain
     * or the polygon has zero area.
     *
     * Consecutive vertices form edges; zero-length edges are skipped.
     * The polygon is stored as an open chain, with closure handled during
     * rendering.
     */
    private Polygon toPolygon(List<Point> points, int color) {
        Polygon result = new Polygon();
        if (points.size() < 3 || area(points) == 0) {
            return result;
        }

        for (int i = 1; i < points.size(); i++) {
            Point a = points.get(i - 1);
            Point b = points.get(i);
            if (a.getX() != b.getX() || a.getY() != b.getY()) {
                result.addLine(new Line(a, b, color));
            }
        }
        return result;
    }

}
