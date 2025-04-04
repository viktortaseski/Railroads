# Railroads: An Evolutionary Approach to Problem Solving

Railroads is a 2D game project where players construct an optimized railway network to guide trains to their destinations. The project demonstrates the use of genetic algorithms in sequential, parallel, and distributed environments to optimize railway map layouts.
Please read the Report_Railroads.pdf before running.

## Features

- **Genetic Algorithm**:  
  Uses population initialization, selection, crossover, and mutation to evolve optimal solutions.

- **Multi-threaded Evaluation**:  
  Leverages Java's `ExecutorService` to evaluate candidate solutions concurrently.

- **Distributed Processing**:  
  Employs MPJ Express (MPI for Java) to distribute workload across multiple nodes.

- **Visualization & Logging**:  
  Provides real-time insights into the evolution process.

## Prerequisites

- Java (JDK 8 or higher)
- MPI (e.g., MPJ Express)
- Git (to clone the repository)

## Installation

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/viktortaseski/Railroads.git
