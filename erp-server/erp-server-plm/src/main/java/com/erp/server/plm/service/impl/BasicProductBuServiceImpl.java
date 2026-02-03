package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.entity.ProductRefBuEntity;
import com.erp.model.scm.entity.CfgSupplierSalesConditionEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.ProductRefBuService;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.entity.BasicProductBuEntity;
import com.erp.server.plm.mapper.BasicProductBuMapper;
import com.erp.server.plm.service.BasicProductBuService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.plm.service.OperateLogService;
import com.common.core.exception.ServiceException;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.BasicProductBuDTO;
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
 * 产品BU信息 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2026-01-16
 */
@Slf4j
@Service
public class BasicProductBuServiceImpl extends SuperServiceImpl<BasicProductBuMapper, BasicProductBuEntity> implements BasicProductBuService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private ProductRefBuService productRefBuService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(BasicProductBuDTO.AddDTO addDTO) {
        BasicProductBuEntity basicProductBuEntity = new BasicProductBuEntity();
        BeanMapperUtils.copy(addDTO, basicProductBuEntity);
        //校验名称不能重复
        BasicProductBuEntity exist = this.lambdaQuery()
                .eq(BasicProductBuEntity::getName, basicProductBuEntity.getName())
                .one();
        if(ObjectUtil.isNotNull(exist)){
            throw new ServiceException(ApiError.COMMON_NAME_EXIST, exist.getName());
        }

        log.info("开始新增产品BU信息");
        boolean save = super.save(basicProductBuEntity);
        if(!save) {
            throw new ServiceException("产品BU信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "产品BU信息" , basicProductBuEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PRODUCT_BU.getCode(), basicProductBuEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(basicProductBuEntity.getId(), basicProductBuEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(BasicProductBuDTO.UpdateDTO addOrUpdateDTO) {
        BasicProductBuEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "产品BU信息"));
        BasicProductBuEntity basicProductBuEntity =  BeanMapperUtils.map(BasicProductBuEntity.class, addOrUpdateDTO);
        //校验名称不能重复
        BasicProductBuEntity exist = this.lambdaQuery()
                .eq(BasicProductBuEntity::getName, basicProductBuEntity.getName())
                .ne(BasicProductBuEntity::getId, basicProductBuEntity.getId())
                .one();
        if(ObjectUtil.isNotNull(exist)){
            throw new ServiceException(ApiError.COMMON_NAME_EXIST, exist.getName());
        }
        log.info("编辑 开始修改产品BU信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(basicProductBuEntity);
        if(!save) {
            throw new ServiceException("产品BU信息保存失败");
        }

        return Boolean.TRUE;
    }

    @Override
    public List<BasicProductBuDTO.DropDownDTO> dropDown() {
        List<BasicProductBuEntity> list = super.list();
        list = list.stream().sorted(Comparator.comparing(BasicProductBuEntity::getIndex)).collect(Collectors.toList());
        return BeanMapperUtils.copyList(BasicProductBuDTO.DropDownDTO.class, list);
    }

    @Override
    public void delete(String id) {
        //校验是否存在
        List<ProductRefBuEntity> productRefBuEntities = productRefBuService.listByBuId(id);
        if(CollUtil.isNotEmpty(productRefBuEntities)){
            throw new ServiceException(ApiError.PRODUCT_BU_IS_EXISTS_REF);
        }
        super.removeById(id);
        operateLogService.addModuleOperateLog(
                StrUtil.format("用户【{}】删除产品BU信息，id为【{}】", UserContext.getDefaultLoginUser().getUserName(), id),
                ModuleTypeEnum.PRODUCT_BU.getCode(),
                id,
                "删除操作"
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdate(List<BasicProductBuDTO.DropDownDTO> list) {
        //dto的name不能重复
        Set<String> nameSet = Sets.newHashSet();
        for(BasicProductBuDTO.DropDownDTO item : list){
            if(!nameSet.add(item.getName())){
                throw new ServiceException(ApiError.COMMON_DUPLICATION_NAME, item.getName());
            }
        }
        List<BasicProductBuEntity> existList = BeanMapperUtils.copyList(BasicProductBuEntity.class, list);
        this.saveOrUpdateBatch(existList);

    }

    @Override
    public BasicProductBuEntity getByName(String rdtTeamName) {
        if(StrUtil.isEmpty(rdtTeamName)) {
            return null;
        }
        // 去除前后空格
        String name = rdtTeamName.trim();
        return  this.lambdaQuery()
                .eq(BasicProductBuEntity::getName, name)
                .one();
    }

    @Override
    public List<BasicProductBuEntity> listByNames(List<String> buNames) {
        if(CollUtil.isNotEmpty(buNames)) {
            return this.lambdaQuery().in(BasicProductBuEntity::getName, buNames).list();
        }
        return Collections.emptyList();
    }

}
