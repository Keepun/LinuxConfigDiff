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

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import LinuxConfigDiff.antlr.KconfigParser;

/** @author Keepun */
public class LinuxConfigDiff
{
    public static final String VERSION = "0.5beta";

    @SuppressWarnings("static-access")
    public static void main(String[] args) throws Exception
    {
        GnuParser cmdparser = new GnuParser();
        Options cmdopts = new Options();
        for (String fld : Arrays.asList("shortOpts", "longOpts", "optionGroups")) {
            // hack for printOptions
            java.lang.reflect.Field fieldopt = cmdopts.getClass().getDeclaredField(fld);
            fieldopt.setAccessible(true);
            fieldopt.set(cmdopts, new LinkedHashMap<>());
        }
        cmdopts.addOption("h", "help", false, "Help");
        cmdopts.addOption(OptionBuilder.withLongOpt("config").withArgName("file").hasArg()
                                       .withDescription("Path to .config").create("c"));
        cmdopts.addOption(OptionBuilder.withLongOpt("arch").withArgName("type").hasArg()
                                       .withDescription("Folder name in linux/arch/").create("a"));
        cmdopts.addOption(null, "html", false, "Output in HTML");
        cmdopts.addOption(OptionBuilder.withLongOpt("depth").withArgName("number").hasArg()
                                       .withDescription("Maximum depth in the output").create("d"));
        cmdopts.addOption(OptionBuilder.withLongOpt("threads").withArgName("number").hasArg()
                                       .withDescription("Threads for parser. Default=CPUs=" +
                                                        Runtime.getRuntime().availableProcessors()).create("t"));
        cmdopts.addOption(null, "version", false, "Version " + VERSION);
        CommandLine cmd = cmdparser.parse(cmdopts, args);

        if (cmd.hasOption("version")) {
            System.out.println("LinuxConfigDiff version " + VERSION);
            return;
        }

        if (args.length == 0 || cmd.hasOption("help")) {
            PrintWriter console = new PrintWriter(System.out);
            HelpFormatter cmdhelp = new HelpFormatter();
            cmdhelp.setOptionComparator(new Comparator<Option>() {
                @Override
                public int compare(Option o1, Option o2) {
                    return 0;
                }
            });
            console.println("LinuxConfigDiff [options] [Linux Sources folder] [Linux Sources folder]");
            cmdhelp.printOptions(console, 80, cmdopts, 3, 2);
            console.flush();
            return;
        }

        String paths[] = cmd.getArgs();
        if (paths.length == 0) {
            System.err.println("ERROR: No path to the Linux Sources.");
            return;
        }
        KconfigTree.ROOTDIR = new Path[2];
        for (int x = 0; x < 2 && x < paths.length; x++) {
            KconfigTree.ROOTDIR[x] = Paths.get(paths[x]);
            if (!Files.isDirectory(KconfigTree.ROOTDIR[x])) {
                System.err.println("ERROR: " + KconfigTree.ROOTDIR[x] + " not found.");
                return;
            }
        }

        BufferedReader fconfig = null;
        if (cmd.hasOption("config")) {
            fconfig = Files.newBufferedReader(Paths.get(cmd.getOptionValue("config")));
        }

        KconfigTree.SRCARCH = cmd.getOptionValue("arch", System.getProperty("os.arch", "x86"));
        if (KconfigTree.SRCARCH.equals("amd64")) {
            KconfigTree.SRCARCH = "x86_64";
        }

        //boolean debug = true; /// cmdline
        boolean debug = false;
        if (debug) {
            String path = "d:\\EclipseJava\\LinuxConfigDiff\\trash\\security\\Kconfig";
            PrintStream flog = new PrintStream(Files.newOutputStream(Paths.get("myflog.txt"),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE));
            PrintStream sout = System.out;
            System.setOut(flog);
            KconfigTree.debug = true;
            KconfigThread kconfig = new KconfigThread(null);
            kconfig.init(path);
            kconfig.parser.setTrace(true);
            Tracer.printParserTree(kconfig.parser, kconfig.parser.getClass(), "input", null, flog, 5);
            flog.close();
            System.setOut(sout);
            System.out.println("FINISH");
            return;
        }

        int cpu = Runtime.getRuntime().availableProcessors();
        if (cmd.hasOption("threads")) {
            cpu = Integer.parseInt(cmd.getOptionValue("threads"));
        }
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
        NormalThreadPool threads = new NormalThreadPool(cpu, cpu, 1, TimeUnit.MINUTES, workQueue,
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
                {
                    System.out.println("Rejected = " + ((KconfigThread)r).getPath());
                }
            });
        KconfigTree.threadPool = threads;

        KconfigParser parser[] = new KconfigParser[2];
        for (int x = 0; x < KconfigTree.ROOTDIR.length && x < paths.length; x++) {
            parser[x] = new KconfigParser(null);
            parser[x].kconfigTreeRoot(x);
            parser[x].kconfigSource("Kconfig");
        }

        HashMap<String, String> config = new HashMap<String, String>();
        if (fconfig != null) {
            String cline;
            while ((cline = fconfig.readLine()) != null) {
                String kv[] = cline.split("=");
                if (kv.length == 2) {
                    config.put(kv[0].trim(), kv[1].trim());
                }
            }
        }

        threads.join(100);

        Output out = new Output();
        if (cmd.hasOption("depth")) {
            out.MaxDepth = Integer.parseInt(cmd.getOptionValue("depth"));
        }
        out.print(parser.KconfigResult);
    }
}
