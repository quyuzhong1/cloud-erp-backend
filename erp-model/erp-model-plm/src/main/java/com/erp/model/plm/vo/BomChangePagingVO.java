package com.erp.model.plm.vo;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品变更信息
 *
 * @Classname

 * @Date 2023-01-28 11:24
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BomChangePagingVO implements Serializable {

    private String id;

    /**
     * 变更类型
     */
    private String type;


    /**
     * 变更来源sku 或者bom
     */
    private String changeSourceNo = "";


    /**
     * 变更来源sku 或者bom 名称
     * bom 没有
     */
    private String changeSourceName = "-";


    /**
     * 变更时间
     */
    private LocalDateTime createTime;


    /**
     * 变更时间
     */
    private LocalDateTime approvalFinishTime;


    /**
     * 来源id
     */
    private String sourceId;


    /**
     * 状态
     */
    private Integer state;

    /**
     * 备注
     */
    private String remark;

    /**
     * 审核人
     */
    private String personApproving;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        /**
         * 类型，all全部、waitAudit待审核
         */
        private String tabFlag;

        /**
         * 数量
         */
        private Integer count;
    }
}



