package com.notemasterv10.takenote.interaction;

public class IntegerAnswer extends Answer {
    private int answer = 0;

    public IntegerAnswer() {
        super("");
    }

    public IntegerAnswer(int answer) {
        super("");
        this.answer = answer;
    }

    public IntegerAnswer(int answer, String extraInstructions) {
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
