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
public class JavaTransform {

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
    public JavaTransform(String originalDirPath, String transformDirPath) {
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
                String path = javaDirPath + file.getAbsolutePath().replace(originalDirPath, "");
                FileUtils.createOrExistsDir(path);
                multiFile(file.getAbsolutePath());
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
        String javaFilePath = javaDirPath + originalFilePath.replace(originalDirPath, "");
//        String javaFilePath = javaDirPath + "/" + originalFileName + ".java";
        if (!FileUtils.createFileByDeleteOldFile(javaFilePath)) {
            LogUtils.log("文件创建失败,路径:" + javaDirPath);
            return;
        }

        //创建string.xml文件
        if (!FileUtils.createOrExistsFile(valuesFilePath)) {
            LogUtils.log("文件创建失败,路径:" + valuesFilePath);
            return;
        }

        String filter1 = "\"";
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
                if (isContinue(filter1, javaBw, lineContent)) continue;
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
     * while循环 是否跳过当前行
     *
     * @param filter1     过滤条件
     * @param javaBw      java文件写入流
     * @param lineContent 读取的每行文本内容
     * @return true 跳过当前行，false 不跳过
     * @throws IOException
     */
    private boolean isContinue(String filter1, BufferedWriter javaBw, String lineContent) throws IOException {
        if(lineContent.contains("Bundle bundle")){
            LogUtils.log("aa");
        }
        //该行不包含替换内容,写入原始内容
        if (!lineContent.contains(filter1)) {
            javaBw.write(lineContent + "\r\n");
            return true;
        }
        //判断字符串是否包含中文
        if (!StringUtils.isContainChinese(lineContent)) {
            javaBw.write(lineContent + "\r\n");
            return true;
        }
        //过滤注释
        if (lineContent.replaceAll(" ", "").indexOf("//") == 0) {
            javaBw.write(lineContent + "\r\n");
            return true;
        }
        //过滤Log.
        if (lineContent.contains("Log")) {
            javaBw.write(lineContent + "\r\n");
            return true;
        }
        //过滤@Suppress
        if (lineContent.contains("@Suppress")) {
            javaBw.write(lineContent + "\r\n");
            return true;
        }
        //过滤public
        if (lineContent.contains("public ")) {
            javaBw.write(lineContent + "\r\n");
            return true;
        }
        //过滤private
        if (lineContent.contains("private ")) {
            javaBw.write(lineContent + "\r\n");
            return true;
        }
        return false;
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
    private void transfer(String originalFileName, String filter, BufferedWriter valuesBw, BufferedWriter javaBw, String lineContent) throws IOException {
        StringBuffer sb = new StringBuffer();

        boolean whileRun = true;
        int nextPostion = 0;
        int stopPostion = lineContent.lastIndexOf("\"");
        while (whileRun) {
            if (nextPostion > 0) nextPostion = nextPostion + 1;
            int startPosition = lineContent.indexOf("\"", nextPostion + 1);
            //获取无需转义的部分
            String originalSubContent = lineContent.substring(nextPostion, startPosition);
            sb.append(originalSubContent);

            int endPosition = lineContent.indexOf("\"", startPosition + 1);
            //获取需要转义的字符串
            String transformSubContent = lineContent.substring(startPosition + 1, endPosition);
            //过滤 单双引号 "\"" 特殊情况
            if (transformSubContent.equals("\\")) {
                endPosition = endPosition + 1;
            }
            if (!filterSymbol(sb, transformSubContent)) {
                //写入string
                String valuesName = originalFileName + "_" + nameIndex;
                String valuesContent = "<string name=\"" + valuesName + "\">" + transformSubContent + "</string>";
                valuesBw.write(valuesContent + "\r\n");
                //文件类型
                String clazzType = null;
                if (originalFileName.length() - 8 > 0) {
                    clazzType = originalFileName.substring(originalFileName.length() - 8);
                }
                if (!StringUtils.isEmpty(clazzType) && clazzType.equals("Activity")) {
                    //Activity添加
                    sb.append("getString(R.string." + valuesName + ")");
                } else if (!StringUtils.isEmpty(clazzType) && clazzType.equals("Fragment")) {
                    //Fragment添加
                    sb.append("getString(R.string." + valuesName + ")");
                } else {
                    //其他类型
                    sb.append("context.getString(R.string." + valuesName + ")");
                }
                nameIndex++;
            }
            nextPostion = endPosition;
            if (nextPostion == stopPostion) {
                whileRun = false;
                originalSubContent = lineContent.substring(nextPostion + 1, lineContent.length());
                sb.append(originalSubContent);
            }
        }

        String javaContent = sb.toString();
        javaBw.write(javaContent + "\r\n");
    }

    /**
     * 过滤特定字符
     *
     * @param sb
     * @param transformSubContent
     * @return true, 为特定字符。进行过滤。false 非特定特定进行转换
     */
    private boolean filterSymbol(StringBuffer sb, String transformSubContent) {
        if (StringUtils.isEmpty(transformSubContent)) {
            //空字符过滤
            sb.append("\"\"");
            return true;
        } else if (transformSubContent.equals("\\")) {
            sb.append("\"\\\"\"");
            return true;
        } else if (transformSubContent.equals(" ")) {
            //过滤空格
            sb.append("\" \"");
            return true;
        } else if (transformSubContent.equals("%")) {
            //过滤%
            sb.append("\"%\"");
            return true;
        } else if (transformSubContent.equals("/")) {
            //过滤/
            sb.append("\"/\"");
            return true;
        } else if (transformSubContent.equals("_")) {
            //过滤/
            sb.append("\"_\"");
            return true;
        } else if (transformSubContent.equals("[")) {
            //过滤[
            sb.append("\"[\"");
            return true;
        } else if (transformSubContent.equals("]")) {
            //过滤]
            sb.append("\"]\"");
            return true;
        } else if (transformSubContent.equals(":")) {
            //过滤:
            sb.append("\":\"");
            return true;
        } else if (transformSubContent.equals("-")) {
            //过滤-
            sb.append("\"-\"");
            return true;
        } else if (transformSubContent.equals(".")) {
            //过滤.
            sb.append("\".\"");
            return true;
        } else if (transformSubContent.equals("\\\"")) {
            //过滤"\"" 特殊情况 单双引号
            sb.append("\"\\\"\"");
            return true;
        } else if (transformSubContent.equals("\\n")) {
            //换行符过滤
            sb.append("\"\\n\"");
            return true;
        } else if (transformSubContent.equals("--")) {
            //--空字符过滤
            sb.append("\"--\"");
            return true;
        } else if (transformSubContent.equals("- -")) {
            //- -空字符过滤
            sb.append("\"- -\"");
            return true;
        } else if (transformSubContent.equals("<")) {
            //<过滤
            sb.append("\"<\"");
            return true;
        } else if (transformSubContent.equals(">")) {
            //>过滤
            sb.append("\">\"");
            return true;
        } else if (transformSubContent.equals("{")) {
            //{过滤
            sb.append("\"{\"");
            return true;
        } else if (transformSubContent.equals("}")) {
            //}过滤
            sb.append("\"}\"");
            return true;
        } else if (transformSubContent.equals("0")) {
            //0过滤
            sb.append("\"0\"");
            return true;
        } else if (transformSubContent.equals("1")) {
            //1过滤
            sb.append("\"1\"");
            return true;
        } else if (transformSubContent.equals("-1")) {
            //-1过滤
            sb.append("\"-1\"");
            return true;
        }
        return false;
    }
}
