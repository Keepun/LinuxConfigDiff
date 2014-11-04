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

import java.io.IOException;
import java.nio.file.Paths;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;

import LinuxConfigDiff.antlr.KconfigLexer;
import LinuxConfigDiff.antlr.KconfigParser;

public class KconfigThread implements Runnable
{
    private final KconfigNode node;
    public KconfigLexer lexer;
    public KconfigParser parser;

    public KconfigThread(KconfigNode node)
    {
        this.node = node;
    }

    public String getPath()
    {
        return Paths.get(KconfigTree.ROOTDIR[node.rootpath].toString(), node.path).toString();
    }

    public void init(String path) throws IOException
    {
        ANTLRFileStream kconfigfile = new ANTLRFileStream(path);
        lexer = new KconfigLexer(kconfigfile);
        lexer.setTokenFactory(new KconfigLexerTokenFactory());
        CommonTokenStream comts = new CommonTokenStream(lexer);
        parser = new KconfigParser(comts);
        parser.kconfigTreeRoot(node);
        parser.setBuildParseTree(false);
        parser.setTrace(false);
    }

    @Override
    public void run()
    {
        KconfigErrorListener errors = new KconfigErrorListener();
        try {
            init(getPath());
            lexer.removeErrorListeners();
            lexer.addErrorListener(errors);
            parser.removeErrorListeners();
            parser.addErrorListener(errors);
            parser.input();
        } catch (IOException e) {
            errors.errors.add(e.getMessage());
        }
        if (errors.errors.size() > 0) {
            StringBuilder info = new StringBuilder(getPath() + ":" + System.lineSeparator());
            for (String err : errors.errors) {
                info.append(err + System.lineSeparator());
            }
            System.out.print(info);
        }
    }
}
