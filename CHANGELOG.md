# ApplicationInsights Debug Log Viewer Changelog

## [2025.1.0] - 2024-04-06

* Support Rider 2025.1

## [2024.3.0] - 2024-11-xx

* Support Rider 2024.3
* Add JSON module dependency
* Upgrade Java version to 21

## [2024.2.0] - 2024-07-26

* Support Rider 2024.2-eap8
* Add support for PageView telemetry

## [2024.1.2] - 2024-03-22

* Add declaration to get suggestion when AI related nuget package is installed

## [2024.1.0] - 2024-03-22

* Support Rider 2024.1-eap9

## [1.10.0] - 2023-02-22

* Support Rider 2023.3-eap8

## [1.9.0] - 2023-07-02

* Support Rider 2023.2-eap6

## [1.8.0] - 2023-02-27

* Support Rider 2023.1-eap4

## [1.7.3] - 2023-01-03

* Support Rider 2022.3.1

## [1.7.2] - 2022-12-07

* Support Rider 2022.3-SNAPSHOT

## [1.7.1] - 2022-11-27

* Support Rider 2022.3-eap7

## [1.7.0]

* Support Rider 2022.3-eap

## [1.6.0]

* Support Rider 2022.2

## [1.5.0]

* Add button to clear filter
* Add a new setting to filter out logs. The settings is per project and persistent
* Add sort by date

## [1.4.0]

* Support Rider 2022.1

## [1.3.0]

* Support Rider 2021.3

## [1.2.1]

* Add a button to sort telemetry by duration

## [1.2.0]

* Support Rider 2021.2

## [1.1.0]

* Support Rider 2021.1

## [1.0.13]

* Fix crash when request does not have a status code

## [1.0.12]

* Rework how exceptions are displayed. Exception are now parsed, and it's possible to click on symbols

## [1.0.11]

* Support Rider 2020.3

## [1.0.10]

* Support Rider 2020.2

## [1.0.9]

* Fix issue with Microsoft.ApplicationInsights 2.14.0

## [1.0.8]

* Support Rider 2020.1

## [1.0.7]

* Display logs in a formatted way. Especially make exceptions more readable, and stack make stack trace clickable.
* Add clear button

## [1.0.6]

* Add button to toggle auto-scroll when new log are added
* General UI improvements
* Add indicator on logs filtered by a TelemetryProcessor (like the AdaptiveSampling or SamplingTelemetryProcessor.
* The ApplicationInsights tab will only appear on first application insights log. It will not appear on project that does not use AI

## [1.0.5]

* Add filter fields. That allow to filtering logs matching the text

## [1.0.4]

* Add numbers of each metrics. It allow to notify there are metrics coming in even when they are filtered out
* Small improvement on UI

## [1.0.3]

* Move Application Insights tool window to the debugger panel, in a new tab
* Selecting an exception in logs will go to where this exception was throw in the code

## [1.0.2]

* Remove "Refresh" button, logs are now collected automatically when starting a debug session
* Add option to filter log by type

## [1.0.2]

* Initial version