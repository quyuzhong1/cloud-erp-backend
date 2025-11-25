package com.erp.model.plm.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
     * sku名称
     */
    private String name;

    /**
     * 0所有产品，1待审核产品，2已审核产品
     */
    private String type;

    /**
     * 产品SPU表id
     */
    private String productId;
    /**
     * 标签关联的产品列表
     */
    private List<String> labelProductIds;

    /**
     * 是否变更（0否，1是）
     */
    private Integer isChange;
    /**
     * 标签列表
     */
    private List<String> labelIds;

    /**
     * 用于列表界面的（所有产品，带我审核产品，已审核产品） 0待审核，1审核中，2审核通过，3审核不通过，4待提交
     */
    private List<Integer> statusList;

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
     * 迭代产品SKU编码
     */
    private String iterateRefSkuNo;


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
    private List<String> notPropertyList;


    /**
     * 开始时间
     */
    private LocalDate startTime;

    /**
     * 结束时间
     */
    private LocalDate endTime;


    /**
     * sku 审核状态
     * 0待审核，1审核中，2审核通过，3审核不通过，4待提交
     */
    private List<Integer> skuStateList;


    /**
     * 侵权风险
     * 1：有侵权风险 2：无侵权风险
     */
    private List<Integer> pirateRiskList;


    /**
     * 销售方式
     * 1商品 2 赠品 3包材 4半成品
     */
    private List<String> saleMethodList;


    /**
     * 销售方式(后端用)
     */
    private String saleMethod;

    /**
     * ean 码
     */
    private String ean;


    /**
     * 销售状态
     */
    private List<Integer> saleStateList;

    /**
     * sku集合
     */
    private List<String> skuList;

    /**
     * 远程搜索sku
     */
    private String remoteSearchSku;

    /**
     * spu集合
     */
    private List<String> spuList;

    /**
     * 供应商名称
     */
    private List<String> supplierNameList;

    /**
     * 应用分类id
     */
    private List<String> applicationCategoryIds;


    /**
     * 页面高级查询
     */
    private List<AdvanceQueryDTO> advanceQueryDTOList;

    /**
     * sqlMap 默认key default
     */
    private Map<String, String> sqlMap;
}


