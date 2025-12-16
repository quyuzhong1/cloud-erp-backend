package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.dto.ApplicationCategoryDTO;
import com.erp.model.plm.entity.ApplicationCategoryEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.plm.mapper.ApplicationCategoryMapper;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeApplicationCategoryService;
import com.erp.server.plm.service.ApplicationCategoryService;
import com.erp.server.plm.service.ProductInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 产品应用分类 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2025-01-09
 */
@Slf4j
@Service
public class ApplicationCategoryServiceImpl extends SuperServiceImpl<ApplicationCategoryMapper, ApplicationCategoryEntity> implements ApplicationCategoryService {

    @Lazy
    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private SyncKingdeeApplicationCategoryService syncKingdeeApplicationCategoryService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ApplicationCategoryDTO.AddDTO addDTO) {
        verifyData(addDTO.getName(), addDTO.getCode());
        verifyData(addDTO.getName(), addDTO.getCode());
        ApplicationCategoryEntity applicationCategoryEntity = new ApplicationCategoryEntity();
        applicationCategoryEntity.setName(addDTO.getName());
        applicationCategoryEntity.setCode(addDTO.getCode());
        applicationCategoryEntity.setId(IdWorker.getIdStr());
        sendPushTask(Collections.singletonList(applicationCategoryEntity),SyncOperateEnum.OPERATE_APPROVE.getCode());
        boolean save = super.save(applicationCategoryEntity);
        if(!save) {
            throw new ServiceException("应用分类保存失败");
        }
        return new BaseResultDTO.AddDTO(applicationCategoryEntity.getId(), addDTO.getCode());
    }

    /**
     * 校验数据
     * @param name 名字
     * @param code code
     */
    private void verifyData(String name, String code) {
        verifyData(name, code, null);
    }

    /**
     * 校验数据
     * @param name 名字
     * @param code code
     * @param id id
     */
    private void verifyData(String name, String code, String id) {
        int nameCount = count(Wrappers.<ApplicationCategoryEntity>lambdaQuery().eq(ApplicationCategoryEntity::getName, name)
                .ne(StringUtils.hasText(id), ApplicationCategoryEntity::getId, id));
        if (nameCount > 0) {
            throw new ServiceException(ApiError.PRODUCT_APP_CATEGORY_NAME_EXISTS);
        }
        int codeCount = count(Wrappers.<ApplicationCategoryEntity>lambdaQuery().eq(ApplicationCategoryEntity::getCode, code)
                .ne(StringUtils.hasText(id), ApplicationCategoryEntity::getId, id));
        if (codeCount > 0) {
            throw new ServiceException(ApiError.PRODUCT_APP_CATEGORY_CODE_EXISTS);
        }
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ApplicationCategoryDTO.UpdateDTO addOrUpdateDTO) {
        verifyData(addOrUpdateDTO.getName(), addOrUpdateDTO.getCode(), addOrUpdateDTO.getId());
        ApplicationCategoryEntity applicationCategory = getById(addOrUpdateDTO.getId());
        ApplicationCategoryEntity applicationCategoryEntity =  BeanMapperUtils.map(ApplicationCategoryEntity.class, addOrUpdateDTO);
        applicationCategoryEntity.setSyncKingdeeId(applicationCategory.getSyncKingdeeId());
        sendPushTask(Collections.singletonList(applicationCategoryEntity),SyncOperateEnum.OPERATE_APPROVE.getCode());
        boolean save = super.updateById(applicationCategoryEntity);
        if(!save) {
            throw new ServiceException("应用分类保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        ApplicationCategoryEntity applicationCategory = getById(id);
        int count = productInfoService.count(Wrappers.<ProductInfoEntity>lambdaQuery().eq(ProductInfoEntity::getApplicationCategoryId, id));
        if (count > 0) {
            throw new ServiceException("分类下存在产品，请调整分类后删除");
        }
        //发送金蝶
        sendPushTask(Collections.singletonList(applicationCategory), SyncOperateEnum.OPERATE_DELETE.getCode());
        removeById(id);
    }

    private void sendPushTask(List<ApplicationCategoryEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeApplicationCategoryService.syncDataToKingdee(obj, operate);
            resultList.add(pushTaskEntity);
        });
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(resultList);
            }
        });
    }

    @Override
    public List<ApplicationCategoryDTO.ViewDTO> list(String searchKeyword) {
        return baseMapper.list(searchKeyword);
    }

    @Override
    public ApplicationCategoryEntity getByName(String applicationCategory) {
        return getOne(Wrappers.<ApplicationCategoryEntity>lambdaQuery().eq(ApplicationCategoryEntity::getName, applicationCategory));
    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return  this.lambdaUpdate()
                .eq(ApplicationCategoryEntity::getId,id)
                .set(!StringUtils.isEmpty(syncKingdeeId),ApplicationCategoryEntity::getSyncKingdeeId,syncKingdeeId)
                .update();
    }
}
