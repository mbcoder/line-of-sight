# Line of sight CPU based for 2D

This is a sample application showing how a line of sight can be acheived using a 2D Esri Maps SDK map.

![screenshot](screenshot.png)

## Instructions

### IntelliJ IDEA

1. Open IntelliJ IDEA and select _File > Open..._.
2. Choose the java-gradle-starter-project directory and click _OK_.
3. Select _File > Project Structure..._ and ensure that the Project SDK and language level are set to use Java 11.
4. Open the Gradle view with _View > Tool Windows > Gradle_.
5. In the Gradle view, double-click `copyNatives` under _Tasks > build_. This will unpack the native library dependencies to $USER_HOME/.arcgis.
6. In the Gradle view, double-click `run` under _Tasks > application_ to run the app.


### Command Line

1. `cd` into the project's root directory.
2. Run `./gradlew clean build` on Linux/Mac or `gradlew.bat clean build` on Windows.
3. Run `./gradlew copyNatives` on Linux/Mac or `gradlew.bat copyNatives` on Windows. This will unpack the native library dependencies to $USER_HOME.arcgis.
4. Run `./gradlew run` on Linux/Mac or `gradlew.bat run` on Windows to run the app.

## Requirements

See the Java Maps SDK [system requirements](https://developers.arcgis.com/java/reference/system-requirements/).

## Resources

* [ArcGIS Maps SDK for Java](https://developers.arcgis.com/java/)  
* [ArcGIS Blog](https://www.esri.com/arcgis-blog/developers/)  
* [Esri Twitter](https://twitter.com/arcgisdevs)  

## Issues

Find a bug or want to request a new feature?  Please let us know by submitting an issue.

## Contributing

Esri welcomes contributions from anyone and everyone. Please see our [guidelines for contributing](https://github.com/esri/contributing).

## Licensing

Licensed under the Apache License, Version 2.0 (the "License"); you may not 
use this file except in compliance with the License. You may obtain a copy 
of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
License for the specific language governing permissions and limitations 
under the License.

A copy of the license is available in the repository's license.txt file.
