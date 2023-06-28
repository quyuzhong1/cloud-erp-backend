package com.erp.server.dmp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpTransferInfoDetailEntity;
import com.erp.model.dmp.entity.DmpTransferInfoEntity;
import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.dmp.mapper.DmpTransferInfoMapper;
import com.erp.server.dmp.service.DmpTransferInfoDetailService;
import com.erp.server.dmp.service.DmpTransferInfoService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 直接调拨单 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-19
 */
@Slf4j
@Service
public class DmpTransferInfoServiceImpl extends SuperServiceImpl<DmpTransferInfoMapper, DmpTransferInfoEntity> implements DmpTransferInfoService {

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private DmpTransferInfoDetailService dmpTransferInfoDetailService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(DmpTransferInfoEntity entity) {
        //新增主表数据
        boolean save = this.save(entity);
        if (!save) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //新增明细
        Boolean addDetail = dmpTransferInfoDetailService.add(entity.getDetailList(), entity.getId());
        if (!addDetail) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(DmpTransferInfoEntity entity) {
        //新增主表数据
        boolean update = this.update(entity);
        if (!update) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        //新增明细
        Boolean updateDetail = dmpTransferInfoDetailService.update(entity.getDetailList(), entity.getId());
        if (!updateDetail) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return Boolean.TRUE;
    }


    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void checkOrder(DmpTransferInfoEntity entity) {

       /**
        * 1、定时获取【直接调拨单】，没有数据则新增，有数据则同步单据状态
        * 2、将【直接调拨单】同步给ERP系统【状态：已审核】【反审核操作，则更新状态为待提交】
        */
       //新增或更新dmp服务直接调拨单数据
        addOrUpdateDmpTransferInfo(entity);

        //新增或更新wms服务直接调拨单数据
        addOrUpdateWmsTransferInfo(entity);
    }

    /**
     * 新增或修改dmp直接调拨单
     * @param entity
     */
    private void addOrUpdateDmpTransferInfo (DmpTransferInfoEntity entity) {

        DmpTransferInfoEntity dmpTransferInfoEntity = this.getByCode(entity.getCode());
        if (ObjectUtils.isEmpty(dmpTransferInfoEntity)) {
            //不存在则新增
            this.add(entity);
        } else {
            //存在则修改
            entity.setId(null);
            entity.setSourceId(dmpTransferInfoEntity.getSourceId());
            List<DmpTransferInfoDetailEntity> oldDetailList = dmpTransferInfoDetailService.listByMainId(dmpTransferInfoEntity.getId());
            if (CollectionUtils.isEmpty(oldDetailList)) {
                throw new ServiceException(ApiError.ERROR_1040,"直接调拨单" + dmpTransferInfoEntity.getCode());
            }
            List<DmpTransferInfoDetailEntity> newDetailList = entity.getDetailList();
            if (CollectionUtils.isEmpty(newDetailList)) {
                throw new ServiceException(ApiError.ERROR_1041,"直接调拨单" + dmpTransferInfoEntity.getCode());
            }
            for (DmpTransferInfoDetailEntity detailEntity : newDetailList) {
                //明细id
                String detailId = oldDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(detailEntity.getSourceDetailId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
                detailEntity.setId(null);
                detailEntity.setId(detailId);
            }
            this.update(entity);
        }


    }

    /**
     * 新增或修改wms直接调拨单
     *  @param entity
     */
    private void addOrUpdateWmsTransferInfo (DmpTransferInfoEntity entity) {

        TransferInfoDTO.ViewDTO viewDTO = wmsTaskFeign.ViewTransferInfoByCode(entity.getCode());
        //数据格式化
        TransferInfoDTO.ViewDTO resultDTO = handleWmsTransferInfo(entity);
        if (ObjectUtils.isEmpty(viewDTO)) {
            //不存在则新增
            //wmsTaskFeign.addTransferInfo(resultDTO);
        } else {
            //存在则更新
            resultDTO.setSourceId(resultDTO.getId());

        }
    }

    private TransferInfoDTO.ViewDTO handleWmsTransferInfo (DmpTransferInfoEntity entity) {
        TransferInfoDTO.ViewDTO resultDTO = new TransferInfoDTO.ViewDTO();

        return resultDTO;
    }


    /**
     * @description: 根据编码查询
     * @author Will
     * @date: 2023/6/28 18:01
     * @param code
     * @return DmpTransferInfoEntity
     */
    private DmpTransferInfoEntity getByCode (String code) {
       return lambdaQuery().eq(DmpTransferInfoEntity::getCode,code)
                .one();
    }
}
