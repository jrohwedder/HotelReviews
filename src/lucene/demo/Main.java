/*
 * Main.java
 *
 * Created on 6 March 2006, 11:51
 *
 */

package lucene.demo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import lucene.demo.business.*;

import org.apache.lucene.document.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author John
 */
public class Main {

    private static HotelDatabase hotels;
    private static int curHotel = 0;
    private static final int DISPLAY = 10;
    private static String indexPath;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        // The indexer takes 2 arguments -docs [data file location] and -index [location to create index]
        // For example, I use
        // -docs /home/jrohwedder/IdeaProjects/HotelReviews/reviews/
        // -index /home/jrohwedder/IdeaProjects/HotelReviews/temp/

        hotels = new HotelDatabase();

        /////////////////
        // INDEX START //
        /////////////////

        String usage = "java org.apache.lucene.demo.IndexFiles"
                + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
                + "This indexes the documents in DOCS_PATH, creating a Lucene index"
                + "in INDEX_PATH that can be searched with SearchFiles";
        indexPath = "index";
        String docsPath = null;
        boolean create = true;
        for (int i = 0; i < args.length; i++) {
            if ("-index".equals(args[i])) {
                indexPath = args[i + 1];
                i++;
            } else if ("-docs".equals(args[i])) {
                docsPath = args[i + 1];
                i++;
            } else if ("-update".equals(args[i])) {
                create = false;
            }
        }

        if (docsPath == null) {
            System.err.println("Usage: " + usage);
            System.exit(1);
        }

        final File docDir = new File(docsPath);
        if (!docDir.exists() || !docDir.canRead()) {
            System.out.println("Document directory '" + docDir.getAbsolutePath() + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexPath + "'...");

            Directory dir = FSDirectory.open(new File(indexPath));
            // :Post-Release-Update-Version.LUCENE_XY:
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);

            if (create) {
                // Create a new index in the directory, removing any
                // previously indexed documents:
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            } else {
                // Add new documents to an existing index:
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            }

            // Optional: for better indexing performance, if you
            // are indexing many documents, increase the RAM
            // buffer.  But if you do this, increase the max heap
            // size to the JVM (eg add -Xmx512m or -Xmx1g):
            //
            // iwc.setRAMBufferSizeMB(256.0);

            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDocs(writer, docDir);
            writer.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }

        /////////////////
        // INDEX END   //
        /////////////////


        //////////////////
        // SEARCH START //
        //////////////////

        while (true) {
            //String usage =
            //        "Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/core/4_1_0/demo/ for details.";
            if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
                System.out.println(usage);
                System.exit(0);
            }

            String index = "temp";
            String field = "content";
            String queries = null;
            int repeat = 0;
            boolean raw = false;
            String queryString = null;
            int hitsPerPage = 10;

            for (int i = 0; i < args.length; i++) {
                if ("-index".equals(args[i])) {
                    index = args[i + 1];
                    i++;
                } else if ("-field".equals(args[i])) {
                    field = args[i + 1];
                    i++;
                } else if ("-queries".equals(args[i])) {
                    queries = args[i + 1];
                    i++;
                } else if ("-query".equals(args[i])) {
                    queryString = args[i + 1];
                    i++;
                } else if ("-repeat".equals(args[i])) {
                    repeat = Integer.parseInt(args[i + 1]);
                    i++;
                } else if ("-raw".equals(args[i])) {
                    raw = true;
                } else if ("-paging".equals(args[i])) {
                    hitsPerPage = Integer.parseInt(args[i + 1]);
                    if (hitsPerPage <= 0) {
                        System.err.println("There must be at least 1 hit per page.");
                        System.exit(1);
                    }
                    i++;
                }
            }

            File file = new File(index);
            System.out.println(file.getAbsolutePath());
            IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);

            BufferedReader in = null;
            if (queries != null) {
                in = new BufferedReader(new InputStreamReader(new FileInputStream(queries), StandardCharsets.UTF_8));
            } else {
                in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            }

            org.apache.lucene.queryparser.classic.QueryParser parser =
                    new org.apache.lucene.queryparser.classic.QueryParser(Version.LUCENE_48, field, analyzer);

            if (queries == null && queryString == null) {                        // prompt the user
                System.out.println("Enter query: ");
            }

            String line = queryString != null ? queryString : in.readLine();

            if (line == null || line.length() == -1) {
                break;
            }

            line = line.trim();
            if (line.length() == 0) {
                break;
            }

            Query query = parser.parse(line);
            System.out.println("Searching for: " + query.toString(field));

            if (repeat > 0) {                           // repeat & time as benchmark
                Date start2 = new Date();
                for (int i = 0; i < repeat; i++) {
                    searcher.search(query, null, 100);
                }
                Date end = new Date();
                System.out.println("Time: " + (end.getTime() - start2.getTime()) + "ms");
            }

            doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null);

            if (queryString != null) {
                break;
            }
            reader.close();
        }
    }




    static void indexDocs(IndexWriter writer, File file)
            throws IOException {
        // do not try to index files that cannot be read
        if (file.canRead()) {
            if (file.isDirectory()) {
                String[] files = file.list();
                // an IO error could occur
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        indexDocs(writer, new File(file, files[i]));
                    }
                }
            } else {

                FileInputStream fis;
                try {
                    fis = new FileInputStream(file);
                } catch (FileNotFoundException fnfe) {
                    // at least on windows, some temporary files raise this exception with an "access denied" message
                    // checking if the file can be read doesn't help
                    return;
                }

                try {

                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;

                    //int curReview = 0;
                    while ((line = br.readLine()) != null) {
                        if (line.contains("<URL>")) {

                            Hotel hotel = new Hotel(curHotel, line.substring(line.indexOf('>')+1));
                            hotels.addHotel(hotel);
                        }
                        else if (line.contains("Content")) {
                            Document docReview = new Document();
                            String content = line.substring(line.indexOf('>') + 1);
                            docReview.add(new TextField("content", content, Field.Store.YES));
                            line = br.readLine();

                            while (!line.contains("Overall")) {
                                line = br.readLine();

                            }
                            Integer rating = Integer.parseInt(line.substring(line.indexOf('>') + 1));
                            docReview.add(new IntField("rating", rating, Field.Store.YES));
                            docReview.add(new IntField("hotelID", curHotel, Field.Store.YES));
                            try {
                                Hotel hotel = hotels.getHotel(curHotel);
                                hotel.addReview(rating);
                            } catch (NullPointerException e) {
                                // File did not have the field I use to create hotel object
                                // So let's give it a generic name
                                Hotel hotel = new Hotel(curHotel, "Hotel " + curHotel + ": No Name in File");
                                hotels.addHotel(hotel);
                                hotel.addReview(rating);
                            }

                            //HotelReview review = new HotelReview(content, rating);
                            addDoc(writer, file, docReview);
                            //curReview++;
                        }

                    }
                    curHotel++;

                } finally {
                    fis.close();
                }
            }
        }
    }

    private static void addDoc(IndexWriter writer, File file, Document doc) throws IOException {
        if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
            // New index, so we just add the document (no old document can be there):
            //System.out.println("adding " + file);
            try {
                writer.addDocument(doc);
            } catch (Exception e) {
                //
            }

        } else {
            // Existing index (an old copy of this document may have been indexed) so
            // we use updateDocument instead to replace the old one matching the exact
            // path, if present:
            //System.out.println("updating " + file);
            writer.updateDocument(new Term("path", file.getPath()), doc);
        }
    }





    public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query,
                                      int hitsPerPage, boolean raw, boolean interactive) throws IOException {

        TopDocs results = searcher.search(query, hotels.getTotalReviews());
        ScoreDoc[] hits = results.scoreDocs;

        int numTotalHits = results.totalHits;
        System.out.println(numTotalHits + " total matching documents");

        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage);

        System.out.println("Before Removal \n");
        displayTop(DISPLAY);

        // remove reviews
        for (int i = 0; i < hits.length; i++) {
            Document doc = searcher.doc(hits[i].doc);
            Integer rating = Integer.parseInt(doc.get("rating"));
            Integer hotelID = Integer.parseInt(doc.get("hotelID"));
            Hotel hotel = hotels.getHotel(hotelID);
            hotel.removeReview(rating);
        }

        removeDocs(query);

        System.out.println("After Removal \n");
        displayTop(DISPLAY);


//      THIS CODE WAS IN DEMO CODE FOR RETURNING SEARCH RESULTS
//      I'M NOT DELETING IN CASE WE WANT TO ALSO INCORPORATE
//      A SEARCH WHERE WE DON'T DELETE RESULTS
//
//        while (true) {
//            if (end > hits.length) {
//                System.out.println("Only results 1 - " + hits.length +" of " + numTotalHits + " total matching documents collected.");
//                System.out.println("Collect more (y/n) ?");
//                String line = in.readLine();
//                if (line.length() == 0 || line.charAt(0) == 'n') {
//                    break;
//                }
//
//                hits = searcher.search(query, numTotalHits).scoreDocs;
//            }
//
//            end = Math.min(hits.length, start + hitsPerPage);
//
//            for (int i = start; i < end; i++) {
//                if (raw) {                              // output raw format
//                    System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
//                    continue;
//                }
//
//                Document doc = searcher.doc(hits[i].doc);
//                String path = doc.get("path");
//                if (path != null) {
//                    System.out.println((i+1) + ". " + path);
//                    String title = doc.get("title");
//                    if (title != null) {
//                        System.out.println("   Title: " + doc.get("title"));
//                    }
//                } else {
//                    System.out.println((i+1) + ". " + "No path for this document");
//                }
//
//            }
//
//            if (!interactive || end == 0) {
//                break;
//            }
//
//            if (numTotalHits >= end) {
//                boolean quit = false;
//                while (true) {
//                    System.out.print("Press ");
//                    if (start - hitsPerPage >= 0) {
//                        System.out.print("(p)revious page, ");
//                    }
//                    if (start + hitsPerPage < numTotalHits) {
//                        System.out.print("(n)ext page, ");
//                    }
//                    System.out.println("(q)uit or enter number to jump to a page.");
//
//                    String line = in.readLine();
//                    if (line.length() == 0 || line.charAt(0)=='q') {
//                        quit = true;
//                        break;
//                    }
//                    if (line.charAt(0) == 'p') {
//                        start = Math.max(0, start - hitsPerPage);
//                        break;
//                    } else if (line.charAt(0) == 'n') {
//                        if (start + hitsPerPage < numTotalHits) {
//                            start+=hitsPerPage;
//                        }
//                        break;
//                    } else {
//                        int page = Integer.parseInt(line);
//                        if ((page - 1) * hitsPerPage < numTotalHits) {
//                            start = (page - 1) * hitsPerPage;
//                            break;
//                        } else {
//                            System.out.println("No such page");
//                        }
//                    }
//                }
//                if (quit) break;
//                end = Math.min(numTotalHits, start + hitsPerPage);
//            }
//        }
    }

    private static void removeDocs(Query query) throws IOException {
        Directory dir = FSDirectory.open(new File(indexPath));


        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);


        IndexWriter writer = new IndexWriter(dir, iwc);
        writer.deleteDocuments(query);
        writer.commit();
        writer.close();
    }

    public static void displayTop(int numToDisplay) {
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
