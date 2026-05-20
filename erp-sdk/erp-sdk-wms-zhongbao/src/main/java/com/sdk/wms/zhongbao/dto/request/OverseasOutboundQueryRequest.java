package com.sdk.wms.zhongbao.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: wtr
 * @Date: 2026/3/12 15:51
 * @Param:
 * @Return:
 * @Description:
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OverseasOutboundQueryRequest {

    /**
     * 起始创建时间
     */
    @JSONField(name = "startCreateTime")
    private String startCreateTime;

    /**
     * 结束创建时间
     */
    @JSONField(name = "endCreateTime")
    private String endCreateTime;

    /**
     * 起始修改时间
     */
    @JSONField(name = "startUpdateTime")
    private String startUpdateTime;

    /**
     * 结束修改时间
     */
    @JSONField(name = "endUpdateTime")
    private String  endUpdateTime;

    /**
     * 订单号列表
     */
    @JSONField(name = "orderNos")
    private List<String> orderNos;

    /**
     * 自定义编号列表
     */
    @JSONField(name = "referenceNos")
    private List<String> referenceNos;

    /**
     * 订单号
     */
    @JSONField(name = "orderNo")
    private String orderNo;

    /**
     * 自定义编号
     */
    @JSONField(name = "referenceNo")
    private String referenceNo;

    /**
     * 库存类型
     */
    @JSONField(name = "inventoryType")
    private Integer inventoryType;

    /**
     * 状态
     */
    @JSONField(name = "status")
    private Integer status;

    /**
     * 通用分页参数
     */
    @JSONField(name = "commonParam")
    private CommonRequest commonParam;
}
