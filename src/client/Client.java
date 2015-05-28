package client;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;

import retailer.RetailerInterface;
import tools.Customer;
import tools.Item;
import tools.ItemShippingStatus;
import tools.Product;
import tools.SignUpResult;

public class Client {

	private RetailerInterface retailer;
	private Customer currentCustomer;

	public Scanner in;
	
	public Client() throws MalformedURLException, RemoteException, NotBoundException{
		String registryURL = "rmi://localhost:1099/retailer";  
		retailer = (RetailerInterface)Naming.lookup(registryURL); // find the remote object and cast it to an Retailer object
		in = new Scanner(System.in);
	}

	public boolean customerSignUp(){
		if(currentCustomer != null){
			customerSignOut();
		}
		System.out.println("Input customer name:");
		String name = in.next();
		System.out.println("Input customer password:");
		String password = in.next();
		System.out.println("Input customer street1:");
		String street1 = in.next();
		System.out.println("Input customer street2:");
		String street2 = in.next();
		System.out.println("Input customer city:");
		String city = in.next();
		System.out.println("Input customer state:");
		String state = in.next();
		System.out.println("Input customer zip code:");
		String zip = in.next();
		System.out.println("Input customer country:");
		String country = in.next();
		try {
			SignUpResult signUpResult  = retailer.signUp(name, password, street1, street2, city, state, zip, country);
			if(signUpResult == null){
				return false;
			}else{
				System.out.println(signUpResult.message);
				if(signUpResult.result){
					currentCustomer = new Customer(signUpResult.customerReferenceNumber, name, password, street1, street2, city, state, zip, country);
				}
				return signUpResult.result;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean customerSignIn(){
		if(currentCustomer != null){
			customerSignOut();
		}
		System.out.println("Input customer ReferenceNumber:");
		int customerReferenceNumber = Integer.parseInt(in.next());
		System.out.println("Input customer password:");
		String password = in.next();
		try {
			currentCustomer = retailer.signIn(customerReferenceNumber, password);
			if(currentCustomer == null){
				System.out.println("Failed to sign in. Please try again!");
				return false;
			}else{
				System.out.println("Signed in. Your person informations are:" + currentCustomer.toString());
				return true;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public void customerSignOut(){
		if(currentCustomer == null)
			return;
		System.out.println("Current customer:" + currentCustomer.getName() + " signed out.");
		currentCustomer = null;
	}

	public ArrayList<Product> getCatalog(){
		if(currentCustomer == null){
			System.out.println("Operation is only allowed for registed user. Please sign in.");
			return null;
		}else{
			try {
				return retailer.getCatalog(currentCustomer.getCustomerReferenceNumber());
			} catch (RemoteException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public ArrayList<ItemShippingStatus> makeOrder(){
		if(currentCustomer == null){
			System.out.println("Operation is only allowed for registed user. Please sign in.");
			return null;
		}else{
			ArrayList<Item> itemList = new ArrayList<Item>();
			//TODO Provide an interface for making itemList
			try {
				return retailer.submitOrder(currentCustomer.getCustomerReferenceNumber(), itemList);
			} catch (RemoteException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public static void main(String[] args) {
		try {
			Client client = new Client();
			String operation;
			do{
				System.out.println("Type to [1] to sign up.");
				System.out.println("Type to [2] to sign in.");
				System.out.println("Type to [3] to sign out.");
				System.out.println("Type to [4] to get product catalog.");
				System.out.println("Type to [5] to make an order.");
				System.out.println("Type to [6] to quit.");
				operation = client.in.next();
				if(operation.compareTo("1") == 0){
					client.customerSignUp();
				}
				else if(operation.compareTo("2") == 0){
					client.customerSignIn();
				}
				else if(operation.compareTo("3") == 0){
					client.customerSignOut();
				}
				else if(operation.compareTo("4") == 0){
					ArrayList<Product> products = client.getCatalog();
					if(products != null){
						for (Product product : products) {
							System.out.println(product.toString());
						}
					}
				}
				else if(operation.compareTo("5") == 0){
					ArrayList<ItemShippingStatus> itemshippingStatusList = client.makeOrder();
					if(itemshippingStatusList != null){
						System.out.println("The item shipping status is:");
						for (ItemShippingStatus itemShippingStatus : itemshippingStatusList) {
							System.out.println(itemShippingStatus.toString());
						}
					}
				}
				else if(operation.compareTo("6") == 0){
					break;
				}
				else{
					System.out.println("Wrong input. Try again.");
				}
			}while(true);
			
			client.in.close();
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
}
