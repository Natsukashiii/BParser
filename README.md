# Build Configuration Parsers

This project extracts and standardizes build configuration steps from various CI/CD tools and build systems in open-source repositories. The extracted data is saved as a structured CSV file for further analysis.

#### Features
	
###### Supports multiple CI/CD tools:
  - GitHub Actions (.github/workflows/)
  - CircleCI (.circleci/config.yml)
  - TravisCI (.travis.yml)
  - Maven (pom.xml)

#### Usage
- Main function: StandardlizeProcess2CSV.class
- application.properties: configure dataset path and output path.
 
