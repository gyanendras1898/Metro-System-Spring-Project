package com.gyan.client;

import java.sql.SQLException;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.gyan.exceptions.InvalidInputException;
import com.gyan.presentation.MetroPresentation;
import com.gyan.presentation.MetroPresentationImpl;
import com.gyan.presentation.UserPresentation;

public class MetroProjectClient {

	public static void main(String[] args) {

		ApplicationContext springContainer = new ClassPathXmlApplicationContext("metroSystem.xml");

		MetroPresentation metroPresentation = (MetroPresentation) springContainer.getBean("metroPresentationImpl");
		UserPresentation userPresentation = (UserPresentation) springContainer.getBean("userPresentationImpl");

		Scanner sc = new Scanner(System.in);
		int choiceInt = 0;

		try {
			do {
				metroPresentation.showMenu();
				System.out.println("Enter Choice : ");
				String choice = sc.next();
				if ((int) choice.charAt(0) - 48 >= 0 && (int) choice.charAt(0) - 48 <= 9) {
					choiceInt = Integer.parseInt(choice);
					if (choiceInt == 2)
						userPresentation.userLogin();
					else
						metroPresentation.performMenu(choiceInt);
					System.out.println();
				} else
					throw new InvalidInputException("Warning!!! Please input integer only");
			} while (choiceInt != 3);
		} catch (InvalidInputException exception) {
			System.out.println(exception.getMessage());
		} catch (NumberFormatException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}finally {
			sc.close();
		}

	}

}
