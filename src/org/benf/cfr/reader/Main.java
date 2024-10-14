package org.benf.cfr.reader;

import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.api.ClassFileSource;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.state.ClassFileSourceImpl;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.AnalysisType;
import org.benf.cfr.reader.util.CannotLoadClassException;
import org.benf.cfr.reader.util.getopt.GetOptParser;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.DumperFactory;
import org.benf.cfr.reader.util.output.SinkDumperFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Collection;



public class Main {
    public static String getType(Object o){ //获取变量类型方法
        return o.getClass().toString(); //使用int类型的getClass()方法
        } 

    @SuppressWarnings({"WeakerAccess", "unused"}) // too many people use it - left for historical reasons.
    public static void doClass(DCCommonState dcCommonState, String path, boolean skipInnerClass, DumperFactory dumperFactory) {
        Driver.doClass(dcCommonState, path, skipInnerClass, dumperFactory);
    }

    @SuppressWarnings({"WeakerAccess", "unused"}) // too many people use it - left for historical reasons.
    public static void doJar(DCCommonState dcCommonState, String path, DumperFactory dumperFactory) {
        // 引入 AnalysisType 参数以符合 doJar 的签名要求
        Driver.doJar(dcCommonState, path, AnalysisType.JAR, dumperFactory);
    }
    
    public static void main(String[] args) {
        GetOptParser getOptParser = new GetOptParser();
    
        Options options = null;
        List<String> files = null;
    
        try {
            Pair<List<String>, Options> processedArgs = getOptParser.parse(args, OptionsImpl.getFactory());
            files = processedArgs.getFirst();
            options = processedArgs.getSecond();
    
            // 如果使用 `--stdin`，则无需文件路径
            if (options.optionIsSet(OptionsImpl.STDIN)) {
                files = Collections.emptyList(); // 清除文件列表，因为我们从标准输入读取
            }
    
            // 检查是否需要文件参数
            if (files.isEmpty() && !options.optionIsSet(OptionsImpl.STDIN)) {
                throw new IllegalArgumentException("Insufficient unqualified parameters - provide at least one filename or use --stdin.");
            }
        } catch (Exception e) {
            getOptParser.showHelp(e);
            System.exit(1);
        }
    
        if (options.optionIsSet(OptionsImpl.HELP)) {
            getOptParser.showOptionHelp(OptionsImpl.getFactory(), options, OptionsImpl.HELP);
            return;
        }
    
        if (options.optionIsSet(OptionsImpl.VERSION)) {
            getOptParser.showVersion();
            return;
        }
        if (options.optionIsSet(OptionsImpl.STDIN)) {
            try {

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int bytesRead;
                while ((bytesRead = System.in.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, bytesRead);
                }
                byte[] classBytes = buffer.toByteArray();

                CfrDriver cfrDriver = new CfrDriver.Builder().withBuiltOptions(options).build();
                cfrDriver.analyse_bytes(classBytes);
            } catch (IOException e) {
                System.err.println("Failed to read from stdin: " + e.getMessage());
                e.printStackTrace();
            } catch (CannotLoadClassException e) {
                System.err.println("Failed to load class from bytes: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        }
        // 默认行为 - 使用文件路径
        CfrDriver cfrDriver = new CfrDriver.Builder().withBuiltOptions(options).build();
        cfrDriver.analyse(files);
    }
    
    
}
