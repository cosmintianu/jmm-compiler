# Compiler Project

Java-- is a simplified subset of the Java programming language, crafted for educational purposes and compiler construction courses. This project guides students through all major phases of compiler development, including lexical and syntactic analysis using ANTLR and custom grammar rules, semantic analysis with symbol table construction and type checking, intermediate code generation to OLLIR (Object-Oriented Low-Level Intermediate Representation), and a range of optimizations such as constant propagation, constant folding, and register allocation. The compiler’s backend generates Jasmin assembly, which is then assembled into JVM bytecode, supporting features like class fields, methods, control flow (if, while), arrays, and varargs. The resulting bytecode runs on any standard Java Virtual Machine, making this project ideal for learning about programming language design, compiler architecture, and JVM internals

## Work Distribution

* Amanda - 40%
* Cosmin - 30%
* Miguel - 30%

## Compiler Optimizations

This compiler includes several optimizations that improve the efficiency of the generated code at different stages of
compilation. These optimizations are applied either at the AST level or during OLLIR generation and translation.

### 1. Register Allocation (`-r=<n>`)

The compiler supports register allocation using a graph coloring algorithm and data-flow analysis. The register
allocation strategy depends on the value of the `-r` option:

- `-r=-1` (default): Uses the same number of variables as in the original OLLIR code.
- `-r=0`: Allocates as few local variables as possible, minimizing the JVM register usage.
- `-r=<n>`, where `n ≥ 1`: Limits the JVM local variables to `n`. If this is not enough, the compiler aborts and
  reports the minimum required.

### 2. Constant Propagation (`-o`)

When optimization is enabled with `-o`, the compiler performs **constant propagation**, replacing variables that are
assigned constant values with those constants wherever possible.

**Example (Before):**

```java
a =10;
b =a +5;
```

**Example (After):**

```java
a =10
b =10+5 
```

### 3. Constant Folding (`-o`)

When the `-o` optimization flag is enabled, the compiler also applies **constant folding** to simplify expressions with
constant operands at compile time. This is performed during AST traversal, before generating OLLIR code.

**What It Handles:**

- Arithmetic operations on constants (e.g., `+`, `-`, `*`, `/`)
- Boolean logic simplification (e.g., `true && false` becomes `false`)
- Comparisons with known values (e.g., `5 < 10` becomes `true`)

**Example (Before Folding):**

```java
int a = 10 + 5
```

**Example (After Folding):**

```java
int a = 15
```




