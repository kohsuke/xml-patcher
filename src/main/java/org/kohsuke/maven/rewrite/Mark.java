package org.kohsuke.maven.rewrite;

/**
 * Points to a single event in XML, which is a region of characters.
 *
 * @author Kohsuke Kawaguchi
 */
public class Mark {
    /**
     * Character position of the start and the end of the mark.
     *
     * The region is [start,end)
     */
    /*package*/ int s, e;
    private final XmlPatcher patcher;

    Mark(XmlPatcher patcher) {
        this.patcher = patcher;
        patcher.marks.add(this);
    }

    /**
     * Releases this mark.
     *
     * Until closed, a mark is tracked by {@link XmlPatcher}, so it'll leak memory unless it's cleared at the end.
     * A mark can also get automatically closed if an update to another mark renders this mark obsolete.
     */
    public void clear() {
        s = e =-1;
        patcher.marks.remove(this);
    }

    /**
     * Checks if this mark is still active.
     */
    boolean isSet() {
        return s !=-1;
    }

    /**
     * Gets the XML text that this mark is pointing at.
     */
    public String verbatim() {
        return isSet() ? patcher.xml.substring(s, e) : "";
    }

    /**
     * Replaces what this mark is pointing at by the specified text.
     */
    public void replace(String replacement) {
        if (!isSet())
            throw new IllegalStateException();
        patcher.xml.replace(s, e, replacement);
        int d = replacement.length() - length();
        patcher.updateMarks(this,d);
    }

    /**
     * Update this mark to point to the current location.
     */
    public void set() {
        set(patcher.getLast());
    }

    void set(int start, int end) {
        this.s = start;
        this.e = end;
    }

    public boolean isLeftOf(Mark that) {
        return e <= that.s;
    }

    public boolean isRightOf(Mark that) {
        return that.isLeftOf(this);
    }

    int length() {
        return e - s;
    }

    public void set(Mark that) {
        this.s = that.s;
        this.e = that.e;
    }

    /**
     * If another mark changes its length by delta, update the position of this mark
     * so that it points to the same thing
     */
    void update(Mark that, int delta) {
        if (!isSet() || delta==0)
            return; // nothing to update
        if (this.isLeftOf(that))
            return; // change to the right. this mark is unaffected
        if (that.isLeftOf(this)) {
            shift(delta);
            return; // change to the left. move our position accordingly
        }
        if (this.contains(that)) {
            grow(delta);
            return; // inside this mark, just update the range
        }
        // otherwise there's some overlap, but can't really do a meaningful update
        clear();
    }

    void shift(int delta) {
        s += delta;
        e += delta;
    }

    void grow(int delta) {
        e += delta;
    }


    /**
     * Check if two marks points to the same range.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mark mark = (Mark) o;

        return e == mark.e && s == mark.s;
    }

    @Override
    public int hashCode() {
        int result = s;
        result = 31 * result + e;
        return result;
    }

    /**
     * Does this mark contains the other mark?
     */
    public boolean contains(Mark that) {
        return this.s <=that.s && that.e <=this.e;
    }

    /**
     * Creates a new mark that spans the range in between this and that mark.
     */
    public Mark to(Mark that) {
        return between(this,that);
    }

    /**
     * Creates a new mark that spans the range in between this mark and the current position of {@link XmlPatcher}.
     */
    public Mark toCurrent() {
        return between(this,patcher.getLast());
    }

    @Override
    public String toString() {
        return verbatim();
    }

    /**
     * Computes a mark that covers the range between two marks.
     */
    static Mark between(Mark a, Mark b) {
        if (a.isSet() && b.isSet() && a.isLeftOf(b)) {
            Mark m = new Mark(a.patcher);
            m.set(a.e,b.s);
            return m;
        }

        throw new IllegalStateException();
    }


}
