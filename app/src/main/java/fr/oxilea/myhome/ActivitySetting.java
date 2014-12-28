package fr.oxilea.myhome;


import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class ActivitySetting extends Activity {

    int currentEditedId=-1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the current edited id from the value set on intent
        Bundle b = getIntent().getExtras();
        currentEditedId = b.getInt("Id");

        setContentView(R.layout.settings_scr);

        // manage spinner for icon type
        Spinner spinner = (Spinner) findViewById(R.id.spinnerIcon);
        String iconType="0";

        // manage spinner for protocol type
        Spinner spinnerProtocol = (Spinner) findViewById(R.id.spinnerProtocol);
        String protocolType="0";

        if (currentEditedId != -1){
            // this is an update of an already defined setting, should retrieve data from BDD
            DeviceBdd mySettingBdd = new DeviceBdd(this);
            mySettingBdd.open();
            ConnectedObject myObject= new ConnectedObject();
            myObject = mySettingBdd.getObjectWithId(currentEditedId);
            mySettingBdd.close();

            // set the already defined Device Name
            TextView myTextView = (TextView) findViewById(R.id.editTextDevice);
            myTextView.setText(myObject.GetObjectName());

            // set current command type value (pulse OFF/ON)
            Switch s = (Switch) findViewById(R.id.switchPulse);
            if (s != null) {
                Boolean valSwitch = !(myObject.GetObjectCdeType().equals("0"));
                s.setChecked(valSwitch);
            }

            myTextView = (TextView) findViewById(R.id.editTextDeviceAdd);
            myTextView.setText(myObject.GetObjectIpAddress());

            myTextView = (TextView) findViewById(R.id.editTextDevicePort);
            myTextView.setText(myObject.GetObjectIpPort());

            myTextView = (TextView) findViewById(R.id.editSettingLogin);
            myTextView.setText(myObject.GetObjectLogin());

            myTextView = (TextView) findViewById(R.id.editSettingPsw);
            myTextView.setText(myObject.GetObjectPassword());

            // set the current icon spinner value
            iconType = myObject.GetObjectIconType();

            // set the current protocol spinner value
            protocolType = myObject.GetObjectProtocol();


        }

        // Icon spinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.icons_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        int iconIndex = Integer.parseInt(iconType);
        spinner.setSelection(iconIndex);

        // Same for protocol spinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterP = ArrayAdapter.createFromResource(this,
                R.array.protocol_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapterP.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerProtocol.setAdapter(adapterP);

        int protocolIndex = Integer.parseInt(protocolType);
        spinnerProtocol.setSelection(protocolIndex);


    }


    // save or update device from BDD
    public void saveDeviceSetting(View v)
    {
        // get all inputs
        TextView myTextView = (TextView) findViewById(R.id.editTextDevice);
        String deviceName = myTextView.getText().toString();

        Switch s = (Switch)findViewById(R.id.switchPulse);
        String deviceCdeType = "0";
        if (s.isChecked())
            deviceCdeType="1";

        myTextView = (TextView) findViewById(R.id.editTextDeviceAdd);
        String deviceDeviceAdd = myTextView.getText().toString();

        myTextView = (TextView) findViewById(R.id.editTextDevicePort);
        String deviceDevicePort = myTextView.getText().toString();

        myTextView = (TextView) findViewById(R.id.editSettingLogin);
        String deviceDeviceLogin = myTextView.getText().toString();

        myTextView = (TextView) findViewById(R.id.editSettingPsw);
        String deviceDevicePsw = myTextView.getText().toString();

        // manage spinner for icon type
        Spinner spinner = (Spinner) findViewById(R.id.spinnerIcon);
        String deviceDeviceIcon = String.valueOf(spinner.getSelectedItemPosition());

        // manage spinner for protocol type
        Spinner spinnerP = (Spinner) findViewById(R.id.spinnerProtocol);
        String deviceDeviceProtocol = String.valueOf(spinnerP.getSelectedItemPosition());

        DeviceBdd mySettingBdd = new DeviceBdd(this);
        mySettingBdd.open();

        Cursor c = mySettingBdd.getBDD().rawQuery("select * from settingTable",null);
        int numRows;

        if (currentEditedId == -1) {
            // save new device with Index value equals numRows
            numRows = c.getCount();

            // free version allow only 2 devices
            /*if (numRows>=2){
                Toast.makeText(getApplicationContext(), R.string.limited_version, Toast.LENGTH_SHORT).show();

                // close BDD
                mySettingBdd.close();

                // exit setting activity
                finish();

                // exit from method
                return;
            }*/
        }
        else
        {
            // keep current index
            numRows = currentEditedId;
        }

        String deviceIndex = String.valueOf(numRows);

        ConnectedObject myObject= new ConnectedObject(deviceName, deviceIndex, deviceCdeType, deviceDeviceAdd, deviceDevicePort, deviceDeviceLogin, deviceDevicePsw, deviceDeviceIcon, deviceDeviceProtocol, "unknown");

        if (currentEditedId == -1) {
                // this is a new object creation}
                mySettingBdd.insertObject(myObject);
            }
                else
            {
                // this is an update of an already defined setting
                mySettingBdd.updateObject(currentEditedId, myObject);
            }

            // close BDD
            mySettingBdd.close();

            // exit setting activity
            finish();
    }


    // remove device from BDD
    public void deleteDeviceSetting(View v)
    {
        if (currentEditedId != -1) {
            DeviceBdd mySettingBdd = new DeviceBdd(this);
            mySettingBdd.open();

            mySettingBdd.removeObjectWithID(currentEditedId);

            // update all database device index
            mySettingBdd.ReorderObjectInBdd();

            // re-initialize powerTab too
            MyActivity.mypowerTab.ResetMeasurementTab();

            // close BDD
            mySettingBdd.close();
        }

        // exit setting activity
        finish();
    }
}

