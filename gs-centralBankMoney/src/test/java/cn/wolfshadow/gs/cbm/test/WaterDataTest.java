package cn.wolfshadow.gs.cbm.test;


import cn.wolfshadow.gs.cbm.service.DbWaterLogService;
import cn.wolfshadow.gs.common.entity.WaterLog;
import cn.wolfshadow.gs.common.exception.NumberConvertException;
import cn.wolfshadow.gs.common.util.DateUtil;
import cn.wolfshadow.gs.common.util.FileUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class WaterDataTest {



    @Autowired
    private DbWaterLogService waterLogService;

    private final String webRoot = "http://www.pbc.gov.cn";
    private String url = "http://www.pbc.gov.cn/zhengcehuobisi/125207/125213/125431/125475/17081/index%s.html";
    private final int pageSize = 113;
    private final int totalCount = 2245;
    private final String listPageDirectory = "D:\\File\\gold_spider\\water\\list";
    private final String detailPageDirectory = "D:\\File\\gold_spider\\water\\detail";
    private final String characterSet = "UTF-8";

    @Test
    public void downLoadPages(){
        //1、下载列表页面
        // saveListPage();

        //2、解析列表页面获取详情页面的链接
        //3、下载详情页面
        String cid = "c882cba8132c626ed9de2175460a9aef681527d8d46042866df435a4ca620fc6e8fc43f0644c02d910d4c6462f8ce93935843ba62d42bf71b651ad57c1f39ef3d6699ee5757e4f3cadcd9b07f8be9eb0";
        String cookie = getCookie();
        System.err.println(cookie);
        //String s = httpGet(String.format(url, 1),cookie);
        /*List<String> list = ();
        Collections.reverse(list);
        for (int i = 0; i < list.size(); i++) {
            String href = list.get(i);
            String saveName = "detailPage"+i+".html";
            boolean b = saveDetailPage(href, saveName,cid);
            if (!b) break;
            // System.err.println(saveName);
        }*/
        //boolean b = saveDetailPage("/zhengcehuobisi/125207/125213/125431/125475/4078485/index.html", "detailPage2244.html",cid);
        //System.err.println(list.size());


        //4、解析详情页面并保存到DB（重点）
        int counter = 0;
        String name = null;

        /*try {
            for (int i = 0; i < totalCount; i++) {
                name = "detailPage"+i+".html";
                List<WaterLog> waterLogs = parseDetail(name);
                if (waterLogs == null || waterLogs.isEmpty()) continue;
                waterLogService.insertBatch(waterLogs);
                counter++;
            }
        } catch (Exception e) {
            log.error("异常页面：{}",name);
        } finally {
            log.info("已解析页面数量：{}",counter);
        }*/
        /*List<WaterLog> waterLogs = parseDetail("detailPage2244.html");
        waterLogService.insertBatch(waterLogs);*/


    }

    @SneakyThrows
    private void saveListPage(){
        for (int i = 1; i <= pageSize; i++) {
            String name = "listPage%s.html";
            name = String.format(name,i);

            String realUrl = String.format(url, i);
            String cookie = "";
            String htmlStr = httpGet(realUrl,cookie);

            File file = FileUtil.getFile(listPageDirectory, name);
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            FileWriter writer = new FileWriter(file);
            writer.write(htmlStr);
            writer.flush();
            writer.close();
        }
    }

    @SneakyThrows
    private List<String> listHref(){
        List<String> result = new ArrayList<>();
        for (int i = 1; i <= pageSize; i++) {
            String name = "listPage%s.html";
            name = String.format(name,i);
            File file = FileUtil.getFile(listPageDirectory, name);
            FileInputStream fis = new FileInputStream(file);
            Document document = Jsoup.parse(file, "UTF-8");
            Elements elements = document.select("a");
            //Elements elements = document.getElementsByTag("a");
            Iterator<Element> iterator = elements.iterator();
            while (iterator.hasNext()){
                Element next = iterator.next();
                String href = next.attr("href");
                
                // /zhengcehuobisi/125207/125213/125431/125475/4078160/index.html
                String rexStr = "/zhengcehuobisi/\\d+/\\d+/\\d+/\\d+/\\d+/index\\.html";
                Pattern compile = Pattern.compile(rexStr);
                Matcher matcher = compile.matcher(href);
                if (!matcher.matches()) continue;

                result.add(href);
                //System.out.println(href);
            }
        }
        return result;
    }

    @SneakyThrows
    private static String getCookie(){
        String cookieUrl = "http://www.pbc.gov.cn/WZWSREL3poZW5nY2VodW9iaXNpLzEyNTIwNy8xMjUyMTMvMTI1NDMxLzEyNTQ3NS8xNzA4MS9pbmRleDEuaHRtbA==?wzwschallenge=V1pXU19DT05GSVJNX1BSRUZJWF9MQUJFTDM4NDUxNjY=";

        //初始化HttpClient
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建HttpGet
        HttpGet httpGet = new HttpGet(cookieUrl);
        RequestConfig config = RequestConfig.custom().setRedirectsEnabled(false).build();
        httpGet.setConfig(config);
        httpGet.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        httpGet.setHeader("Accept-Encoding","gzip, deflate");
        httpGet.setHeader("Accept-Language","zh-CN,zh;q=0.9");
        httpGet.setHeader("Connection","keep-alive");
        httpGet.setHeader("Cookie","wzws_cid=879ffb823e3749250a6248db255bad346682161141c2071caa513062b2a5e1d238546fd7914f67e764ebf3975ee12e06756b2ca620e8c42cf4a81643b639cdd4703059283e8f4569142d62d0f2d375eef2ad6fa881d8709869b5b78511f25217");
        httpGet.setHeader("Host","www.pbc.gov.cn");
        httpGet.setHeader("Referer","http://www.pbc.gov.cn/zhengcehuobisi/125207/125213/125431/125475/17081/index1.html");
        httpGet.setHeader("Upgrade-Insecure-Requests","1");
        httpGet.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.75 Safari/537.36");

        //发起请求，获取response对象
        CloseableHttpResponse response = httpClient.execute(httpGet);
        Header[] headers = response.getHeaders("Set-Cookie");
        if (headers.length < 1) return null;



        return headers[0].getValue();
    }

    @SneakyThrows
    private static String httpGet(String url, String cookie){
        String characterSet = "UTF-8";
        //初始化HttpClient
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建HttpGet
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        httpGet.setHeader("Accept-Encoding","gzip, deflate");
        httpGet.setHeader("Accept-Language","zh-CN,zh;q=0.9");
        httpGet.setHeader("Connection","keep-alive");
        httpGet.setHeader("Cookie",cookie);
        httpGet.setHeader("Host","www.pbc.gov.cn");
        httpGet.setHeader("Referer","url");
        httpGet.setHeader("Upgrade-Insecure-Requests","1");
        httpGet.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.75 Safari/537.36");

        //发起请求，获取response对象
        CloseableHttpResponse response = httpClient.execute(httpGet);
        //获取请求状态码
        //int statusCode = response.getStatusLine().getStatusCode();
        //获取返回数据实体对象
        HttpEntity entity = response.getEntity();
        //转为字符串
        String result = EntityUtils.toString(entity,characterSet);
        return result;
    }

    @SneakyThrows
    private boolean saveDetailPage(String url, String saveName, String cid){
        //初始化HttpClient
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建HttpGet
        HttpGet httpGet = new HttpGet(webRoot+url);
        httpGet.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        httpGet.setHeader("Accept-Encoding","gzip, deflate");
        httpGet.setHeader("Accept-Language","zh-CN,zh;q=0.9");
        httpGet.setHeader("Cache-Control","max-age=0");
        httpGet.setHeader("Connection","keep-alive");
        httpGet.setHeader("Cookie","wzws_cid="+cid);
        httpGet.setHeader("Host","www.pbc.gov.cn");
        //httpGet.setHeader("If-Modified-Since","Mon, 17 Aug 2020 01:20:22 GMT");
        //httpGet.setHeader("If-None-Match","W/\"61d84-7786-5ad08930d0180\"");
        httpGet.setHeader("Referer",url);
        httpGet.setHeader("Upgrade-Insecure-Requests","1");
        httpGet.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.75 Safari/537.36");

        //发起请求，获取response对象
        CloseableHttpResponse response = httpClient.execute(httpGet);
        //获取请求状态码
        //response.getStatusLine().getStatusCode();
        //获取返回数据实体对象
        HttpEntity entity = response.getEntity();
        //转为字符串
        String htmlStr = EntityUtils.toString(entity,characterSet);

        if (htmlStr.contains("请开启JavaScript并刷新该页")) return false;

        File file = FileUtil.getFile(detailPageDirectory, saveName);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        FileWriter writer = new FileWriter(file);
        writer.write(htmlStr);
        writer.flush();
        writer.close();

        return true;
    }

    @SneakyThrows
    private List<WaterLog> parseDetail(String saveName){
        //saveName = "detailPage2240.html";

        List<WaterLog> result = new ArrayList<>();


        File file = FileUtil.getFile(detailPageDirectory, saveName);
        Document document = Jsoup.parse(file, characterSet);
        //标题
        String title = document.title();

        log.info("title: {}",title);
        //公告时间
        Element shijian = document.body().getElementById("shijian");
        String timeStr = shijian.text();
        Date noticeTime = DateUtil.toDate(timeStr);
        String dateStr = DateUtil.getDateStr(noticeTime, DateUtil.YYYY_MM_DD);
        log.info("公告时间： {},保存格式： {}",timeStr,dateStr);

        /**
         * 关键信息
         */
        Element zoom = document.body().getElementById("zoom");
        //央妈放水正文
        Elements children = zoom.children();
        if (children.size() == 1) children = children.get(0).children();
        String content = null;//第1个非居中的p标签正文

        //log.info("央妈放水正文： {}",content);

        Iterator<Element> childrenI = children.iterator();
        int method = -1;//放水方式：-2，正回购；-1，发债；0，逆回购；1，中期借贷便利（MLF）
        StringBuffer contentBuffer = new StringBuffer();
        while (childrenI.hasNext()) {
            Element next = childrenI.next();
            String tagName = next.tagName();
            if (tagName.equals("p")) {
                String align = next.attr("align");
                String text = next.text();
                if ("center".equals(align)) {//假设所有放水类型的表格标题都是居中对齐（目前来看都是这样的）{
                    log.info("央妈放水方式： {}",text);
                    //放水方式：0，逆回购；1，中期借贷便利（MLF）
                    if (text.contains("MLF")){
                        method = 1;
                    }else if (text.contains("逆回购")) {
                        method = 0;
                    }else if (text.contains("正回购")){
                        method = -2;
                    }else if (text.contains("央行票据")){
                        method = -1;
                    }else{ //其他情况：央行发债操、正回购
                        //method = -1;
                    }
                }else {//非居中的p标签认为都是正文
                    if (method == -1 && text.contains("正回购")){//如果正文包含“正回购”，也有可能正回购只是其中一项，需要继续看下面居中p标签内容或是离table最近p标签的内容
                        method = -2;
                    }else if (method == -2 && text.contains("央行票据")){
                        method = -1;
                    }
                    contentBuffer.append(text).append("\n");
                }
            }else if ("table".equals(tagName) || "div".equals(tagName)){
                //有些table可能被div包裹一层
                if ("div".equals(tagName)) {
                    Elements tables = next.getElementsByTag("table");
                    if (tables == null || tables.size() < 1) continue;
                    next = tables.first();
                }

                Elements trs = next.getElementsByTag("tr");
                if (trs == null || trs.size() < 2) continue;
                for (int i = 1; i < trs.size(); i++) {
                    Elements tds = trs.get(i).getElementsByTag("td");
                    WaterLog waterLog = new WaterLog();
                    waterLog.setTitle(title);
                    waterLog.setNoticeTime(noticeTime.getTime());
                    waterLog.setNoticeDate(dateStr);
                    waterLog.setMethod(method);
                    content = contentBuffer.toString();
                    waterLog.setContent(content);
                    for (int j = 0; j < tds.size(); j++) {
                        Element element1 = tds.get(j);
                        parseCoreData(waterLog,noticeTime, element1.text());
                    }
                    result.add(waterLog);
                }

            }
        }


        return result;
    }

    private void parseCoreData(WaterLog waterLog, Date noticeTime, String text){
        if (text.endsWith("亿") || text.endsWith("亿元")){
            double doubleValue = Double.parseDouble(text.replaceAll("亿[元,]", ""));
            int intValue = (int)Math.round(doubleValue);//四舍五入取整
            waterLog.setWater(intValue);
        }else if (text.endsWith("天") || text.endsWith("年") || text.endsWith("月")){
            int day = 0;
            if (text.endsWith("天")){
                day = Integer.parseInt(text.replace("天", ""));
            }else if (text.endsWith("年")){
                //todo 不清楚怎么算，需要查资料
                day = Integer.parseInt(text.replace("年", ""));
                day *= 365;
            }else if (text.endsWith("月")){
                String month = text.replaceAll("[个,]月", "");
                day = NumberConvertEnum.getNumberByWord(month);
                day *= 30;
            }
            Date endDate = DateUtil.add(noticeTime, day);
            waterLog.setEndTime(endDate.getTime());
            waterLog.setEndDate(DateUtil.getDateStr(endDate,DateUtil.YYYY_MM_DD));
            waterLog.setTimeLimit(text);
            waterLog.setCreateTime(new Date());
        }else {
            waterLog.setInterestRate(text);
        }
    }

}

enum NumberConvertEnum{

    NUM_1("一",1),
    NUM_2("二",2),
    NUM_3("三",3),
    NUM_4("四",4),
    NUM_5("五",5),
    NUM_6("六",6),
    NUM_7("七",9),
    NUM_8("八",8),
    NUM_9("九",9),
    NUM_10("十",10);

    private String word;
    private int number;

    NumberConvertEnum(String word, int number){
        this.word = word;
        this.number = number;
    }

    public static int getNumberByWord(String word){
        int num = 0;
        try {
            num = Integer.parseInt(word);//如果是数字，则可以直接转换
        } catch (NumberFormatException e) {
            NumberConvertEnum[] values = NumberConvertEnum.values();
            for(NumberConvertEnum value : values){
                if (word.equals(value.getWord()))
                    return value.getNumber();
            }
            throw new NumberConvertException();
        }
        return num;
    }

    public String getWord() {
        return word;
    }
    public int getNumber() {
        return number;
    }
}