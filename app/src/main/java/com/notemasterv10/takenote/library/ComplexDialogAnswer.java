package com.notemasterv10.takenote.library;

public class ComplexDialogAnswer {

    private String answer="";
    private String extraInstruction="";

    public ComplexDialogAnswer() {
    }

    public ComplexDialogAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getExtraInstruction() {
        return extraInstruction;
    }

    public void setExtraInstruction(String extraInstruction) {
        this.extraInstruction = extraInstruction;
    }
}
