package com.lifel.astx.main;

import com.lifel.astx.transform.LayoutTransform;
import com.lifel.astx.util.LogUtils;
import org.junit.Test;

public class MainApplication {


    public static void main(String[] args) {
        String originalDirPath = "D:\\lifel\\BCHY\\Java\\test\\layout";
        String transformDirPath = "D:\\lifel\\BCHY\\Java\\test\\transfer";
        new LayoutTransform(originalDirPath, transformDirPath);
        LogUtils.log("转换完成");
    }

    @Test
    public void test() {

    }

}
