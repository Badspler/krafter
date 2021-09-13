package com.company;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Scraper {

    private final File driverFile;
    private final String enteredURL;
    private final File ignoreListFile;

    public Scraper(File driverFile, String url, File ignoreListFile) {
        this.driverFile = driverFile;
        this.enteredURL = url;
        this.ignoreListFile = ignoreListFile;
    }

    public void scrape() {
        ArrayList<AnimeSeries> series = new ArrayList<AnimeSeries>();

//        System.setProperty("webdriver.chrome.driver",driverFile.getPath());
//        WebDriver driver = new ChromeDriver();


        System.setProperty("webdriver.chrome.driver",driverFile.getPath());
        ChromeOptions options = new ChromeOptions();
        //C:\Users\Chris\AppData\Local\Google\Chrome\User Data\Default
//        options.addArguments("user-data-dir=C:/Users/Chris/AppData/Local/Google/Chrome/User Data/");
//        options.addArguments("profile-directory=Profile 8");
        options.addArguments("--start-maximized");

        //Anti cloudflare
//        options.add_experimental_option('excludeSwitches', ['load-extension', 'enable-automation'])

//        options.setExperimentalOption("excludeSwitches", Arrays.asList("disable-popup-blocking"));
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        options.addArguments("disable-infobars");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-blink-features=AutomationControlled");
//        options.addArguments("--start-maximized");
//        options.addArguments("--start-maximized");
        WebDriver driver = new ChromeDriver(options);

        //Cloudflare
//        options = webdriver.ChromeOptions()
//        options.add_experimental_option("excludeSwitches", ["enable-automation"])
//        options.add_experimental_option('useAutomationExtension', False)
//        options.add_argument("--disable-blink-features=AutomationControlled")
//        driver = webdriver.Chrome(options=options)
//        driver.get(URL)


        //Exclude season leftovers with: ?leftovers=false
        String livechartURL = enteredURL + "?leftovers=false&ongoing=none";

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

        textToWrite = seriesToMALList(series);
        writePath = System.getProperty("user.dir")+ "\\output\\output_MAL_list.yaml";
        writeStringToFile(textToWrite,writePath);

        //Close off driver
        driver.close();
        System.out.println("Driver Closed");
    }


    private String seriesToMALList(ArrayList<AnimeSeries> series) {
        Collections.sort(series);
        String output = "";
        // Find MAL entry for each show
        for(AnimeSeries a : series){
            String s = a.findInfo("myanimelist.net") + "\n";

            if(s==null){
                s = "No MAL entry found: " + a.getAnimeTitle();
            }

            output = output + s;
        }
        return output;
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

        //Hide leftovers
        driver.get(livechartURL);

        try {
            System.out.println("Waiting");
            Thread.sleep(5000);
            System.out.println("Waiting finished.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Get the ignoreList file and fill it
        List<String> blockList = new ArrayList<>();
        if(ignoreListFile!=null){
            try {
                blockList = Files.readAllLines(ignoreListFile.toPath(), Charset.defaultCharset());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //Find all the season shows
        ArrayList<String> animeList = new ArrayList<>();

        int count=1;
        ArrayList<WebElement> elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/main/article["+count+"]/div/h3/a"));
        while(elem.size() != 0){
//            System.out.println("Block name: " + elem.get(0).getText());
//            System.out.println("Block name: " + elem.get(0).getAttribute("href"));

            //Don't scan anything from the ignoreList
            if(blockList.contains(elem.get(0).getAttribute("href")) || blockList.contains(elem.get(0).getText())){
                System.out.println("Excluding: " + elem.get(0).getText());
                count++;
                elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/main/article["+count+"]/div/h3/a"));
                continue;
            }

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
            if(elem.get(0).getAttribute("href").contains("youtube.com")){
                //Now scrape and check for "subbed/dubbed (english) otherwise discard it
                ArrayList<WebElement> checkSubElement = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/div/div[2]/div[3]/div/li["+count+"]/div/div[2]/small"));
//                System.out.println(checkSubElement.get(0).getText().toLowerCase());

                try {
                    if (checkSubElement.get(0).getText().toLowerCase().contains("english")) {
                        anime.addYouTubeStream(elem.get(0).getText(), elem.get(0).getAttribute("href"));
//                    System.out.println("added youtube stream for: " + elem.get(0).getText());//Might not be an english stream though
//                    System.out.println(elem.get(0).getAttribute("href"));
                    }
                }catch (IndexOutOfBoundsException e){ //TODO fix whatever issue this actually is
                    System.out.println(e);
                    System.out.println("CONTINUING");
                }
                //Intentionally allow it to be added to all streams as well
            }

            anime.addStream(elem.get(0).getAttribute("href"));

            count++;
            elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/div/div[2]/div[3]/div/li["+count+"]/div/div[2]/a[1]"));
        }

        return anime;
    }
}
