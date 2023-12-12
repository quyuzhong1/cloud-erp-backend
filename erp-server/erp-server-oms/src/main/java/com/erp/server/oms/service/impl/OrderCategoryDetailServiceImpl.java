package com.erp.server.oms.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.OrderCategoryDetailDTO;
import com.erp.model.oms.entity.OrderCategoryDetailEntity;
import com.erp.model.oms.entity.SoB2cRefCategoryEntity;
import com.erp.server.oms.mapper.OrderCategoryDetailMapper;
import com.erp.server.oms.service.OrderCategoryDetailService;
import com.erp.server.oms.service.SoB2cRefCategoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-24
 */
@Service
public class OrderCategoryDetailServiceImpl extends SuperServiceImpl<OrderCategoryDetailMapper, OrderCategoryDetailEntity> implements OrderCategoryDetailService {

    @Resource
    private SoB2cRefCategoryService soB2cRefCategoryService;

    /**
     * 添加分类明细
     *
     * @param mainId
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-08-25 14:59
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addList(String mainId, List<OrderCategoryDetailDTO.AddDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<OrderCategoryDetailEntity> addList = BeanMapper.copyList(detailList, OrderCategoryDetailEntity.class);
        addList.forEach(d -> d.setMainId(mainId));
        this.saveBatch(addList);

    }


    /**
     * 检查能否修改
     *
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-08-25 15:10
     */
    @Override
    public void checkUpdate(List<OrderCategoryDetailDTO.UpdateDTO> detailList) {
        List<OrderCategoryDetailDTO.UpdateDTO> updateList = detailList.stream().filter(d -> StringUtils.isNotBlank(d.getId())).collect(Collectors.toList());

        List<String> categoryIdList = updateList.stream().
                map(OrderCategoryDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<OrderCategoryDetailEntity> categoryDetailList = CollectionUtils.isNotEmpty(categoryIdList) ? this.listByIds(categoryIdList) : Collections.emptyList();
        //修改的值 分类
        List<String> updateIdList = new ArrayList<>();
        for (OrderCategoryDetailDTO.UpdateDTO item : updateList) {
            String id = item.getId();
            String name = item.getName();
            String dbName = categoryDetailList.stream().filter(c -> c.getId().equals(id)).findFirst().
                    map(OrderCategoryDetailEntity::getName).orElse("");
            if (!name.equals(dbName)) {
                updateIdList.add(id);
            }
        }

        //TODO 检查销售订单是否用了改分类
        List<SoB2cRefCategoryEntity> list = soB2cRefCategoryService.listCategoryIdList(updateIdList);
        if (list.size() > 0) {
            throw new ServiceException("存在已引用的B2C销售订单 无法更改");
        }

    }


    /**
     * 修改分类详情
     *
     * @param mainId
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-08-25 15:21
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetail(String mainId, List<OrderCategoryDetailDTO.UpdateDTO> detailList) {
        //这是存在数据库的
        List<OrderCategoryDetailEntity> dbList = this.listDbByMainId(mainId);
        //这是修改的
        List<OrderCategoryDetailDTO.UpdateDTO> updateList = detailList.stream().filter(d -> StringUtils.isNotBlank(d.getId())).collect(Collectors.toList());
        List<OrderCategoryDetailEntity> saveOrUpdateList = BeanMapper.copyList(detailList, OrderCategoryDetailEntity.class);
        saveOrUpdateList.forEach(s -> s.setMainId(mainId));
        List<Pair<String, String>> pairList = updateList.stream().map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        //获取到要删除的id
        List<String> deleteIdList = getDeleteIds(pairList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);

            soB2cRefCategoryService.removeByCategoryIds(deleteIdList);
        }
        this.saveOrUpdateBatch(saveOrUpdateList);
    }

    private List<String> getDeleteIds(List<Pair<String, String>> pairList, List<OrderCategoryDetailEntity> dbList) {
        List<String> ids = pairList.stream().filter(g -> StringUtils.isNotBlank(g.getKey())).
                map(obj -> obj.getKey()).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(OrderCategoryDetailEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }

    @Override
    public List<OrderCategoryDetailEntity> listDbByMainId(String mainId) {
        return this.lambdaQuery().eq(OrderCategoryDetailEntity::getMainId, mainId).list();
    }


    /**
     * 订单分类列表
     *
     * @return
     */
    @Override
    public List<OrderCategoryDetailDTO.ListDTO> listOrderCategory() {

        return baseMapper.listOrderCategory();
    }
}
