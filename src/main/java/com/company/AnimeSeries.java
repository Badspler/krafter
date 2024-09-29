package com.company;

import java.util.ArrayList;
import java.util.HashMap;

public class AnimeSeries implements Comparable<AnimeSeries>{

    private String title;
    private String alias;
    private Boolean hasSource;
    private String seriesFormat; //TV, OVA, ONA, Movie, etc
    private String officalSite;

    private ArrayList<String> streamsList = new ArrayList<>();
    private HashMap<String,String> youTubeStreamsList=new HashMap<String,String>();
    private ArrayList<String> infoList = new ArrayList<>();
    private String url;

    public void addInfo(String info){
        infoList.add(info);
    }

    public void printAllStreams(){
        for(String stream: streamsList){
            System.out.println(stream);
        }
    }

    public void addYouTubeStream(String youtubeName, String stream){
        youTubeStreamsList.put(youtubeName,stream);
    }
    public void addStream(String stream){
        streamsList.add(stream);
    }

    public ArrayList<String> getStreamsList(){
        return streamsList;
    }

    public void setAnimeTitle(String title) {
        title = title.replace("(TV)","");//removes (TV) from names

        //TODO auto-replacement of "2nd Season" with "Season 2"
        //holo's standard is Seriesname Season X, anime names are non-standard

        this.title = title;
    }
    public String getAnimeTitle() {return title;}

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setHasSource(Boolean hasSource) {
        this.hasSource = hasSource;
    }

    public void setSeriesFormat(String seriesFormat) {
        this.seriesFormat = seriesFormat;
    }

    public void setOfficalSite(String officalSite) {
        if(officalSite!= null)
        {
            if(!officalSite.equals("null")){
                this.officalSite = officalSite;
            }else{
                this.officalSite = "";
            }

        }
    }

    public void setUrl(String url) { this.url = url;}


    public String findInfo(String contains) {
        for (String s : infoList) {
            if (s.toLowerCase().contains(contains.toLowerCase())) {
                return s;
            }
        }
        return "";
    }

    private String findSource(String contains){
        for(String s : streamsList){
            if(s.toLowerCase().contains(contains.toLowerCase())){
                return s;
            }
        }
        return "";
    }

    private String findYouTubeSource(String contains){
        String s = youTubeStreamsList.get(contains);
        //for debugging: prints everything in map
//        youTubeStreamsList.entrySet().forEach(entry->{
//            System.out.println(entry.getKey() + " " + entry.getValue());
//        });

        if(s ==null)
            return "";//Not found
        else
            return s;
    }

    public String toString(){
        String s ="";
        if(title.contains("'")||title.contains("’"))
        {
            s = s+"title: \""+title+"\""; //escape ' and instead use quotes

        }
        else
        {
            s = s+"title: '"+title+"'";
        }

        s = s+"\ntitle_en: \"\""; //title_en

        if(alias!=null)
            if(alias.contains("'")||alias.contains("’"))
                s = s+"\nalias: [\""+alias+"\"]"; //escape ' and instead use quotes
            else
                s = s+"\nalias: ['"+alias+"']";


        // Set everything else:
        s=s +
                "\nhas_source: " + hasSource+
                "\ntype: " + seriesFormat+
                "\ninfo:" +
                "\n  mal: '"+findInfo("myanimelist.net")+"'" +
                "\n  anilist: '"+findInfo("anilist.co")+"'" +
                "\n  livechart: '"+findInfo("livechart.me")+"'" +
                "\n  anidb: '"+findInfo("anidb.net")+"'" +
                "\n  kitsu: '"+findInfo("kitsu.app")+"'"+
                "\n  animeplanet: '"+findInfo("anime-planet.com")+"'"+
                "\n  official: '"+officalSite+"'"+
                "\n  subreddit: ''";
        s=s +
                "\nstreams:" +
                "\n  crunchyroll: '"+findSource("crunchyroll.com/series")+"'" +
                "\n  museasia: '"+findYouTubeSource("Muse Asia (Playlist)")+"'" +
                "\n  anione: '"+findYouTubeSource("Ani-One Asia (Playlist)")+"'" +
                "\n  funimation|Funimation: '"+findSource("funimation.com")+"'" +
                //Only Wakanim Nordic offers English anime, that comes under '/sc' for whatever reason
                //akanim Nordic countries: Sweden, Norway, Iceland, Finland, the Netherlands and Denmark.
                "\n  wakanim|Wakanim: '"+findSource("wakanim.tv/sc")+"'" +
                "\n  hidive: '"+findSource("hidive.com")+"'" +
                "\n  animelab|AnimeLab: '"+findSource("animelab.com")+"'" +
                "\n  vrv|VRV: '"+findSource("vrv.co/series")+"'" +
                "\n  hulu|Hulu: '"+findSource("hulu.com")+"'" +
                "\n  disney|Disney: '"+findSource("disneyplus.com")+"'" +
                "\n  youtube: '"+findSource("TODO")+"'" +
                "\n  nyaa: ''"; // Title is automatically searched so this is only for alt names

        return s;
    }

    @Override
    public int compareTo(AnimeSeries o) {
        return this.title.compareTo(o.title);
    }




//TODO: EXAMPLE.

//    title: 'Higurashi no Naku Koro ni Gou [Reboot only thread]'
//    alias: ['Higurashi: When They Cry - New']
//    has_source: false
//    info:
//      mal: 'https://myanimelist.net/anime/41006/Higurashi_no_Naku_Koro_ni_2020'
//      anilist: 'https://anilist.co/anime/114446/Higurashi-no-Naku-Koro-ni-2020/'
//      anidb: 'https://anidb.net/anime/15350'
//      kitsu: 'https://kitsu.io/anime/higurashi-no-naku-koro-ni-shin-project'
//      animeplanet: 'https://www.anime-planet.com/anime/higurashi-when-they-cry-new'
//      official: 'https://higurashianime.com/'
//      subreddit: '/r/Higurashinonakakoroni'
//    streams:
//      crunchyroll: ''
//      museasia: ''
//      anione: 'https://www.youtube.com/playlist?list=PLxSscENEp7Jj1KDCcoLBqT6tpeSjzbFN9'
//      funimation|Funimation: 'https://www.funimation.com/shows/higurashi-when-they-cry-new/'
//      wakanim|Wakanim: 'https://www.wakanim.tv/sc/v2/catalogue/show/950/higurashi-when-they-cry-new'
//      hidive: ''
//      animelab|AnimeLab: 'https://www.animelab.com/shows/higurashi-when-they-cry--new'
//      crunchyroll_nsfw|Crunchyroll: ''
//      vrv|VRV: ''
//      hulu|Hulu: 'https://www.hulu.com/series/higurashi-when-they-cry-eebf8a07-1977-4045-9ee9-f9d2f0799634'
//      youtube: ''
//      nyaa: 'Higurashi no Naku Koro ni'
//    ---
}
