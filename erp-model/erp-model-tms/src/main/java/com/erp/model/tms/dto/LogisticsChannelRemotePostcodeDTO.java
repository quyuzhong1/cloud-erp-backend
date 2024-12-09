package com.erp.model.tms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 渠道邮编组设置表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2024-12-05
*/
@Data
@NoArgsConstructor
public class LogisticsChannelRemotePostcodeDTO implements Serializable {

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {
        /**
         * 渠道id
         */
        private String logisticsChannelId;

        /**
         * 邮编组id集合
         */
        private List<String> remotePostcodeIdList;

        /**
         *  邮编组名称集合
         */
        private List<String> remotePostcodeNameList;

    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        public AddDTO(String remotePostcodeId, String channelId) {
            this.setRemotePostcodeId(remotePostcodeId);
            this.setLogisticsChannelId(channelId);
        }
    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 渠道id
        */
        @NotBlank(message = "渠道id不能为空")
        @Size(max = 19,message = "渠道id最大长度不能超过19位")
        private String logisticsChannelId;

        /**
        * 邮编组id
        */
        @NotBlank(message = "邮编组id不能为空")
        @Size(max = 19,message = "邮编组id最大长度不能超过19位")
        private String remotePostcodeId;

    }


}