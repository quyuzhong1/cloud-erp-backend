package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.mrp.dto.ReplenishmentRefLabelDTO;
import com.erp.model.mrp.entity.ReplenishmentRefLabelEntity;
import com.erp.server.mrp.mapper.ReplenishmentRefLabelMapper;
import com.erp.server.mrp.service.OperateLogService;
import com.erp.server.mrp.service.ReplenishmentRefLabelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * <p>
 * 补货建议标签关系表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-30
 */
@Slf4j
@Service
public class ReplenishmentRefLabelServiceImpl extends SuperServiceImpl<ReplenishmentRefLabelMapper, ReplenishmentRefLabelEntity> implements ReplenishmentRefLabelService {
    @Autowired
    private OperateLogService operateLogService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ReplenishmentRefLabelDTO.UpdateDTO updateDTO,String refId) {
        // 数据处理
        List<ReplenishmentRefLabelEntity> list = handleData(updateDTO, refId);
        log.info("编辑 开始修改补货建议标签关系单数据，id：【{}】", refId);
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("补货建议标签关系单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public void deleteLabel(List<String> labelIdList, String refId) {
        lambdaUpdate().in(ReplenishmentRefLabelEntity::getLabelId,labelIdList)
                .eq(ReplenishmentRefLabelEntity::getRefId,refId)
                .remove();
    }


    /**
    * 新增修改处理数据
    */
    private List<ReplenishmentRefLabelEntity> handleData(ReplenishmentRefLabelDTO.UpdateDTO updateDTO,String refId) {
            List<ReplenishmentRefLabelEntity> resultList = new ArrayList<>();
        //标签数据
        List<ReplenishmentRefLabelEntity> replenishmentRefLabelList = listByLabelIdListAndRefIdList(updateDTO.getLabelIdList(), Arrays.asList(refId));
        for (String labelId : updateDTO.getLabelIdList()) {
            ReplenishmentRefLabelEntity resultEntity = new ReplenishmentRefLabelEntity();
            resultEntity.setRefId(refId);
            resultEntity.setLabelId(labelId);
            resultEntity.setType(updateDTO.getType());
            ReplenishmentRefLabelEntity entity = replenishmentRefLabelList.stream().filter(obj -> StrUtil.equals(obj.getLabelId(), labelId) && StrUtil.equals(refId, obj.getRefId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(entity)) {
                resultEntity.setId(entity.getId());
            }
            resultList.add(resultEntity);
        }
        return resultList;
    }

    /**
     * 根据标签id和来源id查询
     * @author will
     * @date 2024/8/30 15:41
     * @param labelIdList
     * @param refIdList
     * @return List<ReplenishmentRefLabelEntity>
     */
    private List<ReplenishmentRefLabelEntity> listByLabelIdListAndRefIdList (List<String> labelIdList, List<String> refIdList) {
        return lambdaQuery().in(ReplenishmentRefLabelEntity::getLabelId,labelIdList)
                .in(ReplenishmentRefLabelEntity::getRefId,refIdList)
                .list();
    }
}
