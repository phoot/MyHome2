package fr.oxilea.myhome;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.regex.Pattern;


public class MonAdaptateurDeListe extends ArrayAdapter<String> {

    // default available icons
    private Integer[][] tab_images_pour_la_liste = {
            {R.drawable.portail,
            R.drawable.prise,
            R.drawable.lampe,
            R.drawable.deficon,},
           {R.drawable.portailgreen,
            R.drawable.prisegreen,
            R.drawable.lampegreen,
            R.drawable.deficongreen,},
           {R.drawable.portailred,
            R.drawable.prisered,
            R.drawable.lampered,
            R.drawable.deficonred,}};


    // global message Format
    // Packet head - Total length (2 bytes) - ID - Command - Parameter - Parity
    // 0x55 0xaa - 0x00 0x?? - 0x01 - 0x?? .. - 0x?? .. -0x??
    // TCP over network send full frame

    public static final byte[] STATUS_RELAY_MESSAGE = {0x55, (byte) 0xaa, 0x00, 0x02, 0x00, 0x0a, 0x0c};
    public static final byte[] CDE_ON_RELAY_MESSAGE = {0x55, (byte) 0xaa, 0x00, 0x03, 0x00, 0x02, 0x01, 0x06};
    public static final byte[] CDE_OFF_RELAY_MESSAGE = {0x55, (byte) 0xaa, 0x00, 0x03, 0x00, 0x01, 0x01, 0x05};
    public static final byte[] CDE_SWITCH_RELAY_MESSAGE = {0x55, (byte) 0xaa, 0x00, 0x03, 0x00, 0x03, 0x01, 0x07};

    public static final String PULSE_TYPE = "1";

    // supported protocol
    public static final int LONHAND_PROTOCOL=0;
    public static final int MAGINON_PROTOCOL=1;


    // psw hex => 62 37 65 62 38
    // psw str =>  "xxxxx"
    // psw command => psw (bytes) + 0x0d, 0x0a
    public byte[] PASSWORD_RELAY_MESSAGE = {0x0, 0x0, 0x0, 0x0, 0x0, 0x0d, 0x0a};

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)
                getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.rowlayout, parent, false);

        TextView textView = (TextView) rowView.findViewById(R.id.label);
        TextView textPowerView = (TextView) rowView.findViewById(R.id.powerView);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

        textView.setText(getItem(position));

        // check if consumption measured
        String myStrCons="";
        if (!(MyActivity.mypowerTab.GetLivePowerMeasurement(position).equals("?")))
        {
            myStrCons+=("Pw: "+(MyActivity.mypowerTab.GetLivePowerMeasurement(position)).substring(0,4) + "." + (MyActivity.mypowerTab.GetLivePowerMeasurement(position)).substring(4,6) + " W    |   ");
        }

        if (!(MyActivity.mypowerTab.GetFullPowerMeasurement(position).equals("?")))
        {
            myStrCons+=("Cumul: "+ (MyActivity.mypowerTab.GetFullPowerMeasurement(position)).substring(0,3) + "." + (MyActivity.mypowerTab.GetFullPowerMeasurement(position)).substring(3,6) + " W/h");
        }
        textPowerView.setText(myStrCons);

        // get the icon from bdd
        DeviceBdd mySettingBdd = new DeviceBdd(getContext());
        mySettingBdd.open();
        ConnectedObject myObject= new ConnectedObject();

        myObject = mySettingBdd.getObjectWithId(position);
        mySettingBdd.close();

        if(convertView == null ) {
            // get icon id of the connected object
            int myIcon = Integer.parseInt(myObject.GetObjectIconType());
            String myState = myObject.GetObjectState();
            int stateIndex=0;

            // display black icon if pulse mode
            if (!(myObject.GetObjectCdeType().equals(PULSE_TYPE))) {
                switch (myState) {
                    case "on":
                        stateIndex = 1;
                        break;
                    case "off":
                        stateIndex = 2;
                        break;
                }
            }

            if (myIcon < 3) {
                imageView.setImageResource(tab_images_pour_la_liste[stateIndex][myIcon]);
            }else{
                imageView.setImageResource(tab_images_pour_la_liste[stateIndex][3]);
            }
        }
        else
            rowView = (View)convertView;

        return rowView;
    }

    public MonAdaptateurDeListe(Context context, String[] values) {
        super(context, R.layout.rowlayout, values);
    }




}