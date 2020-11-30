package com.aniu.downvideo.service.impl;

import com.aniu.downvideo.entity.AniuCcvideoDownloadStatus;
import com.aniu.downvideo.mapper.AniuCcvideoDownloadStatusMapper;
import com.aniu.downvideo.pojo.Course;
import com.aniu.downvideo.service.VideoStatusService;
import com.aniu.downvideo.util.DownloadUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @version: V1.0
 * @ClassName: VideoStatusServiceImpl
 * @Description:
 * @author: hanxie
 * @create: 2020/11/18
 * @Copyright: 上海点掌文化传媒股份有限公司
 */
@Service
public class VideoStatusServiceImpl implements VideoStatusService {

    @Autowired
    private AniuCcvideoDownloadStatusMapper aniuCcvideoDownloadStatusMapper;

    @Autowired
    private DownloadUtils downloadUtils;
    /**
     * 根据Excel文件路径保存Excel数据到数据据库表
     * @param filePath
     * @return true代表成功,false代表失败
     */
    @Override
    public boolean saveExcelData(String filePath) throws IOException, InvalidFormatException {
        if(StringUtils.isBlank(filePath)) {
            throw new RuntimeException("filePath doesn't exist......");
        }
        List<Course> courses = downloadUtils.read(filePath);
        if(CollectionUtils.isEmpty(courses)) {
            throw new RuntimeException("Excel file doesn't have data......");
        }
        Iterator<Course> iterator = courses.iterator();
        List<AniuCcvideoDownloadStatus> statusList = new ArrayList<>(courses.size());
        while (iterator.hasNext()) {
            Course course = iterator.next();
            if(null == course || StringUtils.isBlank(course.getCcContentId())) {
                iterator.remove();
            }else {
                statusList.add(new AniuCcvideoDownloadStatus(course.getId(),course.getCcContentId()));
            }
        }
        aniuCcvideoDownloadStatusMapper.addAll(statusList);
        return true;
    }

}
