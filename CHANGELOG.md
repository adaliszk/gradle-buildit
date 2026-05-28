## 0.2.16

- Merged "latest" resolution via the maven mafiest over `+` selector
- Added versioned and latest install path resolution for Assets.zip
- Updated DevTools Plugin to 0.5.2

## 0.2.15

- Moved asset resolution to Dev Server only, enabling CI/CD runners to build

## 0.2.14

- Fix :runServer missing CLI arguments (missing HytaleAssets shutdown)

## 0.2.13

- Fix IDEA configuration NPE with multi-module setup ([#22](https://github.com/adaliszk/gradle-scaffoldit-modkit/issues/22))

## 0.2.12

- Fix :setupServer evaluation error ([#21](https://github.com/adaliszk/gradle-scaffoldit-modkit/issues/21))

## 0.2.11

- Drop automatic semver matcher as it's counter-intuitive

## 0.2.10

- Upgraded to Kotlin 2.3.10
- Fix quotation usage to IDEA configuration where it is necessary ([#12](https://github.com/adaliszk/gradle-scaffoldit-modkit/issues/12))
- Added automatic semver matcher to avoid wrong version pinning
- Separated the Assets.zip resolution to allow builds without it ([#18](https://github.com/adaliszk/gradle-scaffoldit-modkit/issues/18))
- Separate Toolchain and Project plugins to avoid immutable errors ([#13](https://github.com/adaliszk/gradle-scaffoldit-modkit/issues/13))

## 0.2.9

- Changed quotation to only apply when spaces are found in paths

## 0.2.8

- Fixed `usePatchline` and `useVersion` not applying the desired configuration
- Shelved offline-mode devserver as DevelopmentMode for the future when its available
- Fixed missed quotation around Asset and Mods paths

## 0.2.7

- Fixed missing `compileOnly` access in `dependencies` ([#2](https://github.com/adaliszk/gradle-scaffoldit-modkit/issues/2))
- Fixed plugin not being available in the `runServer` task ([#8](https://github.com/adaliszk/gradle-scaffoldit-modkit/issues/8))
- Disabled offline-mode as it is only meant for singleplayer ([#9](https://github.com/adaliszk/gradle-scaffoldit-modkit/issues/9))
- Fixed missing arguments for `runServer` task ([#10](https://github.com/adaliszk/gradle-scaffoldit-modkit/issues/10))

## 0.2.6

- Fixed `runServer` usage thanks to `sacramentix`
- Added offline mode to skip authentication

## 0.2.5

- Fix devserver module resolution and subsequent reset on non-match

## 0.2.4

- Fixed `devserver` workdir path resolution
- Fixed `latest` version used instead of `+`

## 0.2.3

- Added "usePatchline" and "useVersion" to configure the server versioning
- Added "devserver {}" for fine tuning the server arguments
- Added several feature toggles in the gradle.properties
- Refactored the internals making the extensions run exactly once per resolution

## 0.2.0

- Released on Maven Central

## 0.1.7-dev

- Fixed hot reloading with dependencies

## 0.1.6-dev

- Fixed Includes for monorepo projects
- Reimplemented automatic scaffolding
- Fixed shadow bundle for the hytale package

## 0.1.5-dev

- Fixed Settings-based configurations
- Fixed Kotlin source path resolution
- Finished Agent plugin that reloads on hot swapping
- Include Devtools Agent for development

## 0.1.4-dev

- Rewritten everything using a delegation-pattern
- Added Hot-Swapping IDEA run configuration
- Added PoC Agent Plugin to reload mods at runtime
- Split the entire project into monorepo packages
- Official Hytale Maven server dependencies
- Published on Maven Central for ease of use
- Release of ScaffoldIt delegation-based API library