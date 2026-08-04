package com.codebyriley.laziestae2.client.gui;

import java.awt.Rectangle;

/** A GUI that draws IO tabs outside its panel, so NEI can leave room for them. */
public interface ISideIoGui {

    /** Screen-space bounds of the tabs, or null when none are drawn. */
    Rectangle getSideIoBounds();
}
