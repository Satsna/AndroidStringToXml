package com.lifel.astx.transform;

import com.lifel.astx.util.FileUtils;
import com.lifel.astx.util.ListUtils;
import com.lifel.astx.util.LogUtils;
import com.lifel.astx.util.StringUtils;

import java.io.*;
import java.util.List;

/**
 * android class文件文字提取自strings.xml
 */
public class JavaTransformBySetText {

    //命名序号,每次加+1
    private int nameIndex = 0;
    //转换后的文件存放路径
    private String transformDirPath;
    //转换后的string.xml文件路径
    private String valuesFilePath;
    //转换后的java文件夹路径
    private String javaDirPath;
    //originalDirPath  原始java文件夹
    private String originalDirPath;


    /**
     * @param originalDirPath  原始java文件夹
     * @param transformDirPath 转换后的文件存放文件夹.包含string java2个文件夹
     */
    public JavaTransformBySetText(String originalDirPath, String transformDirPath) {
        this.originalDirPath = originalDirPath;
        this.transformDirPath = transformDirPath;

        init();
        multiFile(originalDirPath);
    }

    private void init() {
        valuesFilePath = transformDirPath + "/string/string.xml";
        javaDirPath = transformDirPath + "/java";

    }

    public void multiFile(String dirPath) {
        if (StringUtils.isEmpty(dirPath)) {
            LogUtils.log("文件夹路径为空");
            return;
        }
        List<File> fileList = FileUtils.listFilesInDir(dirPath);
        if (ListUtils.isEmpty(fileList)) {
            LogUtils.log("文件夹内无对应文件");
            return;
        }
        for (File file : fileList) {
            if (file.isDirectory()) {
                //创建文件夹
//                String path = javaDirPath + file.getAbsolutePath().replace(originalDirPath, "");
//                FileUtils.createOrExistsDir(path);
//                multiFile(file.getAbsolutePath());
            } else {
                //文件扩展名
                String ext = FileUtils.getFileExtension(file);
                if (StringUtils.isEmpty(ext) || !ext.contains("java")) continue;
                LogUtils.log("转换文件:" + FileUtils.getFileName(file));
                singleFile(file.getAbsolutePath());
            }
        }

    }


    /**
     * 单个文件
     *
     * @param originalFilePath 原始布局文件绝对路径
     */
    private void singleFile(String originalFilePath) {


        nameIndex = 0;
        //判断原始文件是否存在
        if (!FileUtils.isFileExists(originalFilePath)) {
            LogUtils.log("原始文件不存在,路径:" + originalFilePath);
            return;
        }

        //获取不带文件后缀名的文件名
        String originalFileName = FileUtils.getFileNameNoExtension(originalFilePath);

        //创建java文件
        String javaFilePath = javaDirPath + "/" + originalFileName + ".java";
        if (!FileUtils.createFileByDeleteOldFile(javaFilePath)) {
            LogUtils.log("文件创建失败,路径:" + javaDirPath);
            return;
        }

        //创建string.xml文件
        if (!FileUtils.createOrExistsFile(valuesFilePath)) {
            LogUtils.log("文件创建失败,路径:" + valuesFilePath);
            return;
        }

        String filter1 = "setText";
        try {
            //读取流
            InputStreamReader inputReader = new InputStreamReader(new FileInputStream(originalFilePath));
            BufferedReader bf = new BufferedReader(inputReader);
            //values文件写入流
            FileWriter valuesWriter = new FileWriter(valuesFilePath, true);
            BufferedWriter valuesBw = new BufferedWriter(valuesWriter);
//            //layout文件写入流
            FileWriter javaWriter = new FileWriter(javaFilePath);
            BufferedWriter javaBw = new BufferedWriter(javaWriter);

            // 按行读取字符串
            String lineContent;
            while ((lineContent = bf.readLine()) != null) {
                //该行不包含替换内容,写入原始内容
                if (!lineContent.contains(filter1)) {
                    javaBw.write(lineContent + "\r\n");
                    continue;
                }
                //已经引用string值得过滤
//                if (lineContent.contains("@")) {
//                    javaBw.write(lineContent + "\r\n");
//                    continue;
//                }
                if (lineContent.contains(filter1)) transfer(originalFileName, filter1, valuesBw, javaBw, lineContent);
            }
            bf.close();
            inputReader.close();

            valuesBw.flush();
            valuesBw.close();
            valuesWriter.close();

            javaBw.flush();
            javaBw.close();
            javaWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * 转换
     *
     * @param originalFileName 原始布局文件绝对路径
     * @param filter           替换条件
     * @param valuesBw         string文件写入流
     * @param javaBw           java文件写入流
     * @param lineContent      读取的每行文本内容
     * @throws IOException
     */
    public void transfer(String originalFileName, String filter, BufferedWriter valuesBw, BufferedWriter javaBw, String lineContent) throws IOException {
        //下标 过滤条件位置
        int filterIndex = lineContent.indexOf(filter);
        //最后一个双引号位置
        int symbolIndex = lineContent.lastIndexOf("\"");
        String space = "";
        if (filterIndex > 0) space = lineContent.substring(0, filterIndex);
        //要转换的文本内容
        String originalContent = lineContent.substring(filterIndex + filter.length() + 1, lineContent.length() - 2);
        String[] originalArray = originalContent.split("\\+");

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < originalArray.length; i++) {
            String content = originalArray[i];
            if (content.contains("\"")) {
                String subContent = content.substring(content.indexOf("\"") + 1, content.lastIndexOf("\""));
                //空字符过滤
                if (StringUtils.isEmpty(subContent)) {
                    sb.append(content);
                    continue;
                }
                //换行符过滤
                if (subContent.equals("\\n")) {
                    sb.append(content);
                    continue;
                }
                //--空字符过滤
                if (subContent.equals("--")) {
                    sb.append(content);
                    continue;
                }
                //写入string
                String valuesName = originalFileName + "_" + nameIndex;
                String valuesContent = "<string name=\"" + valuesName + "\">" + subContent + "</string>";
                valuesBw.write(valuesContent + "\r\n");
                sb.append("context.getResources().getString(R.string." + valuesName + ")");
                nameIndex++;
            } else {
                sb.append(content);
            }
            if (i < originalArray.length - 1) sb.append("+");
        }

//        LogUtils.log("index=" + filterIndex + "=originalContent=" + originalContent);
        //写入java文件
        //前半部分
        String prefixConet = lineContent.substring(0, filterIndex + filter.length());
        String javaContent = prefixConet + "(" + sb.toString() + ")";
        javaBw.write(javaContent + "\r\n");
    }
}
