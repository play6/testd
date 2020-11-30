package com.aniu.downvideo.task;

import com.aniu.downvideo.service.VideoStatusService;
import com.aniu.downvideo.util.DownloadUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

/**
 * @version: V1.0
 * @ClassName: VideoTask
 * @Description:
 * @author: hanxie
 * @create: 2020/11/18
 * @Copyright: 上海点掌文化传媒股份有限公司
 */
@Component
public class VideoTask {

    private static final Logger LOG= LoggerFactory.getLogger(VideoTask.class);

    @Autowired
    private DownloadUtils downloadUtils;

    @Autowired
    private VideoStatusService videoStatusService;

    /*
    定时任务将Excel中的数据保存到数据库
     */
    // "0 15 10 * * ? 2005" 2005年的每天上午10:15触发
    // @Scheduled(cron="0 5 13 24 11 ?")
    public void saveExcelData() throws IOException, InvalidFormatException {
        // 记录开始时间和结束时间
        Date now = new Date();
        LOG.info("saveExcelData()开始时间是-->" + now);
        videoStatusService.saveExcelData("abc.xlsx");
        now = new Date();
        LOG.info("saveExcelData()执行完成时间是-->" + now);
    }

    /*
    定时任务根据ccContentId下载视频
     */
    // "0 0 2 * * ?" 每天凌晨2:00执行
    @Scheduled(cron="0 0 2 * * ?")
    public void downloadVideos() throws IOException, InvalidFormatException {
        LOG.info("downloadVideos()下载视频文件开始了......");
        downloadUtils.downloadCcVideo(80);
    }

    // @Scheduled(cron="0 20 13 24 11 ?")
    public void testDownloadVideos() throws IOException, InvalidFormatException {
        LOG.info("downloadVideos()下载视频文件开始了......");
        downloadUtils.downloadCcVideo(6);
    }


    // 第一次根据房间id保存数据到数据库
    @Scheduled(cron="0 0 23 26 11 ?")
    public void saveLiveVideoDownloadStatus() throws IOException {
        LOG.info("downloadLiveVideo()下载直播视频文件start了......");
        downloadUtils.saveLiveVideoDownloadStatus();
    }

    // 增量根据房间id保存数据到数据库[每天执行]
     @Scheduled(cron="0 21 14 * * ?")
    public void addLiveVideoIncrementally() throws IOException {
        LOG.info("addLiveVideoIncrementally()保存直播视频数据到数据库start了......");
        downloadUtils.addLiveVideoIncrementally();
    }

    // 下载直播间的视频到Linux服务器
    @Scheduled(cron="0 30 17 26 11 ?")
    public void downloadLiveVideo() {
        LOG.info("downloadLiveVideo()下载直播视频文件到Linux start了......");
        downloadUtils.downloadLiveVideo();
    }

}