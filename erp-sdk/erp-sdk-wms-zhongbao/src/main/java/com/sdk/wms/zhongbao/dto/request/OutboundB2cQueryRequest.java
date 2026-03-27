package com.sdk.wms.zhongbao.dto.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName OverseasInboundCreateRequest
 * @description: 海外仓入库单
 * @date 2026年03月03日
 * @version: 1.0
 */
@Data
@Builder
public class OutboundB2cQueryRequest implements Serializable {
    //订单号列表
    private List<String> orderNos;
    //自定义编号列表
    private List<String> referenceNos;
    //起始创建时间（与修改时间互斥）格式：yyyy-MM-dd HH:mm:ss
    private String startCreateTime;
    private String endCreateTime;
    //结束修改时间（与创建时间互斥）格式：yyyy-MM-dd HH:mm:ss
    private String startUpdateTime;
    private String endUpdateTime;
    //订单号
    private String orderNo;
    //自定义编号
    private String referenceNo;
    //库存类型:1=>全部,2=>标准,3=>退件优先
    private Integer inventoryType;
    //状态:-2=>异常,-1=>已取消,1=>草稿,2=>待审核,3=>已审核,4=>待出库,5=>已出库
    private String status;
    //通用请求参数
    private CommonRequest commonParam;
}
