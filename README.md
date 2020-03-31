# Application Insights Debug Log Viewer

This is a plugin for [Rider](https://www.jetbrains.com/rider/) to see, instantly, in a nice way [Application Insights](https://docs.microsoft.com/en-us/azure/azure-monitor/app/app-insights-overview) logs.

To use just start a program with the debugger or attach to a running one that use Application Insights.
The logs will appear in a tab, in the debugger panel when starting a debug session.

### Dev

To edit and test the plugin, just open this project with [InteliJ IDEA](https://www.jetbrains.com/idea/) and run the plugin with predefined run configuration

### Build

```
$ ./gradlew :buildPlugin -PbuildType=stable
```

Then the plugins will be in `build/distributions`

### Screenshot

![Screenshot](screenshots/screenshot1.png)

### TODO:

- Revert ApplicationInsightsBundle to new version once stable is 2020.1 (commit `f36cc8f87e59227bf13fd24de42696f91c0f7130`)
- Add clear button

### License

MIT

### Special thanks

 * Ivan Migalev: Help about Rider / InteliJ plugin API
 