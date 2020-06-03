package com.notemasterv10.takenote.interaction;

public class ComplexStringAnswer extends Answer {

    private String answer="";

    private byte[] new_content = null;
    private String open_existing_note = "";
    private byte[] current_content = null;

    private String rename_oldname ="";
    private String rename_newname ="";
    private boolean rename_iscurrent = false;

    private int position=0;

    public ComplexStringAnswer() {
        super("");
    }
    public ComplexStringAnswer(String answer) {
        super("");
        this.answer = answer;
    }

    public byte[] getNew_content() {
        return new_content;
    }

    public void setNew_content(byte[] new_content) {
        this.new_content = new_content;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
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

    public boolean getRename_iscurrent() {
        return rename_iscurrent;
    }

    public void setRename_iscurrent(boolean rename_iscurrent) {
        this.rename_iscurrent = rename_iscurrent;
    }

    public byte[] getCurrent_content() {
        return current_content;
    }

    public void setCurrent_content(byte[] current_content) {
        this.current_content = current_content;
    }

    public String getOpen_existing_note() {
        return open_existing_note;
    }

    public void setOpen_existing_note(String open_existing_note) {
        this.open_existing_note = open_existing_note;
    }

}
