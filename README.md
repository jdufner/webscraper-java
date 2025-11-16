# Webscraper-Java

## Description

### Data analytics

Count number of document in different states.

    select state, count(id)
    from documents
    group by state;

Minimal, maximal and average download duration of documents. 

    select min(download_stopped_at-download_started_at), avg(download_stopped_at-download_started_at), max(download_stopped_at-download_started_at)
    from documents;

Minimal, maximal and average size of documents.

    select min(content_length), avg(content_length), max(content_length)
    from documents
    group by state;

Count number of links in different states.

    select state, count(id)
    from links
    group by state;

Count number of images in different states.

    select state, count(id)
    from images
    group by state;

Create some sizes of interest and count number of documents belong to sizes.

    select
        case
            when content_length < 10_000 then '01. <10k'
            when content_length >= 10_000 and content_length < 100_000 then '02. <100k'
            when content_length >= 100_000 and content_length < 200_000 then '03. <200k'
            when content_length >= 200_000 and content_length < 300_000 then '04. <300k'
            when content_length >= 300_000 and content_length < 400_000 then '05. <400k'
            when content_length >= 400_000 and content_length < 500_000 then '06. <500k'
            when content_length >= 500_000 and content_length < 1_000_000 then '07. <1m'
            when content_length >= 1_000_000 and content_length < 10_000_000 then '08. <10m'
            else '09. >=10m'
        end as sizes,
    count(id) as number,
    avg(download_stopped_at-download_started_at) as time
    from documents
    group by sizes
    order by sizes;



## Installation

### Prerequisites

### Installation instruction

## Usage

### Basic usage

### Advanced usage

## Configuration

### Configuration file

### Environment variables

To avoid warnings regarding the use of Mockito, the following parameter must be set.  

    -javaagent:$MAVEN_REPOSITORY$/org/mockito/mockito-core/5.17.0/mockito-core-5.17.0.jar

The preferred setup for local tests is hsqldb, localWebDriver, and heise.

    spring.profiles.active=hsqldb,localWebDriver,heise

## Developer information

### Maven Profiles

| Name        | Purpose     | Spring Profiles        |
|-------------|-------------|------------------------|
| local       | Local Test  | heise, localWebDriver  |
| buildserver | Remote Test | heise, remoteWebDriver |

### Spring Profiles

| Name            | Purpose                                                     |
|-----------------|-------------------------------------------------------------|
| apod            | Fetches HTML pages from www.apod.org                        |
| heise           | Fetches HTML pages from www.heise.de                        |
| localWebDriver  | Uses a local Selenium Web Driver with local Chrome browser. |
| remoteWebDriver | Uses a remote Selenium Web Driver as Docker container.      |
| hsqldb          | Uses a local in-memory database.                            |
| postgresql      | Uses a remote PostgreSQL database.                          |

### Todo

* Set up build pipeline in Jenkins (Multibranch-Pipeline?)
* Execute unit and integration tests separately
* Measure code coverage (JaCoCo)
* PiTest
* Static code analysis (CheckStyle, FindBugs)
* OWASP check, [OSV-Scanner](https://google.github.io/osv-scanner/)
* [TruffleHog](https://trufflesecurity.com/trufflehog)
* [Synk](https://snyk.io/)
* [Chekov](https://www.checkov.io/) and / or [Trivy](https://github.com/aquasecurity/trivy)?
* [SonarQube](https://www.sonarsource.com/products/sonarqube/)
* Class Data Sharing ([CDS](https://docs.spring.io/spring-framework/reference/integration/cds.html))? 
* Migrate to GraalVM
* Build native executable
* Build Docker image
* Implement Spring Profile for Postgres and MinIO
* Improve documentation, potentially switch to Arc42 with AsciiDoc
* License: [AGPL-3.0](https://www.gnu.org/licenses/agpl-3.0)

### Firefox

Test CSS selector in Console

    $$("a-sticky-footer button[data-hide-trigger]")
 
Execute Javascript in Console

    document.querySelectorAll('a-sticky-footer button[data-hide-trigger]').forEach((element) => element.click())

## License

Webscraper-Java - Downloads HTML pages.
Copyright (C) 2023 Jurgen Dufner

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

## Contact
