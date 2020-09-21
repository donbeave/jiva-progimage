package com.zhokhov.progimage.shared;

public enum RotationDegree {

    ROTATE_90(90),
    ROTATE_180(180),
    ROTATE_270(270);

    private final int degree;

    RotationDegree(int degree) {
        this.degree = degree;
    }

    public int getDegree() {
        return degree;
    }

}
