package com.company;

import javax.swing.*;
import java.io.File;
import java.util.Scanner;

public class Main {

    public static void main(String[] args){
        //Ask user for URL

        String exampleUrl = "https://www.livechart.me/fall-2023/tv";

        Scanner reader = new Scanner(System.in);  // Reading from System.in
        System.out.println("Enter a livechart.me season URL to scan: ");
        System.out.println("Example: " + exampleUrl);
        String url = reader.nextLine(); // Scans the next token of the input as an int.

        if(url.equals(""))
            url=exampleUrl;

        reader.close();

        if(!url.contains("livechart.me")){
            System.err.println("URL entered: " + url);
            System.err.println("That is not a valid livechart.me URL.");
            System.err.println("Example: " + exampleUrl);
            return;
        }


        //Try find chrome driver, otherwise fall back on filechooser.
        String path = System.getProperty("user.dir")+"\\libs\\chromedriver-win32\\chromedriver.exe";
        File driverFile = new File(path);

        if(!driverFile.exists()) {
            //Ask user to select web driver to use because we couldn't find the expected one
            driverFile = selectFile();
            if(driverFile==null) {
                System.err.println("No driver selected. Aborted.");
                return;
            }
            System.out.println("Using driver at: " + driverFile.getPath());
        } else {
            System.out.println("Using chrome driver at: " + driverFile.getPath());
        }

        String ignoreListPath = System.getProperty("user.dir")+"\\config\\ignoreList.txt";
        File ignoreListFile = new File(ignoreListPath);

        //Start the scrape
        Scraper s = new Scraper(driverFile,url,ignoreListFile);
        s.scrape();
    }

    public static File selectFile(){
        String path = System.getProperty("user.dir")+"\\libs";

        System.out.println("Select selenium web driver.");
        JFileChooser jfc = new JFileChooser(path);
        jfc.setDialogTitle("Select Selenium Web Driver");

        int returnValue = jfc.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            System.out.println(selectedFile.getAbsolutePath());
            return selectedFile;
        }
        return null;
    }
}

