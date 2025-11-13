package com.sdk.oms.wildberries.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author zdy
 * @ClassName OrderRequest
 * @description: TODO
 * @date 2025年09月19日
 * @version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AddOrderToSupplyResponse extends BaseResponse{
    /**
     * 异常信息
     */
    private String message;
    //204表示添加成功，只有完成了以上三步，组包预报单的大包交接状态变更为已提交
}
