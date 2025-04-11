package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 售后进度记录表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-04-03
*/
@Data
@NoArgsConstructor
public class AfterSaleProgressDTO implements Serializable {




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
        * 进度节点
        */
        private String node;

        /**
        * 触发节点的时间
        */
        private LocalDateTime nodeTime;

        /**
        * 快递单号
        */
        private String trackNo;

        /**
        * 排序
        */
        private Integer index;

        /**
        * 备注
        */
        private String remark;


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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 进度节点
        */
        @NotBlank(message = "进度节点不能为空")
        @Size(max = 32,message = "进度节点最大长度不能超过32位")
        private String node;

        /**
        * 触发节点的时间
        */
        private LocalDateTime nodeTime;

        /**
        * 快递单号
        */
        @NotBlank(message = "快递单号不能为空")
        @Size(max = 64,message = "快递单号最大长度不能超过64位")
        private String trackNo;

        /**
        * 排序
        */
        private Integer index;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 500,message = "备注最大长度不能超过500位")
        private String remark;


    }

    /**
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 主表id
         */
        private String mainId;

        /**
         * 进度节点
         */
        private String node;

        /**
         * 触发节点的时间
         */
        private LocalDateTime nodeTime;

        /**
         * 快递单号
         */
        private String trackNo;

        /**
         * 排序
         */
        private Integer index;

        /**
         * 备注
         */
        private String remark;

    }

    /**
     */
    @Data
    @NoArgsConstructor
    public static class RepairRecordDTO {
        /**
         * active
         */
        private Integer active = 0;

        List<RepairRecordListDTO> recordList;
    }

    /**
     */
    @Data
    @NoArgsConstructor
    public static class RepairRecordListDTO {

        /**
         * id
         */
        private String id;

        private String detailId;
        /**
         * 进度节点
         */
        private Integer index;

        /**
         * 进度节点
         */
        private String node;

        /**
         * 进度节点名称
         */
        private String nodeName;

        /**
         * 快递单号
         */
        private String trackNo;

        /**
         * 触发节点的时间
         */
        private LocalDateTime nodeTimeLd;

        private String nodeTime ="";
        /**
         * 描述
         */
        private String remark;
    }

    /**
     */
    @Data
    @NoArgsConstructor
    public static class RepairHistoryListDTO {
        /**
         * id
         */
        private String id;

        /**
         * 时间
         */
        private LocalDateTime updateTime;

        /**
         * 工单号
         */
        private String code;

        /**
         * 状态
         */
        private String status;
        /**
         * 状态名称
         */
        private String statusName;

        /**
         * 描述
         */
        private String remark;


    }


}