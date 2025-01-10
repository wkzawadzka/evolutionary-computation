# Evolutionary Computation

Codes for evolutionary computation class at PUT Poznan.

## Table of Contents
- [Overview](#overview)
- [Navigation](#navigation)
  - [Week 1](#week-1)
  - [Week 2](#week-2)
  - [Week 3](#week-3)
  - [Week 4](#week-4)
  - [Week 5](#week-5)
  - [Week 6](#week-6)
  - [Week 7](#week-7)
  - [Week 8](#week-8)
  - [Week 9](#week-9)
  - [Week 10](#week-10)
- [Automatic Report Creation](#automatic-report-creation)

## Overview

Algorithms written in Java, Python used for visualizations.
- **Java Version**: 23
- **JDK**: OpenJDK 23 or equivalent Java 23 distribution
- **Build Tool**: Apache Maven (3.8.0 or later)

### Week 1
**Assignment 1: Greedy heuristics**
Implemented methods:
- Random solution
- Nearest neighbor considering adding the node only at the end of the current path
- Nearest neighbor considering adding the node at all possible position, i.e. at the end, at the beginning, or at any place inside the current path
- Greedy cycle

### Week 2
**Assignment 2: Greedy regret heuristics**
Implemented two methods based on greedy cycle heuristic:
- Greedy 2-regret heuristics.
- Greedy heuristics with a weighted sum criterion – 2-regret + best change of the objective function. By default use equal weights but you can also experiment with other values.

### Week 3
**Assignment 3: Local search**
Implemented both steepest and greedy version of local search. Type of neighborhood with intra and inter (two node exchange) moves, intra-route moves used two options:
- two-nodes exchange,
- two-edges exchange.

### Week 4
**Assignment 4: Candidate moves**
Improving the time efficiency of the steepest local search with the use of candidate moves using the neighborhood, which turned out to be the best in the previous assignment

### Week 5
**Assignment 5: The use of move evaluations (deltas) from previous iterations in local search**

### Week 6
**Assignment 6: Multiple start local search (MSLS) and iterated local search (ILS)**

### Week 7
**Assignment 7: Large neighborhood search**

### Week 8
**Assignment 8: Global convexity (fitness-distance/similarity correlations) tests**

### Week 9
**Assignment 9: Hybrid evolutionary algorithm**
Implemented a hybrid evolutionary algorithm and compared it with the MSLS, ILS, and LNS methods implemented in the previous assignments.

### Week 10
**Assignment 10. Own method**

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