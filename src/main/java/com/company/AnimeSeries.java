package com.company;

import java.util.ArrayList;

public class AnimeSeries implements Comparable<AnimeSeries>{

    private String title,alias;
    private Boolean hasSource;
    private String officalSite;

    private ArrayList<String> streamsList = new ArrayList<>();
    private ArrayList<String> infoList = new ArrayList<>();
    private String url;

    public void addInfo(String info){
        infoList.add(info);
    }
    public void addStream(String stream){
        streamsList.add(stream);
    }

    public ArrayList<String> getStreamsList(){
        return streamsList;
    }

    public void setAnimeTitle(String title) {
        this.title = title;
    }
    public String getAnimeTitle() {return title;}

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setHasSource(Boolean hasSource) {
        this.hasSource = hasSource;
    }

    public void setOfficalSite(String officalSite) {
        this.officalSite = officalSite;
    }

    public void setUrl(String url) { this.url = url;}


    private String findSource(ArrayList<String> list, String contains){
        for(String s : list){
            if(s.toLowerCase().contains(contains.toLowerCase())){
                return s;
            }
        }
        return "";
    }

    public String toString(){
        String s ="title: '"+title+"'";
        if(alias!=null)
            s = s+"\nalias: ['"+alias+"']";

        s=s +
                "\nhas_source: " + hasSource+
                "\ninfo:" +
                "\n  mal: '"+findSource(infoList,"myanimelist.net")+"'" +
                "\n  anilist: '"+findSource(infoList,"anilist.co")+"'" +
                "\n  anidb: '"+findSource(infoList,"anidb.net")+"'" +
                "\n  kitsu: '"+findSource(infoList,"kitsu.io")+"'"+
                "\n  animeplanet: '"+findSource(infoList,"anime-planet.com")+"'"+
                "\n  official: '"+officalSite+"'"+
                "\n  subreddit: ''";
        s=s +
                "\nstreams: " +
                "\n  crunchyroll: '"+findSource(streamsList,"crunchyroll.com")+"'" +
                "\n  museasia: '"+findSource(streamsList,"TODO")+"'" +
                "\n  anione: '"+findSource(streamsList,"TODO")+"'" +
                "\n  funimation|Funimation: '"+findSource(streamsList,"funimation.com")+"'" +
                //Only Wakanim Nordic offers English anime, that comes under '/sc' for whatever reason
                //akanim Nordic countries: Sweden, Norway, Iceland, Finland, the Netherlands and Denmark.
                "\n  wakanim|Wakanim: '"+findSource(streamsList,"wakanim.tv/sc")+"'" +
                "\n  hidive: '"+findSource(streamsList,"hidive.com")+"'" +
                "\n  animelab|AnimeLab: '"+findSource(streamsList,"animelab.com")+"'" +
                "\n  vrv|VRV: '"+findSource(streamsList,"vrv.co")+"'" +
                "\n  hulu|Hulu: '"+findSource(streamsList,"hulu.com")+"'" +
                "\n  youtube: '"+findSource(streamsList,"TODO")+"'" +
                "\n  nyaa: '"+title+"'"; //+title+

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

    public static final String emptySeriesTemplate = "#---\n" +
            "#title: ''\n" +
            "#alias: ['']\n" +
            "#has_source: true\n" +
            "#info:\n" +
            "#  mal: ''\n" +
            "#  anilist: ''\n" +
            "#  anidb: ''\n" +
            "#  kitsu: ''\n" +
            "#  animeplanet: ''\n" +
            "#  official: ''\n" +
            "#  subreddit: ''\n" +
            "#streams:\n" +
            "#  crunchyroll: ''\n" +
            "#  museasia: ''\n" +
            "#  funimation|Funimation: ''\n" +
            "#  wakanim|Wakanim: ''\n" +
            "#  hidive: ''\n" +
            "#  animelab|AnimeLab: ''\n" +
            "#  crunchyroll_nsfw|Crunchyroll: ''\n" +
            "#  vrv|VRV: ''\n" +
            "#  hulu|Hulu: ''\n" +
            "#  youtube: ''\n" +
            "#  nyaa: ''\n";
}
