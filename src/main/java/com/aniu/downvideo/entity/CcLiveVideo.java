package com.aniu.downvideo.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @version: V1.0
 * @ClassName: CcLiveVideo
 * @Description:
 * @author: hanxie
 * @create: 2020/11/25
 * @Copyright: 上海点掌文化传媒股份有限公司
 */
@Data
public class CcLiveVideo implements Serializable {

    /**
     * 主键
     */
    private Integer id;
    /**
     * roomId
     */
    private String roomId;
    /**
     * ccContentId
     */
    private String ccContentId;

    private String startTime;

    private String endTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 预留字段
     */
    private String standby1;
    /**
     * 下载状态
     */
    private Integer downloadStatus;

}