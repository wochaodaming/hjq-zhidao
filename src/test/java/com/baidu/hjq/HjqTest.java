package com.baidu.hjq;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class HjqTest {
    @Test
    public void testClasspath() {
        String path = System.getProperty("user.dir");
        System.out.println(path);

        System.out.println(System.getProperty("user.dir") + "\\src\\main\\resources\\test\\");
    }

    @Test
    public void test() {
        System.out.println("=-----===");
    }
}

