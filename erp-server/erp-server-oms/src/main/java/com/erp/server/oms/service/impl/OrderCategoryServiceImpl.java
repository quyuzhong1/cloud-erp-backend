package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseChildDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.OrderCategoryDTO;
import com.erp.model.oms.dto.OrderCategoryDetailDTO;
import com.erp.model.oms.entity.OrderCategoryDetailEntity;
import com.erp.model.oms.entity.OrderCategoryEntity;
import com.erp.server.oms.mapper.OrderCategoryMapper;
import com.erp.server.oms.service.OrderCategoryDetailService;
import com.erp.server.oms.service.OrderCategoryService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单分类表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-24
 */
@Service
public class OrderCategoryServiceImpl extends SuperServiceImpl<OrderCategoryMapper, OrderCategoryEntity> implements OrderCategoryService {

    @Resource
    private OrderCategoryDetailService orderCategoryDetailService;

    /**
     * 添加订单分类
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-25 14:53
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(OrderCategoryDTO.AddDTO dto) {
        OrderCategoryEntity entity = new OrderCategoryEntity();
        String id = IdWorker.getIdStr();
        String groupName = dto.getGroupName();
        checkGroupName(id, groupName);
        entity.setId(id);
        entity.setGroupName(groupName);
        entity.setRemark(dto.getRemark());
        List<OrderCategoryDetailDTO.AddDTO> detailList = dto.getDetailList();
        List<String> nameList = detailList.stream().map(OrderCategoryDetailDTO.AddDTO::getName).collect(Collectors.toList());
        if (nameList.size() != nameList.stream().distinct().count()) {
            throw new ServiceException("分类名存在重复");
        }

        Boolean saveResult = this.save(entity);
        if (saveResult) {
            orderCategoryDetailService.addList(id, dto.getDetailList());
        }
        return saveResult;
    }

    /**
     * 检查组名
     *
     * @param id
     * @param groupName
     */
    private void checkGroupName(String id, String groupName) {
        Integer count = this.lambdaQuery().ne(StringUtils.isNotBlank(id), OrderCategoryEntity::getId, id).
                eq(OrderCategoryEntity::getGroupName, groupName).count();
        if (count > 0) {
            throw new ServiceException("组别已存在");
        }
    }


    /**
     * 更改订单分类
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-25 15:09
     */
    @Override
    public Boolean updateCategory(OrderCategoryDTO.UpdateDTO dto) {
        OrderCategoryEntity entity = new OrderCategoryEntity();
        BeanMapper.copy(dto, entity);
        checkGroupName(dto.getId(), dto.getGroupName());

        List<OrderCategoryDetailDTO.UpdateDTO> detailList = dto.getDetailList();
        List<String> nameList = detailList.stream().map(OrderCategoryDetailDTO.UpdateDTO::getName).collect(Collectors.toList());
        if (nameList.size() != nameList.stream().distinct().count()) {
            throw new ServiceException("分类名存在重复");
        }
        orderCategoryDetailService.checkUpdate(detailList);
        Boolean updateResult = this.updateById(entity);
        if (updateResult) {
            orderCategoryDetailService.updateDetail(dto.getId(), dto.getDetailList());
        }
        return updateResult;
    }


    /**
     * 订单分类分页
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.OrderCategoryDTO.PagingViewDTO>
     * @author yl
     * @date 2023-08-25 15:33
     */
    @Override
    public PagingVO<OrderCategoryDTO.PagingViewDTO> paging(PagingDTO<OrderCategoryDTO.PagingParamDTO> dto) {
        OrderCategoryDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<OrderCategoryDTO.PagingViewDTO> list = pageData.getRecords();
        return new PagingVO<>(pageData);
    }


    /**
     * 订单分类详情
     *
     * @param id
     * @return com.erp.model.oms.dto.OrderCategoryDTO.ViewDTO
     * @author yl
     * @date 2023-08-25 15:52
     */
    @Override
    public OrderCategoryDTO.ViewDTO view(String id) {
        OrderCategoryEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("订单分类不存在");
        }
        OrderCategoryDTO.ViewDTO viewDTO = new OrderCategoryDTO.ViewDTO();
        BeanMapper.copy(entity, viewDTO);
        List<OrderCategoryDetailEntity> detailEntityList = orderCategoryDetailService.listDbByMainId(id);
        List<OrderCategoryDetailDTO.UpdateDTO> detailList = BeanMapper.copyList(detailEntityList, OrderCategoryDetailDTO.UpdateDTO.class);
        viewDTO.setDetailList(detailList);
        return viewDTO;
    }


    /**
     * 更改订单分类
     *
     * @param orderCategory
     * @param disabled
     * @return com.common.business.dto.base.BatchResultDTO
     * @author yl
     * @date 2023-08-25 16:01
     */
    @Override
    public BatchResultDTO updateStatus(OrderCategoryEntity orderCategory, Boolean disabled) {
        if (Objects.nonNull(orderCategory)) {
            //数据库的禁用状态
            Boolean dbDisabled = orderCategory.getDisabled();
            if (dbDisabled.equals(disabled)) {
                throw new ServiceException("存在相同的状态");
            }
            orderCategory.setDisabled(disabled);
            this.updateById(orderCategory);
            return BatchResultDTO.success(orderCategory.getId(), orderCategory.getGroupName(), OperationTypeEnum.DISABLED);

        }
        return BatchResultDTO.fail(orderCategory.getId(), orderCategory.getGroupName(), "订单分类不存在");
    }


    /**
     * 订单分类的树结构
     *
     * @return
     */
    @Override
    public List<BaseChildDTO.ListChildTreeDTO> tree() {
        List<OrderCategoryDTO.ListDTO> list = baseMapper.listDetail();
        Map<String, List<OrderCategoryDTO.ListDTO>> map = list.stream().collect(Collectors.groupingBy(OrderCategoryDTO.ListDTO::getId));
        List<BaseChildDTO.ListChildTreeDTO> resultList = new ArrayList<>(map.size());
        for (Map.Entry<String, List<OrderCategoryDTO.ListDTO>> item : map.entrySet()) {
            BaseChildDTO.ListChildTreeDTO treeDTO = new BaseChildDTO.ListChildTreeDTO();
            treeDTO.setId(item.getKey());
            List<OrderCategoryDTO.ListDTO> detailList = item.getValue();
            treeDTO.setName(detailList.get(0).getGroupName());
            treeDTO.setDisabled(detailList.get(0).getDisabled());
            List<BaseChildDTO.ListChildTreeDTO> childList = detailList.stream().
                    map(d -> new BaseChildDTO.ListChildTreeDTO(d.getDetailId(), d.getDetailName(), d.getDisabled(), Collections.emptyList())).
                    collect(Collectors.toList());
            treeDTO.setChildren(childList);
            resultList.add(treeDTO);

        }
        return resultList;
    }


}
