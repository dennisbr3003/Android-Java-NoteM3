package com.notemasterv10.takenote.listeners;

import com.notemasterv10.takenote.library.ComplexDialogAnswer;

public interface DialogAnswerListener {
    public void booleanAnswerConfirmed(Boolean answer);
    public void integerAnswerConfirmed(int answer);
    public void saveAnswerConfirmed(ComplexDialogAnswer answer);
    public void renameAnswerConfirmed(ComplexDialogAnswer answer);
}
