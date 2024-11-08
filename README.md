# AdviseU-Libs
A collection of libraries used to prepare/model data for use in AdviseU.

## ClassScraper
A web scraper to pull every class from the OSU catalog and formulate their course codes
and co/prerequisited in a json file.

Usage: sbt run, this will generate a set of json files in the ./output folder that contain
the classes scraped from the OSU catalog site.

## TimeScraper
An API scraper to pull every section/timeslot for OSU classes. Uses the output of ClassScraper to function.

Usage: sbt run, this will generate a set of json files in the ./output folder that contain
the times scraped from the OSU classes site.
