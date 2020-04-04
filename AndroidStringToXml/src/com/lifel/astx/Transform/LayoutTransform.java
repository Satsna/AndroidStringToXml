package com.lifel.astx.Transform;

import com.lifel.astx.util.FileUtils;
import com.lifel.astx.util.ListUtils;
import com.lifel.astx.util.LogUtils;
import com.lifel.astx.util.StringUtils;

import java.io.*;
import java.util.List;

/**
 * android layout布局文件文字提取自strings.xml
 */
public class LayoutTransform {

    //命名序号,每次加+1
    private int nameIndex = 0;
    //转换后的文件存放路径
    private String transformDirPath;
    //原始布局文件夹路径
    private String originalDirPath;
    //转换后的string.xml文件路径
    private String valuesFilePath;
    //转换后的布局文件夹路径
    private String layoutDirPath;

    /**
     *
     * @param originalDirPath 原始布局文件夹路径
     * @param transformDirPath 转换后的文件存放路径
     */
    public LayoutTransform(String originalDirPath, String transformDirPath) {
        this.transformDirPath = transformDirPath;
        this.originalDirPath = originalDirPath;
        init();
        multiFile();
    }

    private void init() {
        valuesFilePath = transformDirPath + "/string/string.xml";
        layoutDirPath = transformDirPath + "/layout";

    }

    public void multiFile() {
        if (StringUtils.isEmpty(originalDirPath)) {
            LogUtils.log("文件夹路径为空");
            return;
        }
        List<File> fileList = FileUtils.listFilesInDir(originalDirPath);
        if (ListUtils.isEmpty(fileList)) {
            LogUtils.log("文件夹内无对应文件");
            return;
        }
        for (File file : fileList) {
            //文件扩展名
            String ext = FileUtils.getFileExtension(file);
            if (StringUtils.isEmpty(ext) || !ext.contains("xml")) continue;
            LogUtils.log("转换文件:" + FileUtils.getFileName(file));
            singleFile(file.getAbsolutePath());
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

        //创建布局文件
        String layoutFilePath = layoutDirPath + "/" + originalFileName + ".xml";
        if (!FileUtils.createFileByDeleteOldFile(layoutFilePath)) {
            LogUtils.log("文件创建失败,路径:" + layoutDirPath);
            return;
        }

        //创建string.xml文件
        if (!FileUtils.createOrExistsFile(valuesFilePath)) {
            LogUtils.log("文件创建失败,路径:" + valuesFilePath);
            return;
        }

        String filter1 = "android:text=";
        String filter2 = "android:hint=";
        try {
            //读取流
            InputStreamReader inputReader = new InputStreamReader(new FileInputStream(originalFilePath));
            BufferedReader bf = new BufferedReader(inputReader);
            //values文件写入流
            FileWriter valuesWriter = new FileWriter(valuesFilePath, true);
            BufferedWriter valuesBw = new BufferedWriter(valuesWriter);
//            //layout文件写入流
            FileWriter layoutWriter = new FileWriter(layoutFilePath);
            BufferedWriter layoutBw = new BufferedWriter(layoutWriter);

            // 按行读取字符串
            String lineContent;
            while ((lineContent = bf.readLine()) != null) {
                //该行不包含替换内容,写入原始内容
                if (!lineContent.contains(filter1) && !lineContent.contains(filter2)) {
                    layoutBw.write(lineContent + "\r\n");
                    continue;
                }
                //已经引用string值得过滤
                if (lineContent.contains("@")) {
                    layoutBw.write(lineContent + "\r\n");
                    continue;
                }
                if (lineContent.contains(filter1)) transfer(originalFileName, filter1, valuesBw, layoutBw, lineContent);
                if (lineContent.contains(filter2)) transfer(originalFileName, filter2, valuesBw, layoutBw, lineContent);
            }
            bf.close();
            inputReader.close();


            valuesBw.flush();
            valuesBw.close();
            valuesWriter.close();

            layoutBw.flush();
            layoutBw.close();
            layoutWriter.close();
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
     * @param layoutBw         layout布局文件写入流
     * @param lineContent      读取的每行文本内容
     * @throws IOException
     */
    public void transfer(String originalFileName, String filter, BufferedWriter valuesBw, BufferedWriter layoutBw, String lineContent) throws IOException {
        //下标 过滤条件位置
        int filterIndex = lineContent.indexOf(filter);
        //最后一个双引号位置
        int symbolIndex = lineContent.lastIndexOf("\"");
        String space = "";
        if (filterIndex > 0) space = lineContent.substring(0, filterIndex);
        //要转换的文本内容
        String originalContent = lineContent.substring(filterIndex + filter.length() + 1, symbolIndex);
//        LogUtils.log("index=" + filterIndex + "=originalContent=" + originalContent);
        if (originalContent.equals("--")) {
            //--空字符过滤
            layoutBw.write(lineContent + "\r\n");
            return;
        }
        if (originalContent.equals("- -")) {
            //- -空字符过滤
            layoutBw.write(lineContent + "\r\n");
            return;
        }

        //写入string
        String valuesName = originalFileName + "_" + nameIndex;
        String valuesContent = "<string name=\"" + valuesName + "\">" + originalContent + "</string>";
        valuesBw.write(valuesContent + "\r\n");
        //写入layout
        String suffix = lineContent.substring(symbolIndex + 1);//后缀 后半部分
        String layoutContent = space + filter + "\"@string/" + valuesName + "\"" + suffix;
        layoutBw.write(layoutContent + "\r\n");
        nameIndex++;
    }
}
