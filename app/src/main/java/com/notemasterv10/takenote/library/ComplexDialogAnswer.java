package com.notemasterv10.takenote.library;

public class ComplexDialogAnswer {

    private String answer="";
    private String extraInstruction="";
    private byte[] content = null;
    private String rename_oldname ="";
    private String rename_newname ="";
    private int position=0;

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
    public String getRename_oldname() {
        return rename_oldname;
    }

    public void setRename_oldname(String rename_oldname) {
        this.rename_oldname = rename_oldname;
    }

    public String getRename_newname() {
        return rename_newname;
    }

    public void setRename_newname(String rename_newname) {
        this.rename_newname = rename_newname;
    }
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

}
