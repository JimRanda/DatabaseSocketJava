//import java.util.ArrayList;

public class table {
	public String name;
	String columns[];
	//ArrayList<entry> entries = new ArrayList<entry>();
	LinkedList<entry> entries = new LinkedList<entry>();
	
	public table(String s, String Columns[]) {
		name = s;
		columns = Columns;
	}
	
	public void newEntry(entry e) {
		entries.add(e);
	}
}
