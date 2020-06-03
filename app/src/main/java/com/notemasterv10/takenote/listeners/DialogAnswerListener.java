package com.notemasterv10.takenote.listeners;

import com.notemasterv10.takenote.interaction.ComplexBooleanAnswer;
import com.notemasterv10.takenote.interaction.ComplexStringAnswer;
import com.notemasterv10.takenote.interaction.IntegerAnswer;

public interface DialogAnswerListener {
    public void integerAnswerConfirmed(IntegerAnswer answer);
    public void saveNote(ComplexStringAnswer answer);
    public void renameNote(ComplexStringAnswer answer);
    public void deleteNote(ComplexBooleanAnswer answer);
}
