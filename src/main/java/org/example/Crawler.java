package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.concurrent.Callable;

public class Crawler {
    public HashSet<String> urlHash;
    public int MAX_DEPTH = 2;
    public static Connection connection = null;
    public Crawler(){
     // initialize urlHash
        urlHash = new HashSet<>();
        connection = DatabaseConnection.getConnection();
    }
    //to save webpages data into databases;
    public void getPageTextAndLinks(String url, int depth) {
        //if urlHash does not contains url
        if (!urlHash.contains(url)) {
            //  add url to url Hashset
            if (urlHash.add(url)) {
                System.out.println(url);
            }
            try {
                // connecting to webpage and get webpage as document objects
                Document document = Jsoup.connect(url).timeout(5000).get();
                // get text inside that document /webpage
                String text = document.text().length()>1000?document.text().substring(0,999):document.text();
                // get title of that pages
                String title = document.title();
                // prepare an insertion command
                PreparedStatement preparedStatement = connection.prepareStatement("Insert into pages values(?, ?, ?)");
                //at first "?"
                preparedStatement.setString(1, title);
                //at second "?"
                preparedStatement.setString(2, url);
                //at third "?"
                preparedStatement.setString(3, text);
                // execute the prepared command
                preparedStatement.executeUpdate();

                // increasing depth
                depth++;
                if (depth == 2) {
                    return;
                }
                //get all links/anchor tags that are present in webpages
                Elements availableLinksOnPage = document.select("a[href]");
                //for every link, call this function recursively
                for (Element currentLink : availableLinksOnPage) {
                    getPageTextAndLinks(currentLink.attr("abs:href"), depth);
                }
            }
            catch (IOException | SQLException ioException) {
                ioException.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        crawler.getPageTextAndLinks("http://www.javatpoint.com",0);
    }

}