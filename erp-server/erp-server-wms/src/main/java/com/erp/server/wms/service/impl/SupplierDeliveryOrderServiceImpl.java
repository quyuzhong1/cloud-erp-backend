package com.erp.server.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.model.srm.dto.excel.DeliveryOrderExportExcelDTO;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.model.srm.entity.DeliveryOrderEntity;
import com.erp.model.srm.enums.DeliveryOrderEnum;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.WarehouseReceiveEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.srm.feign.SrmDeliveryOrderFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.convert.SupplierDeliveryConverter;
import com.erp.server.wms.service.SupplierDeliveryOrderService;
import com.erp.server.wms.service.WarehouseReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SUPPLIER_DELIVERY_ORDER;

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
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Override
    public Boolean export(DeliveryOrderDTO.ParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("供应商送货单", EXPORT_WMS_SUPPLIER_DELIVERY_ORDER.getCode(), dto);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        generateReceiveDTOMap.forEach((key,value)->{
            DeliveryOrderDTO.GenerateReceiveDTO firstDTO = value.get(0);
            BatchResultDTO resultDTO = new BatchResultDTO();
            resultDTOList.add(resultDTO);
            DeliveryOrderEntity deliveryOrderEntity = deliveryOrderMap.get(key);
            resultDTO.setCode(deliveryOrderEntity.getCode());
            resultDTO.setId(deliveryOrderEntity.getId());
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
                if(StringUtils.isNotBlank(detailEntity.getReceiptStatus())){
                    resultDTO.setSuccess(false);
                    resultDTO.setMsg(StrUtil.format("sku:{}，收货状态不为空，不可下推收货单",detailEntity.getSkuNo()));
                    return;
                }
                detailEntity.setReceiptStatus(DeliveryOrderEnum.ReceiptStatusEnum.WAIT_CONFIRMED.getCode());
                detailEntity.setReceiveUserId(loginUser.getUid());
                detailEntity.setReceiveUserName(loginUser.getUserName());
            }
            //新增采购收货
            SysDepartmentUserNumberDTO deptByUserId = sysUserFeign.getDeptByUserId(loginUser.getUid());
            List<DeliveryOrderDetailEntity> detailEntityGroupList = detailEntityGroupMap.get(key);
            WarehouseReceiveDTO.AddDTO addDTO = SupplierDeliveryConverter.INSTANCE.deliveryToReceiveConvert(deliveryOrderEntity,detailEntityGroupList);
            addDTO.setReceiveDeptId(deptByUserId.getDepartmentId());
            addDTO.setGenerateByDelivery(true);
            addDTO.setReceiveUserId(loginUser.getUid());
            if(StringUtils.isNotBlank(firstDTO.getReceiveDeptId())){
                addDTO.setReceiveDeptId(firstDTO.getReceiveDeptId());
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
            detailEntityGroupList.forEach(v->v.setReceiveCode(warehouseReceiveEntity.getCode()));
            srmDeliveryFeign.updateDeliveryDetail(detailEntityGroupList);
            resultDTO.setSuccess(true);
        });
        return resultDTOList;
    }

    @Override
    public PagingVO<DeliveryOrderExportExcelDTO> exportSupplierDeliveryOrder(PagingDTO<DeliveryOrderDTO.ParamDTO> dto) {
        return srmDeliveryFeign.exportSupplierDeliveryOrder(dto);
    }
}
