# Trefoil-Language-Interpreters

A multi-stage family of interpreters for **Trefoil**, a small but expressive instructional programming language.  
The project traces the evolution of Trefoil from a stack-based calculator into a lexically scoped functional language with structs and pattern matching, implemented across **Java** and **OCaml**.

---

## 🌟 High-Level Overview

Trefoil is developed in three major versions:

- **Trefoil v1 – Stack-based expression language (Java)**
  - Minimalist, reverse-Polish-style arithmetic with an integer stack
  - Custom interpreter with explicit stack manipulation and error handling

- **Trefoil v2 – Lexically scoped expression language (Java)**
  - Parenthesized symbol tree (PST) syntax, similar to S-expressions
  - Rich expression language with variables, conditionals, let, pairs, tests, and higher-order functions
  - Full interpreter built on a clean AST and dynamic environment

- **Trefoil v3 – Structs & pattern matching (OCaml)**
  - Port of Trefoil v2 semantics into OCaml
  - Extended with algebraic data types via `struct` declarations
  - Pattern matching and `match` expressions over custom structs and lists
  - Structural equality, symbols, and `cond` expressions

Together, these components showcase language design, interpreter implementation, static and dynamic semantics, and cross-language porting (Java → OCaml).

---

## 🔧 Trefoil v1 – Stack-Based Interpreter (Java)

**Concept:**  
Trefoil v1 is an integer stack language resembling a programmable calculator.

**Core semantics:**

- A program is a **space-separated sequence** of tokens (integer literals and operators)
- Evaluation proceeds **left-to-right** with an internal **integer stack**
- Integer literal → **pushed** onto the stack  
- Supported operators:
  - `+`, `-`, `*`  
    - Pop the top two integers  
    - Apply the arithmetic operator  
    - Push the result back  
    - For `-`, the semantics are: `second_from_top - top_of_stack`
  - `.` (dot)
    - Pop the top of the stack
    - Print it

**Features:**

- Graceful handling of **stack underflow** (insufficient operands)
- A `pop()` operation and a structured `toString()` representation of the current stack state
- Command-line runner that:
  - Reads from **stdin** or from a file
  - Prints the final stack after evaluating a program

**Testing:**

- JUnit-based tests for:
  - Normal arithmetic behavior
  - Correct handling of subtraction order
  - Underflow and malformed input
  - Comment handling and unknown token errors

---

## 🧠 Trefoil v2 – Functional Core with PSTs (Java)

Trefoil v2 extends the language into a much richer **lexically scoped expression language** with a tree-based syntax.

### Syntax & Representation

- Programs are sequences of **bindings** and top-level expressions evaluated in order
- Source syntax is based on **parenthesized symbol trees (PSTs)**:
  - Internally very similar to S-expressions
  - Parsed via a tokenizer and tree parser
- PSTs are transformed into a well-typed **AST**, with explicit node types for each expression and binding form

### Bindings

The dynamic environment is built up by evaluating bindings:

- **Variable bindings**
  - Associate names with evaluated values
- **Top-level expressions**
  - Evaluated for side effects / final value; update the environment where needed
- **Test bindings**
  - `(test <expr>)`
  - Evaluate the expression; if the result is not `true`, a structured error is raised
- **Function bindings**
  - Named functions with parameters and bodies
  - Capture their defining environment (lexical scope)

### Expressions

Trefoil v2 includes a wide range of expression forms (non-exhaustive):

- **Integer literals** & **boolean literals** (`true`, `false`)
- **Arithmetic operators**: `+`, `-`, `*`
- **Equality** on integers: `(= e1 e2)` returns a boolean
- **Conditionals**: `(if cond then-expr else-expr)`
- **Variables** and variable lookups
- **Let bindings**: `(let ((x e1) (y e2) ...) body)`
- **Pairs & lists-style constructs**:
  - `nil`, `cons`, `nil?`, `cons?`, `car`, `cdr`
- **Function calls**:
  - Call user-defined functions with lexical scoping
  - Environment is extended with parameter bindings before interpreting the body

### Interpreter Architecture (Java)

- **Tokenizer / Reader**:
  - `PeekCharReader`: buffered character reading with line tracking
  - `Tokenizer`: splits input into symbols, parentheses, and comments
  - `PSTParser`: builds the PST representation

- **AST & Parsing**:
  - `Expression` hierarchy: one subclass per expression kind
  - `Binding` hierarchy: variable bindings, test bindings, function declarations, etc.
  - `parsePST` functions convert PSTs into well-formed ASTs, rejecting malformed programs as **abstract syntax errors**

- **Dynamic Environment**:
  - `DynamicEnvironment` maps:
    - Variable names → values
    - Function names → (binding + defining environment)
  - Supports lexical scope and shadowing

- **Interpreter**:
  - `interpretExpression`: evaluates an expression in the context of an environment
  - `interpretBinding`: executes a binding and returns an updated environment
  - Distinguishes:
    - **Runtime errors** (type errors, invalid operations)
    - **Abstract syntax errors** (wrong arity, malformed constructs)

- Extensive unit tests cover:
  - Correct evaluation of each core feature
  - Error cases (wrong types, wrong argument counts)
  - Environment lookup rules
  - Let, tests, and function behavior

---

## 🧬 Trefoil v3 – Structs & Pattern Matching (OCaml)

Trefoil v3 takes the semantics of Trefoil v2 and re-implements them in **OCaml**, then extends the language with **struct types**, **pattern matching**, and richer syntax.

### Core Additions

1. **Port of Trefoil v2 to OCaml**
   - Expression and binding AST defined via OCaml variant types
   - Dynamic environment implemented as an association list mapping strings to entries
   - Interpreter for expressions and bindings, closely mirroring the Java semantics
   - Unit tests written in OCaml (using dune test framework)

2. **Structs**
   - New top-level binding form:
     - `(struct my-record name age)`  
       Defines:
       - A struct constructor `my-record`
       - A predicate function `my-record?`
       - Field accessors `(my-record-name ...)`, `(my-record-age ...)`
   - Internal expressions:
     - Struct constructor values
     - Struct field access expressions
     - Struct predicate expressions

3. **Symbols & Structural Equality**
   - Symbol literals: `'foo`, `'bar`
   - `=` extended to **structural equality on all values**, not just integers

4. **Conditionals with `cond`**
   - `(cond [test1 expr1] [test2 expr2] ... [else exprN])`
   - More ergonomic than nested `if` expressions

5. **Pattern Matching with `match`**
   - Powerful matching on:
     - Integers, booleans, nil
     - Symbols
     - Variables
     - List-style cons patterns
     - Struct patterns
   - Example:
     ```trefoil
     (match value
       [(my-cons x xs)  ...]
       [nil             ...]
       [else            ...])
     ```
   - Pattern semantics implemented via a separate pattern interpreter that:
     - Either fails (`None`) or
     - Succeeds and returns an environment of bound variables (`Some env`)

### OCaml Implementation Details

- **Modules & Files**:
  - `trefoil3.ml` – entry point and REPL-style loop (interactive and file-based)
  - `pstparser.ml` – PST parser (port of the Java PST parser)
  - `pst.ml` – PST type definition
  - `ast.ml` – AST for expressions, bindings, and patterns, plus parsers from PST
  - `interpreter.ml` – semantics for expressions, bindings, and patterns
  - `trefoil3test.ml` – unit tests for parsing and interpretation

- **Environment & Entries**:
  - `dynamic_env` is a list of `(string * entry)`
  - `entry` can represent:
    - Variables
    - Functions (with their defining environments)
    - Struct definitions

- **Build & Test**:
  - Managed with `dune`
  - Uses `ppx_deriving` for automatic AST boilerplate (to-string, equals, etc.)

---

## 🧪 Testing & Tooling

### Java Components

- Build & run (Java parts):

```bash
# From the Java root for Trefoil v1/v2
make          # or: make all
make run      # interactive interpreter
make test     # run JUnit tests