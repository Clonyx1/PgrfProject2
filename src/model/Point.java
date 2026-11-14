package model;

public final class Point {

    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double calculateDistanceFromPoint(Point p){
        double distance = Math.hypot(p.getX() - x, p.getY() - y);

        return distance;
    }

    public boolean equals(Point p){
        return p.getX() == x && p.getY() == y ? true : false;
    }
}
