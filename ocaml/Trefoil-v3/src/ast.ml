
open Errors

(* IGNORE the first 50ish lines (up to "type expr = ...") until working on part 2! *)
type pattern =
  | WildcardPattern
  | ConsPattern of pattern * pattern

(* TODO: add more patterns here *)
  | IntPattern of int
  | BoolPattern of bool
  | NilPattern
  | SymPattern of string
  | VarPattern of string
  | StructPattern of string * pattern list


[@@deriving show]
let string_of_pattern = show_pattern

(* TODO: delete this exception, which we just use because Failure is taken by int_of_string in one place *)
(* exception StarterCodeFailure *)

let rec pattern_of_pst p =
  match p with
  | Pst.Symbol sym -> begin
     try
       let n = int_of_string sym in
       (* TODO: delete the next line and instead build an integer literal pattern here using n *)
       IntPattern n
     with
       Failure _ ->
       match sym with
       | "_" -> WildcardPattern
       | "true" -> BoolPattern true

       (* TODO: add other cases here for "false" and "nil" *)
       | "false" -> BoolPattern false
       | "nil" -> NilPattern
       | _ ->
          if String.get sym 0 = '\'' (* if the string starts with an apostrophe *)
          then let sym_without_apostrophe = String.sub sym 1 (String.length sym - 1)
               in SymPattern sym_without_apostrophe
          else VarPattern sym
    end
  | Pst.Node [] -> raise (AbstractSyntaxError "Expected pattern but got '()'")

  (* Struct pattern here *)
  | Pst.Node (head :: args) ->
     match head, args with
     | Pst.Symbol "cons", [p1; p2] -> ConsPattern (pattern_of_pst p1, pattern_of_pst p2)
     | Pst.Symbol s, ps ->
        let rec helper ps =
          match ps with
          | [] -> []
          | x :: xs -> pattern_of_pst x :: helper xs
        in
        StructPattern (s, helper ps)
     | _ -> raise (AbstractSyntaxError ("Expected pattern, but got " ^ Pst.string_of_pst p))

let pattern_of_string s =
  s
  |> Pstparser.pst_of_string
  |> pattern_of_pst

type expr =
  | IntLit of int
  | BoolLit of bool
  | Variable of string
  | Plus of expr * expr
  | Minus of expr * expr
  | Times of expr * expr
  | Equals of expr * expr
  | If of expr * expr * expr
(* TODO: add constructor for let expressions here *)
  | Let of (expr * expr) * expr

(* My own type *)
  | Rem of expr * expr

  | Nil
  | Cons of expr * expr
  | IsNil of expr
  | IsCons of expr
  | Car of expr
  | Cdr of expr
  | Call of string * expr list

  (* More constructors for Trefoil v3 below. Ignore during Part 1. *)
  | Symbol of string
  | Cond of (expr * expr) list
  | StructConstructor of string * expr list  (* internal AST node; not written by Trefoil programmer *)
  (* TODO: add other "internal" expression ASTs here *)
  | StructPredicate of string * expr
  | StructAccess of string * int * expr

  (* TODO: add match expression constructor to the expr type here *)
  | Match of expr * ((pattern * expr) list)

[@@deriving show]
let string_of_expr = show_expr

let has_duplicates (l: string list) =
  let sorted = List.sort compare l in
  let rec loop xs =
    match xs with
    | [] -> false
    | [_] -> false
    | x :: y :: zs -> x = y || loop (y :: zs)
  in
  loop sorted

let rec expr_of_pst p =
  match p with
  | Pst.Symbol sym -> begin
     try
       IntLit (int_of_string sym)
     with
       Failure _ ->
       match sym with
       | "true" -> BoolLit true
       | "false" -> BoolLit false
       | "nil" -> Nil
       | _ ->
          if String.get sym 0 = '\''
          then Symbol (String.sub sym 1 (String.length sym - 1))
          else Variable sym
    end
  | Pst.Node [] -> raise (AbstractSyntaxError "Expected expression but got '()'")
  | Pst.Node (head :: args) ->
     match head, args with
     | Pst.Node _, _ -> raise (AbstractSyntaxError ("Expression forms must start with a symbol, but got " ^ Pst.string_of_pst head))
     | Pst.Symbol "+", [left; right] -> Plus (expr_of_pst left, expr_of_pst right)
     | Pst.Symbol "+", _ -> raise (AbstractSyntaxError ("operator + expects 2 args but got " ^ Pst.string_of_pst p))
     | Pst.Symbol "-", [left; right] -> Minus (expr_of_pst left, expr_of_pst right)
     | Pst.Symbol "-", _ -> raise (AbstractSyntaxError ("operator - expects 2 args but got " ^ Pst.string_of_pst p))
     | Pst.Symbol "*", [left; right] -> Times (expr_of_pst left, expr_of_pst right)
     | Pst.Symbol "*", _ -> raise (AbstractSyntaxError ("operator * expects 2 args but got " ^ Pst.string_of_pst p))
     | Pst.Symbol "=", [left; right] -> Equals (expr_of_pst left, expr_of_pst right)
     | Pst.Symbol "=", _ -> raise (AbstractSyntaxError ("operator = expects 2 args but got " ^ Pst.string_of_pst p))
     | Pst.Symbol "if", [branch; thn; els] -> If (expr_of_pst branch, expr_of_pst thn, expr_of_pst els)
     | Pst.Symbol "if", _ -> raise (AbstractSyntaxError ("'if' special form expects 3 args but got " ^ Pst.string_of_pst p))
     (* TODO: add cases for let expressions here *)
     | Pst.Symbol "let", [left; right] -> 
          ( match left with
          | Pst.Node (Pst.Node [first; second] :: []) ->
            Let ((expr_of_pst first, expr_of_pst second), expr_of_pst right)
          | _ ->
            raise (AbstractSyntaxError ("operator - expects 2 args but got " ^ Pst.string_of_pst p))
          )

     (* Parse my own feature *)
     | Pst.Symbol "rem", [left; right] -> Rem (expr_of_pst left, expr_of_pst right)
     | Pst.Symbol "rem", _ -> raise (AbstractSyntaxError ("operator + expects 2 args but got " ^ Pst.string_of_pst p))

     | Pst.Symbol "cons", [left; right] -> Cons (expr_of_pst left, expr_of_pst right)
     | Pst.Symbol "cons", _ -> raise (AbstractSyntaxError ("cons expects 2 args but got " ^ Pst.string_of_pst p))
     | Pst.Symbol "nil?", [arg] -> IsNil (expr_of_pst arg)
     | Pst.Symbol "nil?", _ -> raise (AbstractSyntaxError ("nil? expects 1 arg but got " ^ Pst.string_of_pst p))
     | Pst.Symbol "cons?", [arg] -> IsCons (expr_of_pst arg)
     | Pst.Symbol "cons?", _ -> raise (AbstractSyntaxError ("cons? expects 1 arg but got " ^ Pst.string_of_pst p))
     | Pst.Symbol "car", [arg] -> Car (expr_of_pst arg)
     | Pst.Symbol "car", _ -> raise (AbstractSyntaxError ("car expects 1 arg but got " ^ Pst.string_of_pst p))
     | Pst.Symbol "cdr", [arg] -> Cdr (expr_of_pst arg)
     | Pst.Symbol "cdr", _ -> raise (AbstractSyntaxError ("cdr expects 1 arg but got " ^ Pst.string_of_pst p))
     | Pst.Symbol "cond", clauses ->
        (* converts a list of cond clauses in PST syntax into their abstract
           syntax as a list of pairs of expressions. *)
        let rec clause_loop (clauses: Pst.pst list) : (expr * expr) list =
          match clauses with
          | [] -> []
          | Pst.Node [e1; e2] :: xs ->
             (* TODO: replace "[]" below with code to parse a cond clause.
                - Hint: convert e1 and e2 to expressions, pair them up, and cons
                  them onto the recursive call on xs *)
             (expr_of_pst e1, expr_of_pst e2) :: clause_loop (xs)
          | x :: _ -> raise (AbstractSyntaxError("Malformed 'cond' clause: " ^ Pst.string_of_pst x))
        in
        Cond (clause_loop clauses)

     (* TODO: add parsing for match expressions here *)
     | Pst.Symbol "match", exp :: clauses ->
        let rec helper (clauses: Pst.pst list) : (pattern * expr) list =
          match clauses with
          | [] -> []
          | Pst.Node [p1; e1] :: xs -> (pattern_of_pst p1, expr_of_pst e1) :: helper (xs)
          | x :: _ -> raise (AbstractSyntaxError("Malformed 'match' clause: " ^ Pst.string_of_pst x))
        in
        Match (expr_of_pst exp, helper clauses)
        
     (* Otherwise, if it doesn't match any of the above, it's a function call. *)
     | Pst.Symbol f, args ->
        let rec args_loop args =
          match args with
          | [] -> []
          | arg :: args -> expr_of_pst arg :: args_loop args
        in
        Call (f, args_loop args)

let expr_of_string s =
  s
  |> Pstparser.pst_of_string
  |> expr_of_pst

(* checks that all the psts are symbols, and if so, convert to a list of
   strings. if not, throw AbstractSyntaxError. *)
let rec check_symbols (msg, pst_list) =
  match pst_list with
  | [] -> []
  | Pst.Symbol name :: xs -> name :: check_symbols (msg, xs)
  | p :: _ -> raise (AbstractSyntaxError(msg ^ " must be symbol but got " ^ Pst.string_of_pst p))

type function_binding = { name: string; param_names: string list; body: expr }
[@@deriving show]

type struct_binding = { name: string; field_names: string list }
[@@deriving show]

type binding =
   | VarBinding of string * expr
   | TopLevelExpr of expr
   | FunctionBinding of function_binding
   (* TODO: add a constructor for test bindings here *)
   | TestBinding of expr

   | StructBinding of struct_binding
[@@deriving show]
let string_of_binding = show_binding

let binding_of_pst p =
  match p with
  | Pst.Symbol _ -> TopLevelExpr (expr_of_pst p)
  | Pst.Node [] -> raise (AbstractSyntaxError "Expected binding but got '()'")
  | Pst.Node (head :: args) ->
     match head, args with
     | Pst.Symbol "define", [Pst.Symbol lhs_var; rhs] -> VarBinding (lhs_var, expr_of_pst rhs)
     | Pst.Symbol "define", [Pst.Node (Pst.Symbol name :: param_names); rhs] ->
        let param_names = check_symbols ("Function parameter", param_names) in
        if has_duplicates (name :: param_names)
        then raise (AbstractSyntaxError("Function binding reuses a symbol multiple times as a function name or parameter name"));
        FunctionBinding {name; param_names; body = expr_of_pst rhs}
     | Pst.Symbol "define", _ -> raise (AbstractSyntaxError("This definition is malformed " ^ Pst.string_of_pst p))
     (* TODO: parse test bindings here *)
     | Pst.Symbol "test", [tail] -> TestBinding (expr_of_pst tail)
     | Pst.Symbol "test", _ -> raise (AbstractSyntaxError ("Children size should be 2"))

     | Pst.Symbol "struct", Pst.Symbol name :: field_names ->
        (* note: a struct with a field of the same name as the struct itself is allowed *)
        let field_names = check_symbols (name, field_names) in
          ( match field_names with
          | [] -> StructBinding {name; field_names}
          | xs :: l -> 
            if has_duplicates l then raise (AbstractSyntaxError ("Field name can't be same")) else StructBinding {name; field_names} )
            
     | Pst.Symbol "struct", _ -> raise (AbstractSyntaxError("'struct' bindings must at least one argument, but got none"))

     | Pst.Node _, _ -> raise (AbstractSyntaxError("Expected binding to start with a symbol but got " ^ Pst.string_of_pst p))
     | _ -> TopLevelExpr (expr_of_pst p)

let binding_of_string s =
  s
  |> Pstparser.pst_of_string
  |> binding_of_pst

let bindings_of_string s =
  let p = Pstparser.pstparser_of_string s in
  let rec parse_binding_list () =
    match Pstparser.parse_pst p with
    | None -> []
    | Some pst ->
       binding_of_pst pst :: parse_binding_list ()
  in
  parse_binding_list ()