public class StateAndReward {

	
	/* State discretization function for the angle controller */
	public static String getStateAngle(double angle, double vx, double vy) {

		/* TODO: IMPLEMENT THIS FUNCTION */
		//0-1.5
		String state = "OneStateToRuleThemAll";
		//Pointing left
		if(angle < -0.05 && angle > -1.5) {
			state = "A";
		}else if(angle > 0.05 && angle < 1.5) {
			//Pointing right
			state = "B";
		}else if(angle <= 0.05 && angle >= -0.05) {
			//Pointing mostly up
			state = "G";
		}else {
			//Pointing down
			state = "D";
		}
		
		
		return state;
	}

	/* Reward function for the angle controller */
	public static double getRewardAngle(double angle, double vx, double vy) {

		/* TODO: IMPLEMENT THIS FUNCTION */
		
		double reward = 0;
		String state = getStateAngle(angle, vx,vy);
		
		if(state == "A"){
			reward = 5;
		}else if(state == "B") {
			reward = 5;
		}else if(state == "G") {
			reward = 10;
		}else if(state == "D") {
			reward = 0;
		}
		return reward;
	}

	/* State discretization function for the full hover controller */
	public static String getStateHover(double angle, double vx, double vy) {

		int angleDiscrete = discretize2(angle, 8, -Math.PI / 4, Math.PI / 4);
		int vxDiscrete	= discretize2(vx, 5, -1, 1);
		int vyDiscrete = discretize2(vy, 5, -1, 1);

		return angleDiscrete + "." + vxDiscrete + "." + vyDiscrete;
	}

	/* Reward function for the full hover controller */
	public static double getRewardHover(double angle, double vx, double vy) {
		
		
		double angleReward = -Math.abs(angle)*10;
		double vxReward = -Math.abs(vx);
		double vyReward = -Math.abs(vy)*2;

		return (angleReward + vxReward + vyReward);
	}

	// ///////////////////////////////////////////////////////////
	// discretize() performs a uniform discretization of the
	// value parameter.
	// It returns an integer between 0 and nrValues-1.
	// The min and max parameters are used to specify the interval
	// for the discretization.
	// If the value is lower than min, 0 is returned
	// If the value is higher than min, nrValues-1 is returned
	// otherwise a value between 1 and nrValues-2 is returned.
	//
	// Use discretize2() if you want a discretization method that does
	// not handle values lower than min and higher than max.
	// ///////////////////////////////////////////////////////////
	public static int discretize(double value, int nrValues, double min,
			double max) {
		if (nrValues < 2) {
			return 0;
		}

		double diff = max - min;

		if (value < min) {
			return 0;
		}
		if (value > max) {
			return nrValues - 1;
		}

		double tempValue = value - min;
		double ratio = tempValue / diff;

		return (int) (ratio * (nrValues - 2)) + 1;
	}

	// ///////////////////////////////////////////////////////////
	// discretize2() performs a uniform discretization of the
	// value parameter.
	// It returns an integer between 0 and nrValues-1.
	// The min and max parameters are used to specify the interval
	// for the discretization.
	// If the value is lower than min, 0 is returned
	// If the value is higher than min, nrValues-1 is returned
	// otherwise a value between 0 and nrValues-1 is returned.
	// ///////////////////////////////////////////////////////////
	public static int discretize2(double value, int nrValues, double min,
			double max) {
		double diff = max - min;

		if (value < min) {
			return 0;
		}
		if (value > max) {
			return nrValues - 1;
		}

		double tempValue = value - min;
		double ratio = tempValue / diff;

		return (int) (ratio * nrValues);
	}

}
