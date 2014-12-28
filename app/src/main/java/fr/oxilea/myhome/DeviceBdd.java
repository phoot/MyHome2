package fr.oxilea.myhome;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DeviceBdd {

    private static final int VERSION_BDD = 4;
    private static final String NOM_BDD = "myhome.db";

    private static final String SETTING_TABLE="settingTable";
    private static final String ID="Id";
    private static final int NUM_ID=0;
    private static final String COL_OBJECT_NAME="Object_Name";
    private static final int NUM_COL_OBJECT_NAME=1;
    private static final String COL_OBJECT_INDEX="Object_Index";
    private static final int NUM_COL_OBJECT_INDEX=2;
    private static final String COL_OBJECT_CDETYPE="Object_Command";
    private static final int NUM_COL_OBJECT_CDETYPE=3;
    private static final String COL_OBJECT_IP_ADDRESS="Object_IP_Address";
    private static final int NUM_COL_OBJECT_IP_ADDRESS=4;
    private static final String COL_OBJECT_IP_PORT="Object_IP_Port";
    private static final int NUM_COL_OBJECT_IP_PORT=5;
    private static final String COL_OBJECT_LOGIN="Object_login";
    private static final int NUM_COL_OBJECT_LOGIN=6;
    private static final String COL_OBJECT_PSW="Object_password";
    private static final int NUM_COL_OBJECT_PSW=7;
    private static final String COL_OBJECT_ICON="Object_Icon";
    private static final int NUM_COL_OBJECT_ICON=8;
    private static final String COL_OBJECT_PROTOCOL="Object_Protocol";
    private static final int NUM_COL_OBJECT_PROTOCOL=9;
    private static final String COL_OBJECT_STATE="Object_State";
    private static final int NUM_COL_OBJECT_STATE=10;

    private SQLiteDatabase bdd;

    private SettingBdd maBaseSetting;

    public DeviceBdd(Context context){
        // create database a
        maBaseSetting = new SettingBdd(context, NOM_BDD, null, VERSION_BDD);
    }

    public void open(){
        //open database in write mode
        bdd = maBaseSetting.getWritableDatabase();
    }

    public void close(){
        //close database
        bdd.close();
    }

    public SQLiteDatabase getBDD(){
        return bdd;
    }

    public long insertObject(ConnectedObject myConnectedObjet){

        // create ContentValues (behaviour as HashMap)
        ContentValues values = new ContentValues();

        // add each object element)
        values.put(COL_OBJECT_NAME, myConnectedObjet.GetObjectName());
        values.put(COL_OBJECT_INDEX, myConnectedObjet.GetObjectIndex());
        values.put(COL_OBJECT_CDETYPE, myConnectedObjet.GetObjectCdeType());
        values.put(COL_OBJECT_IP_ADDRESS, myConnectedObjet.GetObjectIpAddress());
        values.put(COL_OBJECT_IP_PORT, myConnectedObjet.GetObjectIpPort());
        values.put(COL_OBJECT_LOGIN, myConnectedObjet.GetObjectLogin());
        values.put(COL_OBJECT_PSW, myConnectedObjet.GetObjectPassword());
        values.put(COL_OBJECT_ICON, myConnectedObjet.GetObjectIconType());
        values.put(COL_OBJECT_PROTOCOL, myConnectedObjet.GetObjectProtocol());
        values.put(COL_OBJECT_STATE, myConnectedObjet.GetObjectState());

        //insert in database
        return bdd.insert(SETTING_TABLE, null, values);
    }

    public int updateObject(int id, ConnectedObject myConnectedObjet){
        // Same behaviour as creation but need to give the Id (objectId) we want to update
        ContentValues values = new ContentValues();
        values.put(COL_OBJECT_NAME, myConnectedObjet.GetObjectName());
        values.put(COL_OBJECT_INDEX, myConnectedObjet.GetObjectIndex());
        values.put(COL_OBJECT_CDETYPE, myConnectedObjet.GetObjectCdeType());
        values.put(COL_OBJECT_IP_ADDRESS, myConnectedObjet.GetObjectIpAddress());
        values.put(COL_OBJECT_IP_PORT, myConnectedObjet.GetObjectIpPort());
        values.put(COL_OBJECT_LOGIN, myConnectedObjet.GetObjectLogin());
        values.put(COL_OBJECT_PSW, myConnectedObjet.GetObjectPassword());
        values.put(COL_OBJECT_ICON, myConnectedObjet.GetObjectIconType());
        values.put(COL_OBJECT_PROTOCOL, myConnectedObjet.GetObjectProtocol());
        values.put(COL_OBJECT_STATE, myConnectedObjet.GetObjectState());

        return bdd.update(SETTING_TABLE, values, COL_OBJECT_INDEX + " = " +id, null);
    }

    public int removeObjectWithID(int id){
        //Remove Object from BDD using the IDEX
        return bdd.delete(SETTING_TABLE, COL_OBJECT_INDEX + " = " +id, null);
    }

    public ConnectedObject getObjectWithId(int id){
        // get full object definition
        Cursor c = bdd.query(SETTING_TABLE, new String[] {ID, COL_OBJECT_NAME, COL_OBJECT_INDEX, COL_OBJECT_CDETYPE, COL_OBJECT_IP_ADDRESS, COL_OBJECT_IP_PORT, COL_OBJECT_LOGIN, COL_OBJECT_PSW, COL_OBJECT_ICON, COL_OBJECT_PROTOCOL, COL_OBJECT_STATE}, COL_OBJECT_INDEX + " LIKE \"" + id +"\"", null, null, null, null);
        return cursorToConnectedObject(c);
    }


    public void ReorderObjectInBdd()
    {
        // refactor INDEX
        // read<all database row and set the index with the row number
        // get the number<of row
        Cursor c = this.getBDD().rawQuery("select * from settingTable",null);
        int numRows = c.getCount();
        c.moveToFirst();
        ConnectedObject myObj= new ConnectedObject();
        int i=0;
        while (i < numRows)
        {
            //retrieve the full object from database
            //set info from Cursor
            myObj.SetObjectName(c.getString(NUM_COL_OBJECT_NAME));
            myObj.SetObjectCdeType(c.getString(NUM_COL_OBJECT_CDETYPE));
            myObj.SetObjectIpAddress(c.getString(NUM_COL_OBJECT_IP_ADDRESS));
            myObj.SetObjectIpPort(c.getString(NUM_COL_OBJECT_IP_PORT));
            myObj.SetObjectLogin(c.getString(NUM_COL_OBJECT_LOGIN));
            myObj.SetObjectPassword(c.getString(NUM_COL_OBJECT_PSW));
            myObj.SetObjectIconType(c.getString(NUM_COL_OBJECT_ICON));
            myObj.SetObjectProtocol(c.getString(NUM_COL_OBJECT_PROTOCOL));
            myObj.SetObjectState(c.getString(NUM_COL_OBJECT_STATE));

            // set the new index using the old one
            myObj.SetObjectIndex(String.valueOf(i));

            // update in database with new index at the cursor
            this.updateObject(Integer.parseInt(c.getString(NUM_COL_OBJECT_INDEX)), myObj);

            // move to the next row
            c.moveToNext();
            i++;
        }

    }
    //convert cursor to ConnectedObject
    private ConnectedObject cursorToConnectedObject(Cursor c){
        // if no element found, return null
        if (c.getCount() == 0)
            return null;

        //else set to the first element
        c.moveToFirst();

        //create a ConnectedObject
        ConnectedObject myObj = new ConnectedObject();

        //set info from Cursor
        myObj.SetObjectName(c.getString(NUM_COL_OBJECT_NAME));
        myObj.SetObjectIndex(c.getString(NUM_COL_OBJECT_INDEX));
        myObj.SetObjectCdeType(c.getString(NUM_COL_OBJECT_CDETYPE));
        myObj.SetObjectIpAddress(c.getString(NUM_COL_OBJECT_IP_ADDRESS));
        myObj.SetObjectIpPort(c.getString(NUM_COL_OBJECT_IP_PORT));
        myObj.SetObjectLogin(c.getString(NUM_COL_OBJECT_LOGIN));
        myObj.SetObjectPassword(c.getString(NUM_COL_OBJECT_PSW));
        myObj.SetObjectIconType(c.getString(NUM_COL_OBJECT_ICON));
        myObj.SetObjectProtocol(c.getString(NUM_COL_OBJECT_PROTOCOL));
        myObj.SetObjectState(c.getString(NUM_COL_OBJECT_STATE));

        //Close the cursor
        c.close();

        // Return ConnectedObject
        return myObj;
    }


}
