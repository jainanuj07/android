# SFMovies_android

SF Movies
Create an app that shows on a map where movies have been filmed in San Francisco. The user should be able to filter the view using autocompletion search.
The data is available on DataSF: Film Locations.



Front-end

    The autocompletion search is only applied on the titles of the movies.
    Used the Google Maps API 2 to show the map and add the markers for every movie  
    location.
    Tapping on marker Will info Location name.
    Used Google's Geocoding API to add latitude and longitude values for all movie
    locations.



Google API  used:

Google Map API V2 
Google Geocoding API


Android API Level :

    compileSdkVersion 21
    minSdkVersion 15
    targetSdkVersion 21



Backend  :

Backend is developed using java Jersy REST technlogy.


REST API :

1.

 GET
http://52.25.133.178:8080/HyperTrack/movies/Title

Return List of Movies object  with given movie name. 

Eample Resuest:

http://52.25.133.178:8080/HyperTrack/movies/180

request query parameters :
 
parameter 	value 	      description
Title                String         Movie Title name 


available response representations:

 200 - application/json (content)


2.
 
GET

http://52.25.133.178:8080/HyperTrack/recommend/TypeStr

Return List of Titles  that start with given String.

Eample Resuest:

http://52.25.133.178:8080/HyperTrack/recommend/ab

request query parameters :
 
parameter 	     value 	      description
TypeStr                String                substring of a Title name.

available response representations:

 200 - application/json (content)



Databases:

Databse :  Mysql

Currently data is stoed in mysql with only one table.
