package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 通知配置明细表请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-02-13
*/
@Data
@NoArgsConstructor
public class CfgNoticeDetailDTO implements Serializable {




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
        * 通知类型，noticeUser通知人员,noticeGroup通知群,noticeDay按天,noticeWeek按周
        */
        private String noticeType;

        /**
        * 通知json
        */
        private String noticeValueJson;

        /**
        * 主表id
        */
        private String mainId;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


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
    public static class CommonDTO {

        /**
        * 通知类型，noticeUser通知人员,noticeGroup通知群,noticeDay按天,noticeWeek按周
        */
        @NotBlank(message = "通知类型，noticeUser通知人员,noticeGroup通知群,noticeDay按天,noticeWeek按周不能为空")
        @Size(max = 32,message = "通知类型，noticeUser通知人员,noticeGroup通知群,noticeDay按天,noticeWeek按周最大长度不能超过32位")
        private String noticeType;

        /**
        * 通知json
        */
        @NotBlank(message = "通知json不能为空")
        private String noticeValueJson;

        /**
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;


    }


}