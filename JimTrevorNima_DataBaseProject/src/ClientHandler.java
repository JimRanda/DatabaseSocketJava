import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandler implements Runnable{
	public Socket socket;
	public boolean logged = false;
	public String username;
	public database using = null;
	private CallBack cba;
	
	public ClientHandler(Socket Socket, CallBack CBA) {
		socket = Socket;
		cba = CBA;
	}
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(socket.getInputStream());
			BufferedReader in = new BufferedReader(isr);
			
			String msg;
			
			while(true) {
				msg = in.readLine();
				cba.executeCallBack(msg, this);
			}
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}finally {
			try {
				socket.close();
			}catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
}
