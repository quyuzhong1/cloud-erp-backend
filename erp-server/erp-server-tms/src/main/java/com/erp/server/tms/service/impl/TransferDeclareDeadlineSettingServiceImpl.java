package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.constant.MultipleOptionConstants;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.dto.MultipleOptionDTO;
import com.erp.model.tms.dto.TransferDeclareGenerationSettingDTO;
import com.erp.model.tms.entity.*;
import com.erp.server.tms.mapper.TransferDeclareDeadlineSettingMapper;
import com.erp.server.tms.service.MultipleOptionService;
import com.erp.server.tms.service.TransferDeclareDeadlineSettingService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.TransferDeclareDeadlineSettingDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private MultipleOptionService multipleOptionService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(List<TransferDeclareDeadlineSettingDTO.AddDTO> addList) {

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
            TransferDeclareDeadlineSettingEntity entity = new TransferDeclareDeadlineSettingEntity();
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
