# A more formal language description of Trefoil v2

Start by reading the informal description in the README if you haven't already.

The definition below describes the syntax and semantics of Trefoil v2 programs.
*Italicized words* are technical terms being defined.

## Syntax

The syntax has four levels: characters, tokens, parenthesized symbol trees, and
abstract syntax trees. We only describe the last in detail, because characters,
tokens, and parenthesized symbol trees are handled for you by the starter code.

### Token level

The four *tokens* are `(`, `)`, comments, and symbols.

*Comments* begin with a `;` and continue to the end of the line.
Comments are removed at the token level and are not present in the higher level syntax.

*Symbols* are any nonempty sequence of characters except for whitespace,
parentheses, and semicolons.

Whitespace is ignored except for its purpose to separate adjacent symbols and to
terminate comments.

### Parenthesized Symbol Tree level

A *parenthesized symbol tree* (PST) is either a symbol or a node.

A *node* is a (possibly empty) parenthesized list of PSTs called the *children* of the node.

When a node has at least one child, and the first child is a symbol, then the
first child is called the *head*. If a node's first child is not a symbol, we do
not use the word head for it. So if we say "a node with head `define`", this
means that the node has at least one child, the first child is a symbol, and
that symbol is `define`.

When a node has a head, we call the rest of the children *arguments*.

### Abstract Syntax Tree level

A *program* consists of a sequence of bindings.

A *binding* is one of the following:
- *Variable binding*: a node with head `define` with exactly two arguments, the
  variable name (a symbol) and the variable definition (an expression)
  - Example: `(define x (+ 1 2))`
- *Top-level expression*: any expression (not necessarily a node!) is also
  allowed as a binding.
  - Example: `(+ 1 2)`
- *Function binding*: a node with head `define` with exactly two arguments.
  - The first argument is itself a non-empty node containing only symbols. The
    first symbol in this node is the function's name. The other symbols are the
    function's parameter names.
  - The second argument is an arbitrary expression representing the function's
    body. (Not necessarily a node!)
  - Example: `(define (f x y) (* y (+ x 2)))`
    - defines a function named `f` with parameters `x` and `y` and the given
      body expression
- *Test binding*: a node with head `test` with exactly one argument (an expression)

An *expression* is one of the following:
- *Integer literal*: a symbol consisting of an optional minus sign followed
  (without space) by a nonempty sequence of digits
  - Examples: `123`, `0`, `-42`, `-0`
- *Boolean literal*: the symbol `true` or the symbol `false`
- *Arithmetic operation expression*: a node with head `+`, `-`, `*`, or `=` and exactly two
  arguments, each of which is an expression.
  - Example: `(+ 1 (* 2 (= 3 4)))`
    - (This example is syntactically valid but semantically erroneous. See below.)
- *If expression*: a node with head `if` and exactly three arguments, each of
  which is an expression.
  - Example: `(if true 3 4)`
- *Let expression*: a node with head `let` and exactly two arguments
  - The first argument is a node with exactly one child.
    - That child is a node with exactly two children.
      - The first child is a symbol representing the variable name being bound.
      - The second child is an expression representing the variable's definition.
  - The second argument is an expression representing the "body" of the let
    expression (the scope in which the variable definition is available).
  - Example: `(let ((x 3)) (+ x 1))`
- *Nil literal*: The symbol `nil`
- *Cons expression*: a node with head `cons` and exactly two arguments, each of
  which is an expression.
  - Example: `(cons 0 1)`
- *List operation expression*: a node with head `nil?`, `cons?`, `car`, or `cdr`
  and exactly one argument, which is an expression.
  - Example: `(nil? 17)`, `(cons? nil)`, `(car true)`, `(cdr (cons 1 false))`, etc.
- A *variable reference expression*: a symbol that is **not** any of the
  keywords used as stand-alone symbols anywhere in this section.
- *Function call expression*: a node with a head that is **not** any of the
  keywords used as the head of any node anywhere in this section, and any number
  of arguments, each of which is an expression.
  - Example: `(f 0 true)`
    - Calls the function named `f` with the two arguments `0` and `true`

A *value* is an expression that satisfies one of the following additional constraints:
- It is an integer literal
- It is one of the expressions `true`, `false`, or `nil`
- It is a cons expression **both of whose arguments are values**

List of symbol keywords (cannot be used as variable names)
- `true`, `false`, `nil`

List of node head keywords (cannot be used as function names)
- `test`, `define`, `+`, `-`, `*`, `=`, `if`, `let`, `cons`, `nil?`, `cons?`, `car`, `cdr`

**Exercise**(0 points, just for fun): Explain how the starter code violates this
specification slightly by showing that you can define a function with a reserved
name. What happens when you try it in your solution? (You are not obligated to
fix this problem, but you can if you want.)

## Semantics

The meaning of a Trefoil v2 program is a *dynamic environment transformer*: the
program takes a dynamic environment as input, operates on that environment, and
returns a dynamic environment as a result. A program can also output messages to
the console, and it can also signal an error.

The meaning of a binding is also a dynamic environment transformer that can
output and fail.

Bindings contain expressions. The meaning of an expression is its *value* in the
current dynamic environment. Expressions do not return a new dynamic environment.

A program is a sequence of bindings. The meaning of a program is to process the
bindings in order and execute each one's transformer on the current dynamic
environment. If a binding or any contained expression signals an error, the
binding is ignored and processing continues with the next binding.

At the end of the sequence of bindings, the program outputs the final dynamic
environment.

Throughout the semantics, whenever Trefoil signals an error, it should do so
gracefully (no Java exception stack trace) and then exit with status 1.

### The dynamic environment

The dynamic environment maps strings to entries. An entry is either:
- a *variable entry*, which is just a value
- a *function entry*, which is a function binding and a dynamic environment
  (called the "defining environment")

The two operations on dynamic environments are lookup and extension. The
*lookup* operation looks up the name in the map and returns the corresponding
entry. The *extension* operation takes an old dynamic environment, a name, and
an entry, and returns a new dynamic environment that is the same as the old
except that the given name now maps to the given entry.

The way we describe the dynamic environment in this document, the extension
operation creates a completely separate copy of the old dynamic environment and
updates this new copy with the binding being added.

### Semantics of bindings

A binding takes the current dynamic environment as input. It will return a new
dynamic environment as its result.

- Consider a variable binding `(define x e)`, where `e` stands for **any**
  expression. The semantics is to evaluate `e` in the current dynamic
  environment. Call the resulting value `v`. Trefoil then outputs the string `"x
  = v"` and returns the new dynamic environment, which is the old dynamic
  environment extended with `x` maps to `v`.
- Consider a top-level expression `e`, where `e` stands for **any** expression.
  The semantics is to evaluate `e` in the current dynamic environment.
  Trefoil then outputs `v` and returns the old dynamic environment.
- Consider a function binding `(define (f params) e)`, where `e` stands for
  **any** expression and `params` stands for a possibly empty sequence of
  symbols representing the parameter names. Call the current dynamic environment
  `env`. If any symbol appears more than once in `params`, signal an error.
  Otherwise, return the `env` extended with `f` maps to the pair (`(define (f
  params) e)`, `env`). In other words, we capture the current dynamic
  environment `env` and store it together with the entire function binding for
  `f` in the entry.
- Consider a test binding `(test e)`, where `e` stands for **any** expression.
  The semantics is to evaluate `e` in the current dynamic environment to a value
  `v`. If `v` is true, nothing further happens and Trefoil returns the old
  dynamic environment. If `v` is **anything whatsoever besides `true`**, Trefoil
  signals an error.

### Semantics of expressions

An expression takes the current dynamic environment as input. The semantics
*evaluates* the expression to a value and returns that value.


- Integer literals, boolean literals, and the `nil` literal all evaluate to
  themselves.
- Consider an arithmetic operation `(+ e1 e2)`, where `e1` and `e2` stand for
  **any** expressions. The semantics is to evaluate `e1` to a value in the
  current dynamic environment. Call that value `v1`. Then evaluate `e2` to a
  value in the current dynamic environment. Call that value `v2`. If either `v1`
  or `v2` are anything other than integer literals, signal an error. Otherwise,
  the result is the sum of `v1` and `v2`.
- The semantics of `-` and `*` are the same as `+` but with "sum" replaced by
  "difference" and "product", respectively.
  - Clarifying example: `(- 3 1)` evaluates to `2`, not `-2`.
- Consider an arithmetic operation `(= e1 e2)`, where `e1` and `e2` stand for
  **any** expressions. The semantics is to evaluate `e1` to a value in the
  current dynamic environment. Call that value `v1`. Then evaluate `e2` to a
  value in the current dynamic environment. Call that value `v2`. If either `v1`
  or `v2` are anything other than integer literals, signal an error. Otherwise,
  the result is a boolean literal indicating whether the mathematical integers
  represented by `v1` and `v2` are equal.
- Consider an if expression `(if e1 e2 e3)`, where `e1`, `e2`, and `e3` stand
  for **any** expressions. The semantics is to `e1` to a value in the current
  dynamic environment. Call that value `v1`. If `v1` is `false`, then the
  semantics is to evaluate `e3` to a value and return that. Otherwise, if `v1`
  is **anything whatsoever besides `false`**, then the semantics is to evaluate
  `e2` to a value and return that.
  - Note that the design of what happens when given non-boolean data is the
    opposite of how it is handled in `(test ...)` bindings!
  - The semantics requires that only **one** of the two branches of the if
    expression be evaluated (in addition to `e1`, the "branch condition"). Do
    not evaluate both branches.
- Consider a let expression `(let ((x e1)) e2)`, where `e1` and `e2` stand for
  **any** expressions. The semantics is to first evaluate `e1` in the current
  dynamic environment to a value `v1`. Then evaluate `e2` in an environment
  consisting of the current dynamic environment **extended** by `x` maps to
  `v1`. Call the resulting value `v2`. Return `v2`.
  - Note that since the semantics of expressions does not return a new dynamic
    environment, the environment used to evaluate `e2` is discarded after `e2`
    is evaluated.
- Consider the operation `(cons e1 e2)`, where `e1` and `e2` stand for
  **any** expressions. The semantics is to evaluate `e1` to a value in the
  current dynamic environment. Call that value `v1`. Then evaluate `e2` to a
  value in the current dynamic environment. Call that value `v2`. Return `(cons
  v1 v2)`.
- Consider the operation `(nil? e)`, where `e` stands for **any** expression.
  The semantics is to evaluate `e` in the current dynamic environment. Call that
  value `v`. If `v` is `nil`, return `true`. Else return `false`.
- Consider the operation `(cons? e)`, where `e` stands for **any** expression.
  The semantics is to evaluate `e` in the current dynamic environment. Call that
  value `v`. If `v` is of the form `(cons v1 v2)`, for any `v1` and `v2`, return
  `true`. Else return `false`.
- Consider the operation `(car e)`, where `e` stands for **any** expression.
  The semantics is to evaluate `e` in the current dynamic environment. Call that
  value `v`. If `v` is of the form `(cons v1 v2)`, for any `v1` and `v2`, return
  `v1`. Otherwise, signal an error.
- Consider the operation `(cdr e)`, where `e` stands for **any** expression.
  The semantics is to evaluate `e` in the current dynamic environment. Call that
  value `v`. If `v` is of the form `(cons v1 v2)`, for any `v1` and `v2`, return
  `v2`. Otherwise, signal an error.
- Consider a variable reference expression `x` where `x` stands for **any**
  variable name. The semantics is to perform a lookup operation for `x` in the
  current dynamic environment. If `x` maps to a variable entry with value `v`,
  return `v`. Otherwise signal an error.
  - To clarify, if `x` maps to a function, signal an error.
  - To clarify, if `x` doesn't map to anything in the current environment,
    signal an error.
- Consider a function call expression `(f args)`, where `f` stands for **any**
  function name, and `args` stands for **any** sequence (possibly empty) of
  expressions to be passed as arguments to `f`. Call the current dynamic
  environment `callenv`. The semantics is to first lookup `f` in `callenv`. If
  `f` is not mapped to anything, or maps to anything besides a function entry,
  signal an error. From the function entry mapped to by `f`, let `(define (f
  params) body)` be `f`'s function binding and `defenv` its defining
  environment. If `args` and `params` are sequences of different lengths, signal
  an error. Next, evaluate each element of `args` in left-to-right order in
  `callenv`. This results in a sequence of values. Call that sequence `vals`.
  Then evaluate `body` in a new dynamic environment constructed by extending
  `defenv` with mappings `param -> val` for each `param` and corresponding value
  `val` in the lists `params` and `values`. Return the result of evaluating
  `body`.
  - This is the hardest part of the whole assignment. Do not be surprised if you
    have to read that paragraph 5 or 10 times.


**Exercise**(0 points, just for fun): Convince yourself that if the interpreter
returns normally (does not loop forever and does not signal an error), then it
returns a *value* in the technical sense defined in the Syntax section of this
document (in other words, an expression satisfying those additional
constraints).
