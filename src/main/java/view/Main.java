package view;

/*
 * This is the main class (access gate) of the uni project. 
 * Due to Maven incompatibility with javafx.graphics two "main" functions are provided: 
 * one for the actual start of the program, the other one is only an access gate to the 
 * application itself.
 */

public class Main {
	public static void main(String[] args) {
		SceneSwitcher.main(args);
	}
}