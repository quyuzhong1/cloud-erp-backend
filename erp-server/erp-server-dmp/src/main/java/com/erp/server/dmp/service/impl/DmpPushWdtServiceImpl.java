package com.erp.server.dmp.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.dmp.dto.DmpPushWdtDTO;
import com.erp.model.dmp.dto.DmpPushWdtDetailDTO;
import com.erp.model.dmp.entity.DmpPushWdtDetailEntity;
import com.erp.model.dmp.entity.DmpPushWdtEntity;
import com.erp.server.dmp.mapper.DmpPushWdtMapper;
import com.erp.server.dmp.service.DmpPushWdtDetailService;
import com.erp.server.dmp.service.DmpPushWdtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 推送旺店通中间表服务类
 * @date 2024-07-24
 * @author tanmujin
 */
@Slf4j
@Service
public class DmpPushWdtServiceImpl extends SuperServiceImpl<DmpPushWdtMapper, DmpPushWdtEntity> implements DmpPushWdtService {

    @Resource
    private DmpPushWdtDetailService dmpPushWdtDetailService;

    @Override
    public String add(DmpPushWdtDTO.AddDTO addDTO) {
        DmpPushWdtEntity dmpPushWdtEntity = new DmpPushWdtEntity();
        BeanMapper.copy(addDTO, dmpPushWdtEntity);
        this.saveOrUpdate(dmpPushWdtEntity);

        List<DmpPushWdtDetailEntity> detailList = BeanMapper.copyList(addDTO.getDetailDTOList(), DmpPushWdtDetailEntity.class);
        detailList.forEach(detailEntity -> detailEntity.setMainId(dmpPushWdtEntity.getId()));
        dmpPushWdtDetailService.saveOrUpdateBatch(detailList);
        return dmpPushWdtEntity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addBatch(List<DmpPushWdtDTO.AddDTO> dtoList) {
        for (DmpPushWdtDTO.AddDTO addDTO : dtoList) {
            DmpPushWdtEntity dmpPushWdtEntity = new DmpPushWdtEntity();
            BeanMapper.copy(addDTO, dmpPushWdtEntity);
            this.save(dmpPushWdtEntity);

            List<DmpPushWdtDetailEntity> detailList = BeanMapper.copyList(addDTO.getDetailDTOList(), DmpPushWdtDetailEntity.class);
            detailList.forEach(detailEntity -> detailEntity.setMainId(dmpPushWdtEntity.getId()));
            dmpPushWdtDetailService.saveBatch(detailList);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<DmpPushWdtDTO.ViewDTO> listByIdList(List<String> ids) {
        List<DmpPushWdtDTO.ViewDTO> resultList = new ArrayList<>(ids.size());
        List<DmpPushWdtEntity> list = this.lambdaQuery().in(DmpPushWdtEntity::getId, ids).list();
        for (DmpPushWdtEntity entity : list) {
            DmpPushWdtDTO.ViewDTO viewDTO = new DmpPushWdtDTO.ViewDTO();
            BeanMapper.copy(entity, viewDTO);
            List<DmpPushWdtDetailEntity> detailList = dmpPushWdtDetailService.lambdaQuery().eq(DmpPushWdtDetailEntity::getMainId, entity.getId()).list();
            List<DmpPushWdtDetailDTO> detailDTOList = BeanMapper.copyList(detailList, DmpPushWdtDetailDTO.class);
            viewDTO.setDetailDTOList(detailDTOList);
            resultList.add(viewDTO);
        }
        return resultList;
    }
}
