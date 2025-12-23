package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.file.dto.FileDTO;
import com.erp.model.oms.dto.SoLabelDTO;
import com.erp.model.oms.entity.SoLabelEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.oms.convert.SoLabelConverter;
import com.erp.server.oms.mapper.SoLabelMapper;
import com.erp.server.oms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * B2B订单面单表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-02-24
 */
@Slf4j
@Service
public class SoLabelServiceImpl extends SuperServiceImpl<SoLabelMapper, SoLabelEntity> implements SoLabelService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private FileFeign fileFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoLabelDTO.AddDTO addDTO) {
        SoLabelEntity soLabelEntity = new SoLabelEntity();
        BeanMapperUtils.copy(addDTO, soLabelEntity);

        // 数据处理
        handleData(soLabelEntity);

        log.info("开始新增B2B订单面单表");
        boolean save = super.save(soLabelEntity);
        if(!save) {
            throw new ServiceException("B2B订单面单表保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "B2B订单面单表" , soLabelEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, soLabelEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(soLabelEntity.getId(), soLabelEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoLabelDTO.UpdateDTO addOrUpdateDTO) {
        SoLabelEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "B2B订单面单表"));
        SoLabelEntity soLabelEntity =  BeanMapperUtils.map(SoLabelEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(soLabelEntity);
        log.info("编辑 开始修改B2B订单面单表数据，id：【{}】", old.getId());
        boolean save = super.updateById(soLabelEntity);
        if(!save) {
            throw new ServiceException("B2B订单面单表保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录B2B订单面单表日志数据，id：【{}】", soLabelEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soLabelEntity.getId(), "B2B订单面单表");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, soLabelEntity, null, soLabelEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public SoLabelEntity getByMainId(String mainId) {
        return lambdaQuery().eq(SoLabelEntity::getMainId, mainId).last(" limit 1 ").one();
    }
    @Override
    public List<SoLabelDTO.PrintLabelDTO> listLogisticsLabel(List<String> ids) {
        List<SoDeliveryNoticeEntity> soDeliveryNoticeEntities = FeignQuery.getByIds(SoDeliveryNoticeEntity.class, ids);
        List<SoLabelDTO.PrintLabelDTO> dtoList = SoLabelConverter.INSTANCE.entityToPrintDTO(soDeliveryNoticeEntities);
        List<String> soIds = dtoList.stream().map(SoLabelDTO.PrintLabelDTO::getSoId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SoLabelEntity> soLabelEntityList = this.listByMainIds(soIds);
        if (CollUtil.isEmpty(soLabelEntityList)){
            return dtoList;
        }
        dtoList.forEach(label -> {
            SoLabelEntity labelEntity = soLabelEntityList.stream().filter(e -> e.getMainId().equals(label.getSoId())).findFirst().orElse(null);
            if (Objects.nonNull(labelEntity) && CharSequenceUtil.isNotBlank(labelEntity.getLogisticsLabelUrl())){
                label.setLogisticsLabelUrl(labelEntity.getLogisticsLabelUrl());
                label.setHasLabel(Boolean.TRUE);
            }else {
                label.setHasLabel(Boolean.FALSE);
            }
        });
        return dtoList;
    }

    @Override
    public String printLogisticsLabel(List<String> ids) {
        List<SoLabelEntity> soLabelEntityList = this.listByMainIds(ids);
        if (CollUtil.isEmpty(soLabelEntityList)){
            throw new ServiceException("无可打印的物流面单");
        }
        Map<String, String> baseMap = soLabelEntityList.stream().collect(Collectors.toMap(SoLabelEntity::getMainId, SoLabelEntity::getLogisticsLabelUrl));
        List<String> urlList = ids.stream().map(e -> baseMap.getOrDefault(e,null)).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        try {
            return fileFeign.mergeFiles(urlList);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(ApiError.LOGISTICS_PDF_SO_MERGE_ERROR);
        }
    }

    @Override
    public void changeLogisticsLabelToUrl() {
        //获取所有订单标签数量
        Integer count = lambdaQuery().eq(SoLabelEntity::getLogisticsLabelUrl, CharSequenceUtil.EMPTY).ne(SoLabelEntity::getLogisticsLabelBase64, CharSequenceUtil.EMPTY).count();
        if(count == 0){
            return;
        }
        //分批处理订单标签数据
        // 分页处理
        int pageSize = 100;
        int totalPages = (int) Math.ceil((double) count / pageSize);
        for (int pageNum = 0; pageNum < totalPages; pageNum++) {
            //每次处理100条数据
            List<SoLabelEntity> soB2cLabelEntities = lambdaQuery().eq(SoLabelEntity::getLogisticsLabelUrl, CharSequenceUtil.EMPTY).ne(SoLabelEntity::getLogisticsLabelBase64, CharSequenceUtil.EMPTY).orderByAsc(SoLabelEntity::getCreateTime).last("LIMIT 100").list();
            if(CollectionUtils.isEmpty(soB2cLabelEntities)){
                return;
            }
            //处理数据
            soB2cLabelEntities.forEach(this::uploadFile);
        }
    }

    private void uploadFile(SoLabelEntity entity) {
        if (Objects.isNull(entity)){
            return;
        }
        if (CharSequenceUtil.isNotBlank(entity.getLogisticsLabelUrl())){
            return;
        }
        FileDTO.UploadBase64 uploadBase64 = FileDTO.UploadBase64.builder().base64(entity.getLogisticsLabelBase64()).fileName(entity.getMainId() + ".pdf").build();
        String url = fileFeign.uploadFileByBase64(uploadBase64);
        this.lambdaUpdate().set(SoLabelEntity::getLogisticsLabelUrl, url).eq(SoLabelEntity::getId, entity.getId()).update();
    }

    private List<SoLabelEntity> listByMainIds(List<String> soIds) {
        if (CollUtil.isEmpty(soIds)){
            return Collections.emptyList();
        }
        return lambdaQuery().in(SoLabelEntity::getMainId, soIds).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SoLabelEntity soLabelEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
