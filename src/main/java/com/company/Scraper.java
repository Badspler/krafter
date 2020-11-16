package com.company;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Collections;

public class Scraper {

//    private static String fileOutputLocation = "C:\\DEV\\git\\TopScraperMAL\\outputImages\\";
//    private Logger logger;

    public void scrape() {

        ArrayList<AnimeSeries> series = new ArrayList<AnimeSeries>();

//        logger.info("Logger created");
        System.setProperty("webdriver.chrome.driver","C:\\DEV\\git\\Krafter\\libs\\chromedriver_win32\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();

        //TODO ask on cmdline. Move into scrapeSeasonChart: ?leftovers=false
        String livechartURL = "https://www.livechart.me/winter-2021/tv?leftovers=false";

        //Create href list of all seasons non-ongoing shows
        ArrayList<String> animeList = scrapeSeasonChart(driver,livechartURL);

        //Scrape each anime found, updates animeList as its passed by reference
        scrapeEachAnime(driver,series,animeList);

        //Order alphabetically all series by title and print them
        printSeries(series);

        //Close off driver
        driver.close();
        System.out.println("Driver Closed");
    }

    private void printSeries(ArrayList<AnimeSeries> series) {
        Collections.sort(series);
        for(AnimeSeries a : series){
            System.out.println(a.toString());
            System.out.println("---");
        }
    }


    private void scrapeEachAnime(WebDriver driver, ArrayList<AnimeSeries> series, ArrayList<String> animeList) {
        //For each anime found, scrape it
        for(String url : animeList){
            AnimeSeries anime = new AnimeSeries();
            anime.setUrl(url);

            driver.get(url);
            System.out.println(url);
            //find
            ///html/body/div[2]/div/div[2]/h4
            ///html/body/div[2]/div/div[2]/h4/text()
            WebDriverWait wait = new WebDriverWait(driver, 5);
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("/html/body/div[2]/div/div[2]/h4")));

            //Get title and alias (subtitle)
            String lines[] = element.getText().split("\\r?\\n");
            System.out.println(lines[0]);
            anime.setAnimeTitle(lines[0]);

            System.out.println(lines[0]);
            if(lines.length>1) {
                //If subtitle matches title then skip
                if(!lines[0].toLowerCase().equals(lines[1].toLowerCase())) {
                    System.out.println(lines[1]);
                    anime.setAlias(lines[1]);
                }
            }

            //find alias
            ///html/body/div[2]/div/div[2]/h4/div/small/i
//            elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/div/div[2]/h4/div/small/i"));
//            if(elem.size() != 0)
//                anime.setAlias(elem.get(0).getText());
//                System.out.println(elem.get(0).getText());


            //TODO: this is kinda crap...
            //Check if countdown bar exists
            WebElement e = null;
            Boolean countdownBar = false;
            try{
                e = driver.findElement(By.xpath("//div[@class='callout info-bar countdown-bar active']"));
                if(e!=null){
                    countdownBar = true;
                }
            }catch (Exception e1){
                //Empty catch, countdownBar still false;
            }


            //find offical website
            ///html/body/div[2]/div/div[1]/div[8]/small/a
            ArrayList<WebElement> elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/div/div[1]/div[8]/small/a"));
            if(elem.size() != 0)
                anime.setOfficalSite(elem.get(0).getText());
//              System.out.println(elem.get(0).getText());

            //if below is "Original" - has_source
            ///html/body/div[2]/div/div[2]/div[3]/div[1]/div[2]/div[2]
            if(countdownBar)
                elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/div/div[2]/div["+"3"+"]/div[1]/div[2]/div[2]"));
            else
                elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/div/div[2]/div["+"2"+"]/div[1]/div[2]/div[2]"));

            if(elem.size() != 0){
//                System.out.println(elem.get(0).getText());
                if (elem.get(0).getText().equals("Original")) {
                    anime.setHasSource(false);
                }
                else {
                    anime.setHasSource(true);
                }
            }


//            elem = (ArrayList<WebElement>) driver.findElements(By.className("callout info-bar countdown-bar active"));
//            elem = (ArrayList<WebElement>)



            //Get the external resources, or 'info':
            int i = 1;
            int bar = 5;
            if(countdownBar)
                bar = 6;

//            elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/div/div[2]/div["+bar+"]/div["+i+"]/a"));
//
//            while(elem.size() != 0){
////            System.out.println(elem.get(0).getText());
////                System.out.println(elem.get(0).getAttribute("href"));
//                anime.addInfo(elem.get(0).getAttribute("href"));
//
////                animeList.add(elem.get(0).getAttribute("href"));
//                i++;
//                elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/div/div[2]/div["+bar+"]/div["+i+"]/a"));
//            }




            //find external resources by class id
            WebElement externalResource = null;

            //Anilist
            try{
                externalResource = driver.findElement(By.xpath("//*[@class='button expanded anilist-button']"));
                if(externalResource != null)
                    anime.addInfo(externalResource.getAttribute("href"));
            }catch (Exception exp){/**Not found catch and move on*/}

            //Anime planet
            try{
                externalResource = driver.findElement(By.xpath("//*[@class='button expanded anime_planet-button']"));
                if(externalResource != null)
                    anime.addInfo(externalResource.getAttribute("href"));
            }catch (Exception exp){/**Not found catch and move on*/}

            //MyAnimeList
            try{
                externalResource = driver.findElement(By.xpath("//*[@class='button expanded myanimelist-button']"));
                if(externalResource != null)
                    anime.addInfo(externalResource.getAttribute("href"));
            }catch (Exception exp){/**Not found catch and move on*/}

            //Anidb
            try{
                externalResource = driver.findElement(By.xpath("//*[@class='button expanded anidb-button']"));
                if(externalResource != null)
                    anime.addInfo(externalResource.getAttribute("href"));
            }catch (Exception exp){/**Not found catch and move on*/}

            //Kitsu
            try{
                externalResource = driver.findElement(By.xpath("//*[@class='button expanded kitsu-button']"));
                if(externalResource != null)
                    anime.addInfo(externalResource.getAttribute("href"));
            }catch (Exception exp){/**Not found catch and move on*/}



            //Get all streams:
            //https://www.livechart.me/anime/9711/streams?all_regions=true
            anime = findStreams(driver,anime,url);

            System.out.println("---------------------");
            series.add(anime);
        }
    }

    private ArrayList<String> scrapeSeasonChart(WebDriver driver, String livechartURL) {

        //TODO: require season to be passed by user; https://www.livechart.me/winter-2021/tv
        //Hide leftovers
        driver.get(livechartURL);

        //Find all the season shows
        ArrayList<String> animeList = new ArrayList<>();

        int count=1;
        ArrayList<WebElement> elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/main/article["+count+"]/div/h3/a"));
        while(elem.size() != 0){
//            System.out.println(elem.get(0).getText());
//            System.out.println(elem.get(0).getAttribute("href"));

            animeList.add(elem.get(0).getAttribute("href"));

            count++;
            elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/main/article["+count+"]/div/h3/a"));

//            if(count > 4)
//                break;//TODO REMOVE
        }
        return animeList;
    }

    private AnimeSeries findStreams(WebDriver driver, AnimeSeries anime, String url) {
        //Get all streams:
        //https://www.livechart.me/anime/9711/streams?all_regions=true
        System.out.println(url);
        driver.get(url+"/streams?all_regions=true");

        WebDriverWait wait = new WebDriverWait(driver, 5);
        //Wait till the title is certainly loaded
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div/div[2]/h4")));

        int count=1;
        ArrayList<WebElement> elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/div/div[2]/div[3]/div/li["+count+"]/div/div[2]/a[1]"));
        while(elem.size() != 0){
            System.out.println(elem.get(0).getText());
            System.out.println(elem.get(0).getAttribute("href"));
            anime.addStream(elem.get(0).getAttribute("href"));

            count++;
            elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/div/div[2]/div[3]/div/li["+count+"]/div/div[2]/a[1]"));
        }

        return anime;
    }
}
