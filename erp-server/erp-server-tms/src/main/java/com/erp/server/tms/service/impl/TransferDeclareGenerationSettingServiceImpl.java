package com.erp.server.tms.service.impl;

import com.common.business.constant.MultipleOptionConstants;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.tms.dto.MultipleOptionDTO;
import com.erp.model.tms.dto.TransferDeclareGenerationSettingDTO;
import com.erp.model.tms.entity.MultipleOptionEntity;
import com.erp.model.tms.entity.TransferDeclareGenerationSettingEntity;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.model.tms.entity.TransferLogisticsSupplierEntity;
import com.erp.server.tms.mapper.TransferDeclareGenerationSettingMapper;
import com.erp.server.tms.service.MultipleOptionService;
import com.erp.server.tms.service.TransferDeclareGenerationSettingService;
import com.erp.server.tms.service.TransferLogisticsChannelService;
import com.erp.server.tms.service.TransferLogisticsSupplierService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 预报设置-自动生成 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-24
 */
@Slf4j
@Service
public class TransferDeclareGenerationSettingServiceImpl extends SuperServiceImpl<TransferDeclareGenerationSettingMapper, TransferDeclareGenerationSettingEntity> implements TransferDeclareGenerationSettingService {
    @Resource
    private TransferLogisticsChannelService transferLogisticsChannelService;
    @Resource
    private TransferLogisticsSupplierService transferLogisticsSupplierService;
    @Resource
    private MultipleOptionService multipleOptionService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(List<TransferDeclareGenerationSettingDTO.AddDTO> addDTOList) {
        //校验选择的发货物流商是否重复
        checkDuplicationSupplier(addDTOList);

        //原明细数据
        List<TransferDeclareGenerationSettingEntity> oldList = this.list();

        //比对是否有删除的数据，有就删除
        List<String> deleteIds = getDeleteIds(addDTOList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            //删除关联表
            multipleOptionService.deleteByMainIds(deleteIds);
            //删除原配置
            this.removeByIds(deleteIds);
        }

        //组装数据，保存
        for (TransferDeclareGenerationSettingDTO.AddDTO addDTO : addDTOList) {

            TransferDeclareGenerationSettingEntity entity = new TransferDeclareGenerationSettingEntity();
            TransferLogisticsChannelEntity channelEntity = transferLogisticsChannelService.getById(addDTO.getTransferChannelId());
            entity.setId(addDTO.getId());
            entity.setTransferChannelId(channelEntity.getId());
            entity.setTransferChannelName(channelEntity.getName());
            TransferLogisticsSupplierEntity supplierEntity = transferLogisticsSupplierService.getById(channelEntity.getMainId());
            entity.setTransferLogisticsSupplierId(supplierEntity.getId());
            entity.setTransferLogisticsSupplierName(supplierEntity.getSupplierName());

            //保存
            this.saveOrUpdate(entity);

            //保存下拉多选的中转服务商
            MultipleOptionDTO.AddDTO optionDTO = new MultipleOptionDTO.AddDTO();
            optionDTO.setMainId(entity.getId());
            optionDTO.setType(MultipleOptionConstants.TRANSFER_DECLARE_GENERATION_SETTING);
            optionDTO.setRefIdList(addDTO.getDeliveryLogisticsSupplierIdList());
            multipleOptionService.add(optionDTO);

        }
    }

    /**
     * 校验中转服务商是否重复
     * @Author Luo_WG
     * @Date 2024/1/26 11:16
     * @param addDTOList
     * @return void
     **/
    private void checkDuplicationSupplier(List<TransferDeclareGenerationSettingDTO.AddDTO> addDTOList) {
        List<String> deliveryLogisticsSupplierIdList = new ArrayList<>();
        for (TransferDeclareGenerationSettingDTO.AddDTO addDTO : addDTOList) {
            deliveryLogisticsSupplierIdList.addAll(addDTO.getDeliveryLogisticsSupplierIdList());
        }
        Set<String> set = new HashSet<>(deliveryLogisticsSupplierIdList);
        if (set.size() != deliveryLogisticsSupplierIdList.size()) {
            throw new ServiceException(ApiError.DUPLICATION_DELIVERY_LOGISTICS_SUPPLIER);
        }
    }

    @Override
    public List<TransferDeclareGenerationSettingDTO.ViewDTO> forcastSettingView() {
        List<TransferDeclareGenerationSettingEntity> list = this.list();
        List<String> ids = list.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
        List<MultipleOptionEntity> optionEntityList = multipleOptionService.listByMainIds(ids);
        List<TransferDeclareGenerationSettingDTO.ViewDTO> viewDTOList = BeanMapper.copyList(list, TransferDeclareGenerationSettingDTO.ViewDTO.class);
        for (TransferDeclareGenerationSettingDTO.ViewDTO viewDTO : viewDTOList) {
            List<String> deliveryLogisticsSupplierIdList = optionEntityList.stream().filter(req -> viewDTO.getId().equals(req.getMainId())).map(req -> req.getRefId()).distinct().collect(Collectors.toList());
            viewDTO.setDeliveryLogisticsSupplierIdList(deliveryLogisticsSupplierIdList);
        }
        return viewDTOList;
    }

    private List<String> getDeleteIds(List<TransferDeclareGenerationSettingDTO.AddDTO> newList, List<TransferDeclareGenerationSettingEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(TransferDeclareGenerationSettingDTO.AddDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(TransferDeclareGenerationSettingEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }
}
