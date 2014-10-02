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

import java.util.HashMap;
import java.util.Map;

import LinuxConfigDiff.antlr.KconfigLexer;

public class Output
{
    public boolean Html;
    public boolean Color;
    public boolean Description;
    public int MaxDeep = Integer.MAX_VALUE;

    public Output()
    {
    }

    public Output(boolean html, boolean color, boolean description)
    {
        this.Html = html;
        this.Color = color;
        this.Description = description;
    }

    public void print(KconfigNode tree)
    {
        print(tree, 0);
    }
    protected void print(KconfigNode node, int deep)
    {
        if (deep > MaxDeep) {
            return;
        }
        StringBuilder space = new StringBuilder();
        for (int i = 0; i < deep; i++) {
            space.append("  ");
        }
        //StringBuilder line[] = new StringBuilder[3];
        StringBuilder line[] = {new StringBuilder(), new StringBuilder(), new StringBuilder(), new StringBuilder()};
        for (Map.Entry<Integer, String> opt : node.options.entrySet()) {
            switch (opt.getKey()) {
            case KconfigLexer.T_TYPE_bool:
                line[0].append("[ ]");
                if (!opt.getValue().isEmpty()) {
                    line[1].append(opt.getValue());
                }
                break;
            case KconfigLexer.T_TYPE_tristate:
                line[0].append("< >");
                if (!opt.getValue().isEmpty()) {
                    line[1].append(opt.getValue());
                }
                break;
            case KconfigLexer.T_TYPE_else:
                line[0].append("() ");
                if (!opt.getValue().isEmpty()) {
                    line[1].append(opt.getValue());
                }
                break;
            case KconfigLexer.T_PROMPT:
                line[1].append(opt.getValue());
                break;
            default:
                break;
            }
        }
        int _deep = deep + 1;
        switch (node.type) {
        case KconfigLexer.T_SOURCE:
            line[0].append("source");
            line[1].append(node.path);
            _deep--;
            break;
        case KconfigLexer.T_IF:
            line[0].append("...if");
            line[1].append(node.value);
            break;
        case KconfigLexer.T_CHOICE:
            line[3].append("(choise) ");
        case KconfigLexer.T_MENU:
            if (node.value != null) {
                line[0].append("   ");
                line[1].append(node.value);
            }
        case KconfigLexer.T_MENUCONFIG:
            line[3].append("--->");
            break;
        default:
            break;
        }
        switch (node.type) {
        case KconfigLexer.T_CONFIG:
        case KconfigLexer.T_MENUCONFIG:
            line[2].append("= " + node.value);
            break;
        default:
            break;
        }
        if (line[1].length() > 0 /*&& node.type != KconfigLexer.T_CONFIG*/) {
            StringBuilder linefull = new StringBuilder();
            for (int i = 0; i < line.length; i++) {
                linefull.append(line[i]);
                //if (i < line.length-1) {
                    linefull.append(' ');
                //}
            }
            //System.out.println(space.append(line));
            System.out.println(space + "" + linefull.toString().trim());
            /*System.out.println(space + "*" + node.type + " = " + node.value);
            for (Map.Entry<Integer, String> opt : node.options.entrySet()) {
                System.out.println(space + "|" + opt.getKey() + "=" + opt.getValue() + (opt.getValue()==null?"null":""));
            }*/
        }
        for (KconfigNode child : node.children) {
            print(child, _deep);
        }
        if (node.type == KconfigLexer.T_IF) {
            System.out.println(space + "...endif");
        }
    }
}
