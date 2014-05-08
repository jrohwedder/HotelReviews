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

import lucene.demo.business.Hotel;
import lucene.demo.business.HotelDatabase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.util.Date;

/** Index all text files under a directory.
 * <p>
 * This is a command-line application demonstrating simple Lucene indexing.
 * Run it with no command-line arguments for usage information.
 */
public class FileIndexer {

    private static String docsPath;
    private static String indexPath;
    private static boolean create;
    private static HotelDatabase hotels;
    private static int curHotel = 0;


    public FileIndexer(String docs, String index, boolean create, HotelDatabase hotels) {
        docsPath = docs;
        indexPath = index;
        this.create = create;
        this.hotels = hotels;

    }

    public void index() {
        final File docDir = new File(docsPath);
        if (!docDir.exists() || !docDir.canRead()) {
            System.out.println("Document directory '" + docDir.getAbsolutePath() + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexPath + "'...");

            Directory dir = FSDirectory.open(new File(indexPath));

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
    }


    private void indexDocs(IndexWriter writer, File file)
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
                // index file
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

                            Hotel hotel = new Hotel(curHotel, line.substring(line.indexOf('>') + 1));
                            hotels.addHotel(hotel);
                        } else if (line.contains("Content")) {
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

    private void addDoc(IndexWriter writer, File file, Document doc) throws IOException {
        if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {

            try {
                writer.addDocument(doc);
            } catch (Exception e) {
                //
            }

        } else {
            writer.updateDocument(new Term("path", file.getPath()), doc);
        }
    }
}
