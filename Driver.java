package simu;

import java.util.Random;

public class Driver {
	private final String firstName;
	private final String surname;
	private final int skill;
	private int skillModifier;
	private final int consistency;
	private final int defense = 80;
	private final int overtaking = 80;
	private Random r = FormulaSimu.random;

	public Driver(String firstName, String surname, int skill, int consistency) {
		this.firstName = firstName;
		this.surname = surname;
		this.skill = skill;
		this.consistency = consistency;
	}

	public int getLapTime(Track track) {
		double standardDeviation = 1000 - 7.5 * consistency;
		int idealLapTimeMs = track.getIdealLapTimeMs();
		int lapTimeMs = idealLapTimeMs * 1050 / 1000 - idealLapTimeMs * getSkill() / 2000;
		boolean push = getSkill() > r.nextInt(1000);
		double randomness = Math.abs(r.nextGaussian());
		randomness *= push ? -200 : standardDeviation;
		return lapTimeMs + (int) randomness;
	}
	
	public String getName() {
		return firstName.substring(0, 1) + ". " + surname.toUpperCase();
	}

	public int getDefense() {
		return defense;
	}

	public int getOvertaking() {
		return overtaking;
	}

	public int getSkill() {
		return skill + skillModifier;
	}

	public void adjustSkillModifier(int modifier) {
		skillModifier -= modifier;
	}
}
