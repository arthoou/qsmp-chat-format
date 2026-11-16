# QSMP Chat Format — Forge 1.21.1

Forge 52.1.0, Java 21, version 1.0.0. Full offline build passed on 2026-10-18; see `build-final.log`. Forge 1.21.1 runs official names, so the normal JAR is the production artifact and no reobfuscation task is required.

`verifyChatFormat` validates chat text and colors, join/leave markers, actual Netty outbound suppression of vanilla presence messages, preservation of unrelated packets/messages and formatted-message passthrough. The project contains only chat formatting and its clickable author credit; no remote administration API. All Rights Reserved and supplied QSMP logo retained.

Server/integrated-server formatting uses vanilla system chat packets, which LeafTalk recognizes client-side. No Minecraft process was launched.

Artifact: `build/libs/qsmp-chat-format-forge-1211-1.0.0.jar`.
