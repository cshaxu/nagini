package nagini.utils;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;

public class JavaCommandBuilder {

    List<String> args;
    String javaExec;
    String jvmOptions;
    String classPaths;
    String className;
    List<String> classOptions;

    public JavaCommandBuilder() {
        javaExec = "java";
        jvmOptions = "";
        classPaths = null;
        className = null;
        classOptions = Lists.newArrayList();
    }

    public JavaCommandBuilder setJavaExec(String javaExec) {
        this.javaExec = javaExec;
        return this;
    }

    public JavaCommandBuilder setJvmOption(String jvmOptions) {
        this.jvmOptions = jvmOptions;
        return this;
    }

    public JavaCommandBuilder addJvmOption(String jvmOption) {
        this.jvmOptions = this.jvmOptions + " " + jvmOption;
        return this;
    }

    public JavaCommandBuilder setClassPath(String classPaths) {
        this.classPaths = classPaths;
        return this;
    }

    public JavaCommandBuilder setClassName(String className) {
        this.className = className;
        return this;
    }

    public JavaCommandBuilder addClassOption(String classOption) {
        classOptions.add(classOption);
        return this;
    }

    public JavaCommandBuilder addClassOption(String classOptionName, String classOptionArgument) {
        classOptions.add(classOptionName);
        classOptions.add(classOptionArgument);
        return this;
    }

    public JavaCommandBuilder addClassPath(String classPath) {
        if(this.classPaths == null) {
            this.classPaths = classPath;
        } else {
            this.classPaths = this.classPaths + ":" + classPath;
        }
        return this;
    }

    public JavaCommandBuilder addClassPathByFolder(String folderPath) {
        File folder = new File(folderPath);
        for(File file: folder.listFiles()) {
            addClassPath(file.getPath());
        }
        return this;
    }

    public List<String> getJavaCommand() {
        args = Lists.newArrayList();
        args.add(javaExec);
        for(String jvmOption: jvmOptions.split(" ")) {
            args.add(jvmOption);
        }
        args.add("-cp");
        args.add(classPaths);
        args.add(className);
        args.addAll(classOptions);
        return args;
    }
}
