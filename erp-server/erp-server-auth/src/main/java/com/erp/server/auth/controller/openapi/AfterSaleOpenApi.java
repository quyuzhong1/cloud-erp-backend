package com.erp.server.auth.controller.openapi;

import cn.hutool.core.collection.CollUtil;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;

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

    @Resource
    private Validator validator;


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
        if(CollUtil.isEmpty(dto.getDetailList()) && CollUtil.isEmpty(dto.getAttachmentList())){
            return  ApiResult.error(500, "sku明细或图片附件至少填写一种");
        }
        // 手动校验参数
        Set<ConstraintViolation<AfterSaleDTO.AddDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            ConstraintViolation<AfterSaleDTO.AddDTO> firstViolation = violations.iterator().next();
            return ApiResult.error(500, firstViolation.getMessage());
        }
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
    public ApiResult<List<AfterSaleDTO.NodeDTO>> getNodeList(){
        return afterSaleFeign.getNodeList();
    }

    /**
     * 更新客户运单号
     * @author jack
     * @date:  2025-04-06
     * @return ApiResult
     */
    @OpenApi("udpateTrackNo")
    public ApiResult<Boolean> udpateTrackNo(@Valid AfterSaleDTO.UpdateTrackNoDTO dto){
        return afterSaleFeign.udpateTrackNo(dto);
    }

    /**
     * 取消寄修申请
     * @author jack
     * @date:  2025-04-11
     * @return ApiResult
     */
    @OpenApi("invalidByCode")
    public ApiResult<BatchResultDTO> invalidByCode(@Valid AfterSaleDTO.OpenApiCommonDTO dto){
        return afterSaleFeign.invalidByCode(dto.getCode());
    }

    /**
     * 寄修申请详情
     * @Author jack
     * @since 2025-05-13
     */
    @OpenApi("view")
    public ApiResult<AfterSaleDTO.ViewDTO> view(@Valid AfterSaleDTO.OpenApiCommonDTO dto) {
        return afterSaleFeign.view(dto.getId());
    }

}
