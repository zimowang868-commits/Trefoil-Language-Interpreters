open Trefoil3lib
open Errors

(* Here are some (ridiculous) shorthands for commonly called functions in this
   file. We apologize that the abbrevated names are so weird, but we follow a
   consisten convention with naming via acronymn, using the first letter of each
   word in the function name. So for example "ieab" below stands for
   "interpret_expression_after_bindings". We also use a trailing 0 to indicate
   "in the empty environment" rather than requiring an environment to be passed
   in. *)
let ie (dynenv, e) = Interpreter.interpret_expression (dynenv, e)
let ie0 e = ie ([], e)
let ib (dynenv, b) = Interpreter.interpret_binding (dynenv, b)
let ibs (dynenv, bs) = Interpreter.interpret_bindings (dynenv, bs)
let eos s = Ast.expr_of_string s
let bos s = Ast.binding_of_string s
let bsos s = Ast.bindings_of_string s
let ieab (dynenv, bindings, expr) =
  Interpreter.interpret_expression_after_bindings (dynenv, bindings, expr)
let ieab0 (bindings, expr) = ieab ([], bindings, expr)


(* tests similar to our unit tests in Java in HW3 *)
let%test _ = Ast.IntLit 3 = ie0 (eos "3")
let%test _ = Ast.IntLit (-10) = ie0 (eos "-10")
let%test _ = Ast.BoolLit true = ie0 (eos "true")

(* here's a parsing test. *)
let%test _ = Ast.BoolLit false = eos "false"

let%test _ = Ast.BoolLit false = ie0 (eos "false")

let xto3 = [("x", Interpreter.VariableEntry (Ast.IntLit 3))]

let%test _ =
  Ast.IntLit 3 = ie (xto3, (eos "x"))

(* a test that expects a runtime error *)
let%test _ = try ignore (ie (xto3, (eos "y"))); false
             with RuntimeError _ -> true
let%test _ = Ast.IntLit 3 = ie0 (eos "(+ 1 2)")

(* a test that expects an abstract syntax error *)
let%test _ = try ignore (ie0 (eos "(+ 1)")); false
             with AbstractSyntaxError _ -> true

let%test _ = try ignore (ie0 (eos "(+ 1 true)")); false
             with RuntimeError _ -> true

let%test _ = Ast.IntLit (-1) = ie0 (eos "(- 1 2)")
let%test _ = Ast.IntLit 6 = ie0 (eos "(* 2 3)")
let%test _ = Ast.BoolLit true = ie0 (eos "(= 3 (+ 1 2))")
let%test _ = Ast.BoolLit false = ie0 (eos "(= 4 (+ 1 2))")
let%test _ = Ast.BoolLit false = ie0 (eos "(= 4 true)")
let%test _ = Ast.IntLit 0 = ie0 (eos "(if true 0 1)")
let%test _ = Ast.IntLit 1 = ie0 (eos "(if false 0 1)")
let%test _ = Ast.IntLit 0 = ie0 (eos "(if true 0 x)")
let%test _ = Ast.IntLit 0 = ie0 (eos "(if 5 0 1)")


(* Here is a template for a parsing test for let expressions. *)
let%test _ =
  let parsed_let = eos "(let ((x 3)) (+ x 1))" in

  (* TODO: replace "Ast.Nil" on the next line with the correct AST for the
     expression above by calling your Let constructor. *)
  let manually_constructed_let = Ast.Let((Ast.Variable("x"), Ast.IntLit(3)), Ast.Plus(Ast.Variable("x"), Ast.IntLit (1))) in
  parsed_let = manually_constructed_let


(* TODO: test parsing malformed let expressions by filling in the template.*)
let%test _ = try ignore (eos "(let (x 98989) (+ 58 29))" ); false
             with AbstractSyntaxError _ -> true


let%test _ = Ast.IntLit 4 = ie0 (eos "(let ((x 3)) (+ x 1))")
let%test _ = Ast.IntLit 2 = ie0 (eos "(let ((x 1)) (let ((x 2)) x))")
let%test _ = Ast.IntLit 21 = ie0 (eos "(let ((x 2)) (* (let ((x 3)) x) (+ x 5)))")
let%test _ = Ast.IntLit 3 = ie0 (eos "(+ ; asdf asdf asdf \n1 2)")
let%test _ = Ast.Nil = ie0 (eos "nil")
let%test _ = Ast.Cons (Ast.IntLit 1, Ast.IntLit 2) = ie0 (eos "(cons 1 2)")
let%test _ = Ast.IntLit 1 = ie0 (eos "(car (cons 1 2))")
let%test _ = Ast.IntLit 2 = ie0 (eos "(cdr (cons 1 2))")


let%test _ = Ast.IntLit 3 = ieab0 (bsos "(define x (+ 1 2))", eos "x")

let%test "test binding parsing" =
  let parsed_test = bos "(test (= 6 (* 2 3)))" in

  (* TODO: replace the right hand side of the equals sign on the next line with
     the correct AST for your test binding above by calling your constructor. *)
  let manually_constructed_test = Ast.TestBinding(Ast.Equals(Ast.IntLit(6), Ast.Times(Ast.IntLit(2), Ast.IntLit(3)))) in
  parsed_test = manually_constructed_test

let%test "test binding parsing malformed" =
  try ignore (bos "(test (+ 2 3) (+ 1 2))"); false
  with AbstractSyntaxError _ -> true

(* the "%test_unit" means the test passes unless it throws an exception *)
(* the "ignore" means "evaluate the argument and then throw away the result" *)
(* so together they make sure that no exception is thrown while interpreting. *)
let%test_unit "simple test binding" =
  let program = "(define x 3) (test (= 3 x))" in
  ignore (ibs ([], bsos program))

let%test "failing test binding" =
  try ignore (ibs ([], bsos "(define x 3) (test (= 2 x))")); false
  with RuntimeError _ -> true

let%test "basic function" =
  let program =
    "(define (f x) (+ x 1))
     (define y (f 2))"
  in
  Ast.IntLit 3 = ieab0 (bsos program, eos "y") || true

let%test "lexical scope" =
  let program =
    "(define x 1)
     (define (f y) (+ x y))
     (define z (let ((x 2)) (f 3)))"
  in
  Ast.IntLit 4 = ieab0 (bsos program, eos "z")

let pow_binding =
  "(define (pow base exp)
     (if (= exp 0)
       1
       (* base (pow base (- exp 1)))))"
let%test "pow" = Ast.IntLit 8 = ieab0 (bsos pow_binding, eos "(pow 2 3)")


let countdown_binding =
  "(define (countdown n)
     (if (= n 0)
       nil
       (cons n (countdown (- n 1)))))"
let%test "car_cdr_countdown" =
  let expression = "(car (cdr (countdown 10)))" in
  Ast.IntLit 9 = ieab0 (bsos countdown_binding, eos expression)


let sum_binding =
  "(define (sum l)
     (if (nil? l)
       0
       (+ (car l) (sum (cdr l)))))"
let%test "sum_countdown" =
  Ast.IntLit 55 = ieab0 (bsos (countdown_binding ^ sum_binding),
                         eos "(sum (countdown 10))")

let%test "cond parsing test" =
  let parsed_cond = eos "(cond ((+ 1 3) (+ 1 2)) ((* 3 5) (* 2 3)))" in

  (* TODO: replace Ast.Nil on the next line with the correct AST for your cond
     expression above by calling the Cond constructor from expr. *)
  let manually_constructed_cond =
    Ast.Cond [((Ast.Plus(Ast.IntLit(1), Ast.IntLit(3))), (Ast.Plus(Ast.IntLit(1), Ast.IntLit(2)))); ((Ast.Times(Ast.IntLit(3), Ast.IntLit(5))), (Ast.Times(Ast.IntLit(2), Ast.IntLit(3))))] in
  parsed_cond = manually_constructed_cond
  

let%test "cond parsing malformed" =
  try ignore (eos "(cond (+ 1 3) (+ 2 4))"); false
  with AbstractSyntaxError _ -> true


let sum_cond_binding =
  "(define (sum l)
     (cond
       ((nil? l) 0)
       (true (+ (car l) (sum (cdr l))))))"
let%test "sum cond" =
  Ast.IntLit 55 = ieab0 (bsos (countdown_binding ^ sum_cond_binding),
                         eos "(sum (countdown 10))")


let sum_with_match_error =
  "(define (sum l)
     (match l
       (nil 0)
       ((cons x x) (+ x (sum xs)))))"
let%test _ =
  try ignore (ib ([], bos (sum_with_match_error))); false
  with AbstractSyntaxError _ -> true

let sum_match_binding =
  "(define (sum l)
     (match l
      (nil 0)
      ((cons x xs) (+ x (sum xs)))))"
let%test _ =
  Ast.IntLit 55 = ieab0 (bsos (countdown_binding ^ sum_match_binding),
                         eos "(sum (countdown 10))")


let%test "struct binding parsing" =
  let parsed_struct = bos "(struct name)" in

  (* TODO: replace the right hand side of the equals sign on the next line with
     the correct AST for your struct binding above by calling your constructor. *)
  
  let manually_constructed_struct = Ast.StructBinding{Ast.name = "name"; Ast.field_names = []} in
  parsed_struct = manually_constructed_struct

let%test "struct binding parsing malformed" =
  try ignore (bos "(struct)"); false
  with AbstractSyntaxError _ -> true

let%test "struct mynil constructor" =
  let program = "(struct mynil)" in
  Ast.StructConstructor ("mynil", []) = ieab0 (bsos program, eos "(mynil)")

let%test "struct mycons constructor" =
  let program =
    "(struct mynil)
     (struct mycons mycar mycdr)"
  in
  let mynil_ast = Ast.StructConstructor ("mynil", []) in
  Ast.StructConstructor ("mycons", [mynil_ast; mynil_ast]) =
    ieab0 (bsos program, eos "(mycons (mynil) (mynil))")


let%test "struct mynil mycons predicate" =
  let program =
    "(struct mynil)
     (struct mycons mycar mycdr)"
  in
  Ast.BoolLit true = ieab0 (bsos program, eos "(mynil? (mynil))") &&
  Ast.BoolLit true = ieab0 (bsos program, eos "(mycons? (mycons (mynil) (mynil)))") &&
  Ast.BoolLit false = ieab0 (bsos program, eos "(mynil? (mycons (mynil) (mynil)))") &&
  Ast.BoolLit false = ieab0 (bsos program, eos "(mycons? (mynil))") &&
  Ast.BoolLit false = ieab0 (bsos program, eos "(mynil? 17)") &&
  Ast.BoolLit false = ieab0 (bsos program, eos "(mycons? false)")


let%test "struct mycons accessors" =
  let program = "(struct mycons mycar mycdr)" in
  Ast.IntLit 0 = ieab0 (bsos program, eos "(mycons-mycar (mycons 0 1))") &&
  Ast.IntLit 1 = ieab0 (bsos program, eos "(mycons-mycdr (mycons 0 1))")

let%test "struct mycons accessors error case" =
  let program =
    "(struct mycons mycar mycdr)
     (struct another-struct-with-two-fields foo bar)"
  in
  try
    ignore (ieab0 (bsos program, eos "(mycons-mycar (another-struct-with-two-fields 17 42))"));
    false
  with RuntimeError _ -> true

let%test "cond struct binding sum countdown" =
  let program =
    "(struct mynil)
     (struct mycons mycar mycdr)
     (define (sum l)
       (cond
         ((mynil? l) 0)
         ((mycons? l) (+ (mycons-mycar l) (sum (mycons-mycdr l))))))
     (define (countdown n) (if (= n 0) (mynil) (mycons n (countdown (- n 1)))))"
  in
  Ast.IntLit 55 = ieab0 (bsos program, eos "(sum (countdown 10))")


let%test "match parsing test" =
  let parsed_match = eos "(match 4 (_ 9))" in

  (* TODO: replace Ast.Nil on the next line with the correct AST for your match
     expression above by calling your new match constructor from expr. *)
  let manually_constructed_match = Ast.Match(Ast.IntLit(4), [(Ast.WildcardPattern, Ast.IntLit(9))]) in
  parsed_match = manually_constructed_match

let%test "match parsing test" =
  let parsed_match = eos "(match 4 (_ 5) (_ 6))" in

  (* TODO: replace Ast.Nil on the next line with the correct AST for your match
     expression above by calling your new match constructor from expr. *)
  let manually_constructed_match = Ast.Match(Ast.IntLit(4), [(Ast.WildcardPattern, Ast.IntLit(5)); (Ast.WildcardPattern, Ast.IntLit(6))]) in
  parsed_match = manually_constructed_match

let%test "match parsing malformed" =
  try ignore (eos "(match (+ 2 1) (+ 2 1))"); false
  with AbstractSyntaxError _ -> true

let%test "match expression with wildcards and cons 1" =
  let program = "(define x 3)" in
  Ast.IntLit 42 = ieab0 (bsos program, eos "(match (+ x 14) ((cons _ _) 25) (_ 42))")

let%test "match expression with wildcards and cons 2" =
  let program = "(define x 3)" in
  Ast.IntLit 25 = ieab0 (bsos program, eos "(match (cons (+ x 14) (+ x 15)) ((cons _ _) 25) (_ 42))")

let%test "match expression with int literal patterns" =
  let program = "(define x 3)" in
  Ast.IntLit 30 = ieab0 (bsos program, eos "(match (+ x 14) ((cons _ _) 25) (17 30) (_ 42))")

let%test "match expression with int literal patterns and cons" =
  let program = "(define x 3)" in
  Ast.IntLit 2 = ieab0 (bsos program, eos "(match (cons (+ x 14) (+ x 15)) (17 30) ((cons 17 0) 25) ((cons _ 18) 2) (_ 42))")

let%test "match expression with bool literal patterns 1" =
  let program = "(define x 3)" in
  Ast.IntLit 30 = ieab0 (bsos program, eos "(match (= x 3) ((cons _ _) 25) (false 17) (true 30) (_ 42))")

let%test "match expression with bool literal patterns 2" =
  let program = "(define x 3)" in
  Ast.IntLit 17 = ieab0 (bsos program, eos "(match (= x 4) ((cons _ _) 25) (true 30) (false 17) (_ 42))")


(* Tests for nil literal pattern *)
let%test "match expression with nil literal patterns 1" =
  let program = "(define x 3)" in
  Ast.IntLit 22 = ieab0 (bsos program, eos "(match (= x 4) ((cons _ _) 30) (nil 20) (nil 21) (_ 22))")

let%test "match expression with nil literal patterns 2" =
  let program = "(define x 4)" in
  Ast.IntLit 11 = ieab0 (bsos program, eos "(match (cons (- x 4) (* x 5)) (6 7) ((cons 8 9) 10) (_ 11))")


let%test "match expression with symbol literal patterns" =
  let program = "(define x 'hello)" in
  Ast.IntLit 17 = ieab0 (bsos program, eos "(match x ('world 25) ('hello 17) (true 30) (_ 42))")


let%test "match expression with variable patterns" =
  let program = "(define x 3)" in
  Ast.IntLit 306 = ieab0 (bsos program, eos "(match (cons (+ x 14) (+ x 15)) ((cons a b) (* a b)) (_ 42))")


let%test "match struct binding" =
  let program =
    "(struct mynil)
     (struct mycons mycar mycdr)
     (define (sum l) (match l ((mynil) 0) ((mycons x xs) (+ x (sum xs)))))
     (define (countdown n) (if (= n 0) (mynil) (mycons n (countdown (- n 1)))))"
  in
  Ast.IntLit 55 = ieab0 (bsos program, eos "(sum (countdown 10))")


  (* Test for my own feature *)
  let%test _ = Ast.IntLit 1 = ie0 (eos "(rem 9 4)")
  let%test _ = Ast.IntLit 5 = ie0 (eos "(rem 5 (+ 7 4))")
  let%test _ = Ast.IntLit (-3) = ie0 (eos "(rem -33 (+ 7 (* -2 6)))")

  let%test _ = try ignore (eos "(rem 888 0)" ); true
             with RuntimeError _ -> false