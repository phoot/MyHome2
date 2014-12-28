package fr.oxilea.myhome;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;


public class MyActivity extends ListActivity {

    // global message Format
    // Packet head - Total length (2 bytes) - ID - Command - Parameter - Parity
    // 0x55 0xaa - 0x00 0x?? - 0x01 - 0x?? .. - 0x?? .. -0x??
    // TCP over network send full frame

    public static final byte[] STATUS_RELAY_MESSAGE = {0x55, (byte) 0xaa, 0x00, 0x02, 0x00, 0x0a, 0x0c};
    public static final byte[] CDE_ON_RELAY_MESSAGE = {0x55, (byte) 0xaa, 0x00, 0x03, 0x00, 0x02, 0x01, 0x06};
    public static final byte[] CDE_OFF_RELAY_MESSAGE = {0x55, (byte) 0xaa, 0x00, 0x03, 0x00, 0x01, 0x01, 0x05};
    public static final byte[] CDE_SWITCH_RELAY_MESSAGE = {0x55, (byte) 0xaa, 0x00, 0x03, 0x00, 0x03, 0x01, 0x07};

    // supported protocol
    public static final int LONHAND_PROTOCOL=0;
    public static final int MAGINON_PROTOCOL=1;


    // psw hex => 62 37 65 62 38
    // psw str =>  "xxxxx"
    // psw command => psw (bytes) + 0x0d, 0x0a
    public byte[] PASSWORD_RELAY_MESSAGE = {0x0, 0x0, 0x0, 0x0, 0x0, 0x0d, 0x0a};

    // no id in connectedObject type, id only present in database (index is same as bdd minus 1
    public static final int ObjectName = 0;
    public static final int index = 1;
    public static final int type = 2;
    public static final int networkIpAddress = 3;
    public static final int networkPort = 4;
    public static final int devicePswd = 5;

    // maximum possible object managed through this software
    public static final int MAX_MANAGED_OBJECT = 25;

    private Boolean editMode=false;

    private Boolean mScanning=false;

    private MonAdaptateurDeListe adaptateur;

    // touch position
    private int touchPositionX;

    public static DevicePower mypowerTab;
    public static DevicePower MyActivityPowerTab() {
        return mypowerTab;
    }

    public static Context ctx;

    public static Context MyActivityContext() {
        return ctx;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mypowerTab = new DevicePower();

        // update the device list for display
        UpdateObjectListNameForAdaptateur();

        // full relay state check
        new CheckRelayStateTask(true,0).execute();

        // set the context of MyActivity
        ctx = this;

        // set a touch listener, set the x position when touch made on the view
        getListView().setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {
                touchPositionX = (int)event.getX();

                // do not consume the touch as the you need to consume it on the item click here you only set the X coordinate to catch the area of touch
                return false;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        // update the device list for display if another activity (setting new or change) modify it
        UpdateObjectListNameForAdaptateur();

        // full relay state check
        new CheckRelayStateTask(true, 0).execute();
    }
    // allow to construct the list of object managed
    // should be called on activity creation and when the list is updated
    private void UpdateObjectListNameForAdaptateur()
    {
        // add item in that array to add element in the list
        // icon are retrieved when view is displayed
        // retrieve the object definition

        // get the name from bdd
        DeviceBdd mySettingBdd = new DeviceBdd(this);
        mySettingBdd.open();
        ConnectedObject currentDevice= new ConnectedObject();

        String[] valuesTmp = new String[MAX_MANAGED_OBJECT];
        int i=0;
        currentDevice = mySettingBdd.getObjectWithId(i);
        while (currentDevice != null)
        {
            valuesTmp[i] = currentDevice.GetObjectName();
            i++;
            currentDevice = mySettingBdd.getObjectWithId(i);
        }
        mySettingBdd.close();

        // create the array with only the real number of object currently defined
        String[] values = new String[i];
        int j=0;
        while (j <i)
        {
            values[j]= valuesTmp[j];
            j++;
        }

        //String[] values = new String[]{currentDevice.GetObjectDetails(0)[ObjectName], currentDevice.GetObjectDetails(1)[ObjectName]};

        adaptateur = new MonAdaptateurDeListe(this, values);
        setListAdapter(adaptateur);
    }

    void refreshDeviceList()
    {
        UpdateObjectListNameForAdaptateur();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent intent;

        switch (item.getItemId()) {
            case R.id.action_settings:
                // create setting activity
                intent = new Intent(MyActivity.this, ActivitySetting.class);

                // id = 0 new device creation
                intent.putExtra("Id",-1);
                startActivity(intent);
               break;

            case R.id.action_about:
                // create about activity
                intent = new Intent(MyActivity.this, ActivityAbout.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onOptionSwitchMode(MenuItem item) {
        ActionBar bar;
        bar = this.getActionBar();

        switch (item.getItemId()) {
            case R.id.action_edit:

                if (editMode) {
                    // exit edit mode
                    editMode = false;
                    // change action bar color
                    bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_bg));
                } else{
                    // enter edit mode
                    editMode = true;
                    // change action bar color
                    bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_red_bg));
                }

            break;

            case R.id.action_settings:
            case R.id.action_about:
                //nothing to do right now
            break;
        }
    }

   /*
   ** check network status
    */

    public boolean isNetworkOnline() {
        boolean status=false;
        try{
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState()==NetworkInfo.State.CONNECTED) {
                status= true;
            }else {
                netInfo = cm.getNetworkInfo(1);
                if(netInfo!=null && netInfo.getState()==NetworkInfo.State.CONNECTED)
                    status= true;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return status;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        ConnectedObject myCurrentObject;

            if (editMode) {
                //enter edit mode for the item selected
                // create setting activity
                Intent intent;
                intent = new Intent(MyActivity.this, ActivitySetting.class);

                // transmit the id
                intent.putExtra("Id", position);
                startActivity(intent);


            } else {

                // only activate switch on/off when click on the device icon
                if(touchPositionX < 160) {

                    // check if network available
                    if (isNetworkOnline()) {

                        // retrieve the object definition, (corresponding to the list Item click (line position +1 in BDD)
                        DeviceBdd mySettingBdd = new DeviceBdd(this);
                        mySettingBdd.open();
                        myCurrentObject = new ConnectedObject();
                        myCurrentObject = mySettingBdd.getObjectWithId(position);
                        mySettingBdd.close();

                        // Display feedback to the user, ie the object name
                        Toast.makeText(this, myCurrentObject.GetObjectName(), Toast.LENGTH_SHORT).show();

                        // send relay command
                        new SetRelayOnTask(position).execute();
                    } else {
                        // give feedback to the user he needs to activate network
                        Toast.makeText(this, R.string.no_network, Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    // request only status refresh
                    // give feedback to the user he needs to activate network
                    Toast.makeText(this, R.string.check_device_status, Toast.LENGTH_SHORT).show();

                    new CheckRelayStateTask(false, position).execute();

                }
        }
    }

    // TCP connection should be started in a new thread
    class SetRelayOnTask extends AsyncTask<Void,Void,String> {

        int selectedIndex;

        // add new constructor with param
        public SetRelayOnTask(int index) {
            super();
            // set the index of the request
            selectedIndex=index;
        }

        public String doInBackground(Void... params) {

            // retrieve the object definition, (corresponding to the list Item click (line position +1 in BDD)
            DeviceBdd mySettingBdd = new DeviceBdd(MyActivity.MyActivityContext());
            mySettingBdd.open();
            ConnectedObject myCurrentObject = new ConnectedObject();
            myCurrentObject = mySettingBdd.getObjectWithId(selectedIndex);
            mySettingBdd.close();

            switch (Integer.valueOf(myCurrentObject.GetObjectProtocol())) {
                case LONHAND_PROTOCOL:
                    String deviceState = "unknown";
                    // object definition retrieved from myCurrentObject
                    // connect socket on device IP address and Port
                    String ipPortStr = myCurrentObject.GetObjectIpPort();
                    int ipPort;

                    // check if string has only digits
                    if (Pattern.matches("[0-9]+", ipPortStr))
                        ipPort = Integer.parseInt(ipPortStr);
                    else
                        ipPort = 0;

                    TCPClient sTcpClient = new TCPClient(myCurrentObject.GetObjectIpAddress(), ipPort);

                    // send password over TCP socket just connected
                    // get the password and format the command for the device
                    String pwd = myCurrentObject.GetObjectPassword();
                    int pwdLength = pwd.length();
                    int i = 0;
                    while (i < pwdLength) {
                        PASSWORD_RELAY_MESSAGE[i] = (byte) pwd.charAt(i);
                        i++;
                    }
                    // psw end with 0x0d, 0x0a
                    PASSWORD_RELAY_MESSAGE[i++] = (byte) 0x0d;
                    PASSWORD_RELAY_MESSAGE[i] = (byte) 0x0a;

                    String retStatus = sTcpClient.SendOverTCP(PASSWORD_RELAY_MESSAGE, true, false);

                    // check if password is correct (return OK)
                    if (retStatus.equals("OK")) {
                        // check if pulse command
                        if (myCurrentObject.GetObjectCdeType().equals("1")) {
                            sTcpClient.SendOverTCP(CDE_ON_RELAY_MESSAGE, true, false);
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            sTcpClient.SendOverTCP(CDE_OFF_RELAY_MESSAGE, true, false);
                        } else {
                            deviceState = sTcpClient.SendOverTCP(CDE_SWITCH_RELAY_MESSAGE, true, true);
                        }
                        sTcpClient.CloseSocket();
                    } else {
                        // wrong password display feedback to the user (on main thread)
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.wrong_psw, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    // if not pulse mode  save the state of the Object
                    myCurrentObject.SetObjectState(deviceState);

                    // save new object value at selectedIndex
                    mySettingBdd = new DeviceBdd(MyActivity.MyActivityContext());
                    mySettingBdd.open();
                    mySettingBdd.updateObject(selectedIndex, myCurrentObject);
                    mySettingBdd.close();

                    runOnUiThread(new Runnable() {
                        public void run() {
                            refreshDeviceList();
                        }
                    });

                        break;

                case MAGINON_PROTOCOL:
                    HTTPClient sHttpClient= new HTTPClient();

                    // serveur de test online pour recuperer les info de son POST
                    //String httpAdd = "http://posttestserver.com/post.php";

                    // check status of the plug
                    String httpStatusAdd="http://"+myCurrentObject.GetObjectIpAddress()+":"+myCurrentObject.GetObjectIpPort()+"/adm/system_command.asp";
                    String retContent = sHttpClient.connectGet(httpStatusAdd, myCurrentObject.GetObjectLogin(), myCurrentObject.GetObjectPassword());

                    // depending on status either switch the plug on or off
                    String httpAdd = "http://"+myCurrentObject.GetObjectIpAddress()+":"+myCurrentObject.GetObjectIpPort()+"/goform/SystemCommand";

                    // wrong user/password/...  display feedback to the user (on main thread)
                    if (retContent.contains("Unknown User"))
                    {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.wrong_user, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    if (retContent.contains("Unknown Password"))
                    {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.wrong_psw, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    if (retContent.contains("System command"))
                    {
                        /* if (retContent.contains("GPIO1 = [00]")) {
                            sHttpClient.connectPost(httpAdd, "command", "GpioForCrond 1", myCurrentObject.GetObjectLogin(), myCurrentObject.GetObjectPassword());

                            myCurrentObject.SetObjectState("on");

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.switch_on, Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else if (retContent.contains("GPIO1 = [01]")) {

                            // force 0 to initialize command return
                            sHttpClient.connectPost(httpAdd, "command", "GpioForCrond 0", myCurrentObject.GetObjectLogin(), myCurrentObject.GetObjectPassword());

                            myCurrentObject.SetObjectState("off");

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.switch_off, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else*/
                        {
                            // check using measured current
                            // send command
                            String httpCommandAdd = "http://"+myCurrentObject.GetObjectIpAddress()+":"+myCurrentObject.GetObjectIpPort()+"/goform/SystemCommand";

                            sHttpClient.connectPost(httpCommandAdd, "command", "GetInfo W", myCurrentObject.GetObjectLogin(), myCurrentObject.GetObjectPassword());

                            // get result
                            retContent = sHttpClient.connectGet(httpStatusAdd, myCurrentObject.GetObjectLogin(), myCurrentObject.GetObjectPassword());

                            if (retContent.contains("$01W00")) {
                                int startConsoStr = retContent.lastIndexOf("$01W00");
                                String consomW = retContent.substring(startConsoStr+7, startConsoStr+13);

                                //consoW in mW
                                int consoW = Integer.parseInt(consomW);

                                if (consoW > 100) {
                                    sHttpClient.connectPost(httpAdd, "command", "GpioForCrond 0", myCurrentObject.GetObjectLogin(), myCurrentObject.GetObjectPassword());

                                    myCurrentObject.SetObjectState("off");

                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), R.string.switch_off, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                else {
                                    sHttpClient.connectPost(httpAdd, "command", "GpioForCrond 1", myCurrentObject.GetObjectLogin(), myCurrentObject.GetObjectPassword());

                                    myCurrentObject.SetObjectState("on");

                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), R.string.switch_on, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }
                        // save new object value at selectedIndex
                        mySettingBdd = new DeviceBdd(MyActivity.MyActivityContext());
                        mySettingBdd.open();
                        mySettingBdd.updateObject(selectedIndex, myCurrentObject);
                        mySettingBdd.close();

                        //check new power consumption after device switch
                        String httpCommandAdd = "http://"+myCurrentObject.GetObjectIpAddress()+":"+myCurrentObject.GetObjectIpPort()+"/goform/SystemCommand";

                        runOnUiThread(new Runnable() {
                            public void run() {
                                refreshDeviceList();
                            }
                        });

                        // wait 3000 ms for stabilized current
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        sHttpClient.connectPost(httpCommandAdd, "command", "GetInfo W", myCurrentObject.GetObjectLogin(), myCurrentObject.GetObjectPassword());

                        // get result
                        retContent = sHttpClient.connectGet(httpStatusAdd, myCurrentObject.GetObjectLogin(), myCurrentObject.GetObjectPassword());

                        if (retContent.contains("$01W00")) {
                            int startConsoStr = retContent.lastIndexOf("$01W00");
                            String consomW = retContent.substring(startConsoStr + 7, startConsoStr + 13);

                            mypowerTab.AddLivePowerMeasurement(selectedIndex, consomW);
                        }

                        runOnUiThread(new Runnable() {
                            public void run() {
                                refreshDeviceList();
                            }
                        });

                    }
                    else
                    {
                            runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.wrong_access, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    break;
            }
            return "ok";
        }
    }



    class CheckRelayStateTask extends AsyncTask<Void,Void,String> {

        int objectIndex;
        Boolean fullCheck;

        private ConnectedObject curObject;

        // add new constructor with param
        public CheckRelayStateTask(Boolean checkAll, int index) {
            super();

            objectIndex = index;
            fullCheck = checkAll;
        }

        public String doInBackground(Void... params) {

            // retrieve the object definition, (corresponding to the list Item click (line position +1 in BDD)
            DeviceBdd mySettingBdd = new DeviceBdd(MyActivity.MyActivityContext());
            mySettingBdd.open();
            curObject = new ConnectedObject();

            int index = 0;
            Boolean forceExit=false;

            if (!fullCheck) {
                index = objectIndex;
            }

            while (((curObject = mySettingBdd.getObjectWithId(index)) != null) && (forceExit == false))
            {
                switch (Integer.valueOf(curObject.GetObjectProtocol())) {
                    case LONHAND_PROTOCOL:

                        // object definition retrieved from myCurrentObject
                        // connect socket on device IP address and Port
                        String ipPortStr = curObject.GetObjectIpPort();
                        int ipPort;

                        // check if string has only digits
                        if (Pattern.matches("[0-9]+", ipPortStr))
                            ipPort = Integer.parseInt(ipPortStr);
                        else
                            ipPort = 0;

                        TCPClient sTcpClient = new TCPClient(curObject.GetObjectIpAddress(), ipPort);

                        // send password over TCP socket just connected
                        // get the password and format the command for the device
                        String pwd = curObject.GetObjectPassword();
                        int pwdLength = pwd.length();
                        int i = 0;
                        while (i < pwdLength) {
                            PASSWORD_RELAY_MESSAGE[i] = (byte) pwd.charAt(i);
                            i++;
                        }
                        // psw end with 0x0d, 0x0a
                        PASSWORD_RELAY_MESSAGE[i++] = (byte) 0x0d;
                        PASSWORD_RELAY_MESSAGE[i] = (byte) 0x0a;

                        String retStatus = sTcpClient.SendOverTCP(PASSWORD_RELAY_MESSAGE, true, false);

                        // check if password is correct (return OK)
                        if (retStatus.equals("OK")) {
                            // check state
                            retStatus= sTcpClient.SendOverTCP(STATUS_RELAY_MESSAGE, true, true);

                            curObject.SetObjectState(retStatus);

                        } else {
                            // wrong password status unknown
                            curObject.SetObjectState("unknown");
                        }
                        // if socket exists
                        if (sTcpClient.socket != null) {
                            // close this socket
                            sTcpClient.CloseSocket();
                        }
                        break;

                    case MAGINON_PROTOCOL:
                        HTTPClient sHttpClient= new HTTPClient();

                        // serveur de test online pour recuperer les info de son POST
                        //String httpAdd = "http://posttestserver.com/post.php";

                        // command request
                        String httpCommandAdd = "http://"+curObject.GetObjectIpAddress()+":"+curObject.GetObjectIpPort()+"/goform/SystemCommand";

                        // command result address
                        String httpStatusAdd="http://"+curObject.GetObjectIpAddress()+":"+curObject.GetObjectIpPort()+"/adm/system_command.asp";

                        // check status of the plug
                        String retContent = sHttpClient.connectGet(httpStatusAdd, curObject.GetObjectLogin(), curObject.GetObjectPassword());

                        // wrong user/password/...  display feedback to the user (on main thread)
                        if (retContent.contains("Unknown User") || retContent.contains("Unknown Password"))
                        {
                            // status unknown, display the real status from the application side
                            curObject.SetObjectState("unknown");
                        }

                        if (retContent.contains("System command"))
                        {
                            /*if (retContent.contains("GPIO1 = [00]")) {
                                // status Off
                                curObject.SetObjectState("off");
                            } else if (retContent.contains("GPIO1 = [01]")) {
                                // status On
                                curObject.SetObjectState("on");
                            }
                            else*/
                            {
                                // check measured current
                                // send command
                                sHttpClient.connectPost(httpCommandAdd, "command", "GetInfo W", curObject.GetObjectLogin(), curObject.GetObjectPassword());

                                // get result
                                retContent = sHttpClient.connectGet(httpStatusAdd, curObject.GetObjectLogin(), curObject.GetObjectPassword());

                                if (retContent.contains("$01W00")) {
                                    int startConsoStr = retContent.lastIndexOf("$01W00");
                                    String consomW = retContent.substring(startConsoStr+7, startConsoStr+13);

                                    mypowerTab.AddLivePowerMeasurement(index,consomW);

                                    //consoW in mW
                                    int consoW = Integer.parseInt(consomW);

                                    if (consoW > 100) {
                                        curObject.SetObjectState("on");
                                    }
                                    else {
                                        // status unknown
                                        curObject.SetObjectState("off");
                                    }
                                }
                            }
                            sHttpClient.connectPost(httpCommandAdd, "command", "GetInfo E", curObject.GetObjectLogin(), curObject.GetObjectPassword());

                            // get result
                            retContent = sHttpClient.connectGet(httpStatusAdd, curObject.GetObjectLogin(), curObject.GetObjectPassword());

                            if (retContent.contains("$01E00")) {
                                int startConsoStr = retContent.lastIndexOf("$01E00");
                                String consomW = retContent.substring(startConsoStr + 7, startConsoStr + 13);

                                mypowerTab.AddFullPowerMeasurement(index, consomW);
                            }
                        }
                        else
                        {
                            // status unknown
                            curObject.SetObjectState("unknown");
                        }

                        break;
                }
                // update bdd
                if (curObject!=null)
                    mySettingBdd.updateObject(index, curObject);
                index++;
                // only refresh the selected device
                if (!fullCheck)
                    forceExit=true;
            }
            mySettingBdd.close();

            runOnUiThread(new Runnable() {
                public void run() {
                    refreshDeviceList();
                }
            });

            return "ok";
        }
    }
}
