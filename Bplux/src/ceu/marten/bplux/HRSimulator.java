package ceu.marten.bplux;

/**
 * Created by martencarlos on 25/07/13.
 */
public class HRSimulator {
	@SuppressWarnings("unused")
	private double babyBPM = 0.0d;
	@SuppressWarnings("unused")
	private double childBPM = 0.0d;
	private double adultsBPM = 0.0d;
	@SuppressWarnings("unused")
	private double athletesBPM = 0.0d;
	private int counter = 0;

	public HRSimulator() {
		// using algorithm + standard measures of safe zones depending on ages
		babyBPM = 100 + (double) (Math.random() * ((160 - 100) + 1));
		childBPM = 60 + (double) (Math.random() * ((140 - 60) + 1));
		athletesBPM = 40 + (double) (Math.random() * ((60 - 40) + 1));
	}

	public double getAdultsBPM() {
		if (counter < 10) {
			adultsBPM = 60 + (double) (Math.random() * ((100 - 60) + 1));
			counter++;
		} else {
			adultsBPM = 40 + (double) (Math.random() * ((160 - 40) + 1));
			counter = 0;
		}

		return adultsBPM;
	}
}
