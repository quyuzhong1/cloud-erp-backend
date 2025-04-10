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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
    public ApiResult<List<AfterSaleDTO.DropDownDTO>> getDetailByPlatformCode(@RequestParam("platformCode") String platformCode){
        return afterSaleFeign.getDetailByPlatformCode(platformCode);
    }

    /**
     * 新增寄修用户
     * @Author jack
     * @since 2025-04-07
     */
    @OpenApi("addThridUser")
    public ApiResult<BaseResultDTO.AddDTO> addThridUser(@RequestBody @Validated ThridUserInfoDTO.AddDTO dto){
        return afterSaleFeign.addThridUser(dto);
    }


    /**
     * 寄修申请-购买平台
     * @Author jack
     * @since 2025-04-07
     */
    @OpenApi("listSalesPlatform")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listSalesPlatform(@RequestParam("key") String key){
        return omsDropDownFeign.list(key);
    }


    /**
     * 寄修申请-提交申请单
     * @Author jack
     * @since 2025-04-07
     */
    @OpenApi("add")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AfterSaleDTO.AddDTO dto){
        return afterSaleFeign.add(dto);
    }

    /**
     * 获取寄修进度
     * @Author jack
     * @since 2025-04-07
     */
    @OpenApi("getRepairRecord")
    public ApiResult<List<AfterSaleProgressDTO.RepairRecordListDTO>> getRepairRecord(@RequestBody @Valid AfterSaleDTO.ProgressDTO dto) {
        return afterSaleFeign.getRepairRecord(dto);
    }

    /**
     * 获取寄修历史
     * @Author jack
     * @since 2025-04-07
     */
    @OpenApi("getRepairHistory")
    public ApiResult<List<AfterSaleProgressDTO.RepairHistoryListDTO>> getRepairHistory(@RequestBody @Validated AfterSaleDTO.ThridUserDTO dto){
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

}
