# Evolutionary Computation

## Table of Contents
- [Overview](#overview)
- [Navigation](#navigation)
  - [Week 1](#week-1)
  - [Week 2](#week-2)
  - [Week 3](#week-3)
  - [Week 4](#week-4)
- [Automatic Report Creation](#automatic-report-creation)

## Overview

Written in:
Java
- **Java Version**: 23
- **JDK**: OpenJDK 23 or equivalent Java 23 distribution
- **Build Tool**: Apache Maven (3.8.0 or later)

Python (for Visualization)
- **Python Version**: 3.x todo

### Week 1
- Description of Week 1 activities, methods used, and findings.

### Week 2
- Description of Week 2 activities, methods used, and findings.

### Week 3
- Description of Week 3 activities, methods used, and findings.

### Week 4
- Description of Week 4 activities, methods used, and findings.

---

## Automatic Report Creation
Exporting results, 2D visualization, descriptions etc. to Word.
```
python create_report.py --help
usage: create_report.py [-h] [--type TYPE] [--addon ADDON] week_name

Create a report based on the provided week name and options.

positional arguments:
  week_name      Name of the week (e.g., w3_local_search)

options:
  -h, --help     show this help message and exit
  --type TYPE    Type of report: separate/integrative (default: "separate")
  --addon ADDON  Whether to include previous methods (default: "no")
```