package fr.oxilea.myhome;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SettingBdd extends SQLiteOpenHelper {

    private static final String SETTING_TABLE="settingTable";
    private static final String ID="Id";
    private static final String OBJECT_NAME="Object_Name";
    private static final String OBJECT_INDEX="Object_Index";
    private static final String OBJECT_CDETYPE="Object_Command";
    private static final String OBJECT_IP_ADDRESS="Object_IP_Address";
    private static final String OBJECT_IP_PORT="Object_IP_Port";
    private static final String OBJECT_LOGIN="Object_login";
    private static final String OBJECT_PSW="Object_password";
    private static final String OBJECT_ICON="Object_Icon";
    private static final String OBJECT_PROTOCOL="Object_protocol";
    private static final String OBJECT_STATE="Object_State";

    private static final String CREATE_BDD = "CREATE TABLE " + SETTING_TABLE + " ("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + OBJECT_NAME + " TEXT NOT NULL, "
            + OBJECT_INDEX + " TEXT NOT NULL, "
            + OBJECT_CDETYPE + " TEXT NOT NULL, "
            + OBJECT_IP_ADDRESS + " TEXT NOT NULL, "
            + OBJECT_IP_PORT + " TEXT NOT NULL, "
            + OBJECT_LOGIN + " TEXT NOT NULL, "
            + OBJECT_PSW + " TEXT NOT NULL, "
            + OBJECT_ICON + " TEXT NOT NULL, "
            + OBJECT_PROTOCOL + " TEXT NOT NULL, "
            + OBJECT_STATE + " TEXT NOT NULL);";

    public SettingBdd(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create table from variable CREATE_BDD
        db.execSQL(CREATE_BDD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Suppress and create the table again, on new version id restart from 0
        db.execSQL("DROP TABLE " + SETTING_TABLE + ";");
        onCreate(db);
    }
}
