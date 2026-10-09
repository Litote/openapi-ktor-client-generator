# Changelog

## [0.9.0](https://github.com/Litote/openapi-ktor-client-generator/compare/v0.8.0...v0.9.0) (2026-10-09)


### ⚠ BREAKING CHANGES

* ClientConfiguration has a new `userAgent` parameter before `engine`, and defaultHttpClientConfig a new `userAgent` parameter before `httpClientAuthorization`: calls passing these arguments by position must be updated.

### Features

* configurable User-Agent and HTTP engine ([#93](https://github.com/Litote/openapi-ktor-client-generator/issues/93)) ([f94e17c](https://github.com/Litote/openapi-ktor-client-generator/commit/f94e17c6b57422ecf88779d6166c7f9888909593))


### Bug Fixes

* convert numeric default values of model properties to the property type ([6955482](https://github.com/Litote/openapi-ktor-client-generator/commit/69554829ce8f6075edba464b0e89b8b605980c29))
* do not generate oneOf request/response models of filtered-out operations ([#94](https://github.com/Litote/openapi-ktor-client-generator/issues/94)) ([388246b](https://github.com/Litote/openapi-ktor-client-generator/commit/388246b47e7cc12d784dfc29c39f602c9138aabf))
* use the target type for references to non-object component schemas ([129da9c](https://github.com/Litote/openapi-ktor-client-generator/commit/129da9cca4f4eb591787f18df90a38b63c6252f6))

## [0.8.0](https://github.com/Litote/openapi-ktor-client-generator/compare/v0.7.0...v0.8.0) (2026-09-29)


### ⚠ BREAKING CHANGES

* generate an interface and a factory function for each client ([#86](https://github.com/Litote/openapi-ktor-client-generator/issues/86))
* parameters serialization ([#85](https://github.com/Litote/openapi-ktor-client-generator/issues/85))
* retrieve response headers ([#84](https://github.com/Litote/openapi-ktor-client-generator/issues/84))

### Features

* add binary response support ([#83](https://github.com/Litote/openapi-ktor-client-generator/issues/83)) ([596d4b7](https://github.com/Litote/openapi-ktor-client-generator/commit/596d4b714c08b94e5b93cc81c181242e97f40534))
* add uuid and datetime modules ([#82](https://github.com/Litote/openapi-ktor-client-generator/issues/82)) ([222786a](https://github.com/Litote/openapi-ktor-client-generator/commit/222786ae21e5c8921432dc138ab513663b136fcd))
* generate an interface and a factory function for each client ([#86](https://github.com/Litote/openapi-ktor-client-generator/issues/86)) ([f6631ec](https://github.com/Litote/openapi-ktor-client-generator/commit/f6631ecb83883528492f4f8e83d24a14333bb694))
* parameters serialization ([#85](https://github.com/Litote/openapi-ktor-client-generator/issues/85)) ([0fb6754](https://github.com/Litote/openapi-ktor-client-generator/commit/0fb675453cacde143bb3425124c7b128c69466e2))
* retrieve response headers ([#84](https://github.com/Litote/openapi-ktor-client-generator/issues/84)) ([06848a2](https://github.com/Litote/openapi-ktor-client-generator/commit/06848a2967a0eff2e8821cc1e3f29261a12aa5a3))


### Bug Fixes

* **ci:** add exact version for actions ([eda7f43](https://github.com/Litote/openapi-ktor-client-generator/commit/eda7f430db80e0d99697ffed551896923d1774a8))
* **ci:** avoid dependabot race condition ([#81](https://github.com/Litote/openapi-ktor-client-generator/issues/81)) ([71d78dd](https://github.com/Litote/openapi-ktor-client-generator/commit/71d78dd99806d2cd13a02dc4605d3d0f8bbde9b5))
* **ci:** regenerate Kotlin Wasm yarn lock in dependabot automation ([#61](https://github.com/Litote/openapi-ktor-client-generator/issues/61)) ([405d930](https://github.com/Litote/openapi-ktor-client-generator/commit/405d9300f0d1e22b5917a0089097733945be8863))
* do not catch CancellationException - support coroutine cancellation ([#80](https://github.com/Litote/openapi-ktor-client-generator/issues/80)) ([08eb5d0](https://github.com/Litote/openapi-ktor-client-generator/commit/08eb5d03cf27310312b8255bcb3687b74ada128d))

## [0.7.0](https://github.com/Litote/openapi-ktor-client-generator/compare/v0.6.1...v0.7.0) (2026-06-02)


### Features

* support OpenAPI 3.1 and 3.2 ([a1d5aa5](https://github.com/Litote/openapi-ktor-client-generator/commit/a1d5aa54ae90d52f07257db7d414a0d56865ad5e))

## [0.6.1](https://github.com/Litote/openapi-ktor-client-generator/compare/v0.6.0...v0.6.1) (2026-04-06)


### Bug Fixes

* function header is not imported in BasicAuthModule ([c0b2460](https://github.com/Litote/openapi-ktor-client-generator/commit/c0b2460843e7d7ae45a4b889d51e3c52bf46197e))

## [0.6.0](https://github.com/Litote/openapi-ktor-client-generator/compare/v0.5.0...v0.6.0) (2026-03-31)


### Features

* add basic auth module ([74bfd7f](https://github.com/Litote/openapi-ktor-client-generator/commit/74bfd7ff283bf25654bd1c77d0bb3addff70544d))
* add logging level in ClientConfiguration ([422aac7](https://github.com/Litote/openapi-ktor-client-generator/commit/422aac7c56b671d0941eef651e2f81a68c74da5f))

## [0.5.0](https://github.com/Litote/openapi-ktor-client-generator/compare/v0.4.1...v0.5.0) (2026-03-22)


### Features

* add more configuration options for project generation ([31b3830](https://github.com/Litote/openapi-ktor-client-generator/commit/31b3830c810092e5b1194ff2bb6f72b9c3cf851d))


### Bug Fixes

* inheritance interfaces not collected when splitting generated builds ([dc0f70a](https://github.com/Litote/openapi-ktor-client-generator/commit/dc0f70accaf40adf88786fb4f9349e1dea2fa79b))
* js target compile dependency ([c9f36b8](https://github.com/Litote/openapi-ktor-client-generator/commit/c9f36b8374323244c5274f268e18096010315ee2))
* LoggingKotlinModule is not KMP compatible ([e36a88c](https://github.com/Litote/openapi-ktor-client-generator/commit/e36a88c9dc5daa02db925098e6c9edd924ac1fb4))
* MultiPartFormDataContent does not work - mission ContentDisposition ([83802ce](https://github.com/Litote/openapi-ktor-client-generator/commit/83802ce370537d5c2486817c86179608982bb2ec))

## [0.4.1](https://github.com/Litote/openapi-ktor-client-generator/compare/v0.4.0...v0.4.1) (2026-03-20)


### Bug Fixes

* allOf inheritance ([ec950e2](https://github.com/Litote/openapi-ktor-client-generator/commit/ec950e285d32fbae98a475bd712f04ac02827102))
* oneOf response ([eebb0ca](https://github.com/Litote/openapi-ktor-client-generator/commit/eebb0ca8c9cb13347f8fc73169c0c954cb618dc2))

## [0.4.0](https://github.com/Litote/openapi-ktor-client-generator/compare/v0.3.0...v0.4.0) (2026-03-20)


### Features

* add custom modules ([8292e00](https://github.com/Litote/openapi-ktor-client-generator/commit/8292e00227a56fa392349f4e5595b26f4bc197bb))


### Bug Fixes

* add logging-kotlin module dependency in gradle-plugin ([f211e9f](https://github.com/Litote/openapi-ktor-client-generator/commit/f211e9fb883ac39cb0ed11578c0c325baf201f98))
* do not use kotlin-logging-jvm dependency but kotlin-logging ([6c37d9a](https://github.com/Litote/openapi-ktor-client-generator/commit/6c37d9a760eb18a3ef12d69dc4ff523c53349f46))

## [0.3.0](https://github.com/Litote/openapi-ktor-client-generator/compare/v0.2.0...v0.3.0) (2026-03-17)


### Features

* add dependabot for github actions ([e7b0950](https://github.com/Litote/openapi-ktor-client-generator/commit/e7b09503d6e0e8f42ee1373daf9ae4cffccd063f))
* add gradle dependabot ([3f41fa2](https://github.com/Litote/openapi-ktor-client-generator/commit/3f41fa295da2a187dd0d5161bc447362084c7a07))
* add version-catalog ([2d30be1](https://github.com/Litote/openapi-ktor-client-generator/commit/2d30be10a72d344917fd480cb791a787ade6aedf))
* use release-please plugin ([2d30be1](https://github.com/Litote/openapi-ktor-client-generator/commit/2d30be10a72d344917fd480cb791a787ade6aedf))


### Bug Fixes

* concurrent execution on main ([cccbf48](https://github.com/Litote/openapi-ktor-client-generator/commit/cccbf48dd78f0fb28540950823dbb068be6a8099))
* ignore sonar for dependabot ([3f41fa2](https://github.com/Litote/openapi-ktor-client-generator/commit/3f41fa295da2a187dd0d5161bc447362084c7a07))
* sonar on PR ([530fa73](https://github.com/Litote/openapi-ktor-client-generator/commit/530fa73eee2d9d8dec0cab1da8c1cacd3c0428ac))
* trigger gradle dependabot PR checks ([cccbf48](https://github.com/Litote/openapi-ktor-client-generator/commit/cccbf48dd78f0fb28540950823dbb068be6a8099))
