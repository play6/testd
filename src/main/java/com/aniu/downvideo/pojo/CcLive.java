package com.aniu.downvideo.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @version: V1.0
 * @ClassName: CcLive
 * @Description:
 * @author: hanxie
 * @create: 2020/11/25
 * @Copyright: 上海点掌文化传媒股份有限公司
 */
@Data
public class CcLive implements Serializable {

    private String id;
    private String liveId;
    private String startTime;
    private String stopTime;
    private Integer recordStatus;
    private Integer recordVideoStatus;

    private String recordVideoId;
    private String replayUrl;

    private Integer templateType;
    private Integer sourceType;

    private String title;
    private String desc;

}
