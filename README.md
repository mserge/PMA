
## This is app for Udacity AND course


Based on Master-Detail template from Android Studio 3.0

Dependencies
  * Log4j Android wrapper
  * [TheMovieDB API Java Library](https://github.com/holgerbrandl/themoviedbapi)

Get information from:
  * [ViewModel Guide](https://developer.android.com/topic/libraries/architecture/guide.html)
  * [About handling keys](https://gist.github.com/curioustechizen/9f7d745f9f5f51355bd6)
  * [Add switch to toolbar](https://stackoverflow.com/questions/44514444/radio-button-style-in-menu-with-toolbar-not-working)
  * [Switch detection](https://stackoverflow.com/questions/11278507/android-widget-switch-on-off-event-listener)
  * [Grid Auto Column](https://stackoverflow.com/questions/26666143/recyclerview-gridlayoutmanager-how-to-auto-detect-span-count/30256880#comment62202844_30256880)

To compile:
  * Add API_KEY into secrets.properties file that retrieved from [The Movie Database API](https://developers.themoviedb.org)

For stage 2 I have implemented:
  * Usage of Spinner and saving the choosen sort state
  * [RendererRecyclerViewAdapter](https://github.com/vivchar/RendererRecyclerViewAdapter)
  * Data stored in SQLlite using Content provider
  * I have stored only data for list, the data for details is alwasys loaded
 
  