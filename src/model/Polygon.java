package model;

import java.util.ArrayList;
import java.util.List;

public final class Polygon {

    private final List<Line> lines;

    public Polygon() {
        lines = new ArrayList<>();
    }

    public void clear() {
        lines.clear();
    }

    public Point getLastPoint() {
        return lines.isEmpty() ? null : lines.getLast().getPoint2();
    }

    public List<Line> getLines() {
        return List.copyOf(lines);
    }

    public void removeLinesContaining(Point point) {
        lines.removeIf(line -> line.contains(point));
    }

    public void addLine(Line line) {
        lines.add(line);
    }

    public int getMinX() {
        return lines.stream().mapToInt(line -> Math.min(line.getPoint1().getX(), line.getPoint2().getX())).min().orElse(0);
    }

    public int getMaxX(){
        return lines.stream().mapToInt(line -> Math.max(line.getPoint1().getX(), line.getPoint2().getX())).max().orElse(0);
    }

    public int getMinY() {
        return lines.stream().mapToInt(line -> Math.min(line.getPoint1().getY(), line.getPoint2().getY())).min().orElse(0);
    }

    public int getMaxY() {
        return lines.stream().mapToInt(line -> Math.max(line.getPoint1().getY(), line.getPoint2().getY())).max().orElse(0);
    }

     public Point getClosestVertex(Point lastMousePos) {
        if(lines.size() == 0){
            return null;
        }

        Point closestPoint = lines.getFirst().getPoint1();

        for (Line line : lines) {
            Point p1 = line.getPoint1();
            Point p2 = line.getPoint2();
            double p1Distance = p1
                    .calculateDistanceFromPoint(lastMousePos);
            double p2Distance = p2
                    .calculateDistanceFromPoint(lastMousePos);
            double cpDistance = closestPoint
                    .calculateDistanceFromPoint(lastMousePos);

            if (p1Distance < cpDistance) {
                closestPoint = p1;
                cpDistance = closestPoint.calculateDistanceFromPoint(lastMousePos);
            }
            if (p2Distance < cpDistance) {
                closestPoint = p2;
            }
        }

        return closestPoint;
    }
}
