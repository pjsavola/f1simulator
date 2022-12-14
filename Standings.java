package simu;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Function;

public class Standings {
	private static class LapData {
		private final Driver driver;
		private int time;
		private int gap;
		private int dnfPos;
		private LapData(Driver driver, int time, int gap, int dnfPos) {
			this.driver = driver;
			this.time = time;
			this.gap = gap;
			this.dnfPos = dnfPos;
		}
	}

	private final Track track;
	private final int len;
	private LapData[] lapData;
	private final Standings previous;
	private int bestLap = Integer.MAX_VALUE;
	private Driver bestLapDriver = null;
	private final Driver[] grid;
	
	public Standings(Track track, Driver[] drivers) {
		this.track = track;
		len = drivers.length;
		lapData = new LapData[len];
		for (int i = 0; i < len; ++i) {
			int lapTime = drivers[i].getLapTime(track, false);
			lapData[i] = new LapData(drivers[i], lapTime, 0, -1);
			lapData[i].gap = lapTime;
		}
		sort();
		grid = new Driver[len];
		for (int i = 0; i < len; ++i) {
			lapData[i].gap = i * 200;
			grid[i] = lapData[i].driver;
		}
		previous = null;
	}

	public Standings(Standings s) {
		track = s.track;
		len = s.len;
		lapData = new LapData[len];
		for (int i = 0; i < len; ++i) {
			lapData[i] = new LapData(s.lapData[i].driver, s.lapData[i].time, s.lapData[i].gap, s.lapData[i].dnfPos);
		}
		previous = s;
		bestLap = s.bestLap;
		bestLapDriver = s.bestLapDriver;
		grid = s.grid;
	}
	
	public void addTime(Driver driver, int time) {
		for (int i = 0; i < len; ++i) {
			if (lapData[i].driver == driver) {
				if (lapData[i].dnfPos < 0) {
					lapData[i].time = time;
					if (time == 0) {
						lapData[i].dnfPos = i;
					}
				} else {
					lapData[i].time = 0;
				}
				break;
			}
		}
	}

	private int getRunnerCount() {
		int len = 0;
		while (len < this.len) {
			if (lapData[len].dnfPos >= 0) {
				break;
			}
			++len;
		}
		return len;
	}
	
	public void sort() {
		lapData = Arrays.stream(lapData).sorted((d1, d2) -> (d1.dnfPos - d2.dnfPos) * 10000000 + d1.gap - d2.gap).toArray(LapData[]::new);
	}

	public void resolve() {
		int len = getRunnerCount();
		final int gap = lapData[0].gap;
		for (int i = 0; i < len; ++i) {
			lapData[i].gap -= gap;
			lapData[i].gap += lapData[i].time;
		}
		int[] losses = new int[len];
		boolean[] dnfs = new boolean[len];
		int newDnfs = 0;
		for (int i = 0; i < len; ++i) {
			for (int j = i + 1; j < len; ++j) {
				int aGap = lapData[i].gap;
				int bGap = lapData[j].gap;
				final int limit = track.getOvertakingGap();
				if (bGap - aGap < limit) {
					// Battle between a and b
					final Driver a = lapData[i].driver;
					final Driver b = lapData[j].driver;

					if (bGap > aGap) {
						// B is chasing A but not yet overtaking, both lose a bit of time.
						int maxPenalty = limit - bGap + aGap;
						int bLoss = FormulaSimu.random.nextInt(maxPenalty - maxPenalty * b.getOvertaking() / 200);
						if (bLoss > 0) {
							int aLoss = FormulaSimu.random.nextInt(bLoss - bLoss * a.getDefense() / 200);
							//System.err.println(b.getName() + " chases " + a.getName() + ", losses: " + bLoss + ", " + aLoss);
							losses[i] += aLoss;
							losses[j] += bLoss;
						}
					} else {
						// B tries to overtake A, let's check if it succeeds. Both also lose some time.
						int bLoss = FormulaSimu.random.nextInt(2 * limit - limit * b.getOvertaking() / 100);
						int aLoss = FormulaSimu.random.nextInt(2 * limit - limit * a.getDefense() / 100);
						//System.err.println(b.getName() + " tries to overtake " + a.getName() + ", losses: " + bLoss + ", " + aLoss);
						int interval = aGap + aLoss - bGap - bLoss;
						int distanceFactor = Math.max(0, 500 - Math.abs(interval));
						int skillFactor = Math.max(0, 500 - Math.abs(a.getSkill() - b.getSkill()) * 25);
						int randomFactor = FormulaSimu.random.nextInt(10000);
						int randomFactor2 = FormulaSimu.random.nextInt(10000);
						if (randomFactor < 500 + distanceFactor + skillFactor) {
							System.err.println(a.getName() + " collides when battling with " + b.getName());
							if (FormulaSimu.random.nextInt(10) < 2) {
								if (!dnfs[i]) ++newDnfs;
								dnfs[i] = true;
							} else {
								a.addDamage(FormulaSimu.random.nextInt(5));
								aLoss += FormulaSimu.random.nextInt(5000);
							}
						}
						if (randomFactor2 < 500 + distanceFactor + skillFactor) {
							System.err.println(b.getName() + " collides when battling with " + a.getName());
							if (FormulaSimu.random.nextInt(10) < 2) {
								if (!dnfs[j]) ++newDnfs;
								dnfs[j] = true;
							} else {
								b.addDamage(FormulaSimu.random.nextInt(5));
								bLoss += FormulaSimu.random.nextInt(5000);
							}
						}
						losses[i] += aLoss;
						losses[j] += bLoss;
					}
				}
			}
		}
		for (int i = 0; i < len; ++i) {
			//System.err.println("Losses for " + lapData[i].driver.getName() + ": " + losses[i]);
			lapData[i].gap += losses[i];
			lapData[i].time += losses[i];
			if (dnfs[i]) {
				int pos = len - newDnfs;
				--newDnfs;
				System.err.println(lapData[i].driver.getName() + " gets DNF and position " + pos);
				lapData[i].dnfPos = pos;
			}
		}
		sort();

		len = getRunnerCount();
		int minGap = Arrays.stream(lapData).limit(len).mapToInt(d -> d.gap).min().orElse(0);
		for (int i = 0; i < len; ++i) {
			lapData[i].gap -= minGap;
		}

		for (int i = 1; i < len; ++i) {
			int delta = lapData[i].gap - lapData[i - 1].gap;
			if (delta < track.getOvertakingGap()) {
				int loss = FormulaSimu.random.nextInt(track.getOvertakingGap()) - Math.min(0, delta);
				//System.err.println("Queue losses for " + lapData[i].driver.getName() + ": " + loss);
				lapData[i].gap += loss;
				lapData[i].time += loss;
			}
		}
		for (int i = 0; i < len; ++i) {
			lapData[i].driver.randomizeForm();
			if (lapData[i].time < bestLap) {
				bestLap = lapData[i].time;
				bestLapDriver = lapData[i].driver;
			}
		}
	}

	public String getName(int pos) {
		return getDriver(pos).getName();
	}

	public Driver getDriver(int pos) {
		return lapData[pos].driver;
	}

	public int getDelta(int pos) {
		Driver d = lapData[pos].driver;
		int[] positions = getPositions(d);
		int delta = 0;
		if (positions.length > 1) {
			delta = positions[positions.length - 2] - positions[positions.length - 1];
		}
		return delta;
	}
	
	public String getTime(int pos) {
		if (lapData[pos].dnfPos >= 0) return "-";

		return FormulaSimu.lapTimeToString(lapData[pos].time);
	}

	public String getInterval(int pos) {
		if (pos == 0) return "Interval";

		if (lapData[pos].dnfPos >= 0) return "DNF";
		return FormulaSimu.gapToString(lapData[pos].gap - lapData[pos - 1].gap);
	}

	public String getGap(int pos) {
		if (pos == 0) return "Lap " + getCompletedLapCount() + "/" + track.getLapCount();

		if (lapData[pos].dnfPos >= 0) return "DNF";
		return FormulaSimu.gapToString(lapData[pos].gap);
	}

	public boolean isDnf(Driver driver) {
		for (int i = 0; i < lapData.length; ++i) {
			return lapData[i].dnfPos >= 0;
		}
		return false;
	}

	public int getCompletedLapCount() {
		int laps = 0;
		Standings prev = previous;
		while (prev != null) {
			prev = prev.previous;
			++laps;
		}
		return laps;
	}

	private int[] getData(Driver driver, Function<LapData, Integer> f) {
		int laps = getCompletedLapCount();
		Standings s = this;
		int[] data = new int[laps];
		int limit = data.length;
		for (int i = 0; i < laps; ++i) {
			for (int j = 0; j < s.lapData.length; ++j) {
				if (s.lapData[j].driver == driver) {
					if (s.lapData[j].dnfPos >= 0) {
						--limit;
						break;
					}
					data[laps - 1 - i] = f.apply(s.lapData[j]);
					break;
				}
			}
			s = s.previous;
		}
		return Arrays.stream(data).limit(limit).toArray();
	}

	public int[] getTimes(Driver driver) {
		return getData(driver, d -> d.time);
	}
	
	public int[] getGaps(Driver driver) {
		return getData(driver, d -> d.gap);
	}

	public int[] getPositions(Driver driver) {
		int laps = getCompletedLapCount();
		Standings s = this;
		int[] data = new int[laps + 1];
		for (int i = 0; i < laps; ++i) {
			for (int j = 0; j < s.lapData.length; ++j) {
				if (s.lapData[j].driver == driver) {
					data[laps - i] = j;
					break;
				}
			}
			s = s.previous;
		}
		for (int i = 0; i < grid.length; ++i) {
			if (grid[i] == driver) {
				data[0] = i;
				break;
			}
		}
		return data;
	}

	public int getBestLap() {
		return bestLap;
	}

	public JPanel toPanel() {
		final JPanel header = new JPanel();
		header.setBackground(Color.BLACK);
		FormulaSimu.initTextField(header, 40, "POS", Color.LIGHT_GRAY, Color.BLACK, FormulaSimu.headerFont).setHorizontalAlignment(JLabel.CENTER);
		FormulaSimu.initTextField(header, 120, "NAME", Color.LIGHT_GRAY, Color.BLACK, FormulaSimu.headerFont);
		FormulaSimu.initTextField(header, 100, "INTERVAL", Color.LIGHT_GRAY, Color.BLACK, FormulaSimu.headerFont);
		FormulaSimu.initTextField(header, 100, "BEST", Color.LIGHT_GRAY, Color.BLACK, FormulaSimu.headerFont);

		final JPanel summary = new JPanel();
		FormulaSimu.initTextField(summary, 360, track.getName(), Color.YELLOW, Color.BLACK, FormulaSimu.titleFont).setHorizontalAlignment(JLabel.CENTER);
		summary.setBackground(Color.BLACK);
		summary.add(header);

		for (int i = 0; i < grid.length; ++i) {
			final JPanel row = new JPanel();
			final Color color = i % 2 == 0 ? FormulaSimu.bgColor : Color.BLACK;
			row.setBackground(color);
			FormulaSimu.initTextField(row, 40, Integer.toString(i + 1), Color.CYAN, color, FormulaSimu.textFont).setHorizontalAlignment(JLabel.CENTER);
			FormulaSimu.initTextField(row, 120, getName(i), Color.WHITE, color, FormulaSimu.textFont);
			FormulaSimu.initTextField(row, 100, getGap(i), Color.WHITE, color, FormulaSimu.textFont);
			final Driver driver = getDriver(i);
			final OptionalInt personalBest = Arrays.stream(getTimes(driver)).min();
			final boolean purple = bestLapDriver == driver;
			final String lapString = personalBest.isPresent() ? FormulaSimu.lapTimeToString(personalBest.getAsInt()) : "-";
			FormulaSimu.initTextField(row, 100, lapString, purple ? Color.MAGENTA : Color.WHITE, color, FormulaSimu.textFont);
			summary.add(row);
		}

		final GridLayout layout = new GridLayout(grid.length + 2, 1);
		summary.setLayout(layout);
		return summary;
	}

	void addPoints(Map<Driver, Integer> points, Map<Driver, List<Integer>> positionMap) {
		int[] dist = {25, 18, 15, 12, 10, 8, 6, 4, 2, 1};
		for (int i = 0; i < grid.length; ++i) {
			final Driver driver = getDriver(i);
			positionMap.get(driver).add(i);
			if (i >= dist.length) {
				continue;
			}
			final boolean purple = bestLapDriver == driver;
			Integer pts = points.get(driver);
			if (purple) {
				pts = pts + 1;
			}
			points.put(driver, pts + dist[i]);
		}
	}
}
