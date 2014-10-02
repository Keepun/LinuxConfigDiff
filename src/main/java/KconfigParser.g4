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
    | (T_TYPE_tristate | T_TYPE_bool | T_TYPE_else)
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

config_entry_start: opt=T_CONFIG T_WORD T_EOL {kconfigTreeIn($opt.type, $T_WORD.text);} ;

config_stmt: config_entry_start config_option_list {kconfigTreeOut();} ;

menuconfig_entry_start: opt=T_MENUCONFIG T_WORD T_EOL {kconfigTreeIn($opt.type, $T_WORD.text);} ;

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
      opt=(T_TYPE_tristate | T_TYPE_bool | T_TYPE_else) (prompt if_expr)? T_EOL
        {kcTreeNow.addOption($opt.type, $prompt.text);}
    | opt=T_PROMPT prompt if_expr T_EOL {kcTreeNow.addOption($opt.type, $prompt.text, $if_expr.text);}
    | opt=T_DEFAULT expr if_expr T_EOL {kcTreeNow.addOption($opt.type, $expr.text, $if_expr.text);}
    | opt=T_SELECT T_WORD if_expr T_EOL {kcTreeNow.addOption($opt.type, $T_WORD.text, $if_expr.text);}
    | opt=T_RANGE s1=symbol s2=symbol if_expr T_EOL {kcTreeNow.addOption($opt.type, $s1.text, $s2.text, $if_expr.text);}
;

symbol_option: opt=T_OPTION txt=.*? T_EOL  {kcTreeNow.addOption($opt.type, $txt.text);} ;

/* choice entry */

choice: opt=T_CHOICE word_opt T_EOL {kconfigTreeIn($opt.type, $word_opt.text);} ;

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
      opt=T_PROMPT prompt if_expr T_EOL {kcTreeNow.addOption($opt.type, $prompt.text, $if_expr.text);}
    | opt=(T_TYPE_tristate | T_TYPE_bool | T_TYPE_else) (prompt if_expr)? T_EOL
        {kcTreeNow.addOption($opt.type, $prompt.text);}
    | opt=T_OPTIONAL T_EOL {kcTreeNow.addOption($opt.type);}
    | opt=T_DEFAULT T_WORD if_expr T_EOL {kcTreeNow.addOption($opt.type, $T_WORD.text, $if_expr.text);}
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

if_entry: opt=T_IF expr T_EOL {kconfigTreeIn($opt.type, $expr.text);} ;

if_end: T_ENDIF T_EOL {kconfigTreeOut();} ;

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

mainmenu_stmt: opt=T_MAINMENU prompt T_EOL {kcTreeNow.addOption($opt.type, $prompt.text);} ;

/* menu entry */

menu: opt=T_MENU prompt T_EOL {kconfigTreeIn($opt.type, $prompt.text);} ;

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

source_stmt: T_SOURCE prompt T_EOL {kconfigSource($prompt.text);} ;

/* comment entry */

comment: opt=T_COMMENT prompt T_EOL {kconfigTreeIn($opt.type, $prompt.text);} ;

comment_stmt: comment depends_list {kconfigTreeOut();} ;

/* help option */

help: T_HELP T_HELPTEXT {kcTreeNow.addOption($T_HELP.type, $T_HELPTEXT.text);} ;

/* depends option */

depends_list:
      depends depends_list
    | option_error depends_list
    | T_EOL
    | /* empty */
;

depends: T_DEPENDS T_ON expr T_EOL {kcTreeNow.addOption($T_DEPENDS.type, $expr.text);} ;

/* visibility option */

visibility_list:
      visible visibility_list
    | T_EOL
    | /* empty */
;

visible: T_VISIBLE if_expr {kcTreeNow.addOption($T_VISIBLE.type, $if_expr.text);} ;

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
