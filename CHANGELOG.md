# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),

## [Unreleased]

NA

## [2.0.0] - 2023-03-07

### Changed
- upgrade Spring Boot to 3.0.3
- upgrade Snakeyaml to 2.0 as 1.33 has security issue
- New application.properties changes
- sprint boot upgrade, keycloak upgrade


## [1.0.6] - 2023-02-22

### Added
 - Added AUTHORS.md, INSTALL.md file
 - Added service port to values.yaml
 - Added comments in values.yaml
 - Created README.md inside charts/dapsreg-svc/
 

### Changed
 - Upgrade the springboot Library
 - Modified .helmignore file
 - Referring the tag from values.yaml to deployment.yaml
 - Make referringConnector parameter (which contains BPN number in suffix) be mandatory


## [1.0.5] - 2023-02-05

### Added
- Created new helm charts for DAPS registration service
- The latest version for daps registration service is 1.0.4


## [1.0.4] - 2022-02-13

### Added
- Registration in DAPS
- Adding BPN number in DAT token
- Provide CRUD operations to EDC connnector registration to the DAPS
- Keycloak protection is added

### Changed
- Generate the controller from the openAPI description
- Update all the used libraries to the latest version
- All smells from SonarQube were fixed
- Moved helm charts from `deployment/helm` to `charts`

### Removed
- Controller has been removed

### Known knowns
- Cross side scripting (XSS) shall be mitigated (low risk)
- Improving the validation of the input parameters (low risk)
