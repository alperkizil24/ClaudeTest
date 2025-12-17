# Java Performance Metrics for Multi-Objective Evolutionary Algorithms

A Java library for calculating performance metrics used to evaluate Pareto fronts in multi-objective evolutionary algorithms (MOEAs). This library provides implementations of standard quality indicators including Hypervolume, Inverse Generational Distance, Spacing, and Coverage Metric.

## Overview

Multi-objective optimization algorithms produce a set of trade-off solutions known as a Pareto front. Evaluating the quality of these fronts requires specialized metrics that assess both **convergence** (how close solutions are to the true optimal front) and **diversity** (how well-spread solutions are across the objective space).

This library implements the most commonly used performance metrics for 2-objective optimization problems.

## Project Structure

```
Java-Performance-Metrics/
├── PerfMet/                          # Source code package
│   ├── PM_Main.java                  # Entry point and demonstration
│   ├── PerformanceMetrics.java       # Core metrics calculations
│   ├── Dominance.java                # Dominance comparison logic
│   ├── FitnessComparator.java        # Fitness value sorting
│   └── MyFileReader.java             # Pareto front file I/O
└── paretoFiles/                      # Sample data files
    ├── A.txt                         # Sample Pareto front A
    ├── B.txt                         # Sample Pareto front B
    └── True_Pareto.txt               # Reference/true Pareto front
```

## Implemented Metrics

### 1. Hypervolume (HV)

**Purpose:** Measures the volume of objective space covered by the Pareto front with respect to a reference point.

**How it works:**
- Solutions are normalized to [0, 1] range
- Reference point is set to (1.0, 1.0)
- Calculates the area dominated by the Pareto front
- Uses a linear sweep algorithm on sorted solutions

**Interpretation:** Higher is better - larger covered area indicates a better solution set.

```java
double hv = pm.HV(paretoIndex);
```

### 2. Inverse Generational Distance (IGD)

**Purpose:** Measures convergence by calculating the average distance from each point in the true Pareto front to its nearest point in the approximation.

**How it works:**
- For each solution in the true Pareto front, find the minimum Euclidean distance to any solution in the approximation
- Average all minimum distances

**Formula:**
```
IGD = (1/|True_Pareto|) * Σ min(euclidean_distance(true_solution, approx_solution))
```

**Interpretation:** Lower is better - solutions closer to the true front indicate better convergence.

```java
double igd = pm.IGD(paretoIndex);
```

### 3. Spacing Metric

**Purpose:** Measures the uniformity of solution distribution along the Pareto front.

**How it works:**
- Calculates Manhattan distances between consecutive solutions
- Computes the standard deviation of these distances
- Uniform spacing results in lower values

**Interpretation:** Lower is better - uniform spacing indicates good diversity.

```java
double spacing = pm.Spacing(paretoIndex);
```

### 4. Coverage Metric (C-Metric)

**Purpose:** Binary comparison metric that measures what fraction of one Pareto front is dominated by another.

**How it works:**
- For each solution in front B, check if any solution in front A dominates it
- A solution dominates another if it is better in all objectives

**Formula:**
```
C(A,B) = |{solutions in B dominated by A}| / |B|
```

**Interpretation:**
- Range: [0, 1]
- C(A,B) = 1 means A completely dominates B
- Note: C(A,B) ≠ C(B,A) (asymmetric)

```java
double coverage = pm.C_Metric(firstIndex, secondIndex);
```

## Usage

### Basic Example

```java
import PerfMet.*;

public class Example {
    public static void main(String[] args) {
        // Step 1: Load Pareto fronts from files
        ArrayList<ArrayList<Double>> A = MyFileReader.getPareto("paretoFiles/A.txt");
        ArrayList<ArrayList<Double>> B = MyFileReader.getPareto("paretoFiles/B.txt");
        ArrayList<ArrayList<Double>> trueFront = MyFileReader.getPareto("paretoFiles/True_Pareto.txt");

        // Step 2: Create container for all fronts
        ArrayList<ArrayList<ArrayList<Double>>> allParetos = new ArrayList<>();
        allParetos.add(A);          // Index 0
        allParetos.add(B);          // Index 1
        allParetos.add(trueFront);  // Index 2 (reference front)

        // Step 3: Initialize the metrics engine
        PerformanceMetrics pm = new PerformanceMetrics(allParetos);

        // Step 4: Calculate and display metrics
        System.out.println("Hypervolume of A: " + pm.HV(0));
        System.out.println("Hypervolume of B: " + pm.HV(1));
        System.out.println("IGD of A: " + pm.IGD(0));
        System.out.println("IGD of B: " + pm.IGD(1));
        System.out.println("Spacing of A: " + pm.Spacing(0));
        System.out.println("Spacing of B: " + pm.Spacing(1));
        System.out.println("C-Metric(A,B): " + pm.C_Metric(0, 1));
        System.out.println("C-Metric(B,A): " + pm.C_Metric(1, 0));
    }
}
```

### Input File Format

Pareto front files should be semicolon-delimited text files with the following format:

```
Pareto Front: (f1; f2)
7544.37 ; 23.05
7807.11 ; 20.55
26584.23 ; 2.62
...
```

- First line is a header (skipped during parsing)
- Each subsequent line contains two objective values separated by `;`
- Values can have decimal points

## Architecture

```
PM_Main (Entry Point)
    │
    ├──→ MyFileReader.getPareto()     ← Load Pareto fronts from files
    │
    ├──→ PerformanceMetrics Constructor
    │    ├──→ FitnessComparator       ← Sort solutions by f1
    │    ├──→ findMinMax()            ← Determine normalization bounds
    │    └──→ normalize()             ← Scale to [0,1] range
    │
    └──→ Calculate Metrics
         ├──→ HV()        → Hypervolume calculation
         ├──→ IGD()       → Inverse Generational Distance
         ├──→ Spacing()   → Distribution uniformity
         └──→ C_Metric()  → Coverage comparison
              └──→ Dominance.compare()
```

## Data Structures

| Structure | Description |
|-----------|-------------|
| `ArrayList<Double>` | Single solution with 2 objective values [f1, f2] |
| `ArrayList<ArrayList<Double>>` | One Pareto front (collection of solutions) |
| `ArrayList<ArrayList<ArrayList<Double>>>` | All Pareto fronts |

## Key Classes

### PerformanceMetrics
The core class that handles:
- Sorting Pareto fronts by first objective
- Min-max normalization to [0, 1] range
- All metric calculations

### Dominance
Implements dominance comparison:
- Returns `-1` if first solution dominates second
- Returns `1` if second solution dominates first
- Returns `0` if incomparable

### FitnessComparator
Java Comparator for sorting solutions by first objective value in ascending order.

### MyFileReader
Handles file I/O operations for reading Pareto front data from text files.

## Technical Notes

- **Objective Space:** Designed for 2-objective optimization problems
- **Optimization Direction:** Assumes minimization for all objectives
- **Normalization:** All solutions are normalized to [0, 1] range before metric calculation
- **Reference Front:** IGD calculation expects the true Pareto front at index 2

## Metric Interpretation Summary

| Metric | Measures | Better Value |
|--------|----------|--------------|
| Hypervolume | Convergence + Diversity | Higher |
| IGD | Convergence | Lower |
| Spacing | Diversity/Uniformity | Lower |
| C-Metric | Relative Dominance | Higher (for first front) |

---

# Experimental Data: Cloud VM Task Scheduling

This repository also contains experimental results from a cloud VM task scheduling optimization study. The experiments compare various multi-objective and single-objective evolutionary algorithms for optimizing task scheduling in a cloud computing environment.

## Experiment Configuration

| Parameter | Value |
|-----------|-------|
| Number of VMs | Static |
| Number of Hosts | Static |
| Number of Users | 1 |
| Number of Datacenters | 1 |
| Task Sizes | 700, 900, 1200 tasks |

## Optimization Objectives

Three minimization objectives are considered:

| Objective | Unit | Description |
|-----------|------|-------------|
| **Makespan** | seconds (s) | Total time to complete all tasks |
| **Energy Consumption** | Watt-hours (Wh) | Total energy used by the system |
| **Avg. Wait Time** | seconds (s) | Average time tasks wait before execution |

## Data Structure

```
Multi-Objective Algorithms/
├── 700 Task/                    # Results for 700 tasks
├── 900 Task/                    # Results for 900 tasks
└── 1200 Task/                   # Results for 1200 tasks

Single - Objective Algorithms/
├── 700 Tasks/
│   ├── GA_AvgWait/
│   ├── GA_Energy/
│   ├── GA_ISL_AvgWait/
│   ├── GA_ISL_Energy/
│   ├── GA_ISL_Makespan/
│   ├── GA_MAKESPAN/
│   ├── LJF_BEST/
│   ├── LJF_WORST/
│   ├── SA_AvgWait/
│   ├── SA_Energy/
│   ├── SA_Makespan/
│   ├── SJF_BEST/
│   └── SJF_WORST/
├── 900 Tasks/
│   └── [same structure]
└── 1200 Tasks/
    └── [same structure]
```

## Multi-Objective Algorithms

Algorithms from the MOEA Java Library:

| Algorithm | Full Name | Description |
|-----------|-----------|-------------|
| **MOEA_AMOSA** | Archived Multi-Objective Simulated Annealing | Simulated annealing-based MOEA |
| **MOEA_eNSGAII** | Epsilon-NSGA-II | Enhanced NSGA-II with epsilon dominance |
| **MOEA_NSGAII** | Non-dominated Sorting Genetic Algorithm II | Classic Pareto-based evolutionary algorithm |
| **MOEA_SPEAII** | Strength Pareto Evolutionary Algorithm II | Fitness assignment based on dominance strength |

### Multi-Objective File Naming Convention

```
ALG_NAME_(objective)_rnd_SEED_hh_mm_ss_sol_XX.xlsx
```

| Component | Description |
|-----------|-------------|
| `ALG_NAME` | Algorithm name (e.g., MOEA_AMOSA, MOEA_NSGAII) |
| `objective` | Objective pair being optimized |
| | `eVSs` = Energy vs Avg. Wait Time |
| | `mVSs` = Makespan vs Avg. Wait Time |
| | *(empty)* = Energy vs Makespan |
| `SEED` | Random seed used for the run |
| `hh_mm_ss` | Time of recording |
| `XX` | Solution number in the Pareto front |

**Example:** `MOEA_AMOSA_eVSs_rnd_1200_14_34_17_sol_1.xlsx`
- Algorithm: AMOSA
- Objectives: Energy vs Avg. Wait Time
- Random seed: 1200
- Time: 14:34:17
- Solution: 1st point on Pareto front

## Single-Objective Algorithms

### Genetic Algorithm Variants

| Algorithm | Description |
|-----------|-------------|
| **GA_AvgWait** | Standard GA optimizing Avg. Wait Time |
| **GA_Energy** | Standard GA optimizing Energy Consumption |
| **GA_MAKESPAN** | Standard GA optimizing Makespan |
| **GA_ISL_AvgWait** | Island Model GA optimizing Avg. Wait Time |
| **GA_ISL_Energy** | Island Model GA optimizing Energy Consumption |
| **GA_ISL_Makespan** | Island Model GA optimizing Makespan |

### Simulated Annealing Variants

| Algorithm | Description |
|-----------|-------------|
| **SA_AvgWait** | SA optimizing Avg. Wait Time |
| **SA_Energy** | SA optimizing Energy Consumption |
| **SA_Makespan** | SA optimizing Makespan |

### Heuristic Algorithms

| Algorithm | Description |
|-----------|-------------|
| **LJF_BEST** | Longest Job First with Best Fit allocation |
| **LJF_WORST** | Longest Job First with Worst Fit allocation |
| **SJF_BEST** | Shortest Job First with Best Fit allocation |
| **SJF_WORST** | Shortest Job First with Worst Fit allocation |

### Single-Objective File Naming Convention

```
ALG_NAME_rnd_SEED_hh_mm_ss_sol_1.xlsx
```

Single-objective algorithms produce one solution per run (sol_1).

## Excel File Format

Each `.xlsx` file contains 2 rows:

**Row 1 (Header):**
```
Makespan | Avg Waiting Time | Avg Execution Time | Avg Finish Time | Energy Use Wh | Avg VM Utilization % | Avg Host Utilization% | Avg Host IDLE Time (s)
```

**Row 2 (Values):**
Corresponding metric values from the simulation run.

### Key Metrics (Optimization Objectives)

| Column | Objective? | Description |
|--------|------------|-------------|
| Makespan | Yes | Total completion time (minimize) |
| Avg Waiting Time | Yes | Average task wait time (minimize) |
| Energy Use Wh | Yes | Total energy consumption (minimize) |
| Avg Execution Time | No | Average task execution time |
| Avg Finish Time | No | Average task finish time |
| Avg VM Utilization % | No | Average VM utilization percentage |
| Avg Host Utilization% | No | Average host utilization percentage |
| Avg Host IDLE Time (s) | No | Average host idle time |

## Using Experimental Data with Performance Metrics

The multi-objective algorithm results can be evaluated using the performance metrics library:

1. Extract the 3 key objectives from Excel files
2. Convert to the required input format (semicolon-delimited text)
3. Compare Pareto fronts from different algorithms using HV, IGD, Spacing, and C-Metric

**Example comparison:**
- Compare NSGA-II vs SPEA-II Pareto fronts for 1200 tasks
- Evaluate which algorithm produces better convergence (IGD)
- Evaluate which algorithm produces better diversity (Spacing)
- Calculate coverage metric to see dominance relationships

---

## References

- Zitzler, E., & Thiele, L. (1999). Multiobjective evolutionary algorithms: a comparative case study and the strength Pareto approach. IEEE Transactions on Evolutionary Computation.
- Deb, K. (2001). Multi-objective optimization using evolutionary algorithms. John Wiley & Sons.
