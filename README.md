# Application Insights Debug Log Viewer

This is a plugin for [Rider](https://www.jetbrains.com/rider/) to see, instantly, in a nice way [Application Insights](https://docs.microsoft.com/en-us/azure/azure-monitor/app/app-insights-overview) logs.

To use just start a program with the debugger or attach to a running one that use Application Insights.
The logs will appear in the tool window named Application Insights.

### Dev

To edit and test the plugin, just open this project with [InteliJ IDEA](https://www.jetbrains.com/idea/) and run the plugin with predefined run configuration

### Build

```
$ ./gradlew :buildPlugin -PbuildType=stable
```

Then the plugins will be in `build/distributions`

### Screenshot

![Screenshot](screenshots/screenshot1.png)


### License

MIT

### Special thanks

 * Ivan Migalev: Help about Rider / InteliJ plugin API
 