package com.notemasterv10.takenote.constants;

public interface DatabaseConstants {

    String DB_NAME = "takenote.db";
    int DB_VERSION = 1;

    String TABLE_PNTS = "POINTS";
    String PNTS_ID = "ID";
    String PNTS_X = "X";
    String PNTS_Y = "Y";

    String TABLE_NTS = "NOTES";
    String NTS_ID = "ID";
    String NTS_NAME = "NAME";
    String NTS_CREATED = "CREATED";
    String NTS_FILE = "FILE";
    String NTS_UPDATED = "UPDATED";

    String TABLE_PPI = "PPIMAGE";
    String PPI_ID = "ID";
    String PPI_NAME = "NAME";
    String PPI_CREATED = "CREATED";
    String PPI_FILE = "FILE";
    String PPI_UPDATED = "UPDATED";

}
