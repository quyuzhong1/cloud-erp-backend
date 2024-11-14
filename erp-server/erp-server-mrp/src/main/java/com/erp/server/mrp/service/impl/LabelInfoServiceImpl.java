package com.erp.server.mrp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.LabelInfoDTO;
import com.erp.model.mrp.entity.LabelInfoEntity;
import com.erp.model.mrp.entity.ReplenishmentRefLabelEntity;
import com.erp.model.mrp.vo.LabelVO;
import com.erp.server.mrp.mapper.LabelInfoMapper;
import com.erp.server.mrp.service.LabelInfoService;
import com.erp.server.mrp.service.OperateLogService;
import com.erp.server.mrp.service.ReplenishmentRefLabelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 标签信息表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-30
 */
@Slf4j
@Service
public class LabelInfoServiceImpl extends SuperServiceImpl<LabelInfoMapper, LabelInfoEntity> implements LabelInfoService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private ReplenishmentRefLabelService replenishmentRefLabelService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<LabelInfoDTO.UpdateDTO> updateList) {
        if (CollectionUtils.isEmpty(updateList)) {
            updateList = Collections.EMPTY_LIST;
        }
        List<LabelInfoEntity> list = BeanMapperUtils.copyList(LabelInfoEntity.class, updateList);
        //原标签数据
        List<LabelInfoEntity> oldList = this.list();
        //删除明细
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            deleteIds.stream().forEach(obj -> this.delete(obj));
        }
        if (CollectionUtils.isEmpty(list)) {
            return  Boolean.TRUE;
        }
        // 数据处理
        checkData(list);
        log.info("编辑 开始修改标签信息单数据");
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("标签信息单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public BatchResultDTO delete(String id) {
        LabelInfoEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "标签信息单"));
        List<ReplenishmentRefLabelEntity> refLabelList = replenishmentRefLabelService.listLabelInfoByLabelId(id);
        if (CollectionUtils.isNotEmpty(refLabelList)) {
            throw new ServiceException("标签已被引用不支持删除");
        }
        //删除标签
        this.removeById(old.getId());
        return BatchResultDTO.success(old.getId(), old.getName(), OperationTypeEnum.UPDATE);
    }

    @Override
    public BatchResultDTO updateDisabled(String id,Boolean disabled) {
        LabelInfoEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "标签信息单"));
        if (old.getDisabled().equals(disabled)) {
            if (disabled) {
                return BatchResultDTO.success(old.getId(), old.getName(), "已禁用不支持再次禁用");
            } else {
                return BatchResultDTO.success(old.getId(), old.getName(), "已启用不支持再次启用");
            }
        }
        old.setDisabled(disabled);
        this.updateById(old);
        return BatchResultDTO.success(old.getId(), old.getName(), OperationTypeEnum.UPDATE);
    }

    @Override
    public List<LabelInfoDTO.ListDTO> listLabelInfo() {
        return baseMapper.listLabelInfo();
    }

    @Override
    public List<LabelInfoDTO.ListDTO> searchLabel(String searchKeyword) {
        return baseMapper.searchLabel(searchKeyword);
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<LabelInfoEntity> newList, List<LabelInfoEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(LabelInfoEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(LabelInfoEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 根据名称查询
     * @author will
     * @date 2024/8/30 11:45
     * @param name
     * @return LabelInfoEntity
     */
    private LabelInfoEntity getByName (String name) {
      return lambdaQuery().eq(LabelInfoEntity::getName,name).last("limit 1").one();
    }

    /**
    * 新增修改处理数据
    */
    private void checkData(List<LabelInfoEntity> list) {
       if (CollectionUtils.isEmpty(list)) {
           return;
       }
        String names = list.stream().collect(Collectors.groupingBy(LabelInfoEntity::getName)).entrySet().stream().filter(obj -> obj.getValue().size() > MathUtil.ONE).map(obj -> obj.getKey()).collect(Collectors.joining(","));
        if (StrUtil.isNotBlank(names)) {
            throw new ServiceException(StrUtil.format("标签名称【{}】重复，不支持新增",names));
        }
    }
    @Override
    public List<LabelVO> listLabelByReplenishmentIds(List<String> ids) {
        return baseMapper.listLabelByReplenishmentIds(ids);
    }

    @Override
    public List<LabelVO> listLabelByReplenishmentId(String id) {
        return baseMapper.listLabelByReplenishmentIds(Collections.singletonList(id));
    }
}
