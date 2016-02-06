package org.perez.workflow.elements;

import java.io.Serializable;

/**
 * Created by Fernando on 06/07/2014.
 * Represents a typed pair of elements.
 */
public class Pair<E extends Serializable>
    implements Serializable
{
    /** First element */
    public E _1;
    /** Second element */
    public E _2;

    public Pair(E _1, E _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public E get_1() {
        return _1;
    }

    public void set_1(E _1) {
        this._1 = _1;
    }

    public E get_2() {
        return _2;
    }

    public void set_2(E _2) {
        this._2 = _2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (_1 != null ? !_1.equals(pair._1) : pair._1 != null) return false;
        if (_2 != null ? !_2.equals(pair._2) : pair._2 != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _1 != null ? _1.hashCode() : 0;
        result = 31 * result + (_2 != null ? _2.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("(%s,%s)", this._1.toString(), this._2.toString());
    }
}
