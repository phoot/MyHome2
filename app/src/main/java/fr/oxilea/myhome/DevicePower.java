package fr.oxilea.myhome;

public class DevicePower {

    // maximum possible object managed through this software
    public static final int MAX_MANAGED_OBJECT = 25;
    public static final int LIVE_POWER_VALUE = 0;
    public static final int FULL_POWER_VALUE = 1;


    String myDevicePowerTab [][] = new String[MAX_MANAGED_OBJECT][2] ;


    // default constructeur
    DevicePower()
    {
        ResetMeasurementTab();
    }


    public Boolean AddLivePowerMeasurement(int index, String power){
        if (index < MAX_MANAGED_OBJECT)
        {
            myDevicePowerTab[index][LIVE_POWER_VALUE]=power;
            return true;
        }else
        {
            return false;
        }
    }

    public String GetLivePowerMeasurement(int index){
        return myDevicePowerTab[index][LIVE_POWER_VALUE];
    }

    public Boolean AddFullPowerMeasurement(int index, String power){

        if (index < MAX_MANAGED_OBJECT)
        {
            myDevicePowerTab[index][FULL_POWER_VALUE]=power;
            return true;
        }else
        {
            return false;
        }
    }

    public String GetFullPowerMeasurement(int index){
        return myDevicePowerTab[index][FULL_POWER_VALUE];
    }

    public void ResetMeasurementTab(){
        for (int i=0;i<MAX_MANAGED_OBJECT;i++){
            myDevicePowerTab[i][LIVE_POWER_VALUE]="?";
            myDevicePowerTab[i][FULL_POWER_VALUE]="?";
        }

    }

}
