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
 * 预报设置渠道表请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-04-02
*/
@Data
@NoArgsConstructor
public class SettingForecastChannelDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 主表id
        */
        private String mainId;

        /**
        * 物流渠道id
        */
        private String logisticsChannelId;

        /**
        * 物流渠道名称
        */
        private String logisticsChannelName;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        public AddDTO(String mainId,List<String> logisticsChannelIdList) {
            this.setMainId(mainId);
            this.setLogisticsChannelIdList(logisticsChannelIdList);
        }

    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommonDTO {

        /**
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 物流渠道id
        */
        private List<String> logisticsChannelIdList;

    }


}