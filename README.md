android_xcore
=============

## Android XCore quick usage guide

The main objective of the Android XCore framework is to simplify a development of RESTful service client applications by means of defining data sources just by supplying the URL/URI for the app and merging up them with the UI components.

# UI definition

XListFragment – it is a list fragment, which consumes a generic cursor adapter to display model data. Here you override following methods:
getUrl() – it specifies what URL to use to fetch the JSON (currently only) data from
getUri() – should return a properly formed URI of the model entity. Create it using ModelContract.getUri() method.
Выяснить на счёт вызова loadData метода в onPageLoad коллбеке.
getAdapterLayout() should return an id of the layout resource for a view item of the containing adapter view (ListView in case of XListFragment)
getViewLayout() should return ad id of the layout resource for the fragment content view.

# Data model description

A class, extending ModelContentProvider is supposed to be the main data storage of the app. Extending this class, you’re able to specify entity model classes to use in your application by overriding getDbEntities method
Model entities should be described as classes, implementing BaseColumns interface to be consumable by the ContentProvider.

# Custom data sources

XCore is already capable of the most popular and useful IDataSource implementations, which are: 
HttpAndroidDataSource (uses AndroidHttpClient implementation) – to use a web-resource as a source
FileDataSource – to use device’s file system as a source
AssetsDataSource – to use project assets folder as a source
RawDataSource – to use raw resources folder as a source


## License

    Copyright 2013, Vladimir Klyshevich

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
