package lucene.demo.search;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


import lucene.demo.business.Hotel;
import lucene.demo.business.HotelDatabase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/** Simple command-line based search demo. */
public class FileSearcher {

    private static HotelDatabase hotels;
    private static String indexPath;
    private static final int DISPLAY = 10;

    public FileSearcher(String index, HotelDatabase hotels) {
        this.hotels = hotels;
        indexPath = index;
    }

    public boolean search() throws IOException, ParseException {

        // field from the indexed file we are searching
        String field = "content";


        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

        org.apache.lucene.queryparser.classic.QueryParser parser =
                new org.apache.lucene.queryparser.classic.QueryParser(Version.LUCENE_48, field, analyzer);

        // prompt user for stuff to remove
        System.out.println("I want to remove reviews with(Enter to end): ");
        String line = in.readLine();

        if (line == null || line.length() == -1) {
            return false;
        }

        line = line.trim();
        if (line.length() == 0) {
            return false;
        }


        Query query = null;
        try {
            query = parser.parse(line);
        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            e.printStackTrace();
        }

        System.out.println("Searching for: " + query.toString(field));
        doPagingSearch(in, searcher, query);

        reader.close();
        return true;
    }

    public void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query) throws IOException {

        TopDocs results = searcher.search(query, hotels.getTotalReviews());
        ScoreDoc[] hits = results.scoreDocs;

        int numTotalHits = results.totalHits;
        System.out.println(numTotalHits + " total matching documents");


        System.out.println("Before Removal \n");
        displayTop(DISPLAY);

        removeReviews(searcher, query, hits);

        System.out.println("After Removal \n");
        displayTop(DISPLAY);

    }

    private void removeReviews(IndexSearcher searcher, Query query, ScoreDoc[] hits) throws IOException {
        for (int i = 0; i < hits.length; i++) {
            Document doc = searcher.doc(hits[i].doc);
            Integer rating = Integer.parseInt(doc.get("rating"));
            Integer hotelID = Integer.parseInt(doc.get("hotelID"));
            Hotel hotel = hotels.getHotel(hotelID);
            hotel.removeReview(rating);
        }

        removeDocs(query);
    }

    private void removeDocs(Query query) throws IOException {
        Directory dir = FSDirectory.open(new File(indexPath));
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        IndexWriter writer = new IndexWriter(dir, iwc);
        writer.deleteDocuments(query);
        writer.commit();
        writer.close();
    }

    public void displayTop(int numToDisplay) {
        ArrayList<Hotel> allHotels = hotels.getHotels();
        Collections.sort(allHotels, new Comparator<Hotel>() {
            @Override
            public int compare(Hotel o1, Hotel o2) {
                if (o1.getOverallRating() > o2.getOverallRating())
                    return -1;
                else
                    return 1;
            }
        });

        for (int i = 0; i < numToDisplay; i ++) {
            System.out.println((i+1) + ". " + allHotels.get(i));
        }
        System.out.println("\n\n");
    }
}

