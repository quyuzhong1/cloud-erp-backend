package com.erp.server.auth.controller.openapi;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.dto.AfterSaleProgressDTO;
import com.erp.model.dmp.dto.ThridUserInfoDTO;
import com.erp.rpc.dmp.feign.AfterSaleFeign;
import com.erp.rpc.oms.feign.OmsDropDownFeign;
import com.erp.server.auth.config.OpenApi;

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
    public ApiResult<ThridUserInfoDTO.AddResultDTO> addThridUser(@Valid ThridUserInfoDTO.AddDTO dto){
        return afterSaleFeign.addThridUser(dto);
    }

    /**
     * 新增寄修用户
     * @Author jack
     * @since 2025-04-07
     */
    @OpenApi("code2Session")
    public ApiResult<ThridUserInfoDTO.CodeToSessionResp> code2Session(@Valid ThridUserInfoDTO.CodeToSessionDTO dto){
        return afterSaleFeign.code2Session(dto);
    }


    /**
     * 寄修申请-购买平台
     * @Author jack
     * @since 2025-04-07
     */
    @OpenApi("listSalesPlatform")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listSalesPlatform(AfterSaleDTO.OpenApiCommonDTO dto){
        return  omsDropDownFeign.listInternalSalesPlatform(dto.getKey());
    }


    /**
     * 寄修申请-提交申请单
     * @Author jack
     * @since 2025-04-07
     */
    @OpenApi("add")
    public ApiResult<BaseResultDTO.AddDTO> add( @Valid AfterSaleDTO.AddDTO dto){
        dto.setType("wx");
        return afterSaleFeign.add(dto);
    }

    /**
     * 获取寄修进度
     * @Author jack
     * @since 2025-04-07
     */
    @OpenApi("getRepairRecord")
    public ApiResult<AfterSaleProgressDTO.RepairRecordDTO> getRepairRecord( @Valid AfterSaleDTO.ProgressDTO dto) {
        return afterSaleFeign.getRepairRecord(dto);
    }

    /**
     * 获取寄修历史
     * @Author jack
     * @since 2025-04-07
     */
    @OpenApi("getRepairHistory")
    public ApiResult<List<AfterSaleProgressDTO.RepairHistoryListDTO>> getRepairHistory( @Valid AfterSaleDTO.ThridUserDTO dto){
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
    ApiResult<Boolean> udpateTrackNo(@Valid AfterSaleDTO.UpdateTrackNoDTO dto){
        return afterSaleFeign.udpateTrackNo(dto);
    }

    /**
     * 取消寄修申请
     * @author jack
     * @date:  2025-04-11
     * @return ApiResult
     */
    @OpenApi("invalidByCode")
    ApiResult<BatchResultDTO> invalidByCode(@Valid AfterSaleDTO.OpenApiCommonDTO dto){
        return afterSaleFeign.invalidByCode(dto.getCode());
    }



}
