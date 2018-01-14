package com.baidu.hjq;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HttpClient读取页面的使用例子
 *
 * @author 老紫竹(java2000.net)
 */
public class ParseZhidao implements Callable<Score> {
    private final String key;

    public ParseZhidao(String key) {
        this.key = key;
    }

    public Score parseZhidao() throws IOException {
        SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = simpleFormatter.format(new Date());
        // 测试过程中，用test替代dateString。
        String pageString = getPage("test", this.key);

        // 【规则】对网页代码，按照c-result来分割。因为各个条目是放在class为c-result的<div>中。
        String[] pss = pageString.split(" c-result");

        Score score = new Score().setKey(this.key);
        loop:
        for (int i = 1; i < pss.length; i++) {
            String s = pss[i];
            // 【规则】当存在这两个字符串的时候，可判定为百度知道的条目。
            if (s.contains("zhidao.baidu.com") || s.contains("百度知道")) {
                // 【规则】判断是第几条。笨方法，匹配其order值。
                for (int j = 1; j < 15; j++) {
                    if (s.contains(" order=\"" + j + "\"")) {
                        score.setScore(j);

                        // 【规则】是否为TOP类型
                        if (j == 1){
                            if (s.contains("关于这条结果")){
                                score.setType(ZhidaoType.Top);
                            }
                        }
                        break loop;
                    }
                }
            }
        }
        return score;
    }

    /**
     * 抓取网页信息使用 get请求
     *
     * @param key
     * @throws IOException
     */
    public String getPage(String timestamp, String key) throws IOException {
        String path = System.getProperty("user.dir") + "\\src\\main\\resources\\" + timestamp + "\\";
        // 创建httpClient实例
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String encode = URLEncoder.encode(key, "UTF-8");
        // 创建httpGet实例
        // 模拟手机登录的url格式
        String[] urls = {"https://www.baidu.com/ssid=c795ced2cbc0d6aebaf3d000/from=844b/s?word=", "&ts=2550328&t_kt=0&ie=utf-8&fm_kl=021394be2f&rsv_iqid=0534002157-1&rsv_t=94ebhfNgfIlEyEyF62%252FoXSkyv5uPkryRJ%252BEA8Yyz9qNG65rNAj9uX8AMyA&sa=ib&ms=1&rsv_pq=0534002157&rsv_sug4=2771&inputT=1255&ss=100&tj=1"};
        String url = urls[0] + encode + urls[1];
        //String testUrl = "http://www.tuicool.com";
        HttpGet httpGet = new HttpGet(url);
        // 模拟浏览器的属性，否则同浏览器的效果不同。
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");
        CloseableHttpResponse response = httpClient.execute(httpGet);

        String result = null;
        if (response != null) {
            HttpEntity entity = response.getEntity();   // 获取网页内容
            result = EntityUtils.toString(entity, "UTF-8");
            FileUtils.writeStringToFile(new File(path + key + "-source.html"), result, Charset.defaultCharset());
//            System.out.println("网页内容：" + result);
//            result = filteScript(result, "<head", "</head>");
            result = filterSpecialChar(result);
            result = filteScript(result, "<script", "</script>");
            result = filteScript(result, "<style", "</style>");
            result = filteScript(result, "<form", "</form>");
            result = filteScript(result, "<noscript>", "</noscript>");
            result = filter2(result, " href=\"", '"');
            result = filter2(result, " data-float-href=\"", '"');
            result = filter2(result, " data-sflink=\"", '"');
            result = filter2(result, " data-url=\"", '"');
            result = filter2(result, " data-log=\"", '"');
            result = filter2(result, " data-imagedelaysrc=\"", '"');
            result = filter2(result, " data-sf-href=\"", '"');
            result = filter2(result, "<img src=\"", '"');

            FileUtils.writeStringToFile(new File(path + key + "-simple.html"), result, Charset.defaultCharset());
        }
        if (response != null) {
            response.close();
        }
        if (httpClient != null) {
            httpClient.close();
        }
        return result;
    }

    /**
     * 过滤script和style等代码
     *
     * @param source
     * @param scriptHead
     * @param scriptFoot
     * @return
     */
    public String filteScript(String source, String scriptHead, String scriptFoot) {
        String lishenghao2017 = "lishenghao2017";
        String txt = source.replace(scriptHead, lishenghao2017 + scriptHead);
        txt = txt.replace(scriptFoot, scriptFoot + lishenghao2017);
        String[] split = txt.split(lishenghao2017);

        List<String> result = new ArrayList<>();
        StringBuffer resultString = new StringBuffer();
        for (int i = 0; i < split.length; i++) {
            if (!split[i].startsWith(scriptHead)) {
                resultString.append(split[i]);
            }
        }
        return resultString.toString();
    }

    public String filter2(String source, String start, char firstEnd) {
        String[] ss = source.split(start);
        StringBuilder sb = new StringBuilder();
        sb.append(ss[0]);
        for (int i = 1; i < ss.length; i++) {
            sb.append(start);
            char[] cs = ss[i].toCharArray();
            boolean flag = false;
            for (int i1 = 0; i1 < cs.length; i1++) {
                if (flag) {
                    sb.append(cs[i1]);
                } else {
                    if (cs[i1] == firstEnd) {
                        sb.append(firstEnd);
                        sb.append(" ");
                        flag = true;
                    }
                }
            }
        }
        return sb.toString();
    }

    public String filterSpecialChar(String source) {
        Pattern p = Pattern.compile("\r|\n");
        Matcher m = p.matcher(source);
        String s = m.replaceAll("");
        return s.substring(s.indexOf("<"));
    }

    @Override
    public Score call() throws Exception {
        System.out.println("开始分析词条：" + this.key);
        return this.parseZhidao();
    }
}