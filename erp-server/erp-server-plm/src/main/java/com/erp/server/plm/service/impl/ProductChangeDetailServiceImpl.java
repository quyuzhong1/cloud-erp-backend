package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.erp.model.plm.entity.ProductChangeEntity;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.entity.ProductChangeDetailEntity;
import com.erp.server.plm.mapper.ProductChangeDetailMapper;
import com.erp.server.plm.service.ProductChangeDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.plm.service.OperateLogService;
import com.common.core.exception.ServiceException;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.ProductChangeDetailDTO;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 产品变更信息表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2026-02-03
 */
@Slf4j
@Service
public class ProductChangeDetailServiceImpl extends SuperServiceImpl<ProductChangeDetailMapper, ProductChangeDetailEntity> implements ProductChangeDetailService {
    @Resource
    private OperateLogService operateLogService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(ProductChangeEntity productChangeEntity, List<ProductChangeDetailDTO.AddDTO> detailDTOList) {
        detailDTOList.forEach(v->v.setMainId(productChangeEntity.getSkuId()));
        List<ProductChangeDetailEntity> detailEntityList = BeanMapperUtils.copyList(ProductChangeDetailEntity.class, detailDTOList);
        boolean save = super.saveBatch(detailEntityList);
        if(!save) {
            throw new ServiceException("产品变更信息明细单保存失败");
        }

        return true;
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ProductChangeEntity productChangeEntity ,List<ProductChangeDetailDTO.UpdateDTO> updateDTOList) {
        //查询数据库数据
        List<ProductChangeDetailEntity> dbList = this.listByMains(Lists.newArrayList(productChangeEntity.getSkuId()));
        List<String> dbIdList = dbList.stream().map(ProductChangeDetailEntity::getId).collect(Collectors.toList());
        //删除
        List<String> updateIdList = updateDTOList.stream().filter(v-> ObjectUtil.isNotEmpty(v.getId())).map(ProductChangeDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> delIdList = dbIdList.stream().filter(v->!updateIdList.contains(v)).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(delIdList)){
            boolean remove = this.removeByIds(delIdList);
            if(!remove) {
                throw new ServiceException("产品变更信息明细单删除失败");
            }
        }
        //新增
        List<ProductChangeDetailDTO.AddDTO> addDTOList = updateDTOList.stream().filter(v-> ObjectUtil.isEmpty(v.getId())).map(v->{
            ProductChangeDetailDTO.AddDTO addDTO = BeanUtil.toBean(v, ProductChangeDetailDTO.AddDTO.class);
            addDTO.setMainId(productChangeEntity.getSkuId());
            return addDTO;
        }).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(addDTOList)){
            List<ProductChangeDetailEntity> addEntityList = BeanMapperUtils.copyList(ProductChangeDetailEntity.class, addDTOList);
            boolean save = super.saveBatch(addEntityList);
            if(!save) {
                throw new ServiceException("产品变更信息明细单新增失败");
            }
        }
        //更新
        List<ProductChangeDetailDTO.UpdateDTO> needUpdateList = updateDTOList.stream().filter(v-> ObjectUtil.isNotEmpty(v.getId())).collect(Collectors.toList());
        for (ProductChangeDetailDTO.UpdateDTO updateDTO : needUpdateList) {
            ProductChangeDetailEntity productChangeDetailEntity = BeanUtil.toBean(updateDTO, ProductChangeDetailEntity.class);
            ProductChangeDetailEntity old = dbList.stream().filter(v->v.getId().equals(productChangeDetailEntity.getId())).findFirst().orElse(null);
            boolean update = this.updateById(productChangeDetailEntity);
            if(!update) {
                throw new ServiceException("产品变更信息明细单修改失败");
            }
            // 记录子单操作日志
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), productChangeDetailEntity.getId(), "产品变更信息单明细");
            operateLogService.addSysLogByUpdate(old, productChangeDetailEntity, String.valueOf(ProductChangeDetailEntity.class), productChangeEntity.getId(),productChangeDetailEntity.getId(), msg);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<ProductChangeDetailEntity> listByMains(List<String> mainIds) {
        if(CollectionUtil.isEmpty(mainIds)){
            return new ArrayList<>();
        }

        return lambdaQuery().in(ProductChangeDetailEntity::getMainId,mainIds).list();
    }

    @Override
    public void deleteByMainId(String mainId) {
        lambdaUpdate().eq(ProductChangeDetailEntity::getMainId,mainId).remove();
    }

}
