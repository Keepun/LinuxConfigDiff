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

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.Pair;

public class KconfigLexerTokenFactory implements TokenFactory<KconfigLexerToken>
{
    /**
     * The default {@link CommonTokenFactory} instance.
     *
     * <p>
     * This token factory does not explicitly copy token text when constructing
     * tokens.</p>
     */
    public static final TokenFactory<KconfigLexerToken> DEFAULT = new KconfigLexerTokenFactory();

    /**
     * Indicates whether {@link CommonToken#setText} should be called after
     * constructing tokens to explicitly set the text. This is useful for cases
     * where the input stream might not be able to provide arbitrary substrings
     * of text from the input after the lexer creates a token (e.g. the
     * implementation of {@link CharStream#getText} in
     * {@link UnbufferedCharStream} throws an
     * {@link UnsupportedOperationException}). Explicitly setting the token text
     * allows {@link Token#getText} to be called at any time regardless of the
     * input stream implementation.
     *
     * <p>
     * The default value is {@code false} to avoid the performance and memory
     * overhead of copying text for every token unless explicitly requested.</p>
     */
    protected final boolean copyText;

    /**
     * Constructs a {@link CommonTokenFactory} with the specified value for
     * {@link #copyText}.
     *
     * <p>
     * When {@code copyText} is {@code false}, the {@link #DEFAULT} instance
     * should be used instead of constructing a new instance.</p>
     *
     * @param copyText The value for {@link #copyText}.
     */
    public KconfigLexerTokenFactory(boolean copyText) { this.copyText = copyText; }

    /**
     * Constructs a {@link CommonTokenFactory} with {@link #copyText} set to
     * {@code false}.
     *
     * <p>
     * The {@link #DEFAULT} instance should be used instead of calling this
     * directly.</p>
     */
    public KconfigLexerTokenFactory() { this(false); }

    @Override
    public KconfigLexerToken create(Pair<TokenSource, CharStream> source, int type, String text,
                              int channel, int start, int stop,
                              int line, int charPositionInLine)
    {
        KconfigLexerToken t = new KconfigLexerToken(source, type, channel, start, stop);
        t.setLine(line);
        t.setCharPositionInLine(charPositionInLine);
        if ( text!=null ) {
            t.setText(text);
        }
        else if ( copyText && source.b != null ) {
            t.setText(source.b.getText(Interval.of(start,stop)));
        }

        return t;
    }

    @Override
    public KconfigLexerToken create(int type, String text) {
        return new KconfigLexerToken(type, text);
    }
}
