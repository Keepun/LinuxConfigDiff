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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedList;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Pair;

import LinuxConfigDiff.antlr.KconfigLexer;

public class KconfigLexerToken extends CommonToken
{
    public KconfigLexerToken(int type)
    {
        super(type);
    }

    public KconfigLexerToken(int type, String text)
    {
        super(type, text);
    }

    public KconfigLexerToken(Pair<TokenSource, CharStream> source, int type,
            int channel, int start, int stop)
    {
        super(source, type, channel, start, stop);
    }

    public KconfigLexerToken(Token oldToken)
    {
        super(oldToken);
    }

    public static final String[] ruleNamesLexer;

    static
    {
        HashSet<String> token = new HashSet<String>();
        for (Field field : KconfigLexer.class.getFields()) {
            if (field.getType().equals(int.class) && Modifier.isStatic(field.getModifiers())) {
                token.add(field.getName());
            }
        }
        LinkedList<String> list = new LinkedList<String>();
        for (String rule : KconfigLexer.ruleNames) {
            if (token.contains(rule)) {
                list.add(rule);
            }
        }
        ruleNamesLexer = list.toArray(new String[0]);
    }

    @Override
    public String toString()
    {
        String channelStr = "";
        if ( channel>0 ) {
            channelStr=",channel="+channel;
        }
        String txt = getText();
        if ( txt!=null ) {
            txt = txt.replace("\n","\\n");
            txt = txt.replace("\r","\\r");
            txt = txt.replace("\t","\\t");
        }
        else {
            txt = "<no text>";
        }
        String stype = "" + type;
        if (type > 0 && type <= ruleNamesLexer.length) {
            stype = ruleNamesLexer[type-1] + ":" + type;
        }
        return "[@"+getTokenIndex()+","+start+":"+stop+"='"+txt+"',<"+stype+">"+channelStr+","+line+":"+getCharPositionInLine()+"]";
    }
}
