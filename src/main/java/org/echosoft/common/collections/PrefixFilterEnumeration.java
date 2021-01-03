package org.echosoft.common.collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Filter the source enumeration of the strings by theirs prefix.
 * 
 * @author Anton Sharapov
 */
public class PrefixFilterEnumeration implements Enumeration<String> {
    private final Enumeration e;
    private final String prefix;
    private final int prefixLength;
    private String nextElement;
    private boolean hasNextObject = false;

    public PrefixFilterEnumeration(Enumeration e, String prefix) {
        this.e = e;
        this.prefix = prefix;
        this.prefixLength = prefix.length();
    }

    /**
     *  Returns true if the underlying enumeration contains string value with specified prefix.
     *  @return true if there is another value with same prefix.
     */
    public boolean hasMoreElements() {
        return hasNextObject || findNextObject();
    }

    /**
     *  Returns the next object that contains specified prefix.
     *  @return the next object which contains specified prefix.
     *  @throws NoSuchElementException if there are no more elements that match the predicate
     */
    public String nextElement() {
        if ( !hasNextObject ) {
            if (!findNextObject()) {
                throw new NoSuchElementException();
            }
        }
        hasNextObject = false;
        return nextElement;
    }

    private boolean findNextObject() {
        while ( e.hasMoreElements() ) {
            final String element = (String)e.nextElement();
            if (element.startsWith(prefix)) {
                nextElement = element.substring(prefixLength);
                hasNextObject = true;
                return true;
            }
        }
        return false;
    }
}
