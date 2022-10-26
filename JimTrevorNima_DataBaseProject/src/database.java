import java.util.ArrayList;

public class database implements java.io.Serializable {
	public String name;
	public ArrayList<String> authorizedUsers = new ArrayList<String>();
	
	//public ArrayList<table> tableList = new ArrayList<table>();
	public LinkedList<table> tbLinkedList = new LinkedList<table>();
	
	public database(String s) {
		name = s;
		authorizedUsers.add("admin");
	}
	
}
