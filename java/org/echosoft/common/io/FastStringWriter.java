package org.echosoft.common.io;

import java.io.IOException;
import java.io.Writer;

/**
 * A character stream that collects its output in a character buffer, 
 * which can then be used to construct a string.
 * The main difference from the java.io.StringWriter is non synchronized methods.
 * @author Anton Sharapov
 */
public final class FastStringWriter extends Writer {
    private static final int INITIAL_CAPACITY = 16;

    /**
     * The value is used for character storage.
     */
    private char value[];

    /** 
     * The count is the number of characters in the buffer.
     */
    private int count;



    /**
     * Constructs a string writer with no characters in it and an 
     * default initial capacity characters 
     */
    public FastStringWriter() {
        this(INITIAL_CAPACITY);
    }

    /**
     * Constructs a string writer with no characters in it and an 
     * initial capacity specified by the <code>capacity</code> argument. 
     *
     * @param      capacity   the initial capacity.
     * @exception  NegativeArraySizeException  if the <code>length</code>
     *               argument is less than <code>0</code>.
     */
    public FastStringWriter(final int capacity) {
        value = new char[capacity];
    }

    /**
     * Constructs a string writer so that it represents the same
     * sequence of characters as the string argument; in other
     * words, the initial contents of the string writer is a copy of the
     * argument string. The initial capacity of the string writer is
     * <code>16</code> plus the length of the string argument.
     *
     * @param   str   the initial contents of the buffer.
     * @exception NullPointerException if <code>str</code> is <code>null</code>
     */
    public FastStringWriter(final String str) {
        final int capacity = INITIAL_CAPACITY + str.length();
        value = new char[capacity];
        write(str);
    }

    /**
     * Write a string. 
     * @param str  a string.
     */
    public void write(String str) {
        if (str == null) {
            str = String.valueOf(str);
        }

        final int len = str.length();
        final int newcount = count + len;
        if (newcount > value.length)
            expandCapacity(newcount);
        str.getChars(0, len, value, count);
        count = newcount;
    }

    /**
     * Write a portion of a string.
     * @param str  A string
     * @param offset  Offset from which to start writing characters
     * @param len  Number of characters to write
     */
    public void write(final String str, final int offset, final int len) {
        write(str.substring(offset, offset+len));
    }

    /**
     * Write an array of characters.
     * @param   str   the characters to be writen.
     */
    public void write(final char str[]) { 
        final int len = str.length;
        final int newcount = count + len;
        if (newcount > value.length)
            expandCapacity(newcount);
        System.arraycopy(str, 0, value, count, len);
        count = newcount;
    }

    /**
     * Writes the string representation of a subarray of the 
     * <code>char</code> array argument to this string buffer. 
     * <p>
     * Characters of the character array <code>str</code>, starting at 
     * index <code>offset</code>, are appended, in order, to the contents 
     * of this string buffer. The length of this string buffer increases 
     * by the value of <code>len</code>. 
     * <p>
     * The overall effect is exactly as if the arguments were converted to 
     * a string by the method {@link String#valueOf(char[],int,int)} and the
     * characters of that string were then {@link #write(String) appended} 
     * to this <code>FastStringBuffer</code> object.
     *
     * @param   str      the characters to be appended.
     * @param   offset   the index of the first character to append.
     * @param   len      the number of characters to append.
     */
    public void write(final char str[], final int offset, final int len) {
        final int newcount = count + len;
        if (newcount > value.length)
            expandCapacity(newcount);
        System.arraycopy(str, offset, value, count, len);
        count = newcount;
    }

    /**
     * Writes the string representation of the <code>char</code> 
     * argument to this string buffer. 
     * <p>
     * The argument is appended to the contents of this string buffer. 
     * The length of this string buffer increases by <code>1</code>. 
     * <p>
     * The overall effect is exactly as if the argument were converted to 
     * a string by the method {@link String#valueOf(char)} and the character 
     * in that string were then {@link #write(String) appended} to this
     * <code>FastStringBuffer</code> object.
     * @param   c   a <code>char</code>.
     */
    public void write(final char c) {
        final int newcount = count + 1;
        if (newcount > value.length)
            expandCapacity(newcount);
        value[count++] = c;
    }

    /**
     * Write a single character.
     */
    public void write(final int c) {
        write( (char)c );
    }

    /**
     * Write the contents of this FastStringWriter into a Writer.
     * @param out The writer into which to place the contents of this writer.
     * @throws IOException in case of any errors occurs.
     */
    public void writeOut(final Writer out) throws IOException {
        out.write(value, 0, count);
    }

    /**
     * Write the contents of this FastStringWriter into a other FastStringWriter.
     * @param out The writer into which to place the contents of this writer.
     */
    public void writeOut(final FastStringWriter out) {
        out.write(value, 0, count);
    }

    /**
     * Flush the stream.
     */
    public void flush() {
    }

    /**
     * Close the stream.
     */
    public void close() {
    }

    /**
     * Returns count of the chars, passed into this writer.
     * @return  chars count.
     */
    public int length() {
        return count;
    }

    /**
     * Return the buffer's current value as a string.
     * @return  a string representation of the object.
     */
    public String toString() {
        return new String(value, 0, count);
    }


    /**
     * Ensures that the capacity of the buffer is at least equal to the
     * specified minimum.
     * If the current capacity of this string buffer is less than the 
     * argument, then a new internal buffer is allocated with greater 
     * capacity. The new capacity is the larger of: 
     * <ul>
     * <li>The <code>minimumCapacity</code> argument. 
     * <li>Twice the old capacity, plus <code>2</code>. 
     * </ul>
     * If the <code>minimumCapacity</code> argument is nonpositive, this
     * method takes no action and simply returns.
     *
     * @param   minimumCapacity   the minimum desired capacity.
     */
    private void expandCapacity(final int minimumCapacity) {
        int newCapacity = (value.length + 1) * 2;
        if (newCapacity < 0) {
            newCapacity = Integer.MAX_VALUE;
        } else
        if (minimumCapacity > newCapacity) {
            newCapacity = minimumCapacity;
        }

        final char newValue[] = new char[newCapacity];
        System.arraycopy(value, 0, newValue, 0, count);
        value = newValue;
    }
}
