package fr.oxilea.myhome;


public class ConnectedObject {

    // data structure: ObjectName | index | cdeType (pulse 1 or On/off 0 | networkIpAddress | networkPort | devicePswd | iconType

    private int id;
    private String objectName;
    private String objectIndex;
    private String cdeType;
    private String ipAddress;
    private String ipPort;
    private String login;
    private String password;
    private String iconType;
    private String protocol;
    private String state;


    /**
     * Constructor of the class.
     */
    public ConnectedObject(){
        // default, create only pointer to ConnectedObject

    }

    public ConnectedObject(String objName, String objIndex, String objCdeType, String objIpAddress, String objIpPort, String objLogin, String objPassword, String objIconType, String objProtocol, String objState ){
        objectName = objName;
        objectIndex = objIndex;
        cdeType = objCdeType;
        ipAddress = objIpAddress;
        ipPort = objIpPort;
        login = objLogin;
        password = objPassword;
        iconType = objIconType;
        protocol = objProtocol;
        state = objState;
    }


    int GetId(){
        return id;
    }

    void SetId(int objId){
        id = objId;
    }

    String GetObjectName(){
        return objectName;
    }

    void SetObjectName(String objName){
        objectName = objName;
    }

    String GetObjectIndex(){
        return objectIndex;
    }

    void SetObjectIndex(String objIndex){
        objectIndex = objIndex;
    }

    String GetObjectCdeType(){
        return cdeType;
    }

    void SetObjectCdeType(String objCdeType){
        cdeType = objCdeType;
    }

    String GetObjectIpAddress(){
        return ipAddress;
    }

    void SetObjectIpAddress(String objIpAddress){
        ipAddress = objIpAddress;
    }

    String GetObjectIpPort(){
        return ipPort;
    }

    void SetObjectIpPort(String objIpPort){
        ipPort = objIpPort;
    }

    String GetObjectLogin(){
        return login;
    }

    void SetObjectLogin(String objLogin){
        login = objLogin;
    }

    String GetObjectPassword(){
        return password;
    }

    void SetObjectPassword(String objPassword){
        password = objPassword;
    }

    String GetObjectIconType(){
        return iconType;
    }

    void SetObjectIconType(String objIconType){
        iconType = objIconType;
    }

    String GetObjectProtocol(){
        return protocol;
    }

    void SetObjectProtocol(String objProtocol){
        protocol = objProtocol;
    }

    String GetObjectState(){
        return state;
    }

    void SetObjectState(String objState){
        state = objState;
    }
}
