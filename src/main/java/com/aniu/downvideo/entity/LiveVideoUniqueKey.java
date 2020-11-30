package com.aniu.downvideo.entity;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @version: V1.0
 * @ClassName: LiveVideoUniqueKey
 * @Description:
 * @author: hanxie
 * @create: 2020/11/26
 * @Copyright: 上海点掌文化传媒股份有限公司
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveVideoUniqueKey implements Serializable {
    private String roomId;
    private String ccContentId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LiveVideoUniqueKey that = (LiveVideoUniqueKey) o;
        return Objects.equal(roomId, that.roomId) &&
                Objects.equal(ccContentId, that.ccContentId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(roomId, ccContentId);
    }
}
