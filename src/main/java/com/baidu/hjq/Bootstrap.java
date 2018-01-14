package com.baidu.hjq;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Bootstrap {
    private static final LinkedHashMap<String, Score> scoreMap = new LinkedHashMap<>();

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        // 读取指定文件中的词条列表
        String keysPath = System.getProperty("user.dir") + "\\src\\main\\resources\\keys.txt";
        String text = FileUtils.readFileToString(new File(keysPath), "UTF-8");
        String[] keys = text.split("\r\n");
        // 判断是否有重复的词条。如果不需要判断，可以将下面的throw语句注释掉。
        for (int i = 0; i < keys.length; i++) {
            if (scoreMap.containsKey(keys[i])) {
                throw new RuntimeException("重复的词条：" + keys[i]);
            }
            scoreMap.put(keys[i], new Score().setKey(keys[i]));
        }

        // 启动线程池，采用多线程并发分析。
        ExecutorService pool = Executors.newFixedThreadPool(30);
        try {
            Map<String, Future<Score>> futures = new HashMap<>();
            Iterator<Map.Entry<String, Score>> iter = scoreMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, Score> entry = iter.next();
                Future<Score> future = pool.submit(new ParseZhidao(entry.getKey()));
                futures.put(entry.getKey(), future);
            }

            // 多线程分析的结果搜集
            for (String key : futures.keySet()) {
                System.out.println("开始检查词条分析完成情况：" + key);
                Future<Score> future = futures.get(key);
                Score score = future.get();
                scoreMap.get(score.getKey()).setScore(score.getScore()).setType(score.getType());
            }

            System.out.println("================================");
            System.out.println("解析结果========================");
            System.out.println("================================");
            System.out.println();
            System.out.println();
            for (String key : scoreMap.keySet()) {
                System.out.println(scoreMap.get(key).format());
            }
        } finally {
            // 关闭线程池
            pool.shutdown();
        }
    }
}
