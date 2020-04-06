package com.lifel.astx.main;

import com.lifel.astx.transform.JavaTransform;
import com.lifel.astx.util.LogUtils;
import org.junit.Test;

public class MainApplication {


    public static void main(String[] args) {
        String originalDirPath = "D:\\lifel\\BCHY\\Java\\test\\workspace_tencent_2020.4.6.15.01\\workspace_tencent\\sc500\\src\\main\\java\\com\\bchy\\sc500\\spo";
        String transformDirPath = "D:\\lifel\\BCHY\\Java\\test\\transfer";
        new JavaTransform(originalDirPath, transformDirPath);
        LogUtils.log("转换完成");
    }

    @Test
    public void test() {

    }

}
