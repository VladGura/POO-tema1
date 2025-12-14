package model;

import java.util.Objects;

public class Position implements Comparable<Position> {
    private char x;
    private int y;

    public Position(char x, int y) { this.x = x; this.y = y; }
    public char getX() { return x; }
    public int getY() { return y; }

    @Override
    public int compareTo(Position other) {
        if (this.y != other.y) return Integer.compare(this.y, other.y);
        return Character.compare(this.x, other.x);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() { return Objects.hash(x, y); }

    @Override
    public String toString() { return "" + x + y; }
}
