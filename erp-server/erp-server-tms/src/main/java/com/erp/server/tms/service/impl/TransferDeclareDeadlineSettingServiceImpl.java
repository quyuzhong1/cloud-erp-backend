package com.erp.server.tms.service.impl;


import com.common.business.constant.MultipleOptionConstants;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.tms.dto.MultipleOptionDTO;
import com.erp.model.tms.dto.TransferDeclareDeadlineSettingDTO;
import com.erp.model.tms.entity.MultipleOptionEntity;
import com.erp.model.tms.entity.TransferDeclareDeadlineSettingEntity;
import com.erp.server.tms.mapper.TransferDeclareDeadlineSettingMapper;
import com.erp.server.tms.service.MultipleOptionService;
import com.erp.server.tms.service.TransferDeclareDeadlineSettingService;
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
 * 截单设置 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-24
 */
@Slf4j
@Service
public class TransferDeclareDeadlineSettingServiceImpl extends SuperServiceImpl<TransferDeclareDeadlineSettingMapper, TransferDeclareDeadlineSettingEntity> implements TransferDeclareDeadlineSettingService {
    @Resource
    private MultipleOptionService multipleOptionService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(List<TransferDeclareDeadlineSettingDTO.AddDTO> addList) {
        //校验选择的中转服务商是否重复
        checkDuplicationSupplier(addList);

        //原明细数据
        List<TransferDeclareDeadlineSettingEntity> oldList = this.list();

        //比对是否有删除的数据，有就删除
        List<String> deleteIds = getDeleteIds(addList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            //删除关联表
            multipleOptionService.deleteByMainIds(deleteIds);
            //删除原配置
            this.removeByIds(deleteIds);
        }

        //组装数据，保存
        for (TransferDeclareDeadlineSettingDTO.AddDTO addDTO : addList) {
            if (addDTO.getGenerateTime().isAfter(addDTO.getDeadlineTime())) {
                throw new ServiceException(ApiError.GENERATE_TIME_GT_DEADLINE_TIME);
            }

            TransferDeclareDeadlineSettingEntity entity = new TransferDeclareDeadlineSettingEntity();
            entity.setId(addDTO.getId());
            entity.setDeadlineTime(addDTO.getDeadlineTime());
            entity.setGenerateTime(addDTO.getGenerateTime());

            //保存
            this.saveOrUpdate(entity);

            //保存下拉多选的中转服务商
            MultipleOptionDTO.AddDTO optionDTO = new MultipleOptionDTO.AddDTO();
            optionDTO.setMainId(entity.getId());
            optionDTO.setType(MultipleOptionConstants.TRANSFER_DECLARE_DEADLINE_SETTING);
            optionDTO.setRefIdList(addDTO.getTransferLogisticsSupplierIdList());
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
    private void checkDuplicationSupplier(List<TransferDeclareDeadlineSettingDTO.AddDTO> addDTOList) {
        List<String> transferLogisticsSupplierIdList = new ArrayList<>();
        for (TransferDeclareDeadlineSettingDTO.AddDTO addDTO : addDTOList) {
            transferLogisticsSupplierIdList.addAll(addDTO.getTransferLogisticsSupplierIdList());
        }
        Set<String> set = new HashSet<>(transferLogisticsSupplierIdList);
        if (set.size() != transferLogisticsSupplierIdList.size()) {
            throw new ServiceException(ApiError.TRANSFER_DELIVERY_LOGISTICS_SUPPLIER);
        }
    }

    @Override
    public List<TransferDeclareDeadlineSettingDTO.ViewDTO> view() {
        List<TransferDeclareDeadlineSettingEntity> list = this.list();
        List<String> ids = list.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
        List<MultipleOptionEntity> optionEntityList = multipleOptionService.listByMainIds(ids);
        List<TransferDeclareDeadlineSettingDTO.ViewDTO> viewDTOList = BeanMapper.copyList(list, TransferDeclareDeadlineSettingDTO.ViewDTO.class);
        for (TransferDeclareDeadlineSettingDTO.ViewDTO viewDTO : viewDTOList) {
            List<String> deliveryLogisticsSupplierIdList = optionEntityList.stream().filter(req -> viewDTO.getId().equals(req.getMainId())).map(req -> req.getRefId()).distinct().collect(Collectors.toList());
            viewDTO.setTransferLogisticsSupplierIdList(deliveryLogisticsSupplierIdList);
        }
        return viewDTOList;
    }

    private List<String> getDeleteIds(List<TransferDeclareDeadlineSettingDTO.AddDTO> newList, List<TransferDeclareDeadlineSettingEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(TransferDeclareDeadlineSettingDTO.AddDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(TransferDeclareDeadlineSettingEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }
}
