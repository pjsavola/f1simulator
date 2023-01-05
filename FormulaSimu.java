package simu;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class FormulaSimu extends JPanel {
	
	private static Standings prevStandings;

	public static Random random = new Random();
	public static final Color bgColor = new Color(0x222222);
	public static final Font headerFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
	public static final Font textFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
	public static final Font titleFont = new Font(Font.SANS_SERIF, Font.BOLD, 20);

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
		final JFrame f = new JFrame();

		Season season = new Season();
		season.addTrack(new Track("Spa", 44, getLapTimeMs(1, 44), 500));

		final MenuBar menuBar = new MenuBar();
		final Menu resultMenu = new Menu("Results");

		final MenuItem totalStandingsItem = new MenuItem("Show Standings");
		resultMenu.add(totalStandingsItem);

		for (int trackIndex = 0; trackIndex < season.getTrackCount(); ++trackIndex) {
			final Track track = season.getTrack(trackIndex);
			MenuItem resultItem = new MenuItem((trackIndex + 1) + " - " + track.getName());
			resultItem.setEnabled(false);
			resultMenu.add(resultItem);
			final int idx = trackIndex;
			resultItem.addActionListener(e -> {
				Standings standings = season.getStandings(idx);
				if (standings != null) {
					f.setContentPane(standings.toPanel());
					f.pack();
				}
			});
		}

		f.setMenuBar(menuBar);

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

		totalStandingsItem.addActionListener(e -> {
			showStandings(f, season, drivers);
		});

		final Menu actionMenu = new Menu("Actions");
		final MenuItem nextRaceItem = new MenuItem("Next Race");
		nextRaceItem.addActionListener(e -> {
			final int completedCount = season.getCompletedTrackCount();
			final MenuItem currentTrackItem = resultMenu.getItem(completedCount);
			startRace(season, drivers, f, currentTrackItem, nextRaceItem, totalStandingsItem);
		});
		actionMenu.add(nextRaceItem);
		menuBar.add(actionMenu);
		menuBar.add(resultMenu);

		showStandings(f, season, drivers);

        f.setTitle("F1 Simulator");
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
	}

	public static void showStandings(JFrame f, Season season, Driver[] drivers) {
		JPanel standingsPanel = new JPanel();
		JPanel header = new JPanel();
		header.setBackground(Color.BLACK);
		Map<Driver, Integer> pointMap = new HashMap<>();
		Map<Driver, List<Integer>> positionMap = new HashMap<>();
		for (int i = 0; i < drivers.length; ++i) {
			pointMap.put(drivers[i], 0);
			positionMap.put(drivers[i], new ArrayList<>());
		}

		initTextField(header, 40, "POS", Color.LIGHT_GRAY, Color.BLACK, headerFont).setHorizontalAlignment(JLabel.CENTER);
		initTextField(header, 120, "NAME", Color.LIGHT_GRAY, Color.BLACK, headerFont);
		for (int i = 0; i < season.getTrackCount(); ++i) {
			initTextField(header, 40, Integer.toString(i + 1), Color.LIGHT_GRAY, Color.BLACK, headerFont);
			if (i < season.getCompletedTrackCount()) {
				final Standings standings = season.getStandings(i);
				standings.addPoints(pointMap, positionMap);
			}
		}
		initTextField(header, 60, "TOTAL", Color.LIGHT_GRAY, Color.BLACK, headerFont);
		standingsPanel.add(header);

		List<Driver> driverList = Arrays.stream(drivers).sorted((d1, d2) -> {
			int pts1 = pointMap.get(d1);
			int pts2 = pointMap.get(d2);
			if (pts1 == pts2) {
				for (int pos = 0; pos < drivers.length; ++pos) {
					final int finalPos = pos;
					long posCount1 = positionMap.get(d1).stream().filter(p -> p == finalPos).count();
					long posCount2 = positionMap.get(d2).stream().filter(p -> p == finalPos).count();
					if (posCount1 != posCount2) {
						return (int) (posCount2 - posCount1);
					}
				}
				return 0;
			}
			return pts2 - pts1;
		}).toList();

		int[] dist = {25, 18, 15, 12, 10, 8, 6, 4, 2, 1};
		for (int i = 0; i < driverList.size(); ++i) {
			final Driver driver = driverList.get(i);
			JPanel row = new JPanel();
			Color color = i % 2 == 0 ? bgColor : Color.BLACK;
			row.setBackground(color);
			initTextField(row, 40, Integer.toString(i + 1), Color.CYAN, color, textFont).setHorizontalAlignment(JLabel.CENTER);
			initTextField(row, 120, drivers[i].getName(), Color.WHITE, color, textFont);
			for (int j = 0; j < season.getTrackCount(); ++j) {
				final List<Integer> positions = positionMap.get(driver);
				final int pts = j < positions.size() ? (positions.get(j) < dist.length ? dist[positions.get(j)] : 0) : 0;
				initTextField(row, 40, pts == 0 ? "-" : Integer.toString(pts), Color.LIGHT_GRAY, Color.BLACK, textFont);
			}
			initTextField(row, 60, pointMap.get(driver).toString(), Color.LIGHT_GRAY, Color.BLACK, textFont);
			standingsPanel.add(row);
		}

		standingsPanel.setBackground(Color.BLACK);
		standingsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridLayout layout = new GridLayout(drivers.length + 1, 1);
		standingsPanel.setLayout(layout);
		f.setContentPane(standingsPanel);
		f.pack();
	}

	public static void startRace(Season season, Driver[] drivers, JFrame f, MenuItem currentTrackItem, MenuItem startItem, MenuItem totalStandingsItem) {
		startItem.setEnabled(false);
		totalStandingsItem.setEnabled(false);

		Track track = season.nextTrack();
		prevStandings = new Standings(track, drivers);

		final FormulaSimu p = new FormulaSimu();
		p.setBackground(Color.BLACK);
		p.setBorder(new EmptyBorder(5, 5, 5, 5));

		JPanel header = new JPanel();
		header.setBackground(Color.BLACK);
		initTextField(header, 40, "POS", Color.LIGHT_GRAY, Color.BLACK, headerFont).setHorizontalAlignment(JLabel.CENTER);
		initTextField(header, 120, "NAME", Color.LIGHT_GRAY, Color.BLACK, headerFont);
		initTextField(header, 40, "MOD", Color.LIGHT_GRAY, Color.BLACK, headerFont);
		initTextField(header, 100, "TIME", Color.LIGHT_GRAY, Color.BLACK, headerFont);
		initTextField(header, 100, "INTERVAL", Color.LIGHT_GRAY, Color.BLACK, headerFont);
		initTextField(header, 100, "GAP", Color.LIGHT_GRAY, Color.BLACK, headerFont);
		initTextField(header, 100, "SPEED", Color.LIGHT_GRAY, Color.BLACK, headerFont);
		p.add(header);

		JLabel[] posFields = new JLabel[drivers.length];
		JLabel[] driverFields = new JLabel[drivers.length];
		JLabel[] modFields = new JLabel[drivers.length];
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
			modFields[i] = initTextField(row, 40, "-", Color.WHITE, color, textFont);
			modFields[i].setHorizontalAlignment(JLabel.CENTER);
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
					if (track.getLapCount() == lap) {
						return;
					}

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
					for (int i = 0; i < drivers.length; ++i) {
						Driver driver = prevStandings.getDriver(i);
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
						Color color = Color.WHITE;
						int delta = s.getDelta(i);
						if (delta > 0) {
							modFields[i].setText(Integer.toString(delta) + "▴");
							color = Color.GREEN;
						}
						else if (delta < 0) {
							modFields[i].setText(Integer.toString(-delta) + "▾");
							color = Color.RED;
						}
						else modFields[i].setText("-");
						modFields[i].setForeground(color);
						color = Color.YELLOW;
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

					if (track.getLapCount() == lap + 1) {
						season.save(prevStandings);
						currentTrackItem.setEnabled(true);
						startItem.setEnabled(season.getCompletedTrackCount() < season.getTrackCount());
						totalStandingsItem.setEnabled(true);
					}
				}
			}
		});
		f.setContentPane(p);
		f.pack();
	}
	
	public static JLabel initTextField(JPanel parent, int width, String text, Color fontColor, Color bgColor, Font font) {
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
