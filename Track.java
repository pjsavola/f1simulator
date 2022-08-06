package simu;

public class Track {

	private final int lapCount;
	private final int idealLapTimeMs;
	private final int overtakingGap;

	public Track(int lapCount, int idealLapTimeMs, int overtakingGap) {
		this.lapCount = lapCount;
		this.idealLapTimeMs = idealLapTimeMs;
		this.overtakingGap = overtakingGap;
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
