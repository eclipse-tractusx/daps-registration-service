# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),

## [Unreleased]

 - sprint boot upgrade
 - keycloak upgrade

## [1.0.7] - 2023-03-03

### Added
This Version of DAPS-Registration Service faces several Security Issues. Those were evaluated at time of Release and will be fixed in the next version.
Recommended mitigation action for Operating Companies is to enforce appropriate firewall rules so that the service cannot be accessed externally. (note: within the intended purpose, DAPS-Registration Service shall only be called from Portal

## [1.0.6] - 2023-02-27

### Added
 - Added AUTHORS.md, INSTALL.md file
 - Added service port to values.yaml
 - Added comments in values.yaml
 - Created README.md inside charts/dapsreg-svc/


### Changed
 - Modified .helmignore file
 - Referring the tag from values.yaml to deployment.yaml


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
