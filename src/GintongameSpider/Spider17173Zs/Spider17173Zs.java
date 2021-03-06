package GintongameSpider.Spider17173Zs;

import JavaBean.BasPersonInfo;
import JavaBean.PerKnowledge;
import JavaBean.BasKnowledgeInfo;
import SpiderUtils.SpiderContant;
import SpiderUtils.SpiderUtils;
import dao.impl.BasPersonInfoImpl;
import dao.impl.PerKnowledgeImpl;
import dao.impl.BasKnowledgeInfoDaoImpl;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lenovo on 2017/2/15.
 */
public class Spider17173Zs {
    private static List<BasKnowledgeInfo> basKnowledgeInfoList =new ArrayList<BasKnowledgeInfo>();
    private static List<BasPersonInfo> basPersonInfoList=new ArrayList<BasPersonInfo>();
    private static List<PerKnowledge> perKnowledgeList=new ArrayList<PerKnowledge>();
    private static List<String> ptimeList=new ArrayList<String>();
    private static List<String> authorList=new ArrayList<String>();
    private static BasKnowledgeInfoDaoImpl proknowimpl = new BasKnowledgeInfoDaoImpl();
    private static PerKnowledgeImpl perknowimpl = new PerKnowledgeImpl();
    private static BasPersonInfoImpl basperimpl = new BasPersonInfoImpl();

    public static void main(String args[]) throws IOException, BasKnowledgeInfoDaoImpl.FormatEexception {
        grabWeb();
    }

    public static void grabWeb() throws IOException, BasKnowledgeInfoDaoImpl.FormatEexception {
        System.setProperty("phantomjs.binary.path", "E:\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe");
        WebDriver driver = new PhantomJSDriver();
        int a=1;
        for(int i=429;i<=3008;i++) {
            driver.get("http://search.17173.com/jsp/newslist.jsp?expression=newsChannel%3A10009%20AND%20(newsKind%3A10161)%20AND%20newsClass%3A1&highLights=newsTitle,newsContent&pageNo="+i);
            WebElement web = driver.findElement(By.xpath("/html"));
            int flag = 1;
            int flags=0;
            String html = web.getAttribute("outerHTML");
            Document doc = Jsoup.parse(html);
            Elements linkstime = doc.select("div[style=float: left; width: 200px;]");
            for (Element linktime : linkstime) {
                ptimeList.add(linktime.text());
            }
            Elements linksauthor = doc.select("div[style=float: left; width: 100px; height: 40px;]");
            for (Element linkauthor : linksauthor) {
                authorList.add(linkauthor.text());
            }
            Elements links = doc.select("div.removeItemSign a");
            for (Element link : links) {
                if (flags != 0) {
                    Document doclink=null;
                    String childLink = link.attr("href");
                    try {
                        doclink = Jsoup.connect(childLink).get();
                    }catch (Exception e){
                        continue;
                    }
                    try {
                        dataClean(doclink, childLink, flag);
                    }catch (Exception e1){
                        System.out.println("yichang");
                    }
                }
                System.out.println(a + "+" + i);
                flag++;
                flags++;
                a++;
                System.out.println("-------------------------------------");
            }
            ptimeList.clear();
            authorList.clear();
        }
    }

    public static void dataClean(Document doc,String url,int flag) throws IOException, BasKnowledgeInfoDaoImpl.FormatEexception {
        String tag=null;
        String main=null;
        String type=null;
        String title=null;
        String puuid= UUID.randomUUID().toString();
        String kuuid=UUID.randomUUID().toString();
        String ptime=ptimeList.get(flag);
        String author=authorList.get(flag);
        try {
            type=doc.select("div.crumb.forsetLink1 a[target=_blank]").get(1).text();
        }catch (Exception e1){
            try {
                type = doc.select("div.gb-final-mod-crumbs a").get(2).text();
            }catch (Exception e2){
                try {
                    type = doc.select("div.gb-final-mod-crumbs span.gb-final-cur").get(0).text();
                }catch (Exception e3){
                    type=null;
                }
            }
        }
        if(StringUtils.isNotEmpty(doc.select("h1.article-tit.forsetLink3").text())) {
            title = doc.select("h1.article-tit.forsetLink3").text();
        }else{
            title=doc.select("span.gb-final-cur").text();
        }
        String test=doc.select("span.tag.forsetLink3 script").toString();

        Pattern pat=Pattern.compile(".*var _tags.*",Pattern.CASE_INSENSITIVE);
        Matcher mat=pat.matcher(test);
        while(mat.find()){
            tag=(mat.group(0).replace("var _tags = '","").replace("';","")).trim();
        }

        if(StringUtils.isEmpty(doc.select("div.vr-final-mod-pagination").toString())) {
            if (doc.select("div.vr-article-bd p") != null && doc.select("div.vr-article-bd p").toString().length() > 0) {
                Elements linksmain = doc.select("div.vr-article-bd p");
                for (Element linkmain : linksmain) {
                    if (StringUtils.isNoneEmpty(linkmain.text()) && !linkmain.text().contains("专稿")&& !linkmain.text().contains("转载")) {
                        main = (main + "\r\n" + "<p>" + linkmain.text() + "</p>").replace("null\r\n", "");
                    }
                    if (StringUtils.isNoneEmpty(linkmain.select("img").attr("src"))) {
                        main = (main + "\r\n" + "<img src=" + linkmain.select("img").attr("src") + ">").replace("null\r\n", "");
                    }
                }
            } else {
                Elements linksmain = doc.select("div.gb-final-mod-article.gb-final-mod-article-p2em p");
                for (Element linkmain : linksmain) {
                    if (StringUtils.isNoneEmpty(linkmain.text()) && !linkmain.text().contains("专稿")&& !linkmain.text().contains("转载")) {
                        main = (main + "\r\n" + "<p>" + linkmain.text() + "</p>").replace("null\r\n", "");
                    }
                    if (StringUtils.isNoneEmpty(linkmain.select("img").attr("src"))) {
                        main = (main + "\r\n" + "<img src=" + linkmain.select("a").attr("href") + ">").replace("null\r\n", "");
                    }
                }
            }
        }else{
            if (doc.select("div.vr-article-bd p") != null && doc.select("div.vr-article-bd p").toString().length() > 0) {
                Elements linksmain = doc.select("div.vr-article-bd p");
                for (Element linkmain : linksmain) {
                    if (StringUtils.isNoneEmpty(linkmain.text()) && !linkmain.text().contains("专稿")&& !linkmain.text().contains("转载")) {
                        main = (main + "\r\n" + "<p>" + linkmain.text() + "</p>").replace("null\r\n", "");
                    }
                    if (StringUtils.isNoneEmpty(linkmain.select("img").attr("src"))) {
                        main = (main + "\r\n" + "<img src=" + linkmain.select("img").attr("src") + ">").replace("null\r\n", "");
                    }
                }
            } else {
                Elements linksmain = doc.select("div.gb-final-mod-article.gb-final-mod-article-p2em p");
                for (Element linkmain : linksmain) {
                    if (StringUtils.isNoneEmpty(linkmain.text()) && !linkmain.text().contains("专稿")&& !linkmain.text().contains("转载")) {
                        main = (main + "\r\n" + "<p>" + linkmain.text() + "</p>").replace("null\r\n", "");
                    }
                    if (StringUtils.isNoneEmpty(linkmain.select("img").attr("src"))) {
                        main = (main + "\r\n" + "<img src=" + linkmain.select("a").attr("href") + ">").replace("null\r\n", "");
                    }
                }
            }
            Elements linkschild=doc.select("div.vr-final-mod-pagination li a.page-next");
            for(Element linkchild:linkschild) {
                Document docchild=Jsoup.connect(linkchild.attr("href")).get();
                if (docchild.select("div.vr-article-bd p") != null && docchild.select("div.vr-article-bd p").toString().length() > 0) {
                    Elements linksmain = docchild.select("div.vr-article-bd p");
                    for (Element linkmain : linksmain) {
                        if (StringUtils.isNoneEmpty(linkmain.text()) && !linkmain.text().contains("专稿")&& !linkmain.text().contains("转载")) {
                            main = (main + "\r\n" + "<p>" + linkmain.text() + "</p>").replace("null\r\n", "");
                        }
                        if (StringUtils.isNoneEmpty(linkmain.select("img").attr("src"))) {
                            main = (main + "\r\n" + "<img src=" + linkmain.select("img").attr("src") + ">").replace("null\r\n", "");
                        }
                    }
                } else {
                    Elements linksmain = docchild.select("div.gb-final-mod-article.gb-final-mod-article-p2em p");
                    for (Element linkmain : linksmain) {
                        if (StringUtils.isNoneEmpty(linkmain.text()) && !linkmain.text().contains("专稿")&& !linkmain.text().contains("转载")) {
                            main = (main + "\r\n" + "<p>" + linkmain.text() + "</p>").replace("null\r\n", "");
                        }
                        if (StringUtils.isNoneEmpty(linkmain.select("img").attr("src"))) {
                            main = (main + "\r\n" + "<img src=" + linkmain.select("a").attr("href") + ">").replace("null\r\n", "");
                        }
                    }
                }
            }
        }
        if(type.equals("产业新闻")){
            type="行业动态";
        }else if(type.equals("大陆新闻")){
            type="大陆";
        }else if(type.equals("全球新闻")){
            type="全球";
        }else if(type.equals("手机端-综合推荐")){
            type="手游新闻";
        }else if(type.equals("产业综述")||type.equals("新闻语录")||type.equals("玩家新闻")||type.equals("社会新闻")||type.equals("话题新闻")||type.equals("评论文章")||type.equals("独家策划")||type.equals("综合资讯")||type.equals("头条新闻")){
            type="行业动态";
        }else if(type.equals("财务报告")){
            type="财报";
        }else if(type.equals("新网游动态")){
            type="网游新闻";
        }else if(type.equals("人物")){
            type="人物专栏";
        }else if(type.equals("游戏探索")||type.contains("最新消息")){
            type="产品相关新闻";
        }else if(type.equals("产业数据")){
            type="数字报告";
        }else if(type.equals("网游大观")){
            type="网游新闻";
        }else if(type.equals("网博会最新新闻")){
            type="活动展会";
        }else if(type.equals("17173专访")){
            type="任务专访";
        }else if(type.equals("活动资讯")){
            type="活动展会";
        }
        try {
            storeToDatebase(title, ptime, type, tag, author, main, puuid, kuuid, url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void storeToDatebase(String title,String ptime,String type,String tag,String author,String main,String puuid,String kuuid,String url) throws BasKnowledgeInfoDaoImpl.FormatEexception, SpiderUtils.FormatEexception, ParseException {
        BasKnowledgeInfo basKnowledgeInfo =new BasKnowledgeInfo();
        basKnowledgeInfo.setTitle(title);
        basKnowledgeInfo.setPtime(ptime);
        basKnowledgeInfo.setType(type);
        basKnowledgeInfo.setTag(tag);
        basKnowledgeInfo.setAuthor(author);
        basKnowledgeInfo.setMain(main);
        basKnowledgeInfo.setUrl(url);
        basKnowledgeInfo.setSource("17173");
        basKnowledgeInfo.setUuid(kuuid);
        basKnowledgeInfoList.add(basKnowledgeInfo);

        PerKnowledge perKnow=new PerKnowledge();
        perKnow.setName(author);
        perKnow.setKname(title);
        perKnow.setRtype("原作者");
        perKnow.setPuuid(puuid);
        perKnow.setKuuid(kuuid);
        perKnow.setSource("17173");
        perKnowledgeList.add(perKnow);

        BasPersonInfo basPerson=new BasPersonInfo();
        basPerson.setUuid(puuid);
        basPerson.setName(author);
        basPerson.setSource("17173");
        basPerson.setUrl(url);
        basPersonInfoList.add(basPerson);

        if((basKnowledgeInfoList.size()>0&& basKnowledgeInfoList.size()% SpiderContant.insertBatchContant==0)) {
            Map map=proknowimpl.insertBatchAutoDedup(basKnowledgeInfoList, basPersonInfoList, perKnowledgeList);
            if(((List<Integer>) map.get(4)).get(0)!=0) {
                basperimpl.insertBatch((List<BasPersonInfo>) map.get(1));
                perknowimpl.insertBatch((List<PerKnowledge>) map.get(3));
            }
            basKnowledgeInfoList.clear();
            basPersonInfoList.clear();
            perKnowledgeList.clear();
        }
    }
}
