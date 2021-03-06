Change Log
==========

Version 0.7 *(2015-02-27)*
----------------------------

 * Migrate project to gradle
 * Add `models` package with few useful definitions

Version 0.6 *(2014-02-25)*
----------------------------

 * minSdkVersion = 10

Version 0.5 *(2013-10-29)*
----------------------------

 * Utility methods for defining special cases for your visitors.

Version 0.4 *(2013-09-04)*
----------------------------

 * Simplified polymorphic relationship API.

Version 0.3 *(2013-08-23)*
----------------------------

 * Fixed builder API for many-to-many relationships with custom ID columns.

Version 0.2 *(2013-08-22)*
----------------------------

 * When constructing ModelGraph you have to define the default id column of your models.
 * Allow to override default id column when declaring relationships.
 * Different API for defining one-to-one relationships. Instead of: 
   
   `the(PART_MODEL).isPartOf(PARENT_MODEL).identified().by(COLUMN)`
 
   You should write now:
 
   `the(PARENT_MODEL).mayHave(PART_MODEL).linked().by(COLUMN)`
   
   This change was necessary to maintain the fluent interface after adding the id columns API.


Version 0.1 *(2013-08-14)*
----------------------------

 * Initial release.
