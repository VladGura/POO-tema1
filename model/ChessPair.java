package model;

import java.util.Objects;

public class ChessPair<K extends Comparable<K>, V> implements Comparable<ChessPair<K, V>> {
    private K key;
    private V value;

    public ChessPair(K k, V v) { this.key = k; this.value = v; }

    public K getKey() { return key; }
    public V getValue() { return value; }

    public void setKey(K key) { this.key = key; }
    public void setValue(V value) { this.value = value; }

    // statement wants "key+value" as a String
    public String asOneString() {
        return String.valueOf(key) + ":" + String.valueOf(value);
    }

    @Override
    public int compareTo(ChessPair<K, V> o) {
        if (o == null) return 1;
        if (this.key == null) return -1;
        return this.key.compareTo(o.key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessPair)) return false;
        ChessPair<?, ?> that = (ChessPair<?, ?>) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(key, value); }
}
