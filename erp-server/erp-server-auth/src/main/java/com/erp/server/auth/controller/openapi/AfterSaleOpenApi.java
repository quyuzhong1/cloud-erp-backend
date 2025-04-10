package com.erp.server.auth.controller.openapi;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.dto.AfterSaleProgressDTO;
import com.erp.model.dmp.dto.ThridUserInfoDTO;
import com.erp.rpc.dmp.feign.AfterSaleFeign;
import com.erp.rpc.oms.feign.OmsDropDownFeign;
import com.erp.server.auth.config.OpenApi;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 售后申请表
 * 小程序端接口
 * @author jack
 * @since 2025-04-06
 */
@OpenApi
public class AfterSaleOpenApi {

    @Resource
    private AfterSaleFeign afterSaleFeign;
    @Resource
    private OmsDropDownFeign omsDropDownFeign;


    /**
     * 根据订单编号查询明细
     * @Author jack
     * @since 2025-04-07
     */
    @OpenApi("getDetailByPlatformCode")
    public ApiResult<List<AfterSaleDTO.DropDownDTO>> getDetailByPlatformCode(AfterSaleDTO.OpenApiCommonDTO dto){
        return afterSaleFeign.getDetailByPlatformCode(dto.getPlatformCode());
    }

    /**
     * 新增寄修用户
     * @Author jack
     * @since 2025-04-07
     */
    @OpenApi("addThridUser")
    public ApiResult<BaseResultDTO.AddDTO> addThridUser(@Validated ThridUserInfoDTO.AddDTO dto){
        return afterSaleFeign.addThridUser(dto);
    }


    /**
     * 寄修申请-购买平台
     * @Author jack
     * @since 2025-04-07
     */
    @OpenApi("listSalesPlatform")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listSalesPlatform(AfterSaleDTO.OpenApiCommonDTO dto){
        return omsDropDownFeign.list(dto.getKey());
    }


    /**
     * 寄修申请-提交申请单
     * @Author jack
     * @since 2025-04-07
     */
    @OpenApi("add")
    public ApiResult<BaseResultDTO.AddDTO> add( @Validated AfterSaleDTO.AddDTO dto){
        return afterSaleFeign.add(dto);
    }

    /**
     * 获取寄修进度
     * @Author jack
     * @since 2025-04-07
     */
    @OpenApi("getRepairRecord")
    public ApiResult<List<AfterSaleProgressDTO.RepairRecordListDTO>> getRepairRecord( @Valid AfterSaleDTO.ProgressDTO dto) {
        return afterSaleFeign.getRepairRecord(dto);
    }

    /**
     * 获取寄修历史
     * @Author jack
     * @since 2025-04-07
     */
    @OpenApi("getRepairHistory")
    public ApiResult<List<AfterSaleProgressDTO.RepairHistoryListDTO>> getRepairHistory( @Validated AfterSaleDTO.ThridUserDTO dto){
        return afterSaleFeign.getRepairHistory(dto);
    }


    /**
     * 获取节点配置信息
     * @Author jack
     * @since 2025-04-07
     */
    @OpenApi("getNodeList")
    ApiResult<List<AfterSaleDTO.NodeDTO>> getNodeList(){
        return afterSaleFeign.getNodeList();
    }

    /**
     * 更新客户运单号
     * @author jack
     * @date:  2025-04-06
     * @return ApiResult
     */
    @OpenApi("udpateTrackNo")
    ApiResult<Boolean> udpateTrackNo(@Validated AfterSaleDTO.UpdateTrackNoDTO dto){
        return afterSaleFeign.udpateTrackNo(dto);
    }

}
