open Trefoil3lib
open Errors

let main () =
  (* Our main read-eval-print loop.

     Reads bindings from t one at a time and evaluates them on the current
     dynamic environment. *)
  let rec loop (dynenv, errored_so_far, t) =
    try
      match Pstparser.parse_pst t with
      | None -> errored_so_far
      | Some pst ->
         (* uncomment next line to print each PST *)
         (* print_endline (Pst.string_of_pst pst); *)
         let b = Ast.binding_of_pst pst in
         (* uncomment next line to print each AST *)
         (* print_endline (Ast.string_of_binding b); *)
         let dynenv = Interpreter.interpret_binding (dynenv, b) in
         loop (dynenv, errored_so_far, t)
    with
      (ParenthesizedSymbolError msg |
       AbstractSyntaxError msg |
       RuntimeError msg) -> begin
        Printf.printf "error:%d:%d: %s\n%!" t.reader.line_num t.reader.column_num msg;
        loop (dynenv, true, t)
      end
    | InternalError msg as e ->
        Printf.printf "error:%d:%d: Impossible! %sPlease contact interpreter implementor!! \n%!"
          t.reader.line_num t.reader.column_num msg;
        raise e
  in
  (* beginning of code which handles command line arguments. *)
  (* you don't need to understand how this works. *)
  let arg: string option ref = ref None in
  let set_arg s =
    match !arg with
    | None -> arg := Some s
    | Some _ -> raise (Arg.Bad "got multiple command line arguments, but expected at most one (the filename)")
  in
  let usage =
    "USAGE\n\n" ^

    "    trefoil [FILENAME]\n\n" ^

    "With no argument, read bindings from standard input.\n" ^
    "With an argument, read bindings from the given filename.\n\n" ^

    "OPTIONS"
  in
  Arg.parse [] set_arg usage;
  print_endline "welcome to trefoil v3!";
  let source = match !arg with
    | None -> stdin
    | Some s -> open_in s
  in
  (* end of code that handles command line arguments *)

  (* now call the main read-eval-print loop *)
  let tokenizer = Pstparser.tokenizer_of_source (Pstparser.File source) in
  let errored = loop ([], false, tokenizer) in
  if errored
  then exit 1
  (* otherwise just return normally *)


let () = main ()
