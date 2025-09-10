package com.example.aslib.vo;

import java.util.Objects;

public class MyGes    // class@001363 from classes2.dex
{
    private int num;
    private String pos;

    public MyGes(int num, String pos) {
        this.num = num;
        this.pos = pos;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyGes myGes = (MyGes) o;
        return num == myGes.num;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(num);
    }

    public int getNum() {
        return this.num;
    }

    public String getPos() {
        return this.pos;
    }


    public void setNum(int p0) {
        this.num = p0;
    }

    public void setPos(String p0) {
        this.pos = p0;
    }

    public String toString() {
        return this.pos;
    }
}
