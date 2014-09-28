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
package LinuxConfigDiff;

import java.io.PrintStream;
import java.lang.reflect.Method;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public class Tracer
{
    public Tracer()
    {
    }

    public static void printKconfigTree(KconfigNode kct, String space)
    {
        System.out.println(space + kct.type + " = " + kct.value);
        for (KconfigNode child : kct.children) {
            printKconfigTree(child, space + "  ");
        }
    }

    public static void printParserTree(Parser parser, Class<?> parserclass, String startfunc, Class<?>[] startargs,
            PrintStream printto, int maxerrors)
    {
        try {
            boolean buildtree = (Boolean)parserclass.getMethod("getBuildParseTree").invoke(parser);
            //boolean trace = (Boolean)parserclass.getMethod("getTrace").invoke(parser);
            Method msetbuildtree = parserclass.getMethod("setBuildParseTree", boolean.class);
            //Method msettrace = parserclass.getMethod("setTrace", Boolean.class);
            try {
                msetbuildtree.invoke(parser, true);
                //msettrace.invoke(parser, false);
                ParserRuleContext tree;
                if (startargs == null) {
                    tree = (ParserRuleContext)parserclass.getMethod(startfunc).invoke(parser);
                } else {
                    tree = (ParserRuleContext)parserclass.getMethod(startfunc, startargs).invoke(parser);
                }
                String[] rules = (String[])parserclass.getMethod("getRuleNames").invoke(parser);
                printParserTree(tree, rules, "", printto, maxerrors);
            } catch (Exception ex) {
                System.err.println(ex);
            }
            msetbuildtree.invoke(parser, buildtree);
            //msettrace.invoke(parser, trace);
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }
    protected static void printParserTree(ParserRuleContext parser, String[] rules, String space, PrintStream printto,
            Integer errors)
    {
        int rln = parser.getRuleIndex();
        printto.println(space + (rln > -1 && rln < rules.length ? rules[rln] : "No rule = " + rln));
        //String sp = space + "  ";
        int count = parser.getChildCount();
        for (int x = 0; x < count; x++) {
            ParseTree chld = parser.getChild(x);
            if (chld instanceof ErrorNode) {
                printto.println(space + "ERROR: " + ((ErrorNode)chld).getSymbol());
                errors--;
                if (errors < 1) {
                    return;
                }
            } else if (chld instanceof TerminalNode) {
                printto.println(space + ((TerminalNode)chld).getSymbol());
            } else if (chld instanceof ParserRuleContext) {
                printParserTree((ParserRuleContext)chld, rules, space + "  ", printto, errors);
                if (errors < 1) {
                    return;
                }
            } else {
                printto.println(space + "CLASS: " + chld.getClass().getCanonicalName());
            }
        }
    }
}
