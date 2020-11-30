package com.aniu.downvideo.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @version: V1.0
 * @ClassName: Course
 * @Description:
 * @author: hanxie
 * @create: 2020/11/17
 * @Copyright: 上海点掌文化传媒股份有限公司
 */
@Data
public class Course implements Serializable {

    private Integer id;
    private String prgSubject;
    private String prgDate;
    private String prgStartTime;
    private String prgEndTime;
    private String ccContentId;

    public Course() {
    }

    public Course(Integer id, String prgSubject, String prgDate, String prgStartTime, String prgEndTime, String ccContentId) {
        this.id = id;
        this.prgSubject = prgSubject;
        this.prgDate = prgDate;
        this.prgStartTime = prgStartTime;
        this.prgEndTime = prgEndTime;
        this.ccContentId = ccContentId;
    }

}
