package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.DocsDTO;
import com.erp.model.plm.dto.DocsShowDTO;
import com.erp.model.plm.dto.StateDTO;
import com.erp.model.plm.entity.SysDocsEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.SysDocsMapper;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.SysDocsService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 系统产品文档 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Service
public class SysDocsServiceImpl extends ServiceImpl<SysDocsMapper, SysDocsEntity> implements SysDocsService {


    @Autowired
    private CommonService commonService;

    /**
     * 保存或者修改系统文档
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-09-15 9:51
     */
    @Override
    public void saveOrUpdateDocs(DocsDTO dto) {
        LoginUser loginUser = commonService.getUserInfo();
        String name = dto.getName();
        SysDocsEntity docsEntity = new SysDocsEntity();
        String id = dto.getId();
        String userName = loginUser.getUserName();
        String userId = loginUser.getUid();
        //获取到所有的文档名 如果存在id 就去掉这个name
        List<String> docsNames = getDocsNames(id);
        if (CollectionUtils.isNotEmpty(docsNames) && docsNames.contains(name)) {
            throw new ServiceException(ApiError.ERROR_95003);
        }
        if (StringUtils.isBlank(id)) {
            docsEntity.setCreateUser(userName);
            docsEntity.setCreateUserId(userId);
        } else {
            docsEntity.setUpdateUser(userName);
            docsEntity.setUpdateUserId(userId);
        }
        docsEntity.setId(id);
        docsEntity.setName(name);
        this.saveOrUpdate(docsEntity);
    }


    /**
     * 获取文档名
     *
     * @param id
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-09-15 11:37
     */
    private List<String> getDocsNames(String id) {
        LambdaQueryWrapper<SysDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SysDocsEntity::getName);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(SysDocsEntity::getId, id);
        }
        return this.listObjs(queryWrapper, Object::toString);
    }


    /**
     * 修改状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-15 10:22
     */
    @Override
    public Boolean updateState(StateDTO dto) {
        LoginUser loginUser = commonService.getUserInfo();
        LambdaUpdateWrapper<SysDocsEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SysDocsEntity::getStartState, dto.getState());
        updateWrapper.set(SysDocsEntity::getUpdateUser, loginUser.getUserName());
        updateWrapper.set(SysDocsEntity::getUpdateUserId, loginUser.getUid());
        updateWrapper.eq(SysDocsEntity::getId, dto.getId());
        return this.update(updateWrapper);
    }


    /**
     * 分页展示
     *
     * @param dto
     * @return com.erp.common.vo.PagingVO
     * @author yl
     * @date 2022-09-15 10:43
     */
    @Override
    public PagingVO<DocsShowDTO> paging(PagingDTO<BaseSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BaseSearchDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params, IsConstant.YES);
        return new PagingVO(pageData);
    }


    @Override
    public List<DocsDTO> getDocsNames(Integer state) {
        LambdaQueryWrapper<SysDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SysDocsEntity::getId, SysDocsEntity::getName);
        queryWrapper.eq(SysDocsEntity::getStartState, IsConstant.YES);
        List<SysDocsEntity> list = this.list(queryWrapper);
        List<DocsDTO> resultList = new ArrayList<>();
        for (SysDocsEntity item : list) {
            DocsDTO d = new DocsDTO();
            d.setIsSys(IsConstant.YES);
            d.setId(item.getId());
            d.setName(item.getName());
            d.setState(item.getStartState());
            resultList.add(d);
        }
        return resultList;


    }

    /**
     * 获取系统文档名
     *
     * @param
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @author yl
     * @date 2022-10-08 16:05
     */
    @Override
    public List<Map<String, Object>> sysDocsNames() {
        LambdaQueryWrapper<SysDocsEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.select(SysDocsEntity::getName, SysDocsEntity::getId, SysDocsEntity::getStartState);
        queryWrapper.eq(SysDocsEntity::getStartState, IsConstant.YES);
        return this.listMaps(queryWrapper);
    }

    @Override
    public List<String> getSysDocsName() {
        LambdaQueryWrapper<SysDocsEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.select(SysDocsEntity::getName);
        queryWrapper.eq(SysDocsEntity::getStartState, IsConstant.YES);
        return this.listObjs(queryWrapper, Object::toString);
    }


}
