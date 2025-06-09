package com.erp.model.tms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 * 物流单明细表请求响应实体
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
*/
@Data
@NoArgsConstructor
public class LogisticsBillDetailDTO implements Serializable {




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
        * 物流单id
        */
        private String mainId;

        /**
        * 运输状态
        */
        private String trackStatus;

        /**
        * 运单号
        */
        private String trackNo;


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
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class QueryDTO extends CommonDTO{

        /**
         * 查询方式  TRACK123，api，notQuery
         */
        private String trackQueryMode;

        /**
         * 注册状态（0未注册1注册成功-1注册失败）
         */
        private String registerStatus;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 物流单id
        */
        private String mainId;

        /**
        * 运输状态
        */
        private String trackStatus;

        /**
        * 运单号
        */
        private String trackNo;


    }
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillDetailDTO {
        private String trackNo;
        private String platformOrderNo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillDetailErrorDTO {
        private String id;
        private String errorMsg;
    }

}