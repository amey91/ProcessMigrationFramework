package network;

public class Location {
	public String ipAddress;
	public int socketNumber;
	
	public Location(String newipAddress, int newSocketNumber){
		ipAddress = newipAddress;
		socketNumber = newSocketNumber;
	}
	
	public String toString(){
		return "Location: IP="+ipAddress+" Socket="+socketNumber;
	}
}
