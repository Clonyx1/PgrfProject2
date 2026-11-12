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
}
