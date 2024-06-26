package com.crawler.web.crawler;

import com.crawler.web.crawler.entity.Site;
import com.crawler.web.crawler.service.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import static com.crawler.web.crawler.Constants.*;


@Component
public class CrawlerTask {

    private Logger log = Logger.getLogger(CrawlerTask.class.getName());

    @Autowired
    CrawlerService crawlerServie;

    @Async
    public Future<String> crawl(final String url, final int d, final String id) {

        CrawlerApplication.getCrawlerMap().put(id, PROCESSING);

        // Link tracker

        final LinkedList<String> queue = new LinkedList<>();
        queue.add(url);

        int depthCount = 0;
        while(!queue.isEmpty()) {

            CrawlerApplication.getCrawlerMap().put(id, "processing");

            if(depthCount == d) {
                log.info(" user depth reached..");
                break;
            }

            String link  = queue.poll();

            WebSite webSite = new WebSite(link);

            log.info(" .. make request .. " + link);

            String response = webSite.crawl();

            log.info(" .. response .. ");

            if(response.isEmpty()) {
                depthCount++;
                continue;
            }

            HTMLDocument htmlDocument = new HTMLDocument(response);
            Set<String> links        = htmlDocument.getAllLinks();

            queue.addAll(links);

            log.info(" .. links size .. " + queue.size());
            // save this crawler information to the database
            Site site  = new Site();
            site.setToken(id);
            String title = htmlDocument.getTitle();

            log.info(" .. title.. " + title);
            // upto 100
            title = title == null ? "":title.trim();
            title = title.length() > 100 ? title.substring(0, 100) : title;
            site.setTitle(htmlDocument.getTitle());
            site.setTotalImage(htmlDocument.getImgCount());
            site.setTotalLinks(htmlDocument.getLinkCount());
            site.setLink(link);

            crawlerServie.saveSite(site);
            log.info(" .. saved.. " + title);

            depthCount++;

            log.info(" depth  " + depthCount + " for " + id);

            // wait
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                return new AsyncResult<String>(id);
            }
        }

        CrawlerApplication.getCrawlerMap().put(id, COMPLETED);
        log.info("..FINISHED CRAWLING SITES for " + url);

        return new AsyncResult<String>(id);
    }


}
