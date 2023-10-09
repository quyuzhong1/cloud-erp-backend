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
 * @Classname CopySubjectDTO

 * @Date 2022-12-29 18:30
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class CopySubjectDTO implements Serializable {

    @NotBlank(message = "专题id不能为空")
    private String subjectId;


    /**
     * 专题名
     */
    @NotBlank(message = "专题名不能为空")
    @Size(max = 20,message = "最大20个字符")
    private String name;


    /**
     * 分享标示
     * personal 私人
     * share 按多用户ID共享
     * role 按多角色ID
     */
    @NotBlank(message = "分享类型不能为空")
    @StateEnumValue(strValues = {"personal","share","role"},message = "分享类型有误")
    private String shareFlag="personal";

    /**
     * 分享的用户集合
     */
    @Deprecated
    private List<String> shareUserIdList;

    /**
     * 是否常用
     */
    private Integer isFrequently=0;

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
