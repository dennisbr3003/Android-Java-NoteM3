package com.notemasterv10.takenote.library;

public class ComplexDialogAnswer {

    private String answer="";
    private String extraInstruction="";
    private byte[] content = null;

    public ComplexDialogAnswer() {
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
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
