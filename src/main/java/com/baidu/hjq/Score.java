package com.baidu.hjq;

public class Score {
    private String key;
    private int score = 0;
    private ZhidaoType type = ZhidaoType.Normal;

    public String format() {
        return key + "\t" + score + "\t" + this.type.getDesc();
    }

    public String getKey() {
        return key;
    }

    public Score setKey(String key) {
        this.key = key;
        return this;
    }

    public int getScore() {
        return score;
    }

    public Score setScore(int score) {
        this.score = score;
        return this;
    }

    public ZhidaoType getType() {
        return type;
    }

    public Score setType(ZhidaoType type) {
        this.type = type;
        return this;
    }
}
