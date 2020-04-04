package com.lifel.astx.main;

import com.lifel.astx.Transform.JavaTransform;
import com.lifel.astx.util.LogUtils;
import org.junit.Test;

public class MainApplication {


    public static void main(String[] args) {
        String originalDirPath = "D:\\lifel\\BCHY\\Java\\test\\java";
        String transformDirPath = "D:\\lifel\\BCHY\\Java\\test\\transfer";
        new JavaTransform(originalDirPath, transformDirPath);
        LogUtils.log("转换完成");
    }

    @Test
    public void test() {

    }

}
