package com.aniu.downvideo.service.impl;

import com.aniu.downvideo.service.GetLiveVideosService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @version: V1.0
 * @ClassName: GetLiveListServiceImpl
 * @Description:
 * @author: hanxie
 * @create: 2020/11/18
 * @Copyright: 上海点掌文化传媒股份有限公司
 */
@Service
public class GetLiveVideosServiceImpl implements GetLiveVideosService {
    @Override
    public String getLiveList() throws IOException {

        Map<String, String> params = new HashMap();// 需要传递的参数
        params.put("roomid", "9CCF0890114A099E9C33DC5901307461");
        params.put("userid", "7CEFDE16F4DC35B6");
        String salt = "zeDnFNuXUwqE0QVzNdJ4aRH4YCevPjtt"; //加盐
        long time = System.currentTimeMillis(); //当前时间戳
        String str = createHashedQueryString(params, time, salt);//生成http请求参数
        System.out.println("加密后的字符串:" + str);
        String requestUrl = "http://api.csslcloud.net/api/v2/live/info?" + str;
        HttpGet httpGet = new HttpGet(requestUrl);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = httpclient.execute(httpGet);
        return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

    }

    public static String createQueryString(Map<String, String> map) {
        if (map.isEmpty()) {
            return null;
        }
        StringBuilder res = new StringBuilder();
        for (String key : map.keySet()) {
            String value = map.get(key);
            if (null == key || "".equals(key)) {
                continue;
            }
            try {
                if (null != value && !"".equals(value)) {
                    res.append(key).append("=").append(URLEncoder.encode(value, "UTF-8")).append("&");
                } else {
                    res.append(key).append("=").append(value).append("&");
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        if (res.length() > 1) {
            return res.substring(0, res.length() - 1);
        }
        return null;
    }

    /**
     * 通过md5进行加密
     * @param source 要加密的数据
     * @return
     * @throws NoSuchAlgorithmException
     */
    private static String getMd5(String source) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("md5");
        byte[] bytes = source.getBytes();
        byte[] targetBytes = digest.digest(bytes);
        char[] characters = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuilder builder = new StringBuilder();
        for (byte b : targetBytes) {
            int high = (b >> 4) & 15;
            int low = b & 15;
            char highChar = characters[high];
            char lowChar = characters[low];
            builder.append(highChar).append(lowChar);
        }

        return builder.toString();
    }

    /**
     * 进行MD5加密
     * @param qs
     * @param time
     * @param salt
     * @return
     */
    public static String getSign(String qs, long time, String salt) {
        try {
            return getMd5(String.format("%s&time=%d&salt=%s", qs, time, salt));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 功能：将一个Map按照Key字母升序构成一个QueryString. 并且加入时间混淆的hash串
     * @param queryMap query内容
     * @param time     加密时候，为当前时间；解密时，为从querystring得到的时间；
     * @param salt     加密salt
     * @return
     */
    public static String createHashedQueryString(Map<String, String> queryMap, long time, String salt) {
        Map<String, String> map = new TreeMap<String, String>(queryMap);
        String qs = createQueryString(map); //生成queryString方法可自己编写
        if (qs == null) {
            return null;
        }
        time = time / 1000;
        String hash = getSign(qs, time, salt).toUpperCase();
        String thqs = String.format("%s&time=%d&hash=%s", qs, time, hash);
        return thqs;
    }

}
