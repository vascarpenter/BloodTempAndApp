## BloodTempAndApp

- Android app for posting blood temperature
- use with "httptest" server application

### このandroidアプリをコンパイルする前に

- build.gradle :app から ３つの文字列を参照しているので
- `~/.gradle/gradle.properties` に追加しておく
```
# 自分のサイトにあった設定に差し替えてね
bloodpostapiurl=https://ogehage.tk/post
bloodgetapiurl=https://ogehage.tk/get?apikey=THEKEY
bloodapikey=THEKEY
```
- kotlin ソース内で参照してます