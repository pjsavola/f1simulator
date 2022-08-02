package simu;

public class Track {

	private final int idealLapTimeMs;
	private final int overtakingGap;

	public Track(int idealLapTimeMs, int overtakingGap) {
		this.idealLapTimeMs = idealLapTimeMs;
		this.overtakingGap = overtakingGap;
	}

	public int getIdealLapTimeMs() {
		return idealLapTimeMs;
	}

	public int getOvertakingGap() {
		return overtakingGap;
	}
}
