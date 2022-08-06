package simu;

import java.util.Random;

public class Car {
	private int condition = 100;

	public Car() {
	}

	public void addDamage(int damage) {
		condition -= damage;
	}

	public int getCondition() {
		return condition;
	}
}
