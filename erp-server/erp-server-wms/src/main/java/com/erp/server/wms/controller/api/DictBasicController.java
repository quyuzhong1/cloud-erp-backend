package com.erp.server.wms.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.server.wms.service.DictBasicService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 字典管理
 *
 * @author Lambda
 * @since 2023-03-16
 */
@RestController
@RequestMapping("/dict")
public class DictBasicController extends BaseController {


    @Resource
    private DictBasicService dictBasicService;


    /**
     * 保存或者修改字典信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/saveOrUpdateBatch")
    public ApiResult saveOrUpdate(@RequestBody @Validated List<DictBasicDTO.ListDTO> dto) {
        Boolean result = dictBasicService.saveOrUpdateDict(dto);
        return result == true ? success() : failure();
    }


    /**
     * 获取对应字典数据
     * warehouseType 仓库类型
     * qcProblemType 质检单 问题属性
     * handleModeType 质检单 处理措施
     * qcReportResult 质检单 质检报告结果
     * inventoryDate 库存时间
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<DictBasicDTO.ListDTO>> list(@RequestParam("key") String key) {
        List<DictBasicDTO.ListDTO> list = dictBasicService.getByKey(key);
        return success(list);
    }


    /**
     * 字典通用下拉列表
     * @param type transferType 调拨类型，transferDirection 调拨方向，instockType 入库类型，outstockType 出库类型，workType 事务类型，machineType 加工单类型，inventoryDirection 库存方向
     *             stocktakingMethod 盘点方式 stocktakingType 盘点类型 separateRule 分单规则 stocktakingStatus 盘点状态 warehouseType 仓库类型 qcProblemType 质检单 问题属性
     *             handleModeType 质检单 处理措施 qcReportResult 质检单 质检报告结果 inventoryDirection 库存方向
     *              machineHandleType 加工处理类型    collectMode 揽收方式 issueType 发料类型 soB2cExportType b2c销售订单导出类型
     *             CfgSettingOrderStatistics 配置订单统计,CfgSettingSalesStatistics 配置销售量统计
     * @param remark 备注
     * @return
     */
    @GetMapping("/drop/down")
    public ApiResult<List<DictBasicDTO.DropDownDTO>> dictDropDown(@RequestParam(value = "type")String type, @RequestParam(value = "remark", required = false) String remark) {
        List<DictBasicDTO.DropDownDTO> result =  dictBasicService.listByType(type, remark);
        return success(result);
    }

}
