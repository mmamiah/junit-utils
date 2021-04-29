This project contains the JUnit Utils framework for Java applications, and a sample project.

[![Coverage Status](https://coveralls.io/repos/github/mmamiah/junit-utils/badge.svg?branch=develop)](https://coveralls.io/github/mmamiah/junit-utils?branch=develop)

## Getting Started

These instructions will get you a copy of the project up on your local machine for development.

### Prerequisites

Make sure you have installed :
- [Git](https://git-scm.com/)
- [Maven](https://maven.apache.org/)
- [Java 11](https://openjdk.java.net/install/)
- [JUnit](https://junit.org/junit5/)
- [Hamcrest](http://hamcrest.org/JavaHamcrest/tutorial)
- [Mockito](https://site.mockito.org/)
- [Awaitility](http://www.awaitility.org/)
- [Generate Maven settings.xml from Artifactory](https://confluence.europe.intranet/pages/viewpage.action?pageId=650827708#LinuxDeveloperWorkstation(IPCDevtop)-Mavenconfiguration)

### Installing

Clone the repository:
```bash
git clone https://github.com/mmamiah/junit-utils.git
```

Import into IntelliJ IDEA:
```bash
cd junit-utils-lib
idea .
```

### JUnit Utils POM Artifact
- **Group ID**: `lu.mms.common`
- **Artifact ID**: `junit-utils-pom`
- Java modules name:
  - `junit-utils` (`lu.mms.common.quality`)
  - `user-guide` (`lu.mms.common.quality.samples`)
  
## Integration in other projects
If you want to use `junit-utils` as dependency of any other project, add the following dependency in your `pom.xml` with the scope `test` :
```xml
<dependency>
    <groupId>lu.mms.common</groupId>
    <artifactId>junit-utils</artifactId>
    <version>${junit-utils.version}</version>
    <version>test</version>
</dependency>
```

## Contributing
Please read [Way of Working](https://confluence.europe.intranet/display/LTP/How+To) for details on our code of conduct, 
and the process for submitting pull requests to us.

## Testing standard
Please read [Java Guild](https://confluence.europe.intranet/x/u4k0CQ) for more details.

## Versioning
We use [SemVer](http://semver.org/) for versioning.

## Authors
See the full list of [contributors](https://github.com/junit-utils-lib/tree/master)
who participated in this project.

## Acknowledgments
* [README Template gist](https://gist.githubusercontent.com/PurpleBooth/109311bb0361f32d87a2/raw/824da51d0763e6855c338cc8107b2ff890e7dd43/README-Template.md) 
for the redaction of what you're reading.
