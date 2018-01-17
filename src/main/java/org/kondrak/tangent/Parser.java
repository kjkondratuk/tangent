package org.kondrak.tangent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class Parser {

    private static final Logger LOG = LoggerFactory.getLogger(Parser.class);

    private static final Pattern IS_URL = Pattern.compile("^(www|//|http|https).*$");
    private static final Pattern IS_URL_PREFIX = Pattern.compile("^(http://|https://).*$");
    private static final Pattern IS_FILE_URL= Pattern.compile(
            "^.*(.zip|.rtf|.gz|.bz|.bz2|.xz|.pdf|.png|.gif|.jpeg|.jpg|.mov|.wav|.mp3|.mp4|.mkv)$"
    );

    private final WebDriver driver;

    public Parser(WebDriver driver) {
        this.driver = driver;
    }

    public Optional<Document> getDocument(String url) {
        Optional<Document> doc = Optional.empty();
        if(!IS_FILE_URL.matcher(url).matches()) {
            if (!IS_URL_PREFIX.matcher(url).matches()) {
                url = (driver.getCurrentUrl().startsWith("https://") ? "https://" : "http://") + url;
            }

            LOG.debug("Getting URL: {}", url);
            driver.get(url);
            doc = Optional.of(Jsoup.parse(driver.getPageSource()));
        }

        return doc;
    }

    public List<Element> getMetaTags(Optional<Document> doc) {
        List<Element> el = new ArrayList<>();
        doc.ifPresent(x -> doc.get().head().select("meta").forEach(e -> el.add(e)));
        return el;
    }

    public List<Element> getBase(Optional<Document> doc) {
        List<Element> el = new ArrayList<>();
        doc.ifPresent(x -> doc.get().head().select("base").forEach(e -> el.add(e)));
        return el;
    }

    public Optional<String> getTitle(Optional<Document> doc) {
        if(doc.isPresent()) {
            return Optional.of(doc.get().head().select("title").text());
        }
        return Optional.empty();
    }

    public List<String> getLinks(Optional<Document> doc) {
        if(doc.isPresent()) {
            List<String> parsedLinks = new ArrayList<>();
            doc.get().select("a").forEach(e -> {
                String link = e.attr("href");
                if (link != null && !link.equals("") && !link.startsWith("#")) {
                    if (!IS_URL.matcher(link).matches() && !link.replaceFirst("/", "").startsWith("#")) {
                        if (!link.startsWith("/") && !driver.getCurrentUrl().endsWith("/")) {
                            parsedLinks.add(driver.getCurrentUrl() + "/" + link);
                        } else if (link.startsWith("/") && driver.getCurrentUrl().endsWith("/")) {
                            parsedLinks.add(driver.getCurrentUrl() + link.replaceFirst("/", ""));
                        } else {
                            parsedLinks.add(driver.getCurrentUrl() + link);
                        }
                    } else {
                        if (link.startsWith("//")) {
                            link = link.replaceFirst("//", "");
                        } else if (link.startsWith("www")) {
                            link = "http://" + link;
                        }

                        parsedLinks.add(link);
                    }
                } else {
                    LOG.debug("Not a link: {}", link);
                }
            });

            return parsedLinks;
        }
        return new ArrayList<>();
    }
}
