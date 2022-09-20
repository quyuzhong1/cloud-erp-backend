package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname 产品归档
 * @Description TODO
 * @Date 2022-09-16 10:02
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductDocsDTO implements Serializable {

    //产品id
    private String productId;

    private String productName;

    //产品负责人
    private String productPersonInCharge;

    //项目负责人
    private String projectPersonInCharge;

    //归档时间
    private Date archiveTime;

    //项目状态
    private Integer productState;

    //迭代数量
    private Integer iterationCount;

    //总文档数
    private Integer totalDocs;

    //完成档数
    private Integer finishDocs;


}
