public class AverageBotTester{
	public static void main(String[] args) {
		String mazeFileName = "C30";
	    int numTrials = 1;

	    AverageBot bot = new AverageBot (mazeFileName);
	    bot.run();

	    int pathStepMultiplier = 100;
	    int averageTotalPathSteps = AverageBot.getPaths() / numTrials;
	    int averageTotalExploreSteps = (Room.getNumMoves() - AverageBot.getPaths()) / numTrials;

	    System.out.println("Done! --> Below are averages of " + numTrials + " trials");
	    System.out.println("Average Total moves between Rooms = " + Room.getNumMoves());
	    System.out.println("Average Total path steps          = " + averageTotalPathSteps);
	    System.out.println("Average Total explore steps       = " + averageTotalExploreSteps);
	    System.out.println("Weighted Score                    = " +
	    	(averageTotalPathSteps * pathStepMultiplier + averageTotalExploreSteps));
	    }
}