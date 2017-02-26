package org.echosoft.common.cli.display;

import java.io.Serializable;

/**
 * @author Anton Sharapov
 */
public interface CellValueFormatter extends Serializable {

    public String format(Object obj);
}
