package com.notemasterv10.takenote.listeners;

import com.notemasterv10.takenote.interaction.ExtendedBooleanAnswer;
import com.notemasterv10.takenote.interaction.ExtendedStringAnswer;
import com.notemasterv10.takenote.interaction.SingleIntegerAnswer;

public interface DialogAnswerListener {
    public void integerAnswerConfirmed(SingleIntegerAnswer answer);
    public void saveNote(ExtendedStringAnswer answer);
    public void renameNote(ExtendedStringAnswer answer);
    public void deleteNote(ExtendedBooleanAnswer answer);
}
