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
import java.time.Duration;
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

        System.setProperty("webdriver.chrome.driver", driverFile.getPath());
        ChromeOptions options = new ChromeOptions();
//        options.addArguments("profile-directory=Profile 8");
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");

//        System.out.println("Headless Mode is on");
//        options.addArguments("--headless");//TODO headless?

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
//        WebDriverManager.chromedriver().setup();
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
        ArrayList<String> animeList = scrapeSeasonChart(driver, livechartURL);

        //Scrape each anime found, updates animeList as its passed by reference
        scrapeEachAnime(driver, series, animeList);

        //Write data collected to files
        String textToWrite = seriesToString(series);
        String writePath = System.getProperty("user.dir") + "\\output\\output.yaml";
        writeStringToFile(textToWrite, writePath);

        //TODO: is this needed still
//        textToWrite = streamsToString(series);
//        writePath = System.getProperty("user.dir")+ "\\output\\output_all_streams.yaml";
//        writeStringToFile(textToWrite,writePath);
//
//        textToWrite = seriesToMALList(series);
//        writePath = System.getProperty("user.dir")+ "\\output\\output_MAL_list.yaml";
//        writeStringToFile(textToWrite,writePath);

        //Close off driver
        driver.close();
        System.out.println("Driver Closed");
    }


    private String seriesToMALList(ArrayList<AnimeSeries> series) {
        Collections.sort(series);
        String output = "";
        // Find MAL entry for each show
        for (AnimeSeries a : series) {
            String s = a.findInfo("myanimelist.net") + "\n";

            if (s == null) {
                s = "No MAL entry found: " + a.getAnimeTitle();
            }

            output = output + s;
        }
        return output;
    }

    private String streamsToString(ArrayList<AnimeSeries> series) {
        Collections.sort(series);
        String output = "";
        for (AnimeSeries a : series) {
            output = output + a.getAnimeTitle() + ":\n";
            for (String stream : a.getStreamsList()) {
                output = output + stream + "\n";
            }
            output = output + "---\n";
        }
        return output;
    }

    private String seriesToString(ArrayList<AnimeSeries> series) {
        Collections.sort(series);
        String output = "---" + "\n"; // Initial line must start with '---'
        for (AnimeSeries a : series) {
            output = output + a.toString() + "\n";
            output = output + "---" + "\n";
        }
        //remove the very last line's "---"
        output = output.substring(0, output.length() - 4);

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

    private static final String TITLE = "//*[@class='text-xl font-medium line-clamp-1']";
    private static final String ALIAS_TITLE = "//*[@class='text-lg line-clamp-1']";
    private static final String TITLE_BLOCK = "//*[@class='flex gap-4 items-center md:mb-4']";


    private static final String FORMAT_BLOCK = "//*[@class='text-xs text-base-content/75 whitespace-nowrap']"; //TV, OVA, ONA, etc
    private static final String SOURCE_OR_ORIGINAL_BY_TEXT = "//*[@class='whitespace-nowrap text-ellipsis overflow-hidden']";
    private static final String OFFICIAL_WEBSITE = "//*[@class='lc-btn lc-btn-sm lc-btn-outline']";

    private static final String MAL_BUTTON = "//*[@class='btn btn-sm btn-block lc-btn-myanimelist']";
    private static final String ANILIST_BUTTON = "//*[@class='btn btn-sm btn-block lc-btn-anilist']";
    private static final String KITSU_BUTTON = "//*[@class='btn btn-sm btn-block lc-btn-kitsu']";
    private static final String ANIDB_BUTTON = "//*[@class='btn btn-sm btn-block lc-btn-anidb']";
    private static final String ANIME_PLANET_BUTTON = "//*[@class='btn btn-sm btn-block lc-btn-animeplanet']";


    private void scrapeEachAnime(WebDriver driver, ArrayList<AnimeSeries> series, ArrayList<String> animeList) {
        //For each anime found, scrape it
        for (String url : animeList) {

            AnimeSeries anime = new AnimeSeries();
            anime.setUrl(url);

            try { // Bonus little wait for clourlairing
                Thread.sleep(2000);//TODO: Starting wait time if cloudflair is on
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            driver.get(url);
            System.out.println(url);

            //This one wait ensures the whole page is loaded for all that follow
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement titleContainerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(TITLE_BLOCK)));

            //find show title
            WebElement title = findElementByXpath(driver, TITLE, "TITLE");
            if (title != null) {
                anime.setAnimeTitle(title.getText());
                System.out.println(title.getText());
            }

            //find show alias
            WebElement alias = findElementByXpath(driver, ALIAS_TITLE, "ALIAS_TITLE");
            if (alias != null) {
                anime.setAlias(alias.getText());
                System.out.println(alias.getText());
            }

            //find offical website
            WebElement sourceOrOriginal = findElementByXpath(driver, OFFICIAL_WEBSITE, "OFFICIAL_WEBSITE");
            if (sourceOrOriginal != null)
                anime.setOfficalSite(sourceOrOriginal.getAttribute("href"));

            //find if anime is original, otherwise it has a source. Set it.
            try {
                WebElement element = findElementByXpath(driver, SOURCE_OR_ORIGINAL_BY_TEXT, "SOURCE_OR_ORIGINAL_BY_TEXT");
                boolean isOriginal = element.getText().contains("Original");
                anime.setHasSource(!isOriginal);
            } catch (Exception exc) {/**not found will result in null set*/}

            //find if anime is original, otherwise it has a source. Set it.
            try {
                WebElement element = findElementByXpath(driver, FORMAT_BLOCK, "FORMAT_BLOCK");
                // first element is the "Format" text, the following is actually what we want. But the div doesn't have a class to xpath to directly.
                WebElement parentDiv = element.findElement(By.xpath("./.."));
                String textValue = parentDiv.getText().replace("Format", "").trim();
                textValue = textValue.replace("\n", "").trim();

                anime.setSeriesFormat(textValue);
            } catch (Exception exc) {/**not found will result in null set*/}


            //find external resources by class id
            WebElement externalResource = null;

            //livechart.me
            try {
                anime.addInfo(url);
            } catch (Exception exc) {/**Not found catch and move on*/}

            //Anilist
            try {
                externalResource = findElementByXpath(driver, ANILIST_BUTTON, "ANILIST_BUTTON");
                if (externalResource != null)
                    anime.addInfo(externalResource.getAttribute("href"));
            } catch (Exception exc) {/**Not found catch and move on*/}

            //Anime planet
            try {
                externalResource = findElementByXpath(driver, ANIME_PLANET_BUTTON, "ANIME_PLANET_BUTTON");
                if (externalResource != null)
                    anime.addInfo(externalResource.getAttribute("href"));
            } catch (Exception exc) {/**Not found catch and move on*/}

            //MyAnimeList
            try {
                externalResource = findElementByXpath(driver, MAL_BUTTON, "MAL_BUTTON");
                if (externalResource != null)
                    anime.addInfo(externalResource.getAttribute("href"));
            } catch (Exception exc) {/**Not found catch and move on*/}

            //Anidb
            try {
                externalResource = findElementByXpath(driver, ANIDB_BUTTON, "ANIDB_BUTTON");
                if (externalResource != null)
                    anime.addInfo(externalResource.getAttribute("href"));
            } catch (Exception exc) {/**Not found catch and move on*/}

            //Kitsu
            try {
                externalResource = findElementByXpath(driver, KITSU_BUTTON, "KITSU_BUTTON");
                if (externalResource != null)
                    anime.addInfo(externalResource.getAttribute("href"));
            } catch (Exception exc) {/**Not found catch and move on*/}


            //Get all streams:
            //https://www.livechart.me/anime/9711/streams?all_regions=true
//            System.out.println("Getting streams for: " + anime.getAnimeTitle());

            //TODO Maybe add a sleep/stall here. Find streams performs better with a small wait. Unsure what is causing that need.
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            anime = findStreams(driver, anime, url);
            anime.printAllStreams();

            System.out.println("---------------------");
            series.add(anime);
        }
    }

    private WebElement findElementByXpath(WebDriver driver, String xpathToFind, String elementFinding) {
        WebElement element = null;
        try {
            element = driver.findElement(By.xpath(xpathToFind));
        } catch (Exception e) {
            System.out.println("Unable to locate " + elementFinding);
        }
        return element;
    }


    private ArrayList<String> scrapeSeasonChart(WebDriver driver, String livechartURL) {

        //Hide leftovers
        driver.get(livechartURL);

        try {
            System.out.println("Waiting");
            Thread.sleep(7000);//TODO: Starting wait time if cloudflair is on
            System.out.println("Waiting finished.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Get the ignoreList file and fill it
        List<String> blockList = new ArrayList<>();
        if (ignoreListFile != null) {
            try {
                blockList = Files.readAllLines(ignoreListFile.toPath(), Charset.defaultCharset());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //Find all the season shows
        ArrayList<String> animeList = new ArrayList<>();

        int count = 1;
        ArrayList<WebElement> elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/main/article[" + count + "]/div/h3/a"));
        while (elem.size() != 0) {
//            System.out.println("Block name: " + elem.get(0).getText());
//            System.out.println("Block name: " + elem.get(0).getAttribute("href"));

            //Don't scan anything from the ignoreList
            if (blockList.contains(elem.get(0).getAttribute("href")) || blockList.contains(elem.get(0).getText())) {
                System.out.println("Excluding: " + elem.get(0).getText());
                count++;
                elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/main/article[" + count + "]/div/h3/a"));
                continue;
            }

            animeList.add(elem.get(0).getAttribute("href"));

            count++;
            elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/main/article[" + count + "]/div/h3/a"));

            //TODO: Useful for short testing - Remove otherwise
//            if (count > 10)
//                break;
        }
        return animeList;
    }

    private static final String STREAMS_CONTAINER = "//*[@class='card bg-base-300 shadow-md mb-4 divide-y divide-solid divide-base-200']";

    private AnimeSeries findStreams(WebDriver driver, AnimeSeries anime, String url) {
        System.out.println("Stream URL: " + url);

        //Get all streams eg:
        //https://www.livechart.me/anime/9711/streams?all_regions=true
        driver.get(url + "/streams?hide_unavailable=false");
        System.out.println("---");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        //Wait till the title is certainly loaded
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(TITLE_BLOCK)));

        int count = 1;
//        WebElement elem = driver.findElement(By.xpath(STREAMS_CONTAINER));
        WebElement elem = findElementByXpath(driver, STREAMS_CONTAINER, "STREAMS_CONTAINER");

        while (elem != null) {
            WebElement innerElement = findElementByXpath(driver, "/html/body/div[1]/div[2]/div[4]/div[" + count + "]/div/a", "Stream: " + count);
            // loop terminates when there is no more
            if (innerElement == null)
                break;

            System.out.println("Added stream: " + innerElement.getText() + " | href: " + innerElement.getAttribute("href"));
            anime.addStream(innerElement.getAttribute("href"));

            count++;

//            System.out.println(elem.get(0).getText());
//            System.out.println(elem.get(0).getAttribute("href"));
            //TODO: Test youtube stuff
//            if (elem.get(0).getAttribute("href").contains("youtube.com")) {
//                //Now scrape and check for "subbed/dubbed (english) otherwise discard it
//                ArrayList<WebElement> checkSubElement = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/div/div[2]/div[3]/div/li[" + count + "]/div/div[2]/small"));
////                System.out.println(checkSubElement.get(0).getText().toLowerCase());
//
//                try {
//                    if (checkSubElement.get(0).getText().toLowerCase().contains("english")) {
//                        anime.addYouTubeStream(elem.get(0).getText(), elem.get(0).getAttribute("href"));
////                    System.out.println("added youtube stream for: " + elem.get(0).getText());//Might not be an english stream though
////                    System.out.println(elem.get(0).getAttribute("href"));
//                    }
//                } catch (IndexOutOfBoundsException e) { //TODO fix whatever issue this actually is
//                    System.out.println(e);
//                    System.out.println("CONTINUING");
//                }
//                //Intentionally allow it to be added to all streams as well
//            }
//
//            anime.addStream(elem.get(0).getAttribute("href"));
//            System.out.println("Added stream: " + elem.get(0).getAttribute("href"));
//            count++;
//            elem = (ArrayList<WebElement>) driver.findElements(By.xpath("/html/body/div[2]/div/div[2]/div[3]/div/li[" + count + "]/div/div[2]/a[1]"));
        }

        return anime;
    }
}
