package com.aniu.downvideo.controller;

import com.aniu.downvideo.service.GetLiveVideosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @version: V1.0
 * @ClassName: TestController
 * @Description:
 * @author: hanxie
 * @create: 2020/11/19
 * @Copyright: 上海点掌文化传媒股份有限公司
 */
@RestController
public class TestController {

    @Autowired
    private GetLiveVideosService getLiveVideosService;

    @RequestMapping("/haha")
    public String test1() throws IOException {
        return getLiveVideosService.getLiveList();
    }

}
