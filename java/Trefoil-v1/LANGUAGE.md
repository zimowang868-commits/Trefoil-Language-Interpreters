# A more formal language description of Trefoil v1

Start by reading the informal description in the README if you haven't already.

The definition below describes the syntax and semantic of Trefoil v1 programs.
"Syntax" describes how programs are written down, similar to how one would
describe "spelling and grammar" rules for a human language. "Semantics"
describes what programs mean, similar to how one would describe the meaning of
words, sentences, and paragraphs in a human language.

In the more formal description below, *italicized words* are technical terms being defined.

## Syntax

The syntax has three levels: characters, words, and tokens.

### Character level

At the character level, a Trefoil v1 program is a sequence of characters. For
now, you may assume that the characters are "reasonable" and representable as a
Java string.

### Word level

We use the word *sequence* to mean a (possibly empty) list. We say "non-empty
sequence" if we mean that the sequence will always have at least one element.

At the word level, the sequence of characters in a Trefoil v1 program is
split into a sequence of words separated by whitespace. Between each pair
of adjacent words, the amount of whitespace must be non-empty. Before the first
word and after the last word there can be a possibly-empty amount of whitespace.

For this week, you are free to interpret *whitespace* however is most convenient for you.
Just leaving Java's `Scanner` in its default configuration is fine.

A *word* consists of a non-empty sequence of non-whitespace characters.

### Token level

At the token level, each word in the program is checked to correspond to a token.

A *token* is either a comment, an integer literal, or an operator.

A *comment* is any word that starts with a semicolon.

For this week, you are free to interpret *integer literal* however is most
convenient for you. Just leaving Java's `Scanner` in its default configuration
and calling `hasNextInt`/`nextInt` is fine.

There are four *operators* in Trefoil v1: `+`, `-`, `*`, and `.` (a period).

If an input word is not a token, Trefoil should gracefully report an error to
the Trefoil programmer and exit with status 1. The error message can be anything
you think is useful (the more useful, the better!), but should *not* contain a
Java exception stack trace.

## Semantics

The meaning of a Trefoil v1 program is a *stack transformer*: the
program takes a stack as input, operates on that stack, and returns a stack as a
result. A program can also output integers, and it can also signal an error.

We introduce a notation for stack transformers. We write

```
Syntax           Semantics
f                ( a b -- c )
```

to mean that `f` is a stack transformer that expects at least two elements on
the stack, `a` and `b`, pops them, and pushes one element `c` on the stack.
(Here, `f` is not a real operation, but instead stands for any operation we
might want to describe. Below `f` will be replaced with something like `+` or
`.` or whatever.) In other words, everything before the `--` is the stack before
`f` is evaluated, and everything after the `--` is the stack after `f` is
evaluated. **The top of the stack is written to the right.** So in the example
above, `b` is on the top of the stack, and `a` is right below it on the stack.

This notation also brings up another important point about our stack
transformers: each transformer looks only at some number of elements on the top
of the stack; it does not care what is underneath it. So in our example of `f`
above, even if the stack had 10 elements, `f` would just look at the top two
(`a` under `b` on top) and transform them into one new element `c`. If there
were 10 elements before, `f` popped two and pushed one, so after there would be
9 elements. To emphasize, the notation does *not* mean that `f` would fail if
there were more than two elements on the stack, but rather means that `f`
ignores any additional elements and leaves them as is.

Remember that a program is a sequence of tokens.
The meaning of a program is to process the tokens in left-to-right order
and execute each one's stack transformer.
**At the end of the sequence, the program outputs the entire remaining stack.**

The meaning of an individual token is also a stack transformer that can output and fail.

Remember there are three kinds of tokens: comments, integer literals,
and operators.

A comment has no effect on the stack, performs no output, and never fails.
In our notation,

```
Syntax           Semantics
;comment         ( -- )
```

An integer literal pushes itself onto the stack, performs no output, and never fails.

```
Syntax           Semantics
n                ( -- n )
```

where `n` stands for any integer literal.

The operators `+`, `-`, and `*` each pop two elements off the input stack. If
the input stack contains fewer than two elements, these operations signal an error.
Otherwise, they compute the sum, difference, or product of the two popped elements,
and push the result back onto the stack. These operators perform no output.

```
Syntax           Semantics
+                ( a b -- a+b ) and signal error if <2 elements available
-                ( a b -- a-b ) and signal error if <2 elements available
*                ( a b -- a*b ) and signal error if <2 elements available
```

The operators `+` and `*` are commutative (it doesn't matter what order their
arguments are given in), but the operator `-` is not. Look carefully at the
notation giving the meaning for `-`. The subtraction happens in the same order
that we write the stack from left to right: bottom to top. So the *top* of the
stack is subtracted from the *second* element. Everybody messes this up at least
a few times, because it's very confusing! But eventually it will help to
remember that subtraction happen in the order written in the stack transformer
notation.

Finally, the operator `.` pops one element off of the input stack and prints it.

```
Syntax           Semantics
.                ( a -- ) and output a but signal error if <1 elements available
```

Throughout the semantics, whenever Trefoil signals an error, it should do so
gracefully (no Java exception stack trace) and then immediately exit with status 1.
