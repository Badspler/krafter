package com.company;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;

public class Scraper {

    private final File driverFile;
    private final String enteredURL;

    public Scraper(File driverFile, String url) {
        this.driverFile = driverFile;
        this.enteredURL = url;
    }

    public void scrape() {
        ArrayList<AnimeSeries> series = new ArrayList<AnimeSeries>();

        System.setProperty("webdriver.chrome.driver",driverFile.getPath());
        WebDriver driver = new ChromeDriver();
        //Exclude season leftovers with: ?leftovers=false
        String livechartURL = enteredURL + "?leftovers=false";

        //Create href list of all seasons non-ongoing shows
        ArrayList<String> animeList = scrapeSeasonChart(driver,livechartURL);

        //Scrape each anime found, updates animeList as its passed by reference
        scrapeEachAnime(driver,series,animeList);

        //Write data collected to files
        String textToWrite = seriesToString(series);
        String writePath = System.getProperty("user.dir")+ "\\output\\output.yaml";
        writeStringToFile(textToWrite,writePath);

        textToWrite = streamsToString(series);
        writePath = System.getProperty("user.dir")+ "\\output\\output_all_streams.yaml";
        writeStringToFile(textToWrite,writePath);

        //Close off driver
        driver.close();
        System.out.println("Driver Closed");
    }

    private String streamsToString(ArrayList<AnimeSeries> series) {
        Collections.sort(series);
        String output = "";
        for(AnimeSeries a : series){
            output = output + a.getAnimeTitle() + ":\n";
            for(String stream : a.getStreamsList()){
                output = output + stream + "\n";
            }
            output = output + "---\n";
        }
        return output;
    }

    private String seriesToString(ArrayList<AnimeSeries> series) {
        Collections.sort(series);
        String output = "";
        for(AnimeSeries a : series){
            output = output + a.toString() + "\n";
            output = output + "---" + "\n";
        }
        //remove the very last line's "---"
        output = output.substring(0,output.length()-4);

        //Add the template to the end
        output = output + AnimeSeries.emptySeriesTemplate;

        return output;
    }

    private void writeStringToFile(String textToWrite, String writePath) {
        try {
            File f = new File(writePath);
            //Creates if does not exist
            f.createNewFile();

            //Writes over existing file - intentionally not appending
            Files.write(Paths.get(writePath), textToWrite.getBytes(), StandardOpenOption.WRITE);
            System.out.println("Output written to: " + writePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void scrapeEachAnime(WebDriver driver, ArrayList<AnimeSeries> series, ArrayList<String> animeList) {
        //For each anime found, scrape it
        for(String url : animeList){
            AnimeSeries anime = new AnimeSeries();
            anime.setUrl(url);

            driver.get(url);
            System.out.println(url);

            //This one wait ensures the whole page is loaded for all that follow
            WebDriverWait wait = new WebDriverWait(driver, 5);
            WebElement waitElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("/html/body/div[2]/div/div[2]/h4")));

            //Get title and alias (subtitle)
            String lines[] = waitElement.getText().split("\\r?\\n");
            System.out.println(lines[0]);
            anime.setAnimeTitle(lines[0]);

            if(lines.length>1) {
                //If subtitle matches title then skip
                if(!lines[0].toLowerCase().equals(lines[1].toLowerCase())) {
                    System.out.println(lines[1]);
                    anime.setAlias(lines[1]);
                }
            }

            //find offical website
            ArrayList<WebElement> elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/div/div[1]/div[8]/small/a"));
            if(elem.size() != 0)
//                anime.setOfficalSite(elem.get(0).getText()); //All are href's and don't always align
                anime.setOfficalSite(elem.get(0).getAttribute("href"));



            //find if anime is original, otherwise it has a source. Set it.
            WebElement element = null;
            try{
                element = driver.findElement(By.xpath("//div[@class='info-bar anime-meta-bar']"));
                boolean isOriginal = element.getText().contains("Original");
                anime.setHasSource(!isOriginal);
            }catch (Exception exc){/**not found will result in null set*/}


            //find external resources by class id
            WebElement externalResource = null;

            //Anilist
            try{
                externalResource = driver.findElement(By.xpath("//*[@class='button expanded anilist-button']"));
                if(externalResource != null)
                    anime.addInfo(externalResource.getAttribute("href"));
            }catch (Exception exc){/**Not found catch and move on*/}

            //Anime planet
            try{
                externalResource = driver.findElement(By.xpath("//*[@class='button expanded anime_planet-button']"));
                if(externalResource != null)
                    anime.addInfo(externalResource.getAttribute("href"));
            }catch (Exception exc){/**Not found catch and move on*/}

            //MyAnimeList
            try{
                externalResource = driver.findElement(By.xpath("//*[@class='button expanded myanimelist-button']"));
                if(externalResource != null)
                    anime.addInfo(externalResource.getAttribute("href"));
            }catch (Exception exc){/**Not found catch and move on*/}

            //Anidb
            try{
                externalResource = driver.findElement(By.xpath("//*[@class='button expanded anidb-button']"));
                if(externalResource != null)
                    anime.addInfo(externalResource.getAttribute("href"));
            }catch (Exception exc){/**Not found catch and move on*/}

            //Kitsu
            try{
                externalResource = driver.findElement(By.xpath("//*[@class='button expanded kitsu-button']"));
                if(externalResource != null)
                    anime.addInfo(externalResource.getAttribute("href"));
            }catch (Exception exc){/**Not found catch and move on*/}


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

            //TODO: Useful for short testing - Remove otherwise
//            if(count > 4)
//                break;
        }
        return animeList;
    }

    private AnimeSeries findStreams(WebDriver driver, AnimeSeries anime, String url) {
        //Get all streams eg:
        //https://www.livechart.me/anime/9711/streams?all_regions=true
        driver.get(url+"/streams?all_regions=true");

        WebDriverWait wait = new WebDriverWait(driver, 5);
        //Wait till the title is certainly loaded
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div/div[2]/h4")));

        int count=1;
        ArrayList<WebElement> elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/div/div[2]/div[3]/div/li["+count+"]/div/div[2]/a[1]"));
        while(elem.size() != 0){
//            System.out.println(elem.get(0).getText());
//            System.out.println(elem.get(0).getAttribute("href"));
            anime.addStream(elem.get(0).getAttribute("href"));

            count++;
            elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/div/div[2]/div[3]/div/li["+count+"]/div/div[2]/a[1]"));
        }

        return anime;
    }
}
