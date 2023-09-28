package com.erp.model.bi.dto;

import com.common.core.anno.StateEnumValue;
import com.common.core.exception.ServiceException;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 设置仪表盘的分享信息
 *
 * @Classname

 * @Date 2022-12-08 15:40
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class UpdateSubjectShareDTO implements Serializable {


    @NotBlank(message = "表id不能为空")
    private String id;

    @NotBlank(message = "名字不能为空")
    @Size(max = 20,message = "最大20字符")
    private String name;


    private Integer isFrequently=0;

    @NotBlank(message = "分享类型不能为空")
    @StateEnumValue(strValues = {"personal","share","role"},message = "分享类型有误")
    private String shareFlag="personal";

    /**
     * 分享的用户集合
     */
    @Deprecated
    private List<String> shareUserIdList;

    /**
     * 分享的标识ID集合
     */
    private List<String> shareFlagIdList;


    public List<String> checkAndGetShareFlagIdList() {
        if ("personal".equalsIgnoreCase(this.shareFlag)){
            return shareFlagIdList;
        }
        if (!CollectionUtils.isEmpty(this.shareFlagIdList)){
            return shareFlagIdList;
        }
        // 兼容旧字段
        if (!CollectionUtils.isEmpty(this.shareUserIdList)){
            return shareUserIdList;
        }
        throw new ServiceException("shareFlagIdList分享的标识ID列表不能为空");
    }
}
