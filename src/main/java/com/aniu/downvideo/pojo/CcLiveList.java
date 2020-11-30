package com.aniu.downvideo.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @version: V1.0
 * @ClassName: CcLiveList
 * @Description:
 * @author: hanxie
 * @create: 2020/11/25
 * @Copyright: 上海点掌文化传媒股份有限公司
 */
@Data
public class CcLiveList implements Serializable {

    private String result;
    private Integer pageIndex;
    private Integer count;

    private List<CcLive> records;

}
