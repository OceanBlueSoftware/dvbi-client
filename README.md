# DVB-I Client

Android library that implements a [DVB-I](https://dvb-i.tv/) client for the [Open Red Button](https://github.com/OpenRedButtonProject/orb) (ORB) stack. DVB-I can be integrated into an Android [Tv Input Service](https://developer.android.com/reference/android/media/tv/TvInputService): the library discovers service lists, presents DASH and broadcast instances, and feeds channel/EPG data into Android `TvContract`.

This is a **library**, not a standalone app. Host it from your own `TvInputService` (or equivalent Android TV input).

## What it does

* Fetches and parses a DVB-I service list (ETSI TS 103 770) and keeps it versioned.
* Tunes a service or a specific service instance (DASH via dash.js in a WebView, or RF/broadcast via the host tuner).
* Publishes programmes from the DVB-I content guide into Android `TvContract`.
* Surfaces linked HbbTV applications, parental rating, tracks/components, and stream events to the integrator.

## Using the library

Instantiate once from your TV input or setup flow, then search and tune:

```java
DvbIClient.instantiate(context);
DvbIClient client = DvbIClient.getInstance();

client.setTvInputCallback(/* tuner, video, parental callbacks */);
client.startServiceSearch("https://example.com/servicelist.xml");

client.tune(serviceUid, /* instanceIndex, or -1 for highest priority */ -1);
View dashView = client.getView(); // overlay for DASH playback
```

`startServiceSearch(null)` / `updateServiceList(null)` use the built-in HbbTV test-harness URL (`http://hbbtv1.test/servicelist.xml`). Production integrators should pass their own service list URL.

Host integration is through:

* `ITvInputCallback` — tuner, video availability, parental override
* `DvbCallback` / `HbbTVCallback` (from `companionlibrary`) — channel/EPG and HbbTV signalling
* `DvbIClient.Callback` — DVB-I playback and instance changes

## Layout

* **`src/main/java/org/orbtv/dvbiclient/`** — `DvbIClient` and supporting managers (tune, EPG, database)
* **`src/main/java/org/orbtv/dvbiclient/model/`** — service list, instance, programme, and related types
* **`src/main/assets/polyfill/`** — DASH player page (`dvbipage.html` + dash.js)

Depends on `companionlibrary` for shared channel/EPG models and callbacks.

Minimum SDK 28. Build as an Android library (`build.gradle`) or as the AOSP static Java module `org.orbtv.dvbiclient`.

## Related projects

* [ORB](https://github.com/OpenRedButtonProject/orb) — HbbTV browser integration

## License

ORB Software. Copyright (c) Ocean Blue Software Limited.

Licensed under the Apache License, Version 2.0. See the license header in each source file, or <http://www.apache.org/licenses/LICENSE-2.0>.
