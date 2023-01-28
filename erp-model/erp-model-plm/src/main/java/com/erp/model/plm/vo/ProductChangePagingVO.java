package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 产品变更信息
 *
 * @Classname
 * @Description TODO
 * @Date 2023-01-28 11:24
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductChangePagingVO implements Serializable {

    /**
     * 变更类型
     */
    private String type;


    /**
     * 变更来源sku 或者bom
     */
    private String changeSourceNo="";


    /**
     * 变更来源sku 或者bom 名称
     * bom 没有
     */
    private String changeSourceName="-";


    /**
     * 变更时间
     */
    private Date createTime;


    /**
     * 变更时间
     */
    private Date approvalFinishTime;


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
    private String  personApproving;


}



