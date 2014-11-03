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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import LinuxConfigDiff.antlr.KconfigParser;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        KconfigTree.SRCARCH = "x86";
        KconfigTree.ROOTDIR = Paths.get("d:\\EclipseJava\\LinuxConfigDiff\\trash\\Kconfig").getParent();
        System.out.println(KconfigTree.ROOTDIR);

        //boolean debug = true; /// cmdline
        boolean debug = false;
        if (debug) {
            String path = "d:\\EclipseJava\\LinuxConfigDiff\\trash\\security\\Kconfig";
            PrintStream flog = new PrintStream(Files.newOutputStream(Paths.get("myflog.txt"),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE));
            PrintStream sout = System.out;
            System.setOut(flog);
            KconfigTree.Debug = true;
            KconfigThread kconfig = new KconfigThread(null);
            kconfig.init(path);
            kconfig.Parser.setTrace(true);
            Tracer.printParserTree(kconfig.Parser, kconfig.Parser.getClass(), "input", null, flog, 5);
            flog.close();
            System.setOut(sout);
            System.out.println("FINISH");
            return;
        }

        int cpu = Runtime.getRuntime().availableProcessors();
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
        NormalThreadPool threads = new NormalThreadPool(cpu, cpu, 1, TimeUnit.MINUTES, workQueue,
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
                {
                    System.out.println("Rejected = " + ((KconfigThread)r).getPath());
                }
            });
        KconfigTree.ThreadPool = threads;

        KconfigParser parser = new KconfigParser(null);
        parser.kconfigTreeRoot(null);
        parser.kconfigSource("Kconfig");

        threads.join(100);

        System.out.println("FINISH");
    }
}
