package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BatchStateDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.sys.dto.CompanyPagingSearchDTO;
import com.erp.model.sys.dto.SysAccountingCompanyDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.server.sys.mapper.SysAccountingCompanyMapper;
import com.erp.server.sys.service.SysAccountingCompanyService;

import cn.hutool.core.collection.CollUtil;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Administrator
 * @Classname SysAccountingCompanyImpl
 * @Date 2022-07-12 9:52
 * @Created by yl
 */
@Service
public class SysAccountingCompanyImpl extends ServiceImpl<SysAccountingCompanyMapper, SysAccountingCompanyEntity> implements SysAccountingCompanyService {

    /**
     * 保存公司信息
     *
     * @param dto
     * @return boolean
     * @author yl
     * @date 2022-07-12 10:12
     */

    @Override
    public boolean saveCompany(SysAccountingCompanyDTO dto) {
        SysAccountingCompanyEntity entity = new SysAccountingCompanyEntity();
        checkName("", dto.getCompanyName());
        BeanMapperUtils.copy(dto, entity);
        entity.setCode(entity.getKingdeeCode());
        entity.setOrgFunctions(dto.getOrgFunctionList().stream().collect(Collectors.joining(",")));
        return this.save(entity);
    }

    /**
     * 检查名称
     *
     * @param id
     * @param name
     * @return void
     * @author yl
     * @date 2023-06-13 17:12
     */
    private void checkName(String id, String name) {
        LambdaQueryWrapper<SysAccountingCompanyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysAccountingCompanyEntity::getCompanyName, name);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(SysAccountingCompanyEntity::getId, id);
        }
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_DUPLICATION_NAME);
        }
    }

    /**
     * 修改公司信息
     *
     * @param dto
     * @return boolean
     * @author yl
     * @date 2022-07-12 10:15
     */

    @Override
    public boolean updateCompany(SysAccountingCompanyDTO dto) {
        SysAccountingCompanyEntity entity = this.getById(dto.getId());
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }
        checkName(dto.getId(), dto.getCompanyName());
        entity.setCompanyAddress(dto.getCompanyAddress());
        entity.setCompanyName(dto.getCompanyName());
        entity.setContactAddress(dto.getContactAddress());
        entity.setContactMobile(dto.getContactMobile());
        entity.setCurrency(dto.getCurrency());
        entity.setContactName(dto.getContactName());
        entity.setKingdeeCode(dto.getKingdeeCode());
        entity.setOrgFunctions(dto.getOrgFunctionList().stream().collect(Collectors.joining(",")));
        return this.updateById(entity);
    }

    /**
     * 更改状态
     *
     * @param dto
     * @return boolean
     * @author yl
     * @date 2022-07-12 10:30
     */

    @Override
    public boolean updateCompanyState(UpdateStateDTO dto) {
        SysAccountingCompanyEntity entity = this.getById(dto.getId());
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }
        entity.setDisabled(dto.getState());
        return this.updateById(entity);
    }

    /**
     * 分页获取公司数据
     *
     * @param dto
     * @return com.cloud.erp.common.common.vo.PagingVO<com.cloud.erp.admin.modules.sys.entity.SysAccountingCompanyEntity>
     * @author yl
     * @date 2022-07-12 11:34
     */

    @Override
    public PagingVO paging(PagingDTO<CompanyPagingSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        CompanyPagingSearchDTO params = dto.getParams();
        IPage<SysAccountingCompanyEntity> pageData = baseMapper.paging(query, params);
        List<SysAccountingCompanyEntity> records = pageData.getRecords();
        if(CollUtil.isNotEmpty(records)) {
        	records.forEach(r -> {
        		String orgFunctions = r.getOrgFunctions();
        		if(StringUtils.isNotBlank(orgFunctions)) {
        			r.setOrgFunctionList(Stream.of(orgFunctions.split(",")).collect(Collectors.toList()));
        		}
        	});
        }
        return new PagingVO(pageData);
    }


    /**
     * 批量修改 核算公司状态
     *
     * @param dto
     * @return boolean
     * @author yl
     * @date 2022-07-29 10:10
     */
    @Override
    public boolean batchUpdateCompanyState(BatchStateDTO dto) {
        LambdaUpdateWrapper<SysAccountingCompanyEntity> updateWrapper = new LambdaUpdateWrapper<>();
        List<String> ids = dto.getIds();
        if (CollectionUtils.isNotEmpty(ids)) {
            updateWrapper.in(SysAccountingCompanyEntity::getId, dto.getIds());
            updateWrapper.set(SysAccountingCompanyEntity::getDisabled, dto.getState());
            return this.update(updateWrapper);
        }
        return false;

    }


    /**
     * 获取核算组织
     *
     * @param
     * @return java.util.List<com.erp.model.sys.dto.SysAccountingCompanyDTO.ListDTO>
     * @author yl
     * @date 2023-03-21 17:44
     */
    @Override
    public List<SysAccountingCompanyDTO.ListDTO> getList() {
        List<SysAccountingCompanyEntity> list = this.lambdaQuery().eq(SysAccountingCompanyEntity::getDisabled, false).list();
        // 按创建时间顺序排，最早的排在最前面
        list = list.stream().sorted(Comparator.comparing(SysAccountingCompanyEntity::getCreateTime)).collect(Collectors.toList());
        return BeanMapper.copyList(list, SysAccountingCompanyDTO.ListDTO.class);
    }


    /**
     * 根据ids 获取组织列表
     *
     * @param ids
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO>
     * @author yl
     * @date 2023-03-22 15:29
     */
    @Override
    public List<BaseIdDTO.CodeDTO> getByIds(List<String> ids) {
        List<BaseIdDTO.CodeDTO> resultList = new ArrayList<>(20);
        if (CollectionUtils.isEmpty(ids)) {
            List<SysAccountingCompanyEntity> allList = this.list();
            for (SysAccountingCompanyEntity item : allList) {
                BaseIdDTO.CodeDTO dto = new BaseIdDTO.CodeDTO();
                dto.setId(item.getId());
                dto.setName(item.getCompanyName());
                dto.setCode(item.getCode());
                dto.setFlagId(item.getKingdeeId());
                resultList.add(dto);
            }
            return resultList;
        }
        List<SysAccountingCompanyEntity> list = this.listByIds(ids);
        for (SysAccountingCompanyEntity item : list) {
            BaseIdDTO.CodeDTO dto = new BaseIdDTO.CodeDTO();
            dto.setId(item.getId());
            dto.setName(item.getCompanyName());
            dto.setCode(item.getCode());
            dto.setFlagId(item.getKingdeeId());
            resultList.add(dto);
        }
        return resultList;
    }

    @Override
    @Cacheable(cacheNames = "cache:sys:listAccountingCompany",keyGenerator = "myKeyGenerator")
    public List<BaseIdDTO> listAccountingCompany() {
        List<SysAccountingCompanyEntity> list = lambdaQuery().eq(SysAccountingCompanyEntity::getDisabled, Boolean.FALSE).list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<BaseIdDTO> resultList = new ArrayList<>(list.size());
        for (SysAccountingCompanyEntity item : list) {
            BaseIdDTO dto = new BaseIdDTO();
            dto.setId(item.getId());
            dto.setName(item.getCompanyName());
            resultList.add(dto);
        }
        return resultList;
    }

    @Override
    public List<BaseIdDTO.CodeDTO> listByCodes(List<String> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return Collections.EMPTY_LIST;
        }
        List<SysAccountingCompanyEntity> list = this.lambdaQuery().in(SysAccountingCompanyEntity::getCode, codes).list();
        return BeanMapperUtils.copyList(BaseIdDTO.CodeDTO.class, list);
    }

    @Override
    public SysAccountingCompanyEntity view(String id) {
        SysAccountingCompanyEntity entity = this.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException("公司信息" + id));
        return entity;
    }


}
