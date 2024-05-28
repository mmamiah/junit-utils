# Release notes
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) and this project adheres to 
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 2.0.0 (next release)
- not released

## 1.0.0 - 2024-04-16
### Changed
- changed JDK version `jdk11` to `jdk17` => _**BREAKING CHANGE**_.
### Added
- Added test instance mocks context, to ease mocks & spies retrieval and management.
- added BDD test features
- added DB mock and test features
- added new annotations with corresponding extension & utility class:
  -   `@MyBatisMapperTest`
  -   `@MockValue`
  -   `@InjectMock`
  -   `@SpringContextRunner`
  -   `@WithBeanLifeCycle`
  -   `@MockValue`
  -   `@Fixture`
  -   `@WithBeanLifeCycle`
  -   `@ExtendWithTestUtils`
- added `DataSourceMockBuilder`
- added new extensions:
  -   `ReturnsMocksExtension`
  -   `PostContructExtension`
  -   `DisableTestMethodOnFailureExtension`
  -   `MockInjectionExtension`, `MockitoSpyExtension` & `ReturnsMocksExtension`
- added parameter resolver:
  -   `MyBatisSqlSessionResolver`
  -   `PostContructExtension`

## 0.0.0 - 2020-02-07
- Initial version.
