import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Server<E> extends JFrame implements Runnable, CallBack {

	private JTextField inputBar;
	private JButton btnGo;
	private JTextArea areaResults;
	private JScrollPane sp;
	private Dimension winD;

	final int PORT = 8001;
	ServerSocket serverSock = null;

	ArrayList<String> users = new ArrayList<String>();
	//ArrayList<database> databaseList = new ArrayList<database>();
	
	LinkedList<database> dbLinkedList = new LinkedList<database>();
	Node<database> currentNode = null; //Hard code these types in?
	Node<table> currentNodeTable = null;
	Node<entry> currentNodeEntry = null;

	public Server() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(0, 0, 750, 500);
		this.setLayout(null);
		this.setMinimumSize(this.getSize());
		this.setTitle("Server");
		winD = new Dimension(734, 462);

		LoadUsers();
		LoadDatabases();

		ClientHandler admin = new ClientHandler(null, null);
		admin.username = "Admin";

		areaResults = new JTextArea();
		areaResults.setEditable(false);
		sp = new JScrollPane(areaResults);
		sp.setBounds(5, 5, winD.width - 10, winD.height - 50);
		this.add(sp);

		inputBar = new JTextField();
		inputBar.setBounds(5, sp.getSize().height + 15, winD.width - 230, 25);
		this.add(inputBar);

		btnGo = new JButton("GO");
		btnGo.setBounds(inputBar.getSize().width + 10, sp.getSize().height + 15, 100, 25);
		this.add(btnGo);

		this.setVisible(true);

		try {
			serverSock = new ServerSocket(PORT);
			Thread t = new Thread(Server.this);
			t.start();
		} catch (IOException e1) {
			System.out.println(e1.getMessage());
		}

		btnGo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (inputBar.getText().compareTo("") != 0) {
					String[] data = ParseMsg(inputBar.getText());
					String ret = doCommand(data, admin, true);
					areaResults.append(inputBar.getText() + "\n");
					inputBar.setText("");
					areaResults.append(ret + "\n");
				}
			}
		});

		this.addComponentListener(new ComponentListener() { // Dynamic resizing without layouts
			public void componentResized(ComponentEvent e) {
				winD = getContentPane().getSize();
				sp.setBounds(5, 5, winD.width - 10, winD.height - 50);
				inputBar.setBounds(5, sp.getSize().height + 15, winD.width - 230, 25);
				btnGo.setBounds(inputBar.getSize().width + 10, sp.getSize().height + 15, 100, 25);
			}
			public void componentHidden(ComponentEvent e) {} // Have to override
			public void componentMoved(ComponentEvent e) {}
			public void componentShown(ComponentEvent e) {}
		});
	}

	public static void main(String args[]) {
		new Server();
	}

	public void run() {
		Socket connectionSocket;
		boolean good = true;
		Thread t;
		ClientHandler ch;
		PrintWriter out;
		while (good) {
			try {
				connectionSocket = serverSock.accept();
				ch = new ClientHandler(connectionSocket, this);
				out = new PrintWriter(connectionSocket.getOutputStream(), true);
				out.println("Please login using a username.");
				t = new Thread(ch);
				t.start();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	public void executeCallBack(String msg, ClientHandler client) {
		msg = msg.trim();
		PrintWriter out;
		try {
			out = new PrintWriter(client.socket.getOutputStream(), true);
			if (client.logged) {
				String[] data = ParseMsg(msg);
				String ret = doCommand(data, client, false);
				out.println(ret);
			} else
				login(msg, client, out);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public void login(String msg, ClientHandler client, PrintWriter out) {
		for (String user : users) {
			if (user.compareToIgnoreCase(msg) == 0) {
				client.logged = true;
				client.username = user;
				out.println("Welcome " + user + ", type help for supported commands");
				return;
			}
		}
		out.println("Invalid username");
	}

	public void SaveUsers() {
		PrintWriter pw;
		try {
			pw = new PrintWriter(new FileOutputStream("users.txt"));
			for (String user : users) {
				pw.println(user);
			}
			pw.close();
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
	}

	public void LoadUsers() {
		try {
			FileReader freader = new FileReader("users.txt");
			BufferedReader breader = new BufferedReader(freader);
			String line;
			while ((line = breader.readLine()) != null) {
				users.add(line);
			}
			breader.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void SaveDatabases() {
		try {
			FileOutputStream fos = new FileOutputStream("databases");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			/*for (database db : databaseList) {

			}*/
			currentNode = dbLinkedList.first;
			while(currentNode != null) {
				System.out.println(currentNode.variable.name + " Database Saved");
				oos.writeObject(currentNode.variable);
				currentNode = currentNode.next;
			}
			oos.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void LoadDatabases() {
		try {
			FileInputStream fis = new FileInputStream("databases");
			ObjectInputStream ois = new ObjectInputStream(fis);
			database db;
			while ((db = (database) ois.readObject()) != null) {
				System.out.println(db.name + " Database Loaded");
				//databaseList.add(db);
				dbLinkedList.add(db);
			}
			ois.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public String[] ParseMsg(String s) {
		String[] data = s.split(" ");
		for (String x : data) {
			x = x.trim();
		}
		return data;
	}

	public String doCommand(String[] s, ClientHandler c, boolean admin) {
		if (s[0].compareToIgnoreCase("using") == 0) { // Using command lets program know what database we're editing / viewing for example: "Using 'School'"
			//for (database db : databaseList) { // Iterate through every db in our list
			currentNode = dbLinkedList.first;
			while(currentNode != null) {
				database db = currentNode.variable;
				if (s[1].compareToIgnoreCase(db.name) == 0) { // Find the one named 'School'
					for (String user : db.authorizedUsers) { // Before finishing check to see if the user is authorized to use
						if (user.compareToIgnoreCase(c.username) == 0) { // If the user is in the db's authorized users list
							c.using = db; // We now hold a reference to the database in the client
							return "Successfully changed using to '" + db.name + "'";
						}
					}
					return "Error: user '" + c.username + "' is not authorized to use database '" + db.name + "'";
				}
				currentNode = currentNode.next;
			}
			return "Error: no database '" + s[1] + "' found in database directory";
		}
		if (s[0].compareToIgnoreCase("help") == 0) {
			return ("-Assign \t Assign a user to given databases \t\t\t 'Assign USER DB1 DB2 DB3 ... DBX'"
					+ "\n-Change \t Edits an entry in specified table where filter matches \t\t 'Change entry TABLENAME where FILTER'"
					+ "\n-Create \t Creates users, databases, tables, entries \t\t\t 'Create entry TABLENAME COL1 COL2 COL3 ... COLX'"
					+ "\n-Databases \t Shows databases the user is authorized to view / edit"
					+ "\n-Delete \t Deletes users, databases, tables, entries \t\t\t 'Delete entry TABLENAME where FILTER'"
					+ "\n-ForceSave \t Forces databases and user information to be saved"
					+ "\n-Help \t Shows supported commands"
					+ "\n-Query \t Searches in specified table and returns entries that pass given filter \t 'Query TABLENAME where FILTER' or * for all"
					+ "\n-Remove \t Removes given user's authorization from specified tables \t\t 'Remove USER DB1 DB2 DB3 ... DBX'"
					+ "\n-Tables \t Shows tables present in currently viewing database"
					+ "\n-Using \t Selects a database for viewing / editing \t\t\t 'Using DATABASE'"
					+ "\n-Filters:"
					+ "\n   COLUMN = X"
					+ "\n   COLUMN > X"
					+ "\n   COLUMN < X"
					+ "\n   COLUMN => X"
					+ "\n   COLUMN =< X"
					+ "\n   COLUMN != X");
		}
		if (s[0].compareToIgnoreCase("databases") == 0) { // Checks for user's authorized databases
			String dbs = "";
			//for (database db : databaseList) { // Iterate through every database
			currentNode = dbLinkedList.first;
			while(currentNode != null) {
				database db = currentNode.variable;
				for (String user : db.authorizedUsers) { // Iterate through all of the db's names in authorized list
					if (c.username.compareTo(user) == 0) { // If the username of client is on the authorized list
						dbs += db.name + " "; // append
					}
				}
				currentNode = currentNode.next;
			}
			if (dbs.compareTo("") == 0)
				return "No assigned databases found"; // if none
			return dbs; // return normal
		}
		if (s[0].compareToIgnoreCase("create") == 0) {
			if (s[1].compareToIgnoreCase("entry") == 0) {
				if (c.using != null) {
					if(s.length <= 2)
						return "Error: Please include a specified table and the appropriate information";
					//for (table t : c.using.tableList) {
					currentNodeTable = c.using.tbLinkedList.first;
					while(currentNodeTable != null) {
						table t = currentNodeTable.variable;
						if(t.name.compareToIgnoreCase(s[2]) == 0) {
							int required = s.length - 3;
							if(required == t.columns.length) {
								String[] entries = new String[required];
								int x = 0;
								for(int i = 3; i < s.length; i++) {
									entries[x] = s[i];
									x++;
								}
								t.entries.add(new entry(entries));
								return "Entry in table '" + t.name + "' with column values '" + Arrays.toString(entries) + "' added";
							} else
								return "Error: Inputted column values do not match table columns: " + Arrays.toString(t.columns);
						}
						currentNodeTable = currentNodeTable.next;
					}
					return "Error: No table '"+ s[2] + "' found in database '" + c.using.name +"'";
				} else
					return "Error: unable to use command without selecting a database";
			}
			if (s[1].compareToIgnoreCase("table") == 0) {
				if (c.using != null) {
					if (s.length <= 2)
						return "Error: Please include a name and column names for your table";
					if (s.length <= 3)
						return "Error: Please include column names for your table";

					String columns[] = new String[s.length - 3]; //TODO dupe check if the table already exists
					int x = 0;
					for (int i = 3; i < s.length; i++) {
						columns[x] = s[i];
						x++;
					}
					c.using.tbLinkedList.add(new table(s[2], columns));
					return "Table '" + s[2] + "' sucessfully added with columns '" + Arrays.toString(columns) + "'";
				} else
					return "Error: unable to use command without selecting a database";
			}
			if (s[1].compareToIgnoreCase("database") == 0) {
				if (admin) {
					dbLinkedList.add(new database(s[2]));
					return "Database '" + s[2] + "' added";
				} else
					return "Error: '" + s[0] + " " + s[1] + "' can only be used by the server";
			}
			if (s[1].compareToIgnoreCase("user") == 0) {
				if (admin) {
					users.add(s[2]);
					return "User '" + s[2] + "' added";
				} else
					return "Error: '" + s[0] + " " + s[1] + "' can only be used by the server";
			}
		}
		if (s[0].compareToIgnoreCase("assign") == 0) { // Assigning users to databases
			String str = "";
			boolean found = false;
			boolean dupe = false;
			if (admin) { // only admins can use this command
				for (String u : users) { // Iterate through every user
					if (u.compareToIgnoreCase(s[1]) == 0) { // If there's a user in the user list that matches what was typed in
						for (int i = 2; i < s.length; i++) { // Iterate through every word after the username, these are potential databases
							//for (database db : databaseList) { // For each database
							currentNode = dbLinkedList.first;
							while(currentNode != null) {
								database db = currentNode.variable;
								found = false;
								if (db.name.compareToIgnoreCase(s[i]) == 0) { // Find if the word and database name match
									found = true;
									for (String a : db.authorizedUsers) { // For all users in the authorized list
										if (a.compareToIgnoreCase(s[1]) == 0) { // If the authroized name matches our user that was typed
											dupe = true; // It's a duplicate
											str += "User '" + u + "' already in database '" + db.name + "' \n";
										}
									}
									if (!dupe) {
										db.authorizedUsers.add(u); // If not a dupe then add them
										str += "User '" + u + "' added to database '" + db.name + "' \n";
									}
								}
								currentNode = currentNode.next;
							}
							if (!found)
								str += "Error: No database '" + s[i] + "' found \n";
						}
						if (str.length() < 2)
							return "Error: Please enter databases to assign to user '" + u + "'";
						return str.substring(0, str.length() - 2); // Remove the last new line \n
					}
				}
				return "Error: No user '" + s[1] + "' found in user list";
			} else
				return "Error: '" + s[0] + " " + s[1] + "' can only be used by the server";
		}
		if (s[0].compareToIgnoreCase("remove") == 0) {
			String str = "";
			boolean found = false;
			if (admin) {
				for (String u : users) {
					if (u.compareToIgnoreCase(s[1]) == 0) {
						for (int i = 2; i < s.length; i++) {
							//for (database db : databaseList) {
							currentNode = dbLinkedList.first;
							while(currentNode != null) {
								database db = currentNode.variable;
								found = false;
								if (db.name.compareToIgnoreCase(s[i]) == 0) {
									found = true;
									db.authorizedUsers.remove(u);
									str += "User '" + u + "' removed from database '" + db.name + "' \n";
								}
								currentNode = currentNode.next;
							}
							if (!found)
								str += "Error: No database '" + s[i] + "' found \n";
						}
						return str.substring(0, str.length() - 2); // Remove the last new line \n
					}
				}
				return "Error: No user '" + s[1] + "' found in user list";
			} else
				return "Error: '" + s[0] + " " + s[1] + "' can only be used by the server";
		}
		if (s[0].compareToIgnoreCase("delete") == 0) {
			if (s[1].compareToIgnoreCase("table") == 0) {
				if (c.using != null) {
					//for (table t : c.using.tableList) {
					currentNodeTable = c.using.tbLinkedList.first;
					while(currentNodeTable != null) {
						table t = currentNodeTable.variable;
						if (t.name.compareToIgnoreCase(s[2]) == 0) {
							c.using.tbLinkedList.remove(t);
							return "Table '" + t.name + "' removed";
						}
						currentNodeTable = currentNodeTable.next;
					}
					return "Error: No table '" + s[2] + "' found in database '" + c.using.name + "'";
				} else
					return "Error: unable to use command without selecting a database";
			}
			if (s[1].compareToIgnoreCase("entry") == 0) {
				if (c.using != null) {
					// TODO
				} else
					return "Error: unable to use command without selecting a database";
			}
			if (s[1].compareToIgnoreCase("database") == 0) {
				if (admin) {
					//for (database db : databaseList) {
					currentNode = dbLinkedList.first;
					while(currentNode != null) {
						database db = currentNode.variable;
						if (db.name.compareToIgnoreCase(s[2]) == 0) {
							dbLinkedList.remove(db);
							return "Database '" + db.name + "' removed";
						}
						currentNode = currentNode.next;
					}
					return "Error: No database '" + s[2] + "' found";
				} else
					return "Error: '" + s[0] + " " + s[1] + "' can only be used by the server";
			}
			if (s[1].compareToIgnoreCase("user") == 0) {
				if (admin) {
					for (String u : users) {
						if (u.compareToIgnoreCase(s[2]) == 0) {
							users.remove(u);
							SaveUsers();
							return "User '" + u + "' deleted";
						}
					}
					return "Error: No user '" + s[2] + "' found in user list";
				} else
					return "Error: '" + s[0] + " " + s[1] + "' can only be used by the server";
			}
		}
		if (s[0].compareToIgnoreCase("query") == 0) {
			if(c.using != null) {
				//for (table t : c.using.tableList) {
				currentNodeTable = c.using.tbLinkedList.first;
				while(currentNodeTable != null) {
					table t = currentNodeTable.variable;
					if(t.name.compareToIgnoreCase(s[1]) == 0) {
						if(s[2].compareToIgnoreCase("*") == 0) {
							String ret = "";
							for(String col : t.columns) {
								ret += col + "\t\t";
							}
							ret += "\n";
							for(entry e : t.entries) {
								for(String ent : e.entries) {
									ret += ent + "\t\t";
								}
								ret += "\n";
							}
							return ret;
						}
						//if(//filter stuff){
							// TODO
						
						//TODO
						
						//}
					}
					currentNodeTable = currentNodeTable.next;
				}
				return "Error: No table '" + s[1] + "' found in database '" + c.using.name + "'";
			}else
				return "Error: unable to use command without selecting a database";
		}
		if (s[0].compareToIgnoreCase("change") == 0) {
			// TODO 
		}
		if (s[0].compareToIgnoreCase("tables") == 0) {
			String tables = "";
			if (c.using != null) {
				//for (table t : c.using.tableList) {
				currentNodeTable = c.using.tbLinkedList.first;
				while(currentNodeTable != null) {
					table t = currentNodeTable.variable;
					tables += t.name + " \n";
					currentNodeTable = currentNodeTable.next;
				}
				if (tables.compareTo("") == 0) {
					return "No tables found in database '" + c.using.name + "'";
				} else
					return tables.substring(0, tables.length() - 2);
			} else
				return "Error: unable to use command without selecting a database";
		}
		if (s[0].compareToIgnoreCase("forcesave") == 0) {
			SaveUsers();
			SaveDatabases();
		}
		return "Error: '" + s[0] + "' not a recognized command";
	}
}
