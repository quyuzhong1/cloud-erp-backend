package com.erp.server.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.model.srm.dto.excel.DeliveryOrderExportExcelDTO;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.model.srm.entity.DeliveryOrderEntity;
import com.erp.model.srm.enums.DeliveryOrderEnum;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.WarehouseReceiveEntity;
import com.erp.rpc.srm.feign.SrmDeliveryOrderFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.SupplierFeign;
import com.erp.server.wms.convert.SupplierDeliveryConverter;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.SupplierDeliveryOrderService;
import com.erp.server.wms.service.WarehouseReceiveService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 供应商送货单接口实现类
 */
@Slf4j
@Service
public class SupplierDeliveryOrderServiceImpl implements SupplierDeliveryOrderService{

    @Resource
    private SrmDeliveryOrderFeign srmDeliveryFeign;

    @Resource
    private WarehouseReceiveService warehouseReceiveService;

    @Resource
    private SysUserFeign sysUserFeign;
    @Override
    public Boolean export(DeliveryOrderDTO.ParamDTO dto, HttpServletResponse response) {
        List<DeliveryOrderExportExcelDTO> resultList = srmDeliveryFeign.getExportList(dto);
        String fileName = "供应商送货单";
        try {
            ExcelUtil.export(fileName, "供应商送货单", resultList, DeliveryOrderExportExcelDTO.class, response);
        } catch (Exception e) {
            log.error("供应商送货单导出失败:",e);
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> generateReceive(DeliveryOrderDTO.GenerateDTO dto) {
        List<DeliveryOrderDTO.GenerateReceiveDTO> generateReceiveDTOList = dto.getGenerateReceiveDTOList();
        Map<String,List<DeliveryOrderDTO.GenerateReceiveDTO>> generateReceiveDTOMap = generateReceiveDTOList.stream().collect(Collectors.groupingBy(DeliveryOrderDTO.GenerateReceiveDTO::getId));
        List<String> ids = generateReceiveDTOList.stream().map(DeliveryOrderDTO.GenerateReceiveDTO::getId).distinct().collect(Collectors.toList());
        List<String> detailIds = generateReceiveDTOList.stream().map(DeliveryOrderDTO.GenerateReceiveDTO::getDetailId).distinct().collect(Collectors.toList());
        //准备数据
        List<DeliveryOrderEntity> deliveryOrderList = srmDeliveryFeign.listByIds(ids);
        Map<String,DeliveryOrderEntity> deliveryOrderMap = deliveryOrderList.stream().collect(Collectors.toMap(DeliveryOrderEntity::getId, Function.identity()));
        List<DeliveryOrderDetailEntity> detailEntityList = srmDeliveryFeign.listDetailByIds(detailIds);
        Map<String,DeliveryOrderDetailEntity> detailEntityMap = detailEntityList.stream().collect(Collectors.toMap(DeliveryOrderDetailEntity::getId, Function.identity()));
        Map<String,List<DeliveryOrderDetailEntity>> detailEntityGroupMap = detailEntityList.stream().collect(Collectors.groupingBy(DeliveryOrderDetailEntity::getMainId));
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        //遍历生成采购收货单
        generateReceiveDTOMap.forEach((key,value)->{
            DeliveryOrderDTO.GenerateReceiveDTO firstDTO = value.get(0);
            BatchResultDTO resultDTO = new BatchResultDTO();
            resultDTOList.add(resultDTO);
            DeliveryOrderEntity deliveryOrderEntity = deliveryOrderMap.get(key);
            resultDTO.setCode(deliveryOrderEntity.getCode());
            resultDTO.setId(deliveryOrderEntity.getId());
            if(StringUtils.isNotBlank(deliveryOrderEntity.getReceiptStatus())){
                resultDTO.setSuccess(false);
                resultDTO.setMsg("收货状态不为空，不可下推收货单");
                return;
            }
            //设置收货数量和赠品数量
            for(DeliveryOrderDTO.GenerateReceiveDTO generateReceiveDTO : value){
                DeliveryOrderDetailEntity detailEntity = detailEntityMap.get(generateReceiveDTO.getDetailId());
                detailEntity.setReceiveQty(generateReceiveDTO.getReceiveQty());
                detailEntity.setGiftReceiveQty(generateReceiveDTO.getGiftReceiveQty());
                if(detailEntity.getReceiveQty() > detailEntity.getDeliveryQty() || detailEntity.getGiftReceiveQty() > detailEntity.getGiftQty()){
                    resultDTO.setSuccess(false);
                    resultDTO.setMsg(StrUtil.format("sku:【{}】，收货数量不可超过发货数量",detailEntity.getSkuNo()));
                    return;
                }
            }
            LoginUser loginUser = UserContext.getDefaultLoginUser();
            deliveryOrderEntity.setReceiveUserId(loginUser.getUid());
            deliveryOrderEntity.setReceiveUserName(loginUser.getUserName());
            deliveryOrderEntity.setReceiptStatus(DeliveryOrderEnum.ReceiptStatusEnum.WAIT_CONFIRMED.getCode());
            //新增采购收货
            SysDepartmentUserNumberDTO deptByUserId = sysUserFeign.getDeptByUserId(deliveryOrderEntity.getReceiveUserId());
            List<DeliveryOrderDetailEntity> detailEntityGroupList = detailEntityGroupMap.get(key);
            WarehouseReceiveDTO.AddDTO addDTO = SupplierDeliveryConverter.INSTANCE.deliveryToReceiveConvert(deliveryOrderEntity,detailEntityGroupList);
            addDTO.setReceiveDeptId(deptByUserId.getDepartmentId());
            addDTO.setGenerateByDelivery(true);
            if(StringUtils.isNotBlank(firstDTO.getReceiveDeptId())){
                addDTO.setReceiveDeptId(firstDTO.getReceiveDeptId());
            }
            if(StringUtils.isNotBlank(firstDTO.getReceiveUserId())){
                addDTO.setReceiveUserId(firstDTO.getReceiveUserId());
            }
            if(Objects.nonNull(firstDTO.getBillDate())){
                addDTO.setBillDate(firstDTO.getBillDate());
            }
            String id = warehouseReceiveService.add(addDTO);
            if(firstDTO.isAutoSubmit()){
                if(!warehouseReceiveService.submit(Collections.singletonList(id))){
                    throw new ServiceException("提交审核失败");
                }
            }
            //回写送货单的 收货单号 receive_user_id receive_user_name receipt_status 明细的 收货数量  赠品收货数量
            WarehouseReceiveEntity warehouseReceiveEntity = warehouseReceiveService.getById(id);
            deliveryOrderEntity.setReceiveCode(warehouseReceiveEntity.getCode());
            srmDeliveryFeign.updateDeliveryOrder(deliveryOrderEntity);
            srmDeliveryFeign.updateDeliveryDetail(detailEntityGroupList);
            resultDTO.setSuccess(true);
        });
        return resultDTOList;
    }
}
