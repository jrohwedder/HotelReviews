HotelReviews
============

Instructions:

I'm using an IDE.

Assuming you have the lucene libraries set up.
So in "Run Configurations" or "Edit Configurations" or something of that sort you need to pass 2 arguments to Main.java
-docs [source of dataset] -index [target index directory]

For example, I pass in:
-docs /home/jrohwedder/IdeaProjects/HotelReviews/reviews/ -index /home/jrohwedder/IdeaProjects/HotelReviews/temp/

Wait for a bit. You'll be prompted for a query. Reviews that match this query will be removed from the index and the database.