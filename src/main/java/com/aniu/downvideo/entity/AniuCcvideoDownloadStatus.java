package com.aniu.downvideo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @version: V1.0
 * @ClassName: AniuCcvideoDownloadStatus
 * @Description: 对应aniu_ccvideo_download_status表
 * @author: hanxie
 * @create: 2020/11/18
 * @Copyright: 上海点掌文化传媒股份有限公司
 */
@Data
@NoArgsConstructor
public class AniuCcvideoDownloadStatus implements Serializable {

    private Integer id;
    private Integer vid;
    private String ccContentId;
    private String standby1;
    private Integer downloadStatus;

    public AniuCcvideoDownloadStatus(Integer vid, String ccContentId) {
        this.vid = vid;
        this.ccContentId = ccContentId;
    }
}
