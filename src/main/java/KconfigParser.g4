/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
parser grammar KconfigParser;
options {
    tokenVocab=KconfigLexer;
    superClass=LinuxConfigDiff.KconfigTree;
}

@header {
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package LinuxConfigDiff.antlr;
}

/** Start */
input: nl*? start ;

start: mainmenu_stmt stmt_list | stmt_list ;

stmt_list:
    (   common_stmt
      | choice_stmt
      | menu_stmt
      | T_WORD
      | option_name
      | nl
    ) stmt_list
    | EOF
    | /* empty */
;

option_name:
      T_DEPENDS
    | T_PROMPT
    | T_TYPE
    | T_SELECT
    | T_OPTIONAL
    | T_RANGE
    | T_DEFAULT
    | T_VISIBLE
;

common_stmt:
      if_stmt
    | comment_stmt
    | config_stmt
    | menuconfig_stmt
    | source_stmt
;

option_error:
      T_WORD
;


/* config/menuconfig entry */

config_entry_start: T_CONFIG name=T_WORD T_EOL
    {
        kconfigTreeIn("config", $name.text);
    }
;

config_stmt: config_entry_start config_option_list {kconfigTreeOut();} ;

menuconfig_entry_start: T_MENUCONFIG name=T_WORD T_EOL
    {
        kconfigTreeIn("menuconfig", $name.text);
    }
;

menuconfig_stmt: menuconfig_entry_start config_option_list {kconfigTreeOut();} ;

config_option_list:
    (   config_option
      | symbol_option
      | depends
      | option_error
      | nl
    ) config_option_list
    | help
    | /* empty */
;

config_option:
                 T_TYPE prompt_stmt_opt T_EOL
             | T_PROMPT prompt if_expr T_EOL
             | T_DEFAULT expr if_expr T_EOL
             | T_SELECT T_WORD if_expr T_EOL
             | T_RANGE symbol symbol if_expr T_EOL ;

symbol_option: T_OPTION .*? T_EOL ;
//symbol_option: T_OPTION symbol_option_list T_EOL ;

//symbol_option_list:
//      T_WORD symbol_option_arg symbol_option_list
//    | /* empty */
//;

symbol_option_arg:
      T_EQUAL prompt
    | /* empty */
;

/* choice entry */

choice: T_CHOICE name=word_opt T_EOL
    {
        kconfigTreeIn("choice", $name.text);
    }
;

choice_entry: choice choice_option_list ;

choice_end: T_ENDCHOICE T_EOL {kconfigTreeOut();} ;

choice_stmt: choice_entry choice_block choice_end ;

choice_option_list:
    (   choice_option
      | depends
      | option_error
      | nl
    ) choice_option_list
    | help
    | /* empty */
;

choice_option:
      T_PROMPT prompt if_expr T_EOL
    | T_TYPE prompt_stmt_opt T_EOL
    | T_OPTIONAL T_EOL
    | T_DEFAULT T_WORD if_expr T_EOL
;

choice_block:
    (   common_stmt
      //| menu_stmt
      | choice_stmt
      | nl
    ) choice_block
    | /* empty */
;

/* if entry */

if_entry: T_IF expr nl ;

if_end: T_ENDIF T_EOL ;

if_stmt: if_entry if_block if_end ;

if_block:
    (   common_stmt
      | menu_stmt
      | choice_stmt
      | nl
    ) if_block
    | /* empty */
;

/* mainmenu entry */

mainmenu_stmt: T_MAINMENU name=prompt nl
    {
        kconfigTreeIn("mainmenu", $name.text);
        kconfigTreeOut();
    }
;

/* menu entry */

menu: T_MENU name=prompt T_EOL
    {
        kconfigTreeIn("menu", $name.text);
    }
;

menu_entry: menu visibility_list depends_list ;

menu_end: T_ENDMENU T_EOL {kconfigTreeOut();} ;

menu_stmt: menu_entry menu_block menu_end ;

menu_block:
    (   common_stmt
      | menu_stmt
      | choice_stmt
      | nl
    ) menu_block
    | /* empty */
;

source_stmt: T_SOURCE name=prompt T_EOL
    {
        kconfigSource($name.text);
    }
;

/* comment entry */

comment: T_COMMENT name=prompt T_EOL
    {
        kconfigTreeIn("comment", $name.text);
    }
;

comment_stmt: comment depends_list {kconfigTreeOut();} ;

/* help option */

help: T_HELP txt=T_HELPTEXT
    {
        //System.out.println("help = " + $txt.text);
    }
;

/* depends option */

depends_list:
      depends depends_list
    | option_error depends_list
    | T_EOL
    | /* empty */
;

depends: T_DEPENDS T_ON expr T_EOL ;

/* visibility option */

visibility_list:
      visible visibility_list
    | T_EOL
    | /* empty */
;

visible: T_VISIBLE if_expr ;

/* prompt statement */

prompt_stmt_opt:
      prompt if_expr
    | /* empty */
;

prompt:
      T_WORD
    | T_WORD_QUOTE
;

nl: T_EOL+ ;

if_expr:
      T_IF expr
    | /* empty */
;

expr:
      symbol
    | symbol T_EQUAL symbol
    | symbol T_UNEQUAL symbol
    | T_OPEN_PAREN expr T_CLOSE_PAREN
    | T_NOT expr
    | expr T_OR expr
    | expr T_AND expr
;

symbol:
      T_WORD
    | T_WORD_QUOTE
;

word_opt:
      T_WORD
    | /* empty */
;
