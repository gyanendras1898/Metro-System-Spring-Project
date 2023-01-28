package com.gyan.presentation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.gyan.beans.Station;
import com.gyan.service.CardService;
import com.gyan.service.CardServiceImpl;
import com.gyan.service.JourneyService;
import com.gyan.service.JourneyServiceImpl;
import com.gyan.service.StationService;
import com.gyan.service.StationServiceImpl;

import lombok.Setter;

public class MetroPresentationImpl implements MetroPresentation {
	Scanner sc = new Scanner(System.in);
	@Setter
	private CardService cardService;
	@Setter
	private JourneyService journeyService;
	@Setter
	private StationService stationService;

	@Override
	public void showMenu() {
		System.out.println("1. Register new User");
		System.out.println("2. Log-in");
		System.out.println("3. Exit");

	}

	@Override
	public int registerNewUser() throws ClassNotFoundException, SQLException {
		int userId;
		int chance = 3;
		double balance;
		do {
			System.out.println("Enter Initail Balance");
			balance = sc.nextDouble();

			if (balance < 100)
				System.out.println("Deposit Initial Balance of minimum 100 : " + (chance) + " remaining chance");

		} while (chance-- > 0 && balance < 100);

		if (balance < 100) {
			System.out.println("Thanks for using our System, kindly visit us again!");
			return -1;
		}

		userId = cardService.registerUser(balance);
		return userId;
	}

	public void showUserMenu(int cardId, boolean isJourneyOngoing) throws ClassNotFoundException, SQLException {
		System.out.println("1. View Balance");
		System.out.println("2. Add Balance");
		if (isJourneyOngoing)
			System.out.println("3. Swipe Out");
		else
			System.out.println("3. Swipe In");
		System.out.println("4. Exit");
	}

	public void userLogin() throws ClassNotFoundException, SQLException {
		System.out.println("Enter Card Id");
		int cardId = sc.nextInt();

		boolean cardPresent = cardService.isCardPresent(cardId);

		if (!cardPresent) {
			System.out.println("Card not registered or invalid card ID");
			return;
		}
		int choice;
		do{ 
			boolean isJourneyOngoing = journeyService.isJourneyOngoing(cardId);
			this.showUserMenu(cardId, isJourneyOngoing);
			System.out.println("Enter choice ");
			choice = sc.nextInt();
			this.performUserMenu(choice, cardId, isJourneyOngoing);
		}while(choice != 4);
		

	}

	private void performUserMenu(int choice, int cardId, boolean isJourneyOngoing)
			throws ClassNotFoundException, SQLException {
		switch (choice) {
		// view balance
		case 1:
			double currBalance = cardService.viewBalance(cardId);
			System.out.println("Your current balance is " + currBalance);
			break;

		// add balance
		case 2:
			System.out.println("Enter the amount");
			int amount = sc.nextInt();

			if (amount <= 0)
				System.out.println("Enter valid amount");

			else {
				boolean amountUpdated = cardService.addCardBalance(cardId, amount);

				if (amountUpdated) {
					System.out.println("Amount added successfully");
					currBalance = cardService.viewBalance(cardId);
					System.out.println("Your current balance is " + currBalance);
				} else
					System.out.println("Amount not added: Please contact with admin");
			}
			break;
		// swipe in and out
		case 3:
			List<Station> stations = stationService.getStations();

			// swipe in feature
			if (!isJourneyOngoing) {
				currBalance = cardService.viewBalance(cardId);
				if (currBalance < 25) {
					System.out.println("Your current balance is " + currBalance);
					System.out.println("Please maintain min balance of 25 and try again");
				} else {
					for (Station station : stations)
						System.out.println(station);
					System.out.println("");

					System.out.println("Enter Source Station Number From Above Stations");
					int stationId = sc.nextInt();
					int chance = 3;
					while (chance-- > 0 && (stationId <= 0 || stationId > stations.size())) {
						System.out.println("Enter valid station number");
						stationId = sc.nextInt();
					}
					if (chance == 0) {
						System.out.println("Maximum limit reached, Please login again");
					}
					// logged in
					else {
						boolean swipedIn = journeyService.swipeIn(cardId, stationId);
						if (swipedIn)
							System.out.println("Boarded successfully");
						else
							System.out.println("Not Boarded, Please contact admin");
					}
				}
			}
			// swipe out feature
			else {
				for (Station station : stations)
					System.out.println(station);
				System.out.println("");

				System.out.println("Enter Destination Station Number From Above Stations");
				int stationId = sc.nextInt();

				int chance = 3;
				while (chance-- > 0 && (stationId <= 0 || stationId > stations.size())) {
					System.out.println("Enter valid station number");
					stationId = sc.nextInt();
				}
				if (chance == 0)
					System.out.println("Maximum limit reached, Please login again");

				// logged in
				else {
					boolean swipedOut = journeyService.swipeOut(cardId, stationId);

					if (swipedOut) {
						double fareValue = journeyService.getFare(cardId);
						currBalance = cardService.viewBalance(cardId);
						System.out.println();
						System.out.println("Your fare is " + fareValue + " and remaining balance is " + currBalance);

						boolean late = journeyService.getDuration(cardId);
						
						//fine
						if (late) {

							System.out.println("You are late and will be fined 100");

							if (currBalance < 100) {
								System.out.println("Your current balance is " + currBalance + ". Please add atleast "
										+ (100 - currBalance) + " rupees");
								System.out.println();

								amount = sc.nextInt();
								choice = 3;
								while (choice-- > 0 && amount < 100 - currBalance) {
									System.out.println("Enter valid amount");
									amount = sc.nextInt();
								}
								if (choice == 0)
									System.out.println("Amount not added, Please contact with admin");
								else
									cardService.addCardBalance(cardId, amount);
							}
							currBalance = cardService.viewBalance(cardId);
							if (currBalance >= 100) {
								boolean fine = cardService.addCardBalance(cardId, -100);

								if (fine) {
									double currtBalance = cardService.viewBalance(cardId);
									System.out.println("You are fined 100");
									System.out.println("Your current balance is " + currtBalance);
								} else
									System.out.println("Amount not deducted, Please contact with admin");
							}

							else {
								System.out.println("Balance low : Failed to pay fine");
								System.out.println("Card Blocked, Please contact with admin");
							}
						}
					}
				}
			}
			break;
		case 4:
			System.out.println("Thanks for using Metro Service");
			break;

		default:
			System.out.println("Enter Valid Choice!");

		}
	}

	@Override
	public void performMenu(int choice) throws ClassNotFoundException, SQLException {
			switch (choice) {
			//register new user
			case 1: 
				this.registerNewUser();
				break;
			//login
			case 2: 
				this.userLogin();
			break;
			
			//exit
			case 3:
				System.out.println("Thanks for using our System, kindly visit us again!");
				System.exit(0);
			default:
				System.out.println("Enter Valid Choice!");

			}


	}

}
