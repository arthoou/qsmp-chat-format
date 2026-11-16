# QSMP Chat Format Forge 1.21.1 — 1.0.1 runtime fix

The supplied screenshot and Prism latest.log report an invalid ResourcePackInfo. The produced pack.mcmeta embedded the expanded TOML description, including a raw newline and unescaped quotes in the clickable author link. Gson failed with `Unterminated object at line 4 column 19`.

The resource description is now a plain JSON string, resource format 34 is explicit, and the Gradle resource task expands only mods.toml. The mod description/author credit remain in mods.toml.

Regression: `build/pack-regression-before.log` reproduces the same Gson error against the old processed resource. `build/pack-fix-final.log` passes the full build and the added packaged-resource JSON/format checks, along with the formatter and Netty passthrough checks.

Production JAR comparison: only pack.mcmeta and mods.toml version differ from 1.0.0. Formatter classes and other resources are byte-identical.

Output: build/libs/qsmp-chat-format-forge-1211-1.0.1.jar
SHA-256: 40a04d5578c26ca22b557ba215941abd8eb942092b0db65cb884fbce8087d126

No game was launched during this validation.
