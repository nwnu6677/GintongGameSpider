/*=============================================================================
#
#     FileName: SignTest.java
#         Desc: qcloud 签名测试工具
#
#       Author: gavinyao
#        Email: gavinyao@tencent.com
#
#      Created: 2014-12-19 11:29:41
#      Version: 0.0.1
#      History:
#               0.0.1 | gavinyao | 2014-12-19 11:29:41 | initialization
#
=============================================================================*/
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignTest {

    // SecretId 和 SecretKey
    private static final String SECRET_ID = "AKIDzXJA0vffBWfswxty34Qi09NSuTuXmSq4";
    private static final String SECRET_KEY = "CF73JoLwfitv6JnFWcXIvNtVyFMXkx1D";

    public static void main(String[] args) {

        TreeMap<String, Object> requestParams = new TreeMap<String, Object>();
        requestParams.put("SecretId", SECRET_ID);
        requestParams.put("Region", "gz");
        requestParams.put("Timestamp", System.currentTimeMillis() / 1000);
        Random rand = new Random();
        requestParams.put("Nonce", rand.nextInt(Integer.MAX_VALUE));

        //requestParams.put("channel", "CHnews_news_game");
        requestParams.put("type", "1");
        requestParams.put("Action", "TextSensitivity");
        requestParams.put("content","据新华社报道，2017年4月26日上午，中国第二艘航空母舰在中国船舶重工集团公司大连造船厂举行下水仪式，中共中央政治局委员、中央军委副主席范长龙出席仪式并致辞。9时许，仪式在雄壮的国歌声中开始。按照国际惯例，剪彩后进行“掷瓶礼”。随着一瓶香槟酒摔碎舰艏，两舷喷射绚丽彩带，周边船舶一起鸣响");


        String requestMethod = "POST";
        String requestHost = "wenzhi.api.qcloud.com";
        String requestPath = "/v2/index.php";

        try {
            String plainText = QcloudSign.makeSignPlainText(requestParams, requestMethod, requestHost, requestPath);
            String sign = QcloudSign.sign(plainText, SECRET_KEY);
            System.out.println("原文: " + plainText);
            System.out.println("签名: " + sign);

            if (requestMethod.equals("GET")) {
                requestParams.put("Signature", URLEncoder.encode(sign, "UTF-8"));
            } else {
                requestParams.put("Signature", sign);
            }



            String retStr = _sendRequest("https://" + requestHost + requestPath, requestParams, requestMethod);
            String reagx="\\\\u[0-9,a-f,A-F]{4}";
            Pattern pat=Pattern.compile(reagx);
            Matcher mat=pat.matcher(retStr);
            while(mat.find()){
                int data=Integer.parseInt(mat.group(0).replaceAll("\\\\u",""),16);
                retStr=retStr.replace(mat.group(0),String.valueOf((char) data));
            }
            System.out.println(retStr);

        } catch(Exception e) {
            System.out.println(e);
        }

    }

    protected static String _sendRequest(String url,
            Map<String, Object> requestParams, String requestMethod)
    {
        String result = "";
        BufferedReader in = null;
        String paramStr = "";

        for(String key: requestParams.keySet()) {
            if (!paramStr.isEmpty()) {
                paramStr += '&';
            }
            // paramStr += key + '=' + requestParams.get(key).toString();
            paramStr += key + '=' + URLEncoder.encode(requestParams.get(key).toString());
        }

        try {

            if (requestMethod.equals("GET")) {
                if (url.indexOf('?') > 0)
                {
                    url += '&' + paramStr;
                } else {
                    url += '?' + paramStr;
                }
            }

            URL realUrl = new URL(url);
            URLConnection connection = null;
            if (url.substring(0, 5).toUpperCase().equals("HTTPS")) {
                HttpsURLConnection httpsConn = (HttpsURLConnection)realUrl.openConnection();

                httpsConn.setHostnameVerifier(new HostnameVerifier(){
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                connection = httpsConn;
            } else {
                connection = realUrl.openConnection();
            }

            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");

            if (requestMethod.equals("POST")) {
                // 发送POST请求必须设置如下两行
                connection.setDoOutput(true);
                connection.setDoInput(true);
                // 获取URLConnection对象对应的输出流
                PrintWriter out = new PrintWriter(connection.getOutputStream());
                // 发送请求参数
                out.print(paramStr);
                // flush输出流的缓冲
                out.flush();
            }

            // 建立实际的连接
            connection.connect();

            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 使用finally块来关闭输入流
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }
}

