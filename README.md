Thneed
========

> *This thing is a Thneed.*  
> *A Thneed's a Fine-Something-That-All-People-Need!*  

A library for defining the traversable hierarchy of data models.

```java
ModelGraph<DataModel> graph = ModelGraph.of(DataModel.class)
  .with(ANNOUNCEMENT)
  .where()
  .the(VENDOR).references(TRACK).by(Vendors.TRACK_ID)
  .the(SESSION).references(ROOM).by(Sessions.ROOM_ID)
  .the(SESSION).references(BLOCK).by(Sessions.BLOCK_ID)
  .the(SESSIONS_SPEAKERS)
  .links(SESSION).by(SessionsSpeakers.SESSION_ID)
  .with(SPEAKERS).by(SessionsSpeakers.SPEAKER_ID)
  .the(SESSIONS_TRACKS)
  .links(SESSION).by(SessionsTracks.SESSION_ID)
  .with(TRACK).by(SessionsTracks.TRACK_ID)
  .build();
```

> *It's a shirt.*  
> *It's a sock.*  
> *It's a glove.*  
> *It's a hat.*  

The end goal of defining your data model structure is reduction of boilerplate code in classes handling the data model. See [this package of Google I/O 2012 Android app](https://code.google.com/p/iosched/source/browse/#git%2Fandroid%2Fsrc%2Fcom%2Fgoogle%2Fandroid%2Fapps%2Fiosched%2Fprovider) for an examples of the code that I'd like to get rid of. For every model and relationship between models programmer has to define a ContentProvider endpoint with Uri builder, Uri matcher, database query, ContentResolver notifications, and so on. This amounts to a lot of boilerplate code, especially when the number of different entities in the system grows.

Thneed's approach to this problem is to define all the relationships and models using fluent builder API as shown above. The DataModel class supplied to the ModelGraph builder should implement some meaningful interfaces:

```java
interface DatabaseModel {
  String getTableName();
  // …
}

interface ContentProviderModel {
  boolean createTopLevelEndpoint();
  // ...
}

abstract static class DataModel implements DatabaseModel, ContentProviderModel {
}
```

Each entity type is an implementation of those interfaces:

```java
static final DataModel SESSION = new DataModel() {
  // implementation of interfaces
}
```

The next step is writing a ModelVisitor or RelationshipVisitor which accepts a subtype of this interface and use the data gathered in ModelGraph object to do something useful:

```java
Image visualisation = ModelGraphVisualiser.visualize(graph);

TableJoiner joiner = TableJoiner.from(graph);

ContentProviderHelper contentProviderHelper = ContentProviderHelper.from(graph);
UriBuilder uriBuilder = contentProviderHelper.getUriBuilder();
UriMatcher uriMatcher = contentProviderHelper.getUriMatcher();
```

> *But it has other uses. Yes, far beyond that.*  
> *You can use it for carpets. For pillows! For sheets!*  
> *Or curtains! Or covers for bicycle seats!*

The nice thing about the Visitor pattern is that you can define as many different Visitors as you need. In theory this should allow you to get rid of all of the boilerplate code related to data models.

I'm very curious what problems can be solved with this approach, so please let me know how do you use the Thneed!

Caveats
-------

* The API is subject to **heavy** change. This is more of a draft of the proof of concept than a production ready library.
* The Visitors from the code snippets above are not implemented anywhere (yet). They are just names suggesting how you can use the information about data model relationships to reduce boilerplate code.

Building
--------
This is standard maven project. To build it just execute:
```shell
mvn clean package
```
in directory with pom.xml.

Download
--------
Download [jar](http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=org.chalup.thneed&a=thneed&v=LATEST) or add the dependency to your pom.xml:

```xml
<dependency>
  <groupId>org.chalup.thneed</groupId>
  <artifactId>thneed</artifactId>
  <version>0.1</version>
</dependency>
```

License
-------

    Copyright (C) 2013 Jerzy Chalupski

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. 
