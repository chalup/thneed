Change Log
==========

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
