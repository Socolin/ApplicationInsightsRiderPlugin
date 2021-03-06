### 1.0.13

* Fix crash when request does not have a status code

### 1.0.12

* Rework how exceptions are displayed. Exception are now parsed, and it's possible to click on symbols

### 1.0.11

* Support Rider 2020.3

### 1.0.10

* Support Rider 2020.2

### 1.0.9

* Fix issue with Microsoft.ApplicationInsights 2.14.0

### 1.0.8

* Support Rider 2020.1

### 1.0.7

* Display logs in a formatted way. Especially make exceptions more readable, and stack make stack trace clickable.
* Add clear button

### 1.0.6

* Add button to toggle auto-scroll when new log are added
* General UI improvements
* Add indicator on logs filtered by a TelemetryProcessor (like the AdaptiveSampling or SamplingTelemetryProcessor.
* The ApplicationInsights tab will only appear on first application insights log. It will not appear on project that does not use AI

### 1.0.5

* Add filter fields. That allow to filtering logs matching the text

### 1.0.4

* Add numbers of each metrics. It allow to notify there are metrics coming in even when they are filtered out
* Small improvement on UI

### 1.0.3

* Move Application Insights tool window to the debugger panel, in a new tab
* Selecting an exception in logs will go to where this exception was throw in the code

### 1.0.2

* Remove "Refresh" button, logs are now collected automatically when starting a debug session
* Add option to filter log by type

### 1.0.2

* Initial version