package com.erp.model.wms.dto;

import cn.hutool.json.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * wms虚拟仓明细同步表请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-12-03
*/
@Data
@NoArgsConstructor
public class WmsVirtualDetailMsgDTO implements Serializable {




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
        * json数据
        */
        private JSONObject dataJson;

        /**
        * 备注
        */
        private String remark;

        /**
        * 操作时间
        */
        private LocalDateTime tradeTime;

        /**
        * waitHandle待处理，success成功，fail失败，doing进行中
        */
        private String status;


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
        * json数据
        */
        @NotNull(message = "json数据不能为空")
        private JSONObject dataJson;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 操作时间
        */
        private LocalDateTime tradeTime;

        /**
        * waitHandle待处理，success成功，fail失败，doing进行中
        */
        @NotBlank(message = "waitHandle待处理，success成功，fail失败，doing进行中不能为空")
        @Size(max = 32,message = "waitHandle待处理，success成功，fail失败，doing进行中最大长度不能超过32位")
        private String status;


    }


}