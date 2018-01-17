package org.kondrak.tangent;

import org.jsoup.nodes.Document;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class CrawlerContext {

    private static final Logger LOG = LoggerFactory.getLogger(CrawlerContext.class);

    private List<String> startPoints = new ArrayList<>();

    public CrawlerContext(String... urls) {
        startPoints.addAll(Arrays.asList(urls));
    }

    public void run() {
        LocalDateTime startTime = LocalDateTime.now();
        System.setProperty("webdriver.gecko.driver", "/home/nosferatu/workspace/tangent/geckodriver");
        FirefoxOptions opts = new FirefoxOptions();
        opts.addArguments("-headless");
        opts.setLogLevel(FirefoxDriverLogLevel.ERROR);
        WebDriver driver = new FirefoxDriver(opts);
        Parser parser = new Parser(driver);
        List<String> links = new ArrayList<>();

        startPoints.forEach(init -> {
            Optional<Document> origDoc = parser.getDocument(init);
            parser.getBase(origDoc).forEach(e -> LOG.info("{}", e));
            parser.getMetaTags(origDoc).forEach(e -> LOG.info("{}", e));
            LOG.info("{}", parser.getTitle(origDoc));
            List<String> orig = parser.getLinks(origDoc);
            links.addAll(orig);
            orig.forEach(o -> {
                Optional<Document> secondDoc = parser.getDocument(o);
                parser.getBase(secondDoc).forEach(e -> LOG.info("{}", e));
                parser.getMetaTags(secondDoc).forEach(e -> LOG.info("{}", e));
                LOG.info("{}", parser.getTitle(secondDoc));
                List<String> secondLinks = parser.getLinks(secondDoc);
                secondLinks.forEach(s -> {
                    if(!ListUtil.contains(links, s)) {
                        links.add(s);
                        Optional<Document> thirdDoc = parser.getDocument(s);
                        parser.getBase(thirdDoc).forEach(e -> LOG.info("{}", e));
                        parser.getMetaTags(thirdDoc).forEach(e -> LOG.info("{}", e));
                        LOG.info("{}", parser.getTitle(thirdDoc));
                        List<String> thirdLinks = parser.getLinks(thirdDoc);
                        thirdLinks.forEach(t -> {
                            if(!ListUtil.contains(links, t)) {
                                links.add(t);
                            }
                        });
                    }
                });
            });
        });

        LOG.info("Run from {} to {}", startTime, LocalDateTime.now());
        LOG.info("Links seen: {}", links.size());
    }
}
