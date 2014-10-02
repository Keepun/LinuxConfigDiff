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
lexer grammar KconfigLexer;

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

@members {
    protected boolean isEndHelp()
    {
        int la = _input.LA(1);
        if (la > ' ' || (la == '\n' && _input.LA(2) > ' ')) {
            return true;
        }
        return false;
    }
}

WS : ([ \r\t]+ | '\\'+'\n') -> skip;

T_MAINMENU : 'mainmenu' ;
T_MENU : 'menu' ;
T_ENDMENU : 'endmenu' ;
T_SOURCE : 'source' ;
T_CHOICE : 'choice' ;
T_ENDCHOICE : 'endchoice' ;
T_COMMENT : 'comment' ;
T_CONFIG : 'config' ;
T_MENUCONFIG : 'menuconfig' ;
T_HELP : ('help' | '-'+ 'help' '-'+) -> mode(HELP) ;
T_IF : 'if' ;
T_ENDIF : 'endif' ;
T_DEPENDS : 'depends' ;
T_OPTIONAL : 'optional' ;
T_PROMPT : 'prompt' ;
T_TYPE_tristate : 'tristate' ;
T_TYPE_bool :
      'bool'
    | 'boolean'
;
T_TYPE_else :
      'int'
    | 'hex'
    | 'string'
;
T_DEFAULT :
      'default'
    | 'def_tristate'
    | 'def_bool'
;
T_SELECT : 'select' ;
T_RANGE : 'range' ;
T_VISIBLE : 'visible' ;
T_OPTION : 'option' ;
T_ON : 'on' ;
T_OPT_MODULES : 'modules' ;
T_OPT_DEFCONFIG_LIST : 'defconfig_list' ;
T_OPT_ENV : 'env' ;

T_AND : '&&' ;
T_OR : '||' ;
T_OPEN_PAREN : '(' ;
T_CLOSE_PAREN : ')' ;
T_NOT : '!' ;
T_EQUAL : '=' ;
T_UNEQUAL : '!=' ;

T_WORD_QUOTE :
      '\'' ('\\\'' | ~[\'\n])* [\'\n]
    | '"' ('\\"' | ~["\n])* ["\n]
;

T_WORD : '-'? Name (Name | [\-/.])* ;
T_EOL : '#'~[\n]*'\n' | '\n' ;

fragment
Name : [A-Za-z0-9_] ;

mode HELP;
T_HELPTEXT :
    (   [ \t]*'\n' {isEndHelp()}?
      | EOF
    ) -> mode(DEFAULT_MODE)
;
HELP_1 : . -> more ;
