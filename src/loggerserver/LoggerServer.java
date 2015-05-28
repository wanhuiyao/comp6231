package loggerserver;
import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import tools.ConfigureManager;

public class LoggerServer extends Thread{
	private ServerSocket serverSocket;
	private boolean keepWorking;
	private boolean isWorking;
	private ArrayList<Connection> connections;

	private LoggerWriter loggerWriter;

	public LoggerServer() throws IOException{
		keepWorking = true;
		isWorking = false;

		serverSocket = new ServerSocket(ConfigureManager.getInstance().getInt("loggerServerPort", 2020));
		serverSocket.setSoTimeout(100);

		connections = new ArrayList<Connection>();
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmssS").format(Calendar.getInstance().getTime());
		boolean useHtmlStyle = ConfigureManager.getInstance().getBool("useHtmlStyle", true);
		String logFileName;
		if(useHtmlStyle){
			logFileName = "./log_" + timestamp + ".html";
		}else{
			logFileName = "./log_" + timestamp + ".log";
		}
		loggerWriter = new LoggerWriter(logFileName, useHtmlStyle);
		System.out.println("Logger file is created:" + logFileName);
	}

	public void run(){
		try {
			System.out.println("Logger server, " + InetAddress.getLocalHost() + ", is waiting for connecting on port " + serverSocket.getLocalPort());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		isWorking = true;
		while(keepWorking){
			try{
				Socket communicationSocket = serverSocket.accept();
				System.out.println("Connection request from " + communicationSocket.getRemoteSocketAddress() + " is accepted.");
				Connection thisConnection = new Connection(communicationSocket, loggerWriter);
				connections.add(thisConnection);
				thisConnection.start();
			}catch(SocketTimeoutException s){
				//System.out.println("Socket timed out, try again!");
			}
			catch(IOException e){
				//e.printStackTrace();
				System.out.println("IO Error, The session will be terminated!");
				keepWorking = false;
			}
		}
		isWorking = false;
	}

	public void cleanup(){
		keepWorking = false;
		System.out.print("Waiting for the LoggerServer to stop connection thread.");
		while(isWorking){
			try {
				sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.print(".");
		}
		
		for (Connection connection : connections) {
			connection.cleanup();
			connection = null;
		}
	
		
		try{
			serverSocket.close();
		}catch(IOException e){
			e.printStackTrace();
			System.out.println("Exception on close socket!");
		}	
		loggerWriter.stop();
		System.out.println("\nLoggerServer finished cleaning up.");
	}
	
	public void status(){
		if(isWorking){
			System.out.println("Logger server is active to accept connections.");
		}else{
			System.out.println("Logger server is not active. Remote connections will not be accpted currently.");
		}
		
		for (Connection connection : connections) {
			connection.status();
		}
	}
	
	public static void main(String [] args)
	{
		try
		{
			ConfigureManager.getInstance().loadFile("./settings/logger_server_settings.conf");
			LoggerServer loggerServer = new LoggerServer();
			loggerServer.start();
			
			while(true){
				System.out.println("Type to [status] to show logger server status.");
				System.out.println("Type to [quit] to quit logger server.");
				@SuppressWarnings("resource")
				Scanner in = new Scanner(System.in);
				String operation = in.next();
				if(operation.compareTo("quit") == 0){
					break;
				}
				else if(operation.compareTo("status") == 0){
					loggerServer.status();
				}
				else{
					System.out.println("Wrong input. Try again.");
				}
				
			}
			loggerServer.cleanup();
			System.out.println("Logger server is done.");
		}catch(IOException e){
			e.printStackTrace();
			System.out.println("Failed to create LoggerServer. The session will be terminated.");
		}
	}
}
