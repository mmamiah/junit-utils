
This project contains the JUnit framework for Java applications, and a sample project.

## Getting Started

These instructions will get you a copy of the project up on your local machine for development.

### Prerequisites

Make sure you have installed :
- [Git](https://git-scm.com/)
- [Maven](https://maven.apache.org/)
- [Java 17](https://openjdk.java.net/install/)
- [JUnit](https://junit.org/junit5/)
- [Hamcrest](http://hamcrest.org/JavaHamcrest/tutorial)
- [Mockito](https://site.mockito.org/)
- [Awaitility](http://www.awaitility.org/)
- [Generate Maven settings.xml from Artifactory](https://confluence.europe.intranet/pages/viewpage.action?pageId=650827708#LinuxDeveloperWorkstation(IPCDevtop)-Mavenconfiguration)

### Installing

Clone the repository:
```bash
git clone ssh://git@github.comm:2222/junit-utils-lib.git
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
  - `user-guide` (`lu.mms.common.quality.userguide`)
  
## Integration in other projects

If you want to use `junit-utils` as dependency of any other project, add the following dependency in your `pom.xml` with the scope `test` :
```xml
<dependency>
    <groupId>lu.mms.common</groupId>
    <artifactId>junit-utils</artifactId>
    <version>${junit-utils.version}</version>
    <scope>test</scope>
</dependency>
```

## Contributing

Please read [Way of Working](https://confluence.europe.intranet/display/LTP/How+To) for details on our code of conduct, 
and the process for submitting pull requests to us.

## Testing standard

Please read [ISO/IEC 29119](https://softwaretestingstandard.org/) for more details.

## Versioning

We use [SemVer](http://semver.org/) for versioning.

## Authors

See the full list of [contributors](https://github.com/mmamiah/tangui/tree/master)
who participated in this project.

## Acknowledgments

* [README Template gist](https://gist.githubusercontent.com/PurpleBooth/109311bb0361f32d87a2/raw/824da51d0763e6855c338cc8107b2ff890e7dd43/README-Template.md) 
for the redaction of what you're reading.



