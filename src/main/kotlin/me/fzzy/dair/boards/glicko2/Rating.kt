package me.fzzy.dair.boards.glicko2

/**
 * Holds an individual's glicko2-2 rating.
 *
 *
 * glicko2-2 ratings are an average skill value, a standard deviation and a volatility (how consistent the player is).
 * Prof Glickman's paper on the algorithm allows scaling of these values to be more directly comparable with existing rating
 * systems such as Elo or USCF's derivation thereof. This implementation outputs ratings at this larger scale.
 *
 * @author Jeremy Gooch
 */
class Rating {

    /**
     * Return the average skill value of the player.
     *
     * @return double
     */
    var rating = 0.0
    var ratingDeviation = 0.0
    var volatility = 0.0
    var numberOfResults = 0
        private set // the number of results from which the rating has been calculated

    // the following variables are used to hold values temporarily whilst running calculations
    private var workingRating = 0.0
    private var workingRatingDeviation = 0.0
    private var workingVolatility = 0.0

    /**
     * Return the average skill value of the player scaled down
     * to the scale used by the algorithm's internal workings.
     *
     * @return double
     */
    /**
     * Set the average skill value, taking in a value in Glicko2 scale.
     *
     * @param double
     */
    var glicko2Rating: Double
        get() = RatingCalculator.convertRatingToGlicko2Scale(this.rating)
        set(rating) {
            this.rating = RatingCalculator.convertRatingToOriginalGlickoScale(rating)
        }

    /**
     * Return the rating deviation of the player scaled down
     * to the scale used by the algorithm's internal workings.
     *
     * @return double
     */
    /**
     * Set the rating deviation, taking in a value in Glicko2 scale.
     *
     * @param double
     */
    var glicko2RatingDeviation: Double
        get() = RatingCalculator.convertRatingDeviationToGlicko2Scale(ratingDeviation)
        set(ratingDeviation) {
            this.ratingDeviation = RatingCalculator.convertRatingDeviationToOriginalGlickoScale(ratingDeviation)
        }

    /**
     *
     * @param uid           An value through which you want to identify the rating (not actually used by the algorithm)
     * @param ratingSystem  An instance of the RatingCalculator object
     */
    constructor(ratingSystem: RatingCalculator) {
        this.rating = ratingSystem.defaultRating
        this.ratingDeviation = ratingSystem.defaultRatingDeviation
        this.volatility = ratingSystem.defaultVolatility
    }

    constructor(initRating: Double, initRatingDeviation: Double, initVolatility: Double) {
        this.rating = initRating
        this.ratingDeviation = initRatingDeviation
        this.volatility = initVolatility
    }

    /**
     * Used by the calculation engine, to move interim calculations into their "proper" places.
     *
     */
    fun finaliseRating() {
        this.glicko2Rating = workingRating
        this.glicko2RatingDeviation = workingRatingDeviation
        this.volatility = workingVolatility

        this.setWorkingRatingDeviation(0.0)
        this.setWorkingRating(0.0)
        this.setWorkingVolatility(0.0)
    }

    fun incrementNumberOfResults(increment: Int) {
        this.numberOfResults = numberOfResults + increment
    }

    fun setWorkingVolatility(workingVolatility: Double) {
        this.workingVolatility = workingVolatility
    }

    fun setWorkingRating(workingRating: Double) {
        this.workingRating = workingRating
    }

    fun setWorkingRatingDeviation(workingRatingDeviation: Double) {
        this.workingRatingDeviation = workingRatingDeviation
    }
}