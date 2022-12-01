//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.common.core.anno;


import com.common.core.enums.PannoEnum;

public class AnnoKey {
    private PannoEnum findType;
    private String field;

    public void setFindType(PannoEnum findType) {
        this.findType = findType;
    }

    public void setField(String field) {
        this.field = field;
    }

    public PannoEnum getFindType() {
        return this.findType;
    }

    public String getField() {
        return this.field;
    }

    public AnnoKey(PannoEnum findType, String field) {
        this.findType = findType;
        this.field = field;
    }

    public AnnoKey() {
    }

    public String toString() {
        return "AnnoKey(findType=" + this.getFindType() + ", field=" + this.getField() + ")";
    }
}
