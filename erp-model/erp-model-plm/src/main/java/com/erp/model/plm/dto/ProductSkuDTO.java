package com.erp.model.plm.dto;

import com.erp.common.dto.base.PermissionsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
