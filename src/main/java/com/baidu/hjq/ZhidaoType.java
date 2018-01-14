package com.baidu.hjq;

public enum ZhidaoType {
    Normal("1"),
    Top("2");

    private final String desc;

    ZhidaoType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
