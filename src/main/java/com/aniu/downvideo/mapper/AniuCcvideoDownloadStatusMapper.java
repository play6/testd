package com.aniu.downvideo.mapper;

import com.aniu.downvideo.entity.AniuCcvideoDownloadStatus;
import com.aniu.downvideo.entity.CcLiveVideo;
import com.aniu.downvideo.entity.LiveVideoUniqueKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AniuCcvideoDownloadStatusMapper {

    void addAll(List<AniuCcvideoDownloadStatus> statusList);

    void updateStatusByCcId(@Param("ccId") String ccId);

    void updateLiveStatus(@Param("ccId") String ccId,@Param("roomId") String roomId);

    List<AniuCcvideoDownloadStatus> getCourses(@Param("num") int num);

    List<CcLiveVideo> getLiveList(@Param("num") int num);

    List<String> getAllRoomId();

    void saveCcLiveVideos(@Param("list")List<CcLiveVideo> list);

    List<LiveVideoUniqueKey> getAllLiveVideo();
}
