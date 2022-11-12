package simu;

public class Track {

	private final String name;
	private final int lapCount;
	private final int idealLapTimeMs;
	private final int overtakingGap;

	public Track(String name, int lapCount, int idealLapTimeMs, int overtakingGap) {
		this.name = name;
		this.lapCount = lapCount;
		this.idealLapTimeMs = idealLapTimeMs;
		this.overtakingGap = overtakingGap;
	}

	public String getName() {
		return name;
	}

	public int getLapCount() {
		return lapCount;
	}

	public int getIdealLapTimeMs() {
		return idealLapTimeMs;
	}

	public int getOvertakingGap() {
		return overtakingGap;
	}
}
