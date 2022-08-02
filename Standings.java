package simu;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Standings {
	private static class LapData {
		private final Driver driver;
		private int time;
		private int gap;
		private boolean dnf;
		private LapData(Driver driver, int time, int gap, boolean dnf) {
			this.driver = driver;
			this.time = time;
			this.gap = gap;
			this.dnf = dnf;
		}
	}
	private final int len;
	private LapData[] lapData;
	private final Standings previous;
	private int bestLap = Integer.MAX_VALUE;
	
	public Standings(Driver[] drivers) {
		len = drivers.length;
		lapData = new LapData[len];
		for (int i = 0; i < len; ++i) {
			lapData[i] = new LapData(drivers[i], 0, i * 200, false);
		}
		previous = null;
	}

	public Standings(Standings s) {
		len = s.len;
		lapData = new LapData[len];
		for (int i = 0; i < len; ++i) {
			lapData[i] = new LapData(s.lapData[i].driver, s.lapData[i].time, s.lapData[i].gap, s.lapData[i].dnf);
		}
		previous = s;
		bestLap = s.bestLap;
	}
	
	public void addTime(Driver driver, int time) {
		for (int i = 0; i < len; ++i) {
			if (lapData[i].driver == driver) {
				lapData[i].time = time;
				if (time == 0) {
					lapData[i].dnf = true;
				}
				else if (time < bestLap) {
					bestLap = time;
				}
				break;
			}
		}
	}

	public void resolve(boolean includeLosses) {
		int gap = 0;
		for (int i = 0; i < len; ++i) {
			if (lapData[i].dnf == false) {
				gap = lapData[i].gap;
				break;
			}
		}
		for (int i = 0; i < len; ++i) {
			lapData[i].gap -= gap;
			lapData[i].gap += lapData[i].time;
		}
		if (includeLosses) {
			int[] losses = new int[len];
			for (int i = 0; i < len; ++i) {
				for (int j = i + 1; j < len; ++j) {
					int aGap = lapData[i].gap;
					int bGap = lapData[j].gap;
					if (Math.abs(aGap - bGap) < 500) {
						// Battle between a and b
						final Driver a = lapData[i].driver;
						final Driver b = lapData[j].driver;
						boolean aWasAhead = aGap - lapData[i].time < bGap - lapData[j].time;
						int aStat = aWasAhead ? 250 : 250;
						int bStat = aWasAhead ? 250 : 250;
						int aRandom = FormulaSimu.random.nextInt(500);
						int bRandom = FormulaSimu.random.nextInt(500);
						int aTotal = aRandom + aStat - aGap;
						int bTotal = bRandom + bStat - bGap;
						int delta = Math.abs(aTotal - bTotal);
						int loss = 1500 - 3 * delta;
						if (loss > 0) {
							loss = FormulaSimu.random.nextInt(loss);
							int extraLoss = loss + FormulaSimu.random.nextInt(500);
							losses[i] = aTotal > bTotal ? loss : extraLoss;
							losses[j] = aTotal > bTotal ? extraLoss : loss;
						}
					}
				}
			}
			for (int i = 0; i < len; ++i) {
				System.err.println("Losses for " + lapData[i].driver.getName() + ": " + losses[i]);
				lapData[i].gap += losses[i];
			}
		}
		int minGap = Arrays.stream(lapData).mapToInt(d -> d.gap).min().orElse(0);
		for (int i = 0; i < len; ++i) {
			lapData[i].gap -= minGap;
		}
		lapData = Arrays.stream(lapData).sorted((d1, d2) -> d1.gap - d2.gap).toArray(LapData[]::new);
	}

	public String getName(int pos) {
		return getDriver(pos).getName();
	}

	public Driver getDriver(int pos) {
		return lapData[pos].driver;
	}
	
	public String getTime(int pos) {
		return FormulaSimu.lapTimeToString(lapData[pos].time);
	}

	public String getGap(int pos) {
		if (pos == 0) return "Interval";

		return FormulaSimu.gapToString(lapData[pos].gap - lapData[pos - 1].gap);
	}

	private int getCompletedLapCount() {
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
		for (int i = 0; i < laps; ++i) {
			for (int j = 0; j < s.lapData.length; ++j) {
				if (s.lapData[j].driver == driver) {
					data[laps - 1 - i] = f.apply(s.lapData[j]);
					break;
				}
			}
			s = s.previous;
		}
		return data;
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
		int[] data = new int[laps];
		for (int i = 0; i < laps; ++i) {
			for (int j = 0; j < s.lapData.length; ++j) {
				if (s.lapData[j].driver == driver) {
					data[laps - 1 - i] = j;
					break;
				}
			}
		}
		return data;
	}

	public int getBestLap() {
		return bestLap;
	}
}
