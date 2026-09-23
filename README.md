# Krafter

Krafter is a tool to build seasonal yaml for the [holo](https://github.com/r-anime/holo) project, which is for creating Anime episode discussion threads on Reddit for seasonal airing shows.

Krafter works by scraping all necessary information from [livechart.me](https://www.livechart.me/).

This tool aims to complete the bulk of the work for a seasonal yaml and should be manually reviewed. 
Especially for episode names. 

---

Setup to use Chrome, no testing has been done on other web browsers.

Uses the selenium chromedriver_win32 driver in /libs.

---

### Example output entry

```yaml
title: 'Higurashi no Naku Koro ni Gou [Reboot only thread]'
alias: ['Higurashi: When They Cry - New']
has_source: false
info:
  mal: 'https://myanimelist.net/anime/41006/Higurashi_no_Naku_Koro_ni_2020'
  anilist: 'https://anilist.co/anime/114446/Higurashi-no-Naku-Koro-ni-2020/'
  anidb: 'https://anidb.net/anime/15350'
  kitsu: 'https://kitsu.io/anime/higurashi-no-naku-koro-ni-shin-project'
  animeplanet: 'https://www.anime-planet.com/anime/higurashi-when-they-cry-new'
  official: 'https://higurashianime.com/'
  subreddit: '/r/Higurashinonakakoroni'
streams:
  crunchyroll: ''
  museasia: ''
  anione: 'https://www.youtube.com/playlist?list=PLxSscENEp7Jj1KDCcoLBqT6tpeSjzbFN9'
  funimation|Funimation: 'https://www.funimation.com/shows/higurashi-when-they-cry-new/'
  wakanim|Wakanim: 'https://www.wakanim.tv/sc/v2/catalogue/show/950/higurashi-when-they-cry-new'
  hidive: ''
  animelab|AnimeLab: 'https://www.animelab.com/shows/higurashi-when-they-cry--new'
  crunchyroll_nsfw|Crunchyroll: ''
  vrv|VRV: ''
  hulu|Hulu: 'https://www.hulu.com/series/higurashi-when-they-cry-eebf8a07-1977-4045-9ee9-f9d2f0799634'
  youtube: ''
  nyaa: 'Higurashi no Naku Koro ni'
---
```
