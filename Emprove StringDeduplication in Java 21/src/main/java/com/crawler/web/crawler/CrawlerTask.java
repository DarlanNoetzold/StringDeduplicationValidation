package com.crawler.web.crawler;

import com.crawler.web.crawler.entity.Site;
import com.crawler.web.crawler.service.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Logger;

@Component
public class CrawlerTask {

    private static final Logger log = Logger.getLogger(CrawlerTask.class.getName());

    static {
        try {
            System.loadLibrary("deduplication"); // Load the native library
            log.info("Native library loaded successfully.");
        } catch (UnsatisfiedLinkError e) {
            log.severe("Error loading native library: " + e.getMessage());
        }
    }

    // Assinatura correta do método nativo
    public native static void deduplicateStrings(String[] strings);

    @Autowired
    CrawlerService crawlerService;

    @Async
    public Future<String> crawl(final String url, final int d, final String id) {
        try {
            CrawlerApplication.getCrawlerMap().put(id, "PROCESSING");

            final LinkedList<String> queue = new LinkedList<>();
            queue.add(url);

            int depthCount = 0;
            while (!queue.isEmpty()) {

                CrawlerApplication.getCrawlerMap().put(id, "processing");

                if (depthCount == d) {
                    log.info("User depth reached.");
                    break;
                }

                String link = queue.poll();

                WebSite webSite = new WebSite(link);

                log.info("Making request: " + link);

                String response = webSite.crawl();

                log.info("Received response.");

                if (response.isEmpty()) {
                    depthCount++;
                    continue;
                }

                HTMLDocument htmlDocument = new HTMLDocument(response);
                Set<String> links = htmlDocument.getAllLinks();

                // Deduplicar links
                String[] linksArray = links.toArray(new String[0]);

                // Redirecionar a saída padrão e a saída de erro para capturar os logs do código nativo
                PrintStream originalOut = System.out;
                PrintStream originalErr = System.err;
                ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
                ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
                PrintStream newOut = new PrintStream(baosOut);
                PrintStream newErr = new PrintStream(baosErr);
                System.setOut(newOut);
                System.setErr(newErr);

                try {
                    long startTime = System.nanoTime();
                    log.info("Calling deduplicateStrings method...");
                    deduplicateStrings(linksArray);
                    long endTime = System.nanoTime();

                    newOut.flush();
                    newErr.flush();
                    System.setOut(originalOut);
                    System.setErr(originalErr);
                    log.info("Deduplication time: " + (endTime - startTime) + " ns");
                    log.info("Native stdout logs:\n" + baosOut.toString());
                    log.severe("Native stderr logs:\n" + baosErr.toString());
                } catch (Exception e) {
                    System.setOut(originalOut);
                    System.setErr(originalErr);
                    log.severe("Error during native call: " + e.getMessage());
                    e.printStackTrace();
                }

                // Adicionar links deduplicados de volta à fila
                for (String deduplicatedLink : linksArray) {
                    if (deduplicatedLink != null) {
                        queue.add(deduplicatedLink);
                    }
                }

                log.info("Queue size: " + queue.size());

                // Save this crawler information to the database
                Site site = new Site();
                site.setToken(id);
                String title = htmlDocument.getTitle();

                log.info("Title: " + title);
                title = title == null ? "" : title.trim();
                title = title.length() > 100 ? title.substring(0, 100) : title;
                site.setTitle(title);
                site.setTotalImage(htmlDocument.getImgCount());
                site.setTotalLinks(htmlDocument.getLinkCount());
                site.setLink(link);

                crawlerService.saveSite(site);
                log.info("Saved: " + title);

                depthCount++;
                log.info("Depth count " + depthCount + " for " + id);

                // Wait
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    return new AsyncResult<>(id);
                }
            }

            CrawlerApplication.getCrawlerMap().put(id, "COMPLETED");
            log.info("Finished crawling sites for " + url);

            return new AsyncResult<>(id);
        } catch (Exception e) {
            log.severe("Erro durante o processo de crawling: " + e.getMessage());
            e.printStackTrace();
            return new AsyncResult<>(id);
        }
    }
}

