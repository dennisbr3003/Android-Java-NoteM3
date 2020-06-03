package com.notemasterv10.takenote.interaction;

public class SingleIntegerAnswer extends Answer {
    private int answer = 0;

    public SingleIntegerAnswer() {
        super("");
    }

    public SingleIntegerAnswer(int answer) {
        super("");
        this.answer = answer;
    }

    public SingleIntegerAnswer(int answer, String extraInstructions) {
        super(extraInstructions);
        this.answer = answer;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }
}
