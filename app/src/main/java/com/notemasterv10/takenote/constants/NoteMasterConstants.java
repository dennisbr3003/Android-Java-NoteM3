package com.notemasterv10.takenote.constants;

public interface NoteMasterConstants {
    enum NoteAction{
        SAVE_RETURN, SAVE_NEW
    }

    String BACKGROUND_COLOR = "BackGroundColor";
    String SHAREDPREF_NAME = "TakeNote";
    String NOTE_FILENAME = "sometextfile";
    String PASSPOINT_IMAGE = "passpointImage";
    String SETTING_UNKNOWN = "Unknown";
    String PASSPOINTS_SET = "PointsSet";
    String CAMERA_ABSOLUTE_FILEPATH = "New_Photo_Filepath";
    String SAVED_INSTANCE_EDITORTEXT_TAG = "EditorText";
    String CAMERA_PHOTO_FILENAME = "passpoint_picture.jpg";
    String OPEN_NOTE = "open_note";
    String NO_FILENAME = "no_name_entered";
    String NOTELIST_FRAGMENT_TAG = "note_list";

    int REQUEST_ID_CAMERA = 263;
    int REQUEST_ID_GALLERY = 264;
    int DEFAULT_EDITOR_BACKGROUND_COLOR = -1;

    // PointCollector
    int MAX_DEVIATION = 40;

    // Custom Dialog change passpoint picture
    int CANCEL_CLICKED = 0;
    int CAMERA_CLICKED = 1;
    int GALLERY_CLICKED = 2;

}
