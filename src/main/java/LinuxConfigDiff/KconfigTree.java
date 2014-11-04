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

import java.nio.file.Path;
import java.util.concurrent.Executor;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.TokenStream;

import LinuxConfigDiff.antlr.KconfigLexer;

public abstract class KconfigTree extends Parser {
    public static String SRCARCH;
    public static Path ROOTDIR[];
    public KconfigNode KconfigResult;

    public static Executor ThreadPool;
    public static boolean Debug;

    public KconfigTree(TokenStream input)
    {
        super(input);
    }

    public void kconfigTreeRoot(int rootpath)
    {
        KconfigResult = new KconfigNode(rootpath);
        kcTreeNow = KconfigResult;
    }
    public void kconfigTreeRoot(KconfigNode node)
    {
        KconfigResult = node;
        kcTreeNow = KconfigResult;
    }

    protected KconfigNode kcTreeNow;
    public KconfigNode kconfigTreeIn(int type, String value)
    {
        return kcTreeNow = kcTreeNow.addChild(type, value, null);
    }
    public KconfigNode kconfigTreeOut()
    {
        return kcTreeNow = kcTreeNow.parent;
    }

    public void kconfigSource(String path)
    {
        if (!Debug) {
            ThreadPool.execute(new KconfigThread(kcTreeNow.addChild(KconfigLexer.T_SOURCE, "",
                    path.replace("$SRCARCH", SRCARCH).replace("\"", ""))));
        }
    }
}
