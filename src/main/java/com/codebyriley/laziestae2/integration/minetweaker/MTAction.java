package com.codebyriley.laziestae2.integration.minetweaker;

import minetweaker.IUndoableAction;

/** Base for our tweaker actions; scripts are reapplied on reload, so undo is a no-op. */
public abstract class MTAction implements IUndoableAction {

    private final String description;

    protected MTAction(String description) {
        this.description = description;
    }

    @Override
    public boolean canUndo() {
        return true;
    }

    @Override
    public void undo() { }

    @Override
    public String describe() {
        return description;
    }

    @Override
    public String describeUndo() {
        return "Reverting: " + description;
    }

    @Override
    public Object getOverrideKey() {
        return null;
    }
}
