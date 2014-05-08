/*
 * Accommodation.java
 *
 * Created on 6 March 2006, 11:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lucene.demo.business;

import java.util.ArrayList;

/**
 *
 * @author John
 */
public class Hotel {

    private int reviews;
    private int overallSum;
    private float overallRating;
    private float changeAfterRemoval;
    
    /** Creates a new instance of Accommodation */
    public Hotel() {
        overallSum = 0; reviews = 0; changeAfterRemoval = 0;
    }

    /** Creates a new instance of Accommodation */
    public Hotel(int id,
                 String name, 
                 String city, 
                 String description) {
        this.id = id;     
        this.name = name;     
        this.description = description;     
        this.city = city;
        overallSum = 0;
        changeAfterRemoval =0;
    }

    public Hotel(int id, String name) {
        this.id = id;
        this.name = name;
        overallSum = 0;
        changeAfterRemoval = 0;
    }
    
    /**
     * Holds value of property name.
     */
    private String name;

    /**
     * Getter for property title.
     * @return Value of property title.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for property title.
     * @param name New value of property title.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Holds value of property id.
     */
    private int id;

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Setter for property id.
     * @param id New value of property id.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Holds value of property description.
     */
    private String description;

    /**
     * Getter for property details.
     * @return Value of property details.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Setter for property details.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Holds value of property city.
     */
    private String city;

    /**
     * Getter for property city.
     * @return Value of property city.
     */
    public String getCity() {
        return this.city;
    }

    /**
     * Setter for property city.
     * @param city New value of property city.
     */
    public void setCity(String city) {
        this.city = city;
    }

    public String toString() {
        return "Hotel "
               + getId()
               +": "
               + getName()
               +" \t with "
                + getOverallRating()
                + " overall rating \t and "
                + getReviews()
                + " reviews";
    }

    public void addReview(int rating) {
        overallSum += rating;
        reviews += 1;
    }

    public void removeReview(int rating) {
        overallSum -= rating;
        reviews -=1;
    }

    public float getOverallRating() {
        if (reviews > 0)
            return (float)overallSum/reviews;
        else
            return 0;
    }

//    public int getOverallSum() {
//        return overallSum;
//    }
//
//    public void setOverallSum(int overallSum) {
//        this.overallSum = overallSum;
//    }
//
//    public void setReviews(int reviews) {
//        this.reviews = reviews;
//    }
//
    public int getReviews() {
        return reviews;
    }

    public float getChangeAfterRemoval() {
        return changeAfterRemoval;
    }

    public void setChangeAfterRemoval(float changeAfterRemoval) {
        this.changeAfterRemoval = changeAfterRemoval;
    }
}
