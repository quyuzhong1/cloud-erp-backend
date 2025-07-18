package com.erp.server.oms.controller.feign;

import cn.hutool.core.collection.CollUtil;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.BaseController;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.dto.SoInfoToSdyDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.wms.dto.ReportOrderDataDTO;
import com.erp.server.oms.kingdee.SyncKingdeeSoService;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoInfoService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 采购单
 *
 * @Author Luo_WG
 * @Date 2023/5/15 9:12
 **/
@RestController
@Slf4j
@RequestMapping("feign/soInfo")
public class SoInfoFeignController extends BaseController {
    @Resource
    private SoDetailService soDetailService;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private SyncKingdeeSoService syncKingdeeSoService;

    /**
     * 根据主键id查询销售单主表信息
     *
     * @param id id
     * @return com.erp.model.oms.entity.SoInfoEntity
     * @Author Luo_WG
     * @Date 2023/5/15 18:18
     **/
    @PostMapping("/getSoInfoById")
    public SoInfoEntity getSoInfoById(@RequestBody String id) {
        return soInfoService.getById(id);
    }

    /**
     * 根据ids获取销售订单
     * @Author Luo_WG
     * @Date 2023/7/13 11:26
     * @param ids
     * @return java.util.List<com.erp.model.oms.entity.SoInfoEntity>
     **/
    @PostMapping("/listSoInfoByIds")
    public List<SoInfoEntity> listSoInfoByIds(@RequestBody List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return soInfoService.listByIds(ids);
    }

    /**
     * 根据销售单详情id查询详情表信息
     *
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.entity.SoDetailEntity>
     * @Author Luo_WG
     * @Date 2023/5/15 18:18
     **/
    @PostMapping("/listSoDetailByIds")
    public List<SoDetailEntity> listSoDetailByIds(@RequestBody List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        return soDetailService.listSoDetailByIds(ids);
    }

    /**
     * 根据销售单主表id查询详情表信息
     *
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.entity.SoDetailEntity>
     * @Author Luo_WG
     * @Date 2023/5/15 18:18
     **/
    @PostMapping("/listSoDetailByMainIds")
    public List<SoDetailEntity> listSoDetailByMainIds(@RequestBody List<String> ids) {
        return soDetailService.listSoDetailByMainIds(ids);
    }

    /**
     * 获取订单明细全量字段
     *
     * @param id
     * @return
     */
    @PostMapping("/listSoDetailByMainId")
    List<SoDetailEntity> listSoDetailByMainId(@RequestBody String id){
        return soDetailService.listSoDetailByMainId(id);
    }
    /**
     * 根据主表id 获取对应基础信息
     *
     * @param id
     * @return java.util.List<com.erp.model.oms.entity.SoInfoDTO.CustomerDTO>
     * @author yl
     * @date 2023-05-19 11:14
     */
    @PostMapping("/getSoBaseById")
    public SoInfoDTO.CustomerDTO getSoBaseById(@RequestBody String id) {
        return soInfoService.getSoCustomer(id);
    }

    /**
     * 根据销售订单id 集合 获取到对应客户信息
     *
     * @param soIdList
     * @return com.erp.model.oms.dto.SoInfoDTO.CustomerDTO
     * @author yl
     * @date 2023-05-22 10:41
     */
    @PostMapping("/listSoCustomerByIds")
    public List<SoInfoDTO.CustomerDTO> listSoCustomerByIds(@RequestBody List<String> soIdList) {
        return soInfoService.listSoCustomerByIds(soIdList);
    }

    /**
     * 更改发货状态
     * @param paramList
     */
    @PostMapping("/updateDeliveryStatus")
    public void updateDeliveryStatus(@RequestBody List<SoDetailDTO.UpdateDeliveryStatusDTO> paramList) {
        soDetailService.updateDeliveryStatus(paramList);
    }

    /**
     * 销售订单审核
     * @Author Luo_WG
     * @Date 2023/7/4 12:28
     * @param dto
     * @return java.lang.Boolean
     **/
    @PostMapping("/approve")
    public List<BatchResultDTO> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoInfoEntity> soInfoEntityList = soInfoService.listByIds(ids);
        for (String id : ids) {
            SoInfoEntity entity = soInfoEntityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售订单不存在"));
                continue;
            }
            try {
                resultDTOS.add(soInfoService.approve(dto, entity));
            }catch (Exception e){
                log.error("B2B销售订单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS;
    }

 
    /**
     * 获取到销售订单历史数据 折扣额大于0
     * @author yl
     * @date 2023-09-28 10:30
     * @param
     * @return java.util.List<com.erp.model.oms.dto.SoInfoDTO.ListDTO>
     */
    @PostMapping("/listRepairHistoryDb")
    public List<SoInfoDTO.ListDTO> listRepairHistoryDb(){
        return soInfoService.listRepairHistoryDb();
    }

    /**
     * 更新冻结数量
     * @author will
     * @date 2024/7/16 20:08
     * @param soParamList
     */
    @PostMapping("/updateFrozenQty")
    public void updateFrozenQty(@RequestBody @Validated List<SoDetailDTO.UpdateFrozenQtyDTO> soParamList){
         soDetailService.updateFrozenQty(soParamList);
    }

    /**
     * 查询所有虚拟仓B2B销售订单数据
     * @author will
     * @date 2024/9/26 16:57
     * @return List<ViewDTO>
     */
    @GetMapping("/listAllVirtualSoDetail")
    public List<ReportOrderDataDTO.ViewDTO> listAllVirtualSoDetail(){
       return soDetailService.listAllVirtualSoDetail();
    }

    /**
     * 同步速递云B2B订单
     * @param soInfoToSdyDTO
     */
    @PostMapping("/sdyFieldOrderHandler")
    public void sdyFieldOrderHandler(@RequestBody SoInfoToSdyDTO soInfoToSdyDTO) {
        soInfoService.sdyFieldOrderHandler(soInfoToSdyDTO.getSoId(), soInfoToSdyDTO.getOperateEnum());
    }

    @PostMapping("/listByCodes")
    public List<SoInfoEntity> listByCodes(List<String> list) {
        return soInfoService.listByCodes(list);
    }

    @PostMapping("/updateApproveStatus")
    public void updateApproveStatus(SoInfoDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO) {
        soInfoService.updateApproveStatus(updateApprovalStatusDTO);
    }
}
