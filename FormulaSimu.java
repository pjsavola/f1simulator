package simu;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class FormulaSimu extends JPanel {
	
	private static Standings prevStandings;

	public static Random random = new Random();
	
	public static int getLapTimeMs(int minutes, int seconds) {
		return 1000 * (60 * minutes + seconds);
	}

	public static String lapTimeToString(int lapTimeMs) {
		int seconds = lapTimeMs / 1000;
		int minutes = seconds / 60;
		int secs = (seconds - minutes * 60);
		int ms = (lapTimeMs - seconds * 1000);
		String secString = Integer.toString(secs);
		if (secs < 10) secString = "0" + secString;

		String msString = Integer.toString(ms);
		for (int i = 10; i < 1000; i *= 10) {
			if (ms < i) msString = "0" + msString;
		}
		return String.format("%d:%s.%s", minutes, secString, msString);
	}

	public static String gapToString(int lapTimeMs) {
		String s = lapTimeToString(lapTimeMs);
		if (s.startsWith("0:")) s = s.substring(2);
		if (s.startsWith("0")) s = s.substring(1);
		return "+" + s;
	}
	
	public static void main(String[] args) {
		final int lapCount = 44;
		Track track = new Track(lapCount, getLapTimeMs(1, 44), 500);

		final Driver[] drivers = new Driver[20];
		drivers[0] = new Driver("Max", "Verstappen", 96, 95, 94);
		drivers[1] = new Driver("Sergio", "Perez", 90, 90, 90);
		drivers[2] = new Driver("Charles", "Leclerc", 93, 89, 96);
		drivers[3] = new Driver("Carlos", "Sainz", 90, 89, 93);
		drivers[4] = new Driver("Lewis", "Hamilton", 94, 90, 92);
		drivers[5] = new Driver("George", "Russell", 91, 96, 94);
		drivers[6] = new Driver("Lando", "Norris", 88, 90, 93);
		drivers[7] = new Driver("Daniel", "Ricciardo", 86, 82, 88);
		drivers[8] = new Driver("Fernando", "Alonso", 91, 93, 91);
		drivers[9] = new Driver("Esteban", "Ocon", 89, 90, 88);
		drivers[10] = new Driver("Valtteri", "Bottas", 90, 88, 87);
		drivers[11] = new Driver("Guanyu", "Zhou", 88, 83, 86);
		drivers[12] = new Driver("Mick", "Schumacher", 87, 79, 87);
		drivers[13] = new Driver("Kevin", "Magnussen", 89, 85, 88);
		drivers[14] = new Driver("Sebastian", "Vettel", 88, 90, 84);
		drivers[15] = new Driver("Lance", "Stroll", 87, 86, 84);
		drivers[16] = new Driver("Pierre", "Gasly", 88, 90, 86);
		drivers[17] = new Driver("Yuki", "Tsunoda", 88, 88, 87);
		drivers[18] = new Driver("Alexander", "Albon", 89, 90, 88);
		drivers[19] = new Driver("Nicholas", "Latifi", 83, 78, 86);
		
		prevStandings = new Standings(track, drivers);

		//Driver lewis = new Driver("Lewis", "Hamilton", 95, 95);
		//Driver max = new Driver("Max", "Verstappen", 96, 93);
		
        final JFrame f = new JFrame();
        f.setTitle("F1 Simulator");
        final FormulaSimu p = new FormulaSimu();
        p.setBackground(Color.BLACK);
        p.setBorder(new EmptyBorder(5, 5, 5, 5));
        final Font headerFont = new Font("Tahoma", Font.BOLD, 12);
        final Font textFont = new Font("Tahoma", Font.BOLD, 12);
        final Color bgColor = new Color(0x222222);

        JPanel header = new JPanel();
        header.setBackground(Color.BLACK);
        initTextField(header, 40, "POS", Color.LIGHT_GRAY, Color.BLACK, headerFont).setHorizontalAlignment(JLabel.CENTER);
        initTextField(header, 120, "NAME", Color.LIGHT_GRAY, Color.BLACK, headerFont);
        initTextField(header, 100, "TIME", Color.LIGHT_GRAY, Color.BLACK, headerFont);
        initTextField(header, 100, "INTERVAL", Color.LIGHT_GRAY, Color.BLACK, headerFont);
		initTextField(header, 100, "GAP", Color.LIGHT_GRAY, Color.BLACK, headerFont);
		initTextField(header, 100, "SPEED", Color.LIGHT_GRAY, Color.BLACK, headerFont);
        p.add(header);

		JLabel[] posFields = new JLabel[drivers.length];
		JLabel[] driverFields = new JLabel[drivers.length];
		JLabel[] infoFields = new JLabel[drivers.length];
		JLabel[] intervalFields = new JLabel[drivers.length];
		JLabel[] gapFields = new JLabel[drivers.length];
		JLabel[] speedFields = new JLabel[drivers.length];
		for (int i = 0; i < drivers.length; ++i) {
			JPanel row = new JPanel();
			Color color = i % 2 == 0 ? bgColor : Color.BLACK;
			row.setBackground(color);
			posFields[i] = initTextField(row, 40, Integer.toString(i + 1), Color.CYAN, color, textFont);
			posFields[i].setHorizontalAlignment(JLabel.CENTER);
			//drivers[i] = new Driver(Character.toString((char) ('A' + i)), "Player", 80 + i, 100 - i);
			driverFields[i] = initTextField(row, 120, prevStandings.getName(i), Color.WHITE, color, textFont);
			infoFields[i] = initTextField(row, 100, prevStandings.getTime(i), Color.WHITE, color, textFont);
			intervalFields[i] = initTextField(row, 100, "-", Color.WHITE, color, textFont);
			gapFields[i] = initTextField(row, 100, "-", Color.WHITE, color, textFont);
			speedFields[i] = initTextField(row, 100, Integer.toString(prevStandings.getDriver(i).getSkill()), Color.WHITE, color, textFont);
			p.add(row);
		}

        GridLayout layout = new GridLayout(drivers.length + 1, 1);
        p.setLayout(layout);
		//p.setPreferredSize(new Dimension(600, 550));
		Map<Driver, Integer> prevSpeeds = new HashMap<>();
        f.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}
			@Override
			public void keyReleased(KeyEvent e) {
			}
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					int lap = prevStandings.getCompletedLapCount();
					if (lapCount == lap) return;

					Standings s = new Standings(prevStandings);
					for (int i = 0; i < drivers.length; ++i) {
						int time = drivers[i].getLapTime(track, true);
						if (lap == 0) time += 5000;
						s.addTime(drivers[i], time);
					}
					s.resolve();

					int bestLap = prevStandings.getBestLap();
					Driver newBestLapDriver = null;
					Set<Driver> personalBests = new HashSet<>();
					for (Driver driver : drivers) {
						int personalBest = Arrays.stream(prevStandings.getTimes(driver)).min().orElse(Integer.MAX_VALUE);
						int best = Arrays.stream(s.getTimes(driver)).min().orElse(Integer.MAX_VALUE);
						if (best < personalBest) {
							personalBests.add(driver);
							if (best < bestLap) {
								bestLap = best;
								newBestLapDriver = driver;
							}
						}
					}
					if (newBestLapDriver != null)
						System.err.println("Best lap: " + lapTimeToString(bestLap) + " by " + newBestLapDriver.getName());

					for (int i = 0; i < drivers.length; ++i) {
						driverFields[i].setText(s.getName(i));
						infoFields[i].setText(s.getTime(i));
						Color color = Color.YELLOW;
						if (s.isDnf(s.getDriver(i))) color = Color.WHITE;
						if (s.getDriver(i) == newBestLapDriver) color = Color.MAGENTA;
						else if (personalBests.contains(s.getDriver(i))) color = Color.GREEN;
						infoFields[i].setForeground(color);
						intervalFields[i].setText(s.getInterval(i));
						gapFields[i].setText(s.getGap(i));
						color = Color.WHITE;
						int newSpeed = s.getDriver(i).getSkill();
						Integer oldSpeed = prevSpeeds.get(s.getDriver(i));
						if (oldSpeed != null) {
							if (oldSpeed < newSpeed) color = Color.GREEN;
							else if (oldSpeed > newSpeed) color = Color.RED;
						}
						speedFields[i].setText(Integer.toString(newSpeed));
						speedFields[i].setForeground(color);
						prevSpeeds.put(s.getDriver(i), newSpeed);
					}
					p.repaint();
					prevStandings = s;
				}
			}
		});
        f.setContentPane(p);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
	}
	
	private static JLabel initTextField(JPanel parent, int width, String text, Color fontColor, Color bgColor, Font font) {
		JLabel field = new JLabel(text);
		field.setForeground(fontColor);
		field.setBackground(bgColor);
		field.setBorder(new LineBorder(bgColor, 0));
		field.setPreferredSize(new Dimension(width, 16));
		field.setFont(font);
		parent.add(field);
		return field;
	}
}
