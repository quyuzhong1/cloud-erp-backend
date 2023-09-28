package com.erp.model.bi.dto;

import com.common.core.anno.StateEnumValue;
import com.common.core.exception.ServiceException;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class BiBatchShareDTO implements Serializable {

    /**
     * 主实体IDS
     */
    @NotBlank(message = "ids不能为空")
    private String id;

    /**
     * 分享标示
     * share 按多用户ID共享
     * role 按多角色ID
     */
    @NotBlank(message = "分享类型不能为空")
    @StateEnumValue(strValues = {"personal","share","role"},message = "分享类型有误")
    private String shareFlag;

    /**
     * 分享的身份id列表(用户ID/角色ID)
     */
//    @NotEmpty(message = "分享的身份id列表(shareFlagIdList)不能为空")
//    private List<@NotBlank(message = "分享的身份id不能为空") String> shareFlagIdList;
    private List<String> shareFlagIdList;

    public List<String> checkAndGetShareFlagIdList() {
        if ("personal".equalsIgnoreCase(this.shareFlag)){
            return shareFlagIdList;
        }
        if (CollectionUtils.isEmpty(this.shareFlagIdList)){
            throw new ServiceException("shareFlagIdList分享的标识ID列表不能为空");
        }
        return shareFlagIdList;
    }

}
