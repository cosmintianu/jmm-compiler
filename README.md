# JMM Compiler — Java-- to JVM Bytecode

A complete compiler for the **Java-- (JMM)** programming language, developed as part of the Compilers course at FEUP (Faculty of Engineering, University of Porto). This compiler transforms JMM source code through multiple compilation stages, ultimately generating executable JVM bytecode via Jasmin assembly.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Project Structure](#project-structure)
- [Language Specification](#language-specification)
- [Compilation Pipeline](#compilation-pipeline)
- [Usage](#usage)
- [Compiler Options](#compiler-options)
- [Optimizations](#optimizations)
- [Building from Source](#building-from-source)
- [Testing](#testing)
- [Team](#team)
- [Technologies Used](#technologies-and-tools)

---

## Overview

Java-- (JMM) is a simplified subset of Java designed for educational purposes. This compiler implements all major phases of a modern compiler:

1. **Lexical & Syntactic Analysis** — ANTLR4-based parsing
2. **Semantic Analysis** — Type checking, symbol table construction, and error reporting
3. **Intermediate Representation** — OLLIR (OO-based Low-Level Intermediate Representation)
4. **Code Generation** — Jasmin assembly output compatible with the JVM

The compiler supports class inheritance, method overloading, arrays, control flow statements, and seamlessly integrates with existing Java libraries.

---

## Features

- ✅ Full JMM language support (classes, methods, arrays, inheritance)
- ✅ Comprehensive semantic analysis with meaningful error messages
- ✅ OLLIR intermediate representation generation
- ✅ JVM bytecode generation via Jasmin
- ✅ Register allocation with graph coloring algorithm
- ✅ Constant propagation and constant folding optimizations
- ✅ Support for importing and using external Java classes
- ✅ Varargs support for method parameters
- ✅ Array literals and dynamic array initialization

---

## Project Structure

```
jmm-compiler/
├── src/
│   └── main/
│       ├── antlr/comp2025/grammar/     # ANTLR4 grammar (Javamm.g4)
│       └── pt/up/fe/comp2025/
│           ├── analysis/               # Semantic analysis passes
│           │   └── passes/             # Individual semantic checks
│           ├── ast/                    # AST utilities and node types
│           ├── backend/                # Jasmin code generation
│           ├── optimization/           # OLLIR generation & optimizations
│           ├── parser/                 # Parser implementation
│           ├── symboltable/            # Symbol table construction
│           └── Launcher.java           # Main entry point
├── test/                               # Unit and integration tests
├── inputs/                             # Sample JMM source files
├── libs/                               # External dependencies
├── libs-jmm/                           # Compiled Java libraries for JMM
├── build.gradle                        # Gradle build configuration
└── README.md
```

---

## Language Specification

### Supported Constructs

| Feature | Syntax Example |
|---------|----------------|
| Class declaration | `class MyClass { ... }` |
| Inheritance | `class Child extends Parent { ... }` |
| Imports | `import io;` or `import foo.bar.Baz;` |
| Instance variables | `int x; boolean flag;` |
| Methods | `public int compute(int a, int b) { ... }` |
| Static main | `public static void main(String[] args) { ... }` |
| Arrays | `int[] arr; arr = new int[10];` |
| Array literals | `int[] arr; arr = [1, 2, 3, 4, 5];` |
| Control flow | `if (cond) { ... } else { ... }` |
| Loops | `while (cond) { ... }` |
| Object instantiation | `MyClass obj; obj = new MyClass();` |
| Method calls | `obj.method(arg1, arg2);` |
| Arithmetic | `+`, `-`, `*`, `/` |
| Comparison | `<`, `>` |
| Logical | `&&`, `||`, `!` |
| Varargs | `public int sum(int... numbers) { ... }` |

### Example Program

```java
import io;

class Factorial {
    public static void main(String[] args) {
        Factorial f;
        f = new Factorial();
        io.println(f.compute(10));
    }

    public int compute(int n) {
        int result;
        if (n < 2) {
            result = 1;
        } else {
            result = n * this.compute(n - 1);
        }
        return result;
    }
}
```

---

## Compilation Pipeline

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  JMM Source     │────▶│  Lexer/Parser    │────▶│  AST            │
│  (.jmm file)    │     │  (ANTLR4)        │     │                 │
└─────────────────┘     └──────────────────┘     └────────┬────────┘
                                                          │
                                                          ▼
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Symbol Table   │◀────│  Semantic        │◀────│  AST Traversal  │
│                 │     │  Analysis        │     │                 │
└─────────────────┘     └──────────────────┘     └────────┬────────┘
                                                          │
                                                          ▼
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  JVM Bytecode   │◀────│  Jasmin          │◀────│  OLLIR          │
│  (.class file)  │     │  Generator       │     │  (IR Code)      │
└─────────────────┘     └──────────────────┘     └─────────────────┘
```

### Stage Details

1. **Parsing**: The ANTLR4-generated lexer and parser process the source code and construct an Abstract Syntax Tree (AST).

2. **Semantic Analysis**: Multiple analysis passes verify:
   - Variable declarations before use
   - Type compatibility in expressions and assignments
   - Method signature matching
   - Array access validity
   - Return type correctness
   - Import resolution

3. **OLLIR Generation**: The AST is transformed into OLLIR, a stack-based intermediate representation that simplifies code generation.

4. **Jasmin Code Generation**: OLLIR instructions are translated to Jasmin assembly, which is then assembled into JVM bytecode.

---

## Usage

### Running the Compiler

```bash
# Using the wrapper script (Windows)
.\jmm.bat -i=<input_file.jmm> [options]

# Using the wrapper script (Unix/Linux)
./jmm -i=<input_file.jmm> [options]

# Using Gradle directly
./gradlew run --args="-i=inputs/MyProgram.jmm"
```

### Example

```bash
# Compile a JMM file with optimizations enabled
.\jmm.bat -i=inputs/Lazysort.jmm -o

# Compile with limited register allocation
.\jmm.bat -i=inputs/Factorial.jmm -r=5
```

---

## Compiler Options

| Option | Description |
|--------|-------------|
| `-i=<file>` | **(Required)** Path to the input JMM source file |
| `-o` | Enable optimizations (constant propagation, constant folding) |
| `-r=<n>` | Register allocation mode (see below) |
| `-d` | Enable debug output |

### Register Allocation Modes (`-r`)

| Value | Behavior |
|-------|----------|
| `-r=-1` | Default: use original variable count from OLLIR |
| `-r=0` | Minimize local variable usage (optimal allocation) |
| `-r=<n>` | Limit to `n` local variables; aborts if insufficient |

---

## Optimizations

This compiler implements several optimizations to improve the efficiency of the generated bytecode.

### 1. Register Allocation (`-r=<n>`)

Uses a **graph coloring algorithm** with **liveness analysis** to allocate JVM local variables efficiently. The algorithm:

- Builds an interference graph based on live variable ranges
- Colors the graph to minimize register usage
- Handles spilling when the constraint cannot be satisfied

### 2. Constant Propagation (`-o`)

Replaces variables holding constant values with their actual constants throughout the code.

**Before:**
```java
int a;
int b;
a = 10;
b = a + 5;
```

**After:**
```java
int a;
int b;
a = 10;
b = 10 + 5;
```

### 3. Constant Folding (`-o`)

Evaluates constant expressions at compile time, reducing runtime computation.

**Supported Operations:**
- Arithmetic: `10 + 5` → `15`
- Boolean logic: `true && false` → `false`
- Comparisons: `5 < 10` → `true`

**Before:**
```java
int result;
result = 10 + 5 * 2;
```

**After:**
```java
int result;
result = 20;
```

---

## Building from Source

### Prerequisites

- **Java 21** or later
- **Gradle** (wrapper included)

### Build Commands

```bash
# Build the project
./gradlew build

# Create distribution archives
./gradlew distZip
./gradlew distTar

# Run tests
./gradlew test

# Clean build artifacts
./gradlew clean
```

### Generated Artifacts

After building, the compiled JAR and launcher scripts can be found in:
- `build/distributions/` — Distribution archives
- `build/libs/` — Compiled JAR files
- `build/scripts/` — Launcher scripts

---

## Testing

The project includes comprehensive test suites covering all compiler phases:

```bash
# Run all tests
./gradlew test

# View test reports
# Open: build/reports/tests/test/index.html
```

### Test Categories

| Test Suite | Description |
|------------|-------------|
| `GrammarTest` | Parser and lexer validation |
| `SymbolTableTest` | Symbol table construction |
| `SemanticAnalysisTest` | Type checking and semantic rules |
| `OllirTest` | OLLIR code generation |
| `JasminTest` | Bytecode generation correctness |
| `OptimizationsTest` | Optimization pass verification |

---

## Team

| Member | Contribution |
|--------|--------------|
| **Amanda** | 40% |
| **Cosmin** | 30% |
| **Miguel** | 30% |

---

## Technologies and Tools

| Technology | Description | Link |
|------------|-------------|------|
| **Java 21** | Primary implementation language | [OpenJDK](https://openjdk.org/) |
| **ANTLR4** | Parser generator for lexical and syntactic analysis | [antlr.org](https://www.antlr.org/) |
| **Jasmin** | Assembler for JVM bytecode generation | [jasmin.sourceforge.net](http://jasmin.sourceforge.net/) |
| **OLLIR** | OO-based Low-Level Intermediate Representation | Course-specific IR |
| **Gradle** | Build automation and dependency management | [gradle.org](https://gradle.org/) |
| **JUnit** | Unit testing framework | [junit.org](https://junit.org/) |

---

## Disclaimer

This project was developed for **educational purposes only** as part of the Compilers course (COMP 2024/2025) at the Faculty of Engineering, University of Porto ([FEUP](https://sigarra.up.pt/feup/)). 

This repository is intended for learning and academic reference. Please respect academic integrity policies if you are a student taking the same or a similar course.

---

## Acknowledgments

- Course instructors and teaching assistants at FEUP
- [ANTLR4](https://www.antlr.org/) — Powerful parser generator
- [Jasmin](http://jasmin.sourceforge.net/) — JVM bytecode assembler
- [Gradle](https://gradle.org/) — Build automation tool

---




