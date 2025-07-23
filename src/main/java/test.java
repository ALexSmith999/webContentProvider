import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Collation;
import org.bson.*;
import org.jsoup.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Sorts.descending;

public class test {
    //PARENTS
    //"https://books.toscrape.com";
    //"https://quotes.toscrape.com/"
    //"https://sandbox.oxylabs.io/products"
    //"https://www.scrapethissite.com/pages/"

    /*Document existing = collection.find(eq("uid"
                    , "26ba5d403cd8ee6191c37fe397820b4328bc18f0"))
            .sort(descending("version"))
            .limit(1)
            .first();*/
    private static String getMetaProperty(org.jsoup.nodes.Document doc, String property) {
        Element meta = doc.selectFirst("meta[property=" + property + "]");
        return meta != null ? meta.attr("content").replaceAll("[\\[\\]']", "").trim() : "Not found";
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {

        StringBuilder uri = new StringBuilder("mongodb://");
        uri.append("vm");
        uri.append(":");
        uri.append(27017);

        MongoClient mongoclient = MongoClients.create(uri.toString());
        MongoDatabase DB = mongoclient.getDatabase("loads");

        MongoCollection<Document> collection
                = DB.getCollection("COLLECTION");

        for (var item : collection.find()) {

            String child = (String) item.get("link");;
            String content = (String) item.get("content");
            org.jsoup.nodes.Document html = Jsoup.parse(content);

            if (child.startsWith("https://books.toscrape.com")) {
                Element title = html.selectFirst("div.product_main > h1");
                if (title != null) {
                    System.out.println(title.text());
                    Element descHeader = html.selectFirst("div#product_description");
                    if (descHeader != null) {
                        Element descPara = descHeader.nextElementSibling();
                        if (descPara != null && descPara.tagName().equals("p")) {
                            System.out.println("Description: " + descPara.text());
                        }
                    }
                    Element table = html.selectFirst("table.table.table-striped");
                    if (table != null) {
                        System.out.println("Product Info Table:");
                        Elements rows = table.select("tr");
                        for (Element row : rows) {
                            String key = row.selectFirst("th").text();
                            String value = row.selectFirst("td").text();
                            System.out.println("  " + key + ": " + value);
                        }
                    }
                    Element rating = html.selectFirst("p.star-rating");
                    if (rating != null) {
                        String classes = rating.className();  // e.g., "star-rating Three"
                        String[] classParts = classes.split("\\s+");
                        if (classParts.length > 1) {
                            String star = classParts[1];
                            System.out.println("Rating: " + star + " stars");
                        }
                    }
                }
            } else if (child.startsWith("https://quotes.toscrape.com")) {

                Element authorDetails = html.selectFirst("div.author-details");
                if (authorDetails != null) {
                    Element author = authorDetails.selectFirst("h3.author-title");
                    Element birth = authorDetails.selectFirst("span.author-born-date");
                    Element loc = authorDetails.selectFirst("span.author-born-location");
                    Element desc = authorDetails.selectFirst("div.author-description");

                    System.out.println(author.text() + " " + birth.text() + " " + loc.text());
                    System.out.println(desc.text());
                    System.out.println("\n");
                }

                Elements quotes = html.select("div.quote");
                if (!quotes.isEmpty()) {
                    for (var quote : quotes) {
                        System.out.println(quote.selectFirst("span.text").text());;
                        System.out.println(quote.selectFirst("small.author").text());;
                        Element tagsDiv = quote.selectFirst("div.tags");
                        if (tagsDiv != null) {
                            Elements tagLinks = tagsDiv.select("a.tag");
                            for (Element tag : tagLinks) {
                                System.out.println(tag.text());
                            }
                        }
                    }
                }
            }
            else if (child.startsWith("https://sandbox.oxylabs.io/products")) {
                String title = html.title();
                String gameName = title.contains("|") ? title.split("\\|")[0].trim() : title.trim();

                String developer = getMetaProperty(html, "og:developer");
                String platform = getMetaProperty(html, "og:platform");
                String type = getMetaProperty(html, "og:type");
                String price = getMetaProperty(html, "og:price");
                String stock = getMetaProperty(html, "og:availability");
                String genres = getMetaProperty(html, "og:genre");

                System.out.println("Name: " +  gameName);
                System.out.println("Developer: " + developer);
                System.out.println("Platform: " + platform);
                System.out.println("Type: " + type);
                System.out.println("Price: " + price);
                System.out.println("Stock: " + stock);
                System.out.println("Genres: " + genres);
            }
            else {

                Elements countries = html.select("div.country");

                for (Element country : countries) {
                    String name = country.selectFirst("h3.country-name").text().trim();
                    String capital = country.selectFirst("span.country-capital").text().trim();
                    String population = country.selectFirst("span.country-population").text().trim();
                    String area = country.selectFirst("span.country-area").text().trim();
                    System.out.println("Country: " + name);
                    System.out.println("Capital: " + capital);
                    System.out.println("Population: " + population);
                    System.out.println("Area (km²): " + area);
                    System.out.println("----------------------------------------");
                }

                Elements teams = html.select("tr.team");

                for (Element team : teams) {
                    String name = team.selectFirst("td.name").text();
                    String year = team.selectFirst("td.year").text();
                    String wins = team.selectFirst("td.wins").text();
                    String losses = team.selectFirst("td.losses").text();
                    String otLosses = team.selectFirst("td.ot-losses").text();
                    String winPct = team.selectFirst("td.pct").text();
                    String goalsFor = team.selectFirst("td.gf").text();
                    String goalsAgainst = team.selectFirst("td.ga").text();
                    String diff = team.selectFirst("td.diff").text();

                    System.out.println("Team: " + name);
                    System.out.println("Year: " + year);
                    System.out.println("Wins: " + wins);
                    System.out.println("Losses: " + losses);
                    System.out.println("OT Losses: " + (otLosses.isEmpty() ? "0" : otLosses));
                    System.out.println("Win %: " + winPct);
                    System.out.println("Goals For: " + goalsFor);
                    System.out.println("Goals Against: " + goalsAgainst);
                    System.out.println("Goal Diff: " + diff);
                    System.out.println("---------------------------------------------");
                }
            }
        }
    }
}
