# A more formal language description of Trefoil v3

Start by reading the informal description in the README if you haven't already.

The definition below describes the syntax and semantics of Trefoil v3 programs.
*Italicized words* are technical terms being defined.

Many of the concepts are the same as Trefoil v2. We do not repeat them here.
Instead, we describe only the changes and new features.

## Syntax

The syntax has four levels: characters, tokens, parenthesized symbol trees, and
abstract syntax trees. Only the last level changes compared to Trefoil v2.

### Abstract Syntax Tree level

A *program* consists of a sequence of bindings.

A *binding* is one of the following:
- Any of the binding forms available in Trefoil v2.
- *Struct binding*: a node with head `struct` with at least one argument. All
  the arguments must be symbols. The first argument is called the *struct name*.
  The rest of the arguments (possibly none) are called the *field names*. The
  field names must be distinct (but it *is* allowed to have a field with the
  same name as the struct name(!)).
  - Example with two fields: `(struct my-record name age)`
  - Example with zero fields `(struct my-empty-record)`

An *expression* is one of the following:
- Any of the expression forms available in Trefoil v2.
- *Trefoil-symbol literal*: (Since the word "symbol" overlaps between PSTs and
  Trefoil expressions here, we will say "PST-symbol" and "Trefoil-symbol" to
  distinguish them.) The syntax of a Trefoil-symbol literal is a PST-symbol that
  starts with an apostrophe.
  - Example: `'hello-world`
- *Cond expression*: a node with head `cond` and any number of arguments, each
  of which is a cond clause.
  - A *cond clause* is a node with exactly two children, both of which are expressions.
  - Example: The body of this function is a `cond` expression with two clauses.
```
(define (sum l)
  (cond
    ((nil? l) 0)
    (true (+ (car l) (sum (cdr l))))))
```
- *Match expression*: a node with head `match` and at least one argument.
  - The first argument is an expression.
  - Any remaining arguments are match clauses.
  - A *match clause* is a node with exactly two children.
    - The first child is a pattern.
      - If the pattern contains reuses variable patterns with the same name more
        than once, it is an abstract syntax error.
    - The second child is an expression.
  - Example: The body of this function is a `match` expression with two clauses.
```
(define (sum l)
  (match l
    (nil 0)
    ((cons x xs) (+ x (sum xs)))))
```


There are also three "internal" expressions used by the interpreter to implement
structs. Trefoil users cannot write these expressions directly, so they have no
concrete syntax as PSTs. But they do have purely abstract syntax as ASTs. So we
describe them in terms of what fields their AST nodes have.
- *Struct constructor expression*: represents a value built with a struct constructor.
  The AST node has two fields.
  - A (OCaml) string (the name of the struct)
  - A (OCaml) list of Trefoil v3 expression ASTs (the arguments to the constructor)
  - Since the expression has no concrete syntax, but sometimes we need to refer
    to it in this document, we will write it as `StructConstructor(s, es)` where `s`
    stands for any string and `es` stand for any list of expression ASTs.
- *Struct predicate expression*: represents a predicate that checks whether a
  value was built by a struct's constructor. The AST node has two fields.
  - A (OCaml) string (the name of the struct)
  - A Trefoil v3 expression AST (the expression that will evaluate to the value
    the programmer wants to examine)
  - Since the expression has no concrete syntax, but sometimes we need to refer
    to it in this document, we will write it as `StructPredicate(s, e)` where
    `s` stands for any string and `e` stands for any expression AST.
- *Struct access expression*: represents a field access to a struct value.
  The AST node has three fields.
  - A (OCaml) string (the name of the struct)
  - A (OCaml) integer (the index of the field to access)
  - A Trefoil v3 expression AST (the expression that will evaluate to the struct
    value whose fields the programmer wishes to access)
  - Since the expression has no concrete syntax, but sometimes we need to refer
    to it in this document, we will write it as `StructAccess(s, i, e)` where
    `s` stands for any string, `i` stands for any (OCaml) integer, and `e`
    stands for any expression AST.

A *pattern* is one of the following:
- *Wildcard pattern*: the PST-symbol `_` (the underscore character)
- *Variable pattern*: a PST-symbol that is **not** any of the keywords used as
  stand-alone symbols anywhere in this section, nor `_`, nor any symbol that
  starts with apostrophe.
- *Integer literal pattern*: a symbol consisting of an optional minus sign
  followed (without space) by a nonempty sequence of digits
- *Boolean literal pattern*: the symbol `true` or the symbol `false`
- *Nil literal pattern*: The symbol `nil`
- *Trefoil-symbol pattern*: a PST-symbol that starts with an apostrophe
- *Cons pattern*: a node with head `cons` and exactly two arguments, each of
  which is a pattern.
- *Struct pattern*: a node with a head that is **not** any of the keywords used
  as the head of any node anywhere in this section, nor `_`, nor any symbol that
  starts with an apostrophe. The node can have any number of arguments after the
  head, each of which is a pattern.

A *value* is an expression that satisfies one of the following additional constraints:
- The constraints of any Trefoil v2 value.
- It is a Trefoil-symbol expression.
- It is a struct constructor expression **all of whose subexpressions (children) are values**.

List of symbol keywords (cannot be used as variable names)
- `true`, `false`, `nil`, `_`, and any symbol that starts with an apostrophe

List of node head keywords (cannot be used as function names)
- `test`, `define`, `+`, `-`, `*`, `=`, `if`, `let`, `cons`, `nil?`, `cons?`,
  `car`, `cdr`, `cond`, `match`, `struct`, `_`, and any symbol that starts with
  an apostrophe

## Semantics

The meaning of a Trefoil v3 program is the same as in Trefoil v2. We only need
to describe the semantics of the new binding and expression forms, some of which
will depend on a new semantics for patterns.

### The dynamic environment

There is one new kind of *entry*:
- a *struct entry* which is just a struct binding

### Semantics of bindings

We extend the semantics for the new kinds of bindings.
- Consider a struct binding `(struct s fs)` where `s` is the name of the struct
  and `fs` stands for a possibly empty sequence of symbols representing the
  field names. The semantics is to extend the current dynamic environment with
  the following mappings:
  - `s` maps to the entire struct binding
  - `s?` maps to a function defined as if by the binding `(define (s? x) StructPredicate(s, x))`
  - for each field `f` in `fs`, `s-f` maps to a function defined as if by the
    binding `(define (s-f x) StructAccess(s, i, x))` where `i` is the index of
    the field `f` in the sequence `fs`.
  - (Side note: the functions described in the previous bullet points have
    bodies which **cannot be written** by the Trefoil programmer, because there
    is (intentionally) no concrete syntax for `StructPredicate` and
    `StructAccess`. We have used a semi-concrete syntax to describe what AST the
    interpreter should construct for these function bodies.)

### Semantics of expressions

We **change** the semantics of the `=` operator so that it works on all values.
- Consider an expression `(= e1 e2)`, where `e1` and `e2` stand for **any**
  expressions. The semantics is to evaluate `e1` to a value in the current
  dynamic environment. Call that value `v1`. Then evaluate `e2` to a value in
  the current dynamic environment. Call that value `v2`. The result is a boolean
  literal indicating whether `v1` and `v2` are structurally equal.

We **change** the semantics of function calls so that it works to "call" a
struct's name (which really calls its constructor).
- We begin as in Trefoil v2. Consider a function call expression `(f args)`,
  where `f` stands for **any** function name, and `args` stands for **any**
  sequence (possibly empty) of expressions to be passed as arguments to `f`.
  Call the current dynamic environment `callenv`. The semantics is to first
  lookup `f` in `callenv`. If `f` maps to a function entry, proceed as in
  Trefoil v2. If `f` maps to anything besides a function entry **or a struct
  entry**, signal an error. Otherwise, if `f` maps to a struct entry (whose name
  is the same as `f`), evaluate each element of `args` in left-to-right order in
  `callenv`. Call the resulting sequence of values `vals`. Return
  `StructConstructor(f, vals)`.

We also extend the semantics for the new kinds of user-facing expressions.
- Trefoil-symbol literals evaluate to themselves.
- Consider a cond expression `(cond clauses)` where `clauses` stands for **any**
  sequence (possibly empty) of cond clauses. Each cond clause is of the form
  `(pi bi)` where `pi` and `bi` stand for arbitrary expressions. The semantics
  is to iterate over the clauses in order. For each clause `(pi bi)`, evaluate
  `pi` in the current dynamic environment to a value `vi`. If `vi` is `false`,
  continue the loop. Otherwise, if `vi` is **anything whatsoever besides
  `false`**, stop the loop and evaluate `bi` in the current dynamic environment
  to a value and return that. If the loop reaches the end of the sequence of
  clauses without finding any `pi` that evaluates to something besides `false`,
  then signal an error.
- Consider a match expression `(match e clauses)` where `e` stands for any
  expression and `clauses` stands for any sequence (possibly empty) of match
  clauses. Each match clause is of the form `(pi bi)` where `pi` **is a
  pattern** and `bi` is an expression. (Note how this is different from cond
  clauses, where both are expressions!!) The semantics is to first evaluate `e`
  in the current dynamic environment to a value `v`. Then iterate over the
  clauses in order. For each clause `(pi bi)`, ask `pi` whether it matches `v`.
  If `pi` answers "no", continue the loop. If `pi` answers "yes, and `B`", then
  stop the loop and evaluate `bi` in the current dynamic environment
  **extended** by all the bindings in `B`. Return the value the `bi` evaluates
  to. If the loop gets to the end of the sequence of clauses without finding any
  `pi` that matches `v`, then signal an error.

Even though the three "internal" expressions cannot be written directly by the
Trefoil programmer, they *can* be created by the new kind of function call as
well as the struct accessor and struct predicate functions introduced by struct
bindings, so we need to give semantics to these "internal" expressions as well:
- Consider a struct constructor expression `StructConstructor(s, es)`, where `s`
  stands for any string and `es` stand for any list of expression ASTs. Evaluate
  the expressions in `es` in the current dynamic environment to a sequence of
  values `vs`. Return `StructConstructor(s, vs)`.
- Consider a struct predicate expression `StructPredicate(s, e)` where `s`
  stands for any string and `e` stands for any expression AST. Evaluate `e` in
  the current dynamic environment to a value `v`. If `v` is of the form
  `StructConstructor(s', vs)` and `s` and `s'` are equal as strings, return
  `true`. In all other cases, return `false`.
- Consider a struct access expression `StructAccess(s, i, e)` where `s` stands
  for any string, `i` stands for any (OCaml) integer, and `e` stands for any
  expression AST. Evaluate `e` in the current dynamic environment to a value
  `v`. If `v` is of the form `StructConstructor(s', vs)` and `s` and `s'` are
  equal as strings and `i` is less than the length of `vs`, then return the
  `i`th element of `vs`. In all other cases, signal an error.

### Structural equality

Let `v1` and `v2` be two values. We say that `v1` is *structurally equal* to
`v2` if any of the following are true.
- `v1` and `v2` are both integer literals and they are equal as integers
- `v1` and `v2` are both `true`
- `v1` and `v2` are both `false`
- `v1` and `v2` are both `nil`
- `v1` and `v2` are both Trefoil-symbol values, and the symbols are equal as strings
- `v1` and `v2` are both `cons` values, say `v1` is `(cons v11 v12)` and `v2` is
  `(cons v21 v22)`, and `v11` is structurally equal to `v21` and `v12` is
  structurally equal to `v22`.
  - (Side note 1: by definition of what it means for a `cons` expression to be a
    value, `v11`, `v12`, `v21`, and `v22` are all guaranteed to be values.)
  - (Side note 2: the definition of structural equality is recursive!)
- `v1` and `v2` are both struct constructor values, say `v1` is
  `StructConstructor(s1, vs1)` and `v2` is `StructConstructor(s2, vs2)`, and
  `s1` and `s2` are equal as strings, and `vs1` and `vs2` have the same length
  as lists, and each element of `vs1` is structurally equal to the corresponding
  element of `vs2`.
  - (Side note: by definition of what it means for a struct constructor
    expression to be a value, all the elements of `vs1` and `vs2` are values.)

### Semantics of patterns

The semantics of a pattern takes a value `v` as an argument and returns one of
the following:
- "no", indicating that the pattern does not match `v`
- "yes and `B`" (where `B` is a dynamic environment), indicating that the
  pattern matches `v` and introduces bindings `B`

Given `v`, we describe the semantics of each kind of pattern.
- A wildcard pattern always answers "yes and `[]`" (the
  empty dynamic environment)
- A variable pattern `x` always answers "yes and `[(x -> v)]`" (the dynamic
  environment that maps `x` to `v` and has no other mappings)
- An integer literal pattern `n`, where `n` stands for any integer, examines `v`:
  - If `v` is an integer literal with same value as `n`, answer "yes and `[]`"
  - Otherwise, answer "no"
- A boolean literal pattern `b`, where `b` stands for any boolean, examines `v`:
  - If `v` is a boolean literal with the same value as `b`, answer "yes and `[]`"
  - Otherwise, answer "no"
- A nil literal pattern examines `v`:
  - If `v` is a nil literal, answer "yes and `[]`"
  - Otherwise, answer "no"
- A Trefoil-symbol pattern `s`, where `s` stands for any symbol-string, examines `v`:
  - If `v` is a Trefoil-symbol literal with the same value as `s`, answer "yes and `[]`"
  - Otherwise, answer "no"
- A cons pattern `(cons p1 p2)`, where `p1` and `p2` stand for any patterns, examines `v`:
  - If `v` is a cons expression, it can be written as `(cons v1 v2)` where `v1`
    and `v2` are values. Ask `p1` if it matches `v1`
    - If "no", then answer "no".
    - If "yes and `B1`", ask `p2` if it matches `v2`
      - If "no", then answer "no".
      - If "yes and `B2`", answer "yes and `B1 @ B2`", where `@` denotes
        appending the two dynamic environments together, as in OCaml
  - Otherwise, if `v` is not a `cons` expression, answer "no".
- A struct pattern `(s ps)`, where `s` stands for any struct name PST-symbol and
  `ps` stands for a (possibly empty) sequence of patterns, examines `v`
  - If `v` is a struct constructor expression, it can be written `StructConstructor(s', vs)`,
    where `s'` is a struct name PST-symbol and `vs` is a sequence of values.
    - If `s` and `s'` are not equal as strings, answer "no".
    - If `ps` and `vs` are sequences of different lengths, answer "no".
    - Otherwise, iterate through `ps` and `vs` considering the i-th element of
      each sequence. Call these elements `pi` and `vi` respectively. At each
      iteration of the loop, ask `pi` if it matches `vi`.
      - If "no", answer "no"
      - If "yes and `Bi`", continue the loop.
    - If you reach the end of the iteration, then all of the `pi` match `vi`.
      Collect all the `Bi` from each iteration of the loop and concatenate them
      all together. Call this concatenated environment `B`. Answer "yes and `B`".
  - Otherwise, if `v` is not a struct constructor expression, answer "no".
