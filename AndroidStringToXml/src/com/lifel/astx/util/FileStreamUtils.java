package com.lifel.astx.util;

import java.io.*;
import java.util.ArrayList;

/**
 * 文件流帮助类
 */
public class FileStreamUtils {

    /**
     * 文本文件按行读取
     *
     * @param name
     * @return
     */
    public static ArrayList<String> toListByInputStreamReader(String name) {
        // 使用ArrayList来存储每行读取到的字符串
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            File file = new File(name);
            if (!file.exists()) return null;
            InputStreamReader inputReader = new InputStreamReader(new FileInputStream(file));
            BufferedReader bf = new BufferedReader(inputReader);
            // 按行读取字符串
            String str;
            while ((str = bf.readLine()) != null) {
                arrayList.add(str);
            }
            bf.close();
            inputReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return arrayList;
    }

}
