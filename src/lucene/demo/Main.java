/*
 * Main.java
 *
 * Created on 6 March 2006, 11:51
 *
 */

package lucene.demo;

import lucene.demo.business.*;

import lucene.demo.search.FileIndexer;
import lucene.demo.search.FileSearcher;

/**
 *
 * @author John
 */
public class Main {

    private static HotelDatabase hotels;
    private static String indexPath;
    private static String docsPath;
    private static boolean create;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        hotels = new HotelDatabase();

        // Getting parameters for file locations
        String usage = "java org.apache.lucene.demo.IndexFiles"
                + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
                + "This indexes the documents in DOCS_PATH, creating a Lucene index"
                + "in INDEX_PATH that can be searched with SearchFiles";
        indexPath = "index";
        docsPath = null;
        create = true;
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

        // create indexer and searcher
        FileIndexer fileIndexer = new FileIndexer(docsPath, indexPath, create, hotels);
        FileSearcher fileSearcher = new FileSearcher(indexPath, hotels);

        fileIndexer.index();

        // keep removing reviews until user ends it
        boolean isRemoving = true;
        while (isRemoving) {
            isRemoving = fileSearcher.search();
        }



    }
}




