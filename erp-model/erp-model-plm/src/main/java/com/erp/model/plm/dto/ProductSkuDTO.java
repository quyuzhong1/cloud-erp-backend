package com.erp.model.plm.dto;

import com.common.business.dto.base.PermissionsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @Description 产品信息-主页列表-查询
 * @Author Luo_WG
 * @Date 2022/9/28 14:21
 **/
@Data
@NoArgsConstructor
public class ProductSkuDTO extends PermissionsDTO {

    /**
     * sku/spu/编号
     */
    private String no;

    /**
     * 产品SPU表id
     */
    private String productId;

    /**
     * 审核状态 0：待审核 1：审核中 2：审核通过 3：审核不通过
     */
    private Integer status;

    /**
     * 是否变更（0否，1是）
     */
    private Integer isChange;

    /**
     * 产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发
     */
    private List<Integer> stateList;

    /**
     * 类别id
     */
    private List<String> categoryIds;

    /**
     * 负责人id
     */
    private List<String> chargeIds;

    /**
     * 流程id
     */
    private List<String> processIds;


    /**
     * 产品名称
     */
    private String productName;


    /**
     * 产品等级
     */
    private List<String> gradeList;

    /**
     * 产品 品牌
     */
    private List<String> brandList;



    /**
     * 产品 经理
     */
    private List<String> productChargeIdList;

    /**
     * 项目经理列表
     */
    private List<String> projectChargeIdList;


    /**
     * 产品 属性
     */
    private List<String> propertyList;




    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;



    /**
     * sku 审核状态
     *  0待审核，1审核中，2审核通过，3审核不通过，4待提交
     */
    private List<Integer> skuStateList;


    /**
     * 侵权风险
     * 1：有侵权风险 2：无侵权风险
     */
    private List<Integer> pirateRiskList;


    /**
     * 销售方式
     */
    private List<String> saleMethodList;


    /**
     * ean 码
     */
    private String ean;


}


