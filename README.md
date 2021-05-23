<img src="https://avatars.githubusercontent.com/u/83244875?s=400&v=4" width="50"/>

Business Logic - Wikipedia
=====================

This business logic is an example how to implement a custom logic for your chatbot on the [Business Bot Platform](https://www.citunius.de/). This project implements a simple Wikipedia logic and allows developers to use it as template for their own business logic and run it on the Business Bot Platform.

A business logic is a service that is connected to a chatbot in order to provide a specific service for mobile users. The chatbot defines how the message is transmitted, while the business logic decides how the message is processed.

The business logic is effectively the WebApp while the Business Bot Platform is the application server such as Tomcat.


Features/Benefits
-------------------
- Query Wikipedia article
- Configurable language of retrieved Wikipedia article (e.g. EN=English, DE=German aso.)
- Supports Dialog Designer of the Business Bot Platform

Please check out the [documentation](https://library.citunius.de/products/bbp_edition_community/) for
additional information on this project.

JDK 1.8 is required. Make sure your default JDK version is >=1.8
by typing `java -version`.


## How to use


## Compile
Use the [Business Logic - Creator](https://github.com/citunius/businesslogic.creator) to compile this project. Once compiled, deploy the business logic package on the Business Bot Platform as documented [here](https://library.citunius.de/products/bbp_edition_community/documentation/R2021-FP2118/en/administration/botadministration/businesslogics/#add-business-logic-from-a-local-file).


## Business Bot Platform Documentation
For a more detailed documentation about the Business Bot Platform, please visit: [https://library.citunius.de/products/bbp_edition_community/](https://library.citunius.de/products/bbp_edition_community/) 


# History
Initial version by [Citunius GmbH](https://www.citunius.de/)

## License
This software is under [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Suggestions
Any suggestions are  welcome.