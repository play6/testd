package com.aniu.downvideo.util;


import com.aniu.downvideo.entity.AniuCcvideoDownloadStatus;
import com.aniu.downvideo.entity.CcLiveVideo;
import com.aniu.downvideo.entity.LiveVideoUniqueKey;
import com.aniu.downvideo.mapper.AniuCcvideoDownloadStatusMapper;
import com.aniu.downvideo.pojo.CcLive;
import com.aniu.downvideo.pojo.CcLiveList;
import com.aniu.downvideo.pojo.Course;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

/**
 * @version: V1.0
 * @ClassName: SignUtils
 * @Description: 加密工具类
 * @author: hanxie
 * @create: 2020/10/28
 * @Copyright: 上海点掌文化传媒股份有限公司
 */
//@Component
    @Controller
public class DownloadUtils {

    private static final Logger LOG= LoggerFactory.getLogger(DownloadUtils.class);

    @Value("${downvideo.tigaoban}")
    private String saveDir;

    @Value("${downvideo.zhibo}")
    private String saveLiveDir;


    @Resource(name = "consumerQueueThreadPool")
    private ExecutorService consumerQueueThreadPool;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private AniuCcvideoDownloadStatusMapper aniuCcvideoDownloadStatusMapper;

    @RequestMapping("/q123")
    @ResponseBody
    public String test(String vid) throws IOException {
       return ""+downloadCCVideo(vid, saveDir,123,null);
    }


    // TODO 视频下载Demo
    public void downloadCcVideo(int num) throws IOException, InvalidFormatException {
        // 获取所有的ccID   // List<Course> courses = read("abc.xlsx");
        List<AniuCcvideoDownloadStatus> courseList = aniuCcvideoDownloadStatusMapper.getCourses(num);
        if (CollectionUtils.isEmpty(courseList)) {
            throw new RuntimeException("courses no records...");
        }
        for (AniuCcvideoDownloadStatus course : courseList) {
            String ccId = course.getCcContentId();
            consumerQueueThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        downloadCCVideo(ccId, saveDir,course.getVid(),null);
                        // 下载成功后根据ccId更新数据库下载状态
                        aniuCcvideoDownloadStatusMapper.updateStatusByCcId(ccId);
                        LOG.info(ccId + "-->视频已下载完成&&&视频下载状态表success。。。");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            // break;
        }
    }

    public List<Course> read(String fileName) throws IOException, InvalidFormatException {
        if (fileName == null) return null;
        // File xlsFile = new File(fileName);
        // File xlsFile = null;//= ResourceUtils.getFile("classpath:" + fileName);
        // TODO 获取excel文件
        org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:abc.xlsx");
        InputStream inputStream = resource.getInputStream(); // <-- this is the difference

        if (null == inputStream) return null;
        // 工作表
        Workbook workbook = WorkbookFactory.create(inputStream);
        // 表个数
        int numberOfSheets = workbook.getNumberOfSheets();
        if (numberOfSheets <= 0) {
            return null;
        }
        Sheet sheet = workbook.getSheetAt(0);
        // 行数
        int rowNumbers = sheet.getLastRowNum() + 1;
        Course c;
        // 读数据，第二行开始读取
        List<Course> list = new ArrayList<>();
        for (int row = 1; row < rowNumbers; row++) {
            Row r = sheet.getRow(row);
            //我们只需要前两列
            if (null != r && r.getPhysicalNumberOfCells() >= 2) {
                c = new Course(Integer.parseInt(r.getCell(0).toString().trim().replace(".0", "")), r.getCell(1).toString(),
                        r.getCell(2).toString(), r.getCell(3).toString(),
                        r.getCell(4).toString(), r.getCell(5).toString().trim());
                list.add(c);
            }
        }
        LOG.info("excel表的总行数是-->" + list.size());
        return list;
    }


    /**
     * 请求url的所有参数拼接成字符串
     *
     * @param map
     * @return
     */
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
     *
     * @param source 要加密的数据
     * @return
     * @throws NoSuchAlgorithmException
     */
    private static String getMd5(String source) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("md5");
        byte[] bytes = source.getBytes();
        byte[] targetBytes = digest.digest(bytes);
        char[] characters = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
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
     *
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
     * 将一个Map按照Key字母升序构成一个QueryString. 并且加入时间混淆的hash串
     *
     * @param queryMap query内容
     * @param time     加密时候，为当前时间；解密时，为从querystring得到的时间；
     * @param salt     加密salt
     * @return
     */
    public static String createHashedQueryString(Map<String, String> queryMap, long time, String salt) {
        Map<String, String> map = new TreeMap(queryMap);
        String qs = createQueryString(map); //生成queryString方法可自己编写
        if (qs == null) {
            return null;
        }
        time = time / 1000;
        String hash = getSign(qs, time, salt).toUpperCase();
        String thqs = String.format("%s&time=%d&hash=%s", qs, time, hash);
        return thqs;
    }



    /**
     * 根据ccContentId下载视频[工具类方法]
     *
     * @param ccContentId ccContentId
     * @param saveDir     文件保存路径
     * @throws IOException
     */
    public static boolean downloadCCVideo(String ccContentId, String saveDir,Integer vid,String date) throws IOException {
        if (StringUtils.isBlank(ccContentId)) {
            throw new RuntimeException("缺少ccContentId...");
        }
        if (StringUtils.isBlank(saveDir)) {
            throw new RuntimeException("视频保存路径不可为空...");
        }
        Map<String, String> params = new HashMap();// 需要传递的参数
        params.put("userid", "7CEFDE16F4DC35B6");
        params.put("videoid", ccContentId);
        String salt = "zeDnFNuXUwqE0QVzNdJ4aRH4YCevPjtt"; //加盐
        long time = System.currentTimeMillis(); //当前时间戳
        String str = createHashedQueryString(params, time, salt);//生成http请求参数
        String requestUrl = "http://spark.bokecc.com/api/video/original?" + str;
        HttpGet httpGet = new HttpGet(requestUrl);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = httpclient.execute(httpGet);
        String result = "";
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            //请求体内容
            result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
        }
        if (StringUtils.isBlank(result)) {
            return false;
        }
        if (result.contains("error")) {
            return false;
        }
        // 从返回结果中搂出下载url
        JsonObject jsonObject = new JsonParser().parse(result).getAsJsonObject();
        String downloadUrl = jsonObject.get("video").getAsJsonObject().get("url").getAsString();
        int i = downloadUrl.indexOf("?");
        String suffix = downloadUrl.substring(i-3,i);
        LOG.info("-->视频保存路径是。。。" + saveDir);
        if(StringUtils.isNotBlank(date)) {
            downloadByNIO2(downloadUrl, saveDir, ccContentId + "_" + date + "." + suffix);
        }else if(null != vid) {
            downloadByNIO2(downloadUrl, saveDir, vid + "." + suffix);
        }
        return true;
    }

    public static void downloadByNIO2(String url, String saveDir, String fileName) {
        try (InputStream ins = new URL(url).openStream()) {
            Path target = Paths.get(saveDir, fileName);
            Files.createDirectories(target.getParent());
            Files.copy(ins, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 下载直播视频实现
     */
    public void saveLiveVideoDownloadStatus() throws IOException {

        // 获取所有的roomId;
        List<String> roomIds = aniuCcvideoDownloadStatusMapper.getAllRoomId();
        Set<String> roomIdSet = new HashSet<>(roomIds);
        // 根据roomId请求cc的接口获取ccContentId
        List<CcLiveVideo> ccLiveVideos = getLiveVideoByRoomIds(roomIdSet);

        // 将多个CcLiveVideo保存到数据库中
        aniuCcvideoDownloadStatusMapper.saveCcLiveVideos(ccLiveVideos);

        LOG.info("save to database successfully[CC直播列表保存到数据库成功]。。。。。。");
    }

    private List<CcLiveVideo> getLiveVideoByRoomIds(Set<String> roomIdSet) throws IOException {
        if (CollectionUtils.isEmpty(roomIdSet)) {
            throw new RuntimeException("No available roomId[没有可用的roomId]......");
        }
        List<CcLiveVideo> result = new ArrayList<>();
        for (String s : roomIdSet) {
            if (StringUtils.isBlank(s)) {
                continue;
            }
            String jsonResult = getJsonFromCCByRoomId(s);
            LOG.info("调用CC直播列表的结果是-->" + jsonResult);
            Gson gson = new Gson();
            CcLiveList ccLiveList = gson.fromJson(jsonResult, CcLiveList.class);
            List<CcLive> lives = ccLiveList.getRecords();
            if (CollectionUtils.isEmpty(lives)) {
                LOG.info(s + "-->当前roomId中无直播视频ccContentId");
                continue;
            }

            // TODO 换了个接口需要重新保存字段
            Date now = new Date();
            for (CcLive live : lives) {
                String startTime = live.getStartTime();
                // String endTime = live.getEndTime();
                if(StringUtils.isBlank(startTime) ) {
                    continue;
                }
                // roomid、recordid、liveid、roomtitie、replayUrl、recordstatus、
                // offlinePackageSize、downloadUrl、videotime（yyyy-MM-dd）
                CcLiveVideo ccLiveVideo = new CcLiveVideo();
                ccLiveVideo.setRoomId(s);
                ccLiveVideo.setCcContentId(live.getId());
                ccLiveVideo.setLiveId(live.getLiveId());
                ccLiveVideo.setRoomTitie(live.getTitle());
                ccLiveVideo.setReplayUrl(live.getReplayUrl());
                ccLiveVideo.setRecordStatus(live.getRecordStatus());
                ccLiveVideo.setVideoTime(startTime.substring(0,10));
                ccLiveVideo.setCreateTime(now);
                ccLiveVideo.setDownloadStatus(0);
                result.add(ccLiveVideo);
            }
        }
        return result;
    }

    private String getJsonFromCCByRoomId(String roomId) throws IOException {
        Map<String, String> queryMap = new HashMap<String, String>();
        queryMap.put("userid", "7CEFDE16F4DC35B6");
        queryMap.put("roomid", roomId);
        long time = Calendar.getInstance().getTime().getTime();
        String url = "http://api.csslcloud.net/api/v2/record/info?" + APIServiceFunction.createHashedQueryString(queryMap,
                time, "aGU8q3Z8CUlVQE4Zhy0CmgTwIimyrXDR");
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = httpclient.execute(httpGet);
        String result = "";
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            //请求体内容
            result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
        }

        return result;


    }

    private String getLiveDownloadURLByReplayId(String replayId) throws IOException {
        Map<String, String> queryMap = new HashMap<String, String>();
        queryMap.put("userid", "7CEFDE16F4DC35B6");
        queryMap.put("roomid", replayId);
        long time = Calendar.getInstance().getTime().getTime();
        String url = "http://api.csslcloud.net/api/v2/record/search?" + APIServiceFunction.createHashedQueryString(queryMap,
                time, "aGU8q3Z8CUlVQE4Zhy0CmgTwIimyrXDR");
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = httpclient.execute(httpGet);
        String result = "";
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            //请求体内容
            result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
        }
        return result;
    }




    public void addLiveVideoIncrementally() throws IOException {
        // 获取所有的roomId;
        List<LiveVideoUniqueKey> allKeys = aniuCcvideoDownloadStatusMapper.getAllLiveVideo();

        // 获取所有的roomId;
        List<String> roomIds = aniuCcvideoDownloadStatusMapper.getAllRoomId();
        Set<String> roomIdSet = new HashSet<>(roomIds);
        // 根据roomId请求cc的接口获取未入库的ccContentId
        List<CcLiveVideo> unsavedCcLiveVideos = getUnsavedLiveVideoByRoomIds(roomIdSet,allKeys);

        // 将多个CcLiveVideo保存到数据库中
        aniuCcvideoDownloadStatusMapper.saveCcLiveVideos(unsavedCcLiveVideos);

        LOG.info("save to database successfully[为保存的CC直播列表保存到数据库成功]。。。。。。");

    }

    private List<CcLiveVideo> getUnsavedLiveVideoByRoomIds(Set<String> roomIdSet, List<LiveVideoUniqueKey> allKeys) throws IOException {
        if (CollectionUtils.isEmpty(roomIdSet)) {
            throw new RuntimeException("No available roomId[没有可用的roomId]......");
        }

        List<CcLiveVideo> result = new ArrayList<>();

        Set<LiveVideoUniqueKey> savedKeys = new HashSet<>(allKeys);
        Gson gson = new Gson();
        for (String s : roomIdSet) {
            if (StringUtils.isBlank(s)) {
                continue;
            }
            String jsonResult = getJsonFromCCByRoomId(s);
            LOG.info("调用CC直播列表的结果是-->" + jsonResult);

            CcLiveList ccLiveList = gson.fromJson(jsonResult, CcLiveList.class);
            List<CcLive> lives = ccLiveList.getRecords();
            if (CollectionUtils.isEmpty(lives)) {
                LOG.info(s + "-->当前roomId中无直播视频ccContentId");
                continue;
            }

            LiveVideoUniqueKey liveVideoUniqueKey;
            Date now = new Date();
            for (CcLive live : lives) {
                String ccContentId = live.getId();
                liveVideoUniqueKey = new LiveVideoUniqueKey(s, ccContentId);
                if(CollectionUtils.isNotEmpty(savedKeys) && savedKeys.contains(liveVideoUniqueKey)) {
                    continue;
                }

                String startTime = live.getStartTime();
                if(StringUtils.isBlank(startTime)) {
                    continue;
                }
                CcLiveVideo ccLiveVideo = new CcLiveVideo();
                ccLiveVideo.setRoomId(s);
                ccLiveVideo.setCcContentId(live.getId());
                ccLiveVideo.setLiveId(live.getLiveId());
                ccLiveVideo.setRoomTitie(live.getTitle());
                ccLiveVideo.setReplayUrl(live.getReplayUrl());
                ccLiveVideo.setRecordStatus(live.getRecordStatus());
                ccLiveVideo.setVideoTime(startTime.substring(0,10));
                ccLiveVideo.setCreateTime(now);
                ccLiveVideo.setDownloadStatus(0);
                result.add(ccLiveVideo);
            }
        }
        return result;
    }





    public void downloadLiveVideo() throws IOException {

        // 从数据库获取未下载的直播间的视频ccId
        // 获取所有的ccID
        List<CcLiveVideo> liveList = aniuCcvideoDownloadStatusMapper.getLiveList(28);
        if (CollectionUtils.isEmpty(liveList)) {
            throw new RuntimeException("live no records...");
        }
        for (CcLiveVideo live : liveList) {
            String ccId = live.getCcContentId();
            String videoTime = live.getVideoTime();
            String roomId = live.getRoomId();
            if (StringUtils.isBlank(ccId)) {
                continue;
            }

            consumerQueueThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    boolean b;
                    try {
                        b = doDownloadLiveVideo(ccId, saveLiveDir + "/" + roomId, ccId + "_" + videoTime);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    // 下载成功后根据ccId更新数据库下载状态
                    if (b) {
                        // 下载成功后根据ccId更新数据库下载状态
                        aniuCcvideoDownloadStatusMapper.updateLiveStatus(ccId, roomId);
                        LOG.info(ccId + "-->Live视频已下载完成&&&Live视频下载更新状态表success。。。");
                    } else {
                        // 失败 退出当前循环
                        LOG.info(ccId + "-->Live视频已下载失败 download fail。。。");
                    }

                }
            });

        }
    }








    public boolean doDownloadLiveVideo(String reminisceId,String saveDir,String fileName) throws IOException {

        if (StringUtils.isBlank(reminisceId)) {
            throw new RuntimeException("缺少ccContentId...");
        }
        if (StringUtils.isBlank(saveDir)) {
            throw new RuntimeException("视频保存路径不可为空...");
        }
        Map<String, String> params = new HashMap();// 需要传递的参数
        params.put("userid", "7CEFDE16F4DC35B6");
        params.put("recordid", reminisceId);
        // String salt = "zeDnFNuXUwqE0QVzNdJ4aRH4YCevPjtt"; // 加盐
        String salt = "aGU8q3Z8CUlVQE4Zhy0CmgTwIimyrXDR";
        long time = System.currentTimeMillis(); // 当前时间戳
        String str = createHashedQueryString(params, time, salt);// 生成http请求参数
        String requestUrl = "http://api.csslcloud.net/api/v2/record/search?" + str;
        HttpGet httpGet = new HttpGet(requestUrl);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = httpclient.execute(httpGet);
        String result = "";
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            //请求体内容
            result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
        }
        if (StringUtils.isBlank(result)) {
            return false;
        }
        if (result.contains("error")) {
            return false;
        }
        LOG.info("获取单个回放信息返回结果是--->" + result);
        // 从返回结果中搂出下载url
        JsonObject jsonObject = new JsonParser().parse(result).getAsJsonObject();
        String downloadUrl = jsonObject.get("record").getAsJsonObject().get("downloadUrl").getAsString();
        int i = downloadUrl.indexOf("?");
        String suffix = downloadUrl.substring(i-3,i);
        LOG.info("-->视频保存路径是。。。" + saveDir);
        downloadByNIO2(downloadUrl, saveDir, reminisceId + "_" +  "." + suffix);
        return true;

    }





}
