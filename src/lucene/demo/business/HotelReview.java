package lucene.demo.business;

/**
 * Created by jrohwedder on 5/6/14.
 */
public class HotelReview {
    private String content;
    private int overallRating;

    // TODO: Implement after basic functionality works
    private float valueRating;
    private float roomRating;
    private float locationRating;
    private float cleanlinessRating;
    private float checkInRating;
    private float serviceRating;
    private float businessServiceRating;
    // TODO: end



    public HotelReview(String content, int rating) {
        this.content = content;
        overallRating = rating;
    }

    public int getOverallRating() {
        return overallRating;
    }

    public String getContent() {
        return content;
    }
}
