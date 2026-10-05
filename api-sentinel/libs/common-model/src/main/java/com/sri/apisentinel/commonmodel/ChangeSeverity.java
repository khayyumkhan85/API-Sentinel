package com.sri.apisentinel.commonmodel;

public enum ChangeSeverity {
    NONE(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int rank;

    ChangeSeverity(int rank) {
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }

    public boolean atLeast(ChangeSeverity other) {
        return this.rank >= other.rank;
    }
}