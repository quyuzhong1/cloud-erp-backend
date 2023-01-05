package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FileUtil;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.dto.base.UpdateStateDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.CategoryModuleDTO;
import com.erp.model.bi.dto.ModuleDTO;
import com.erp.model.bi.dto.ModulePagingDTO;
import com.erp.model.bi.entity.BiModuleEntity;
import com.erp.server.bi.constant.IsDeleted;
import com.erp.server.bi.enums.DictEnum;
import com.erp.server.bi.mapper.BiModuleMapper;
import com.erp.server.bi.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 模块表(BiModule)表服务实现类
 *
 * @author yl
 * @since 2022-12-08 14:31:14
 */
@Service
public class BiModuleServiceImpl extends ServiceImpl<BiModuleMapper, BiModuleEntity> implements BiModuleService {


    @Resource
    private BiModulePermissionService modulePermissionService;

    @Resource
    private BiSysModuleService sysModuleService;

    @Resource
    private CommonService commonService;

    @Resource
    private BiDictService dictService;


    /**
     * 模块分页
     *
     * @param dto
     * @return com.erp.common.vo.PagingVO<com.erp.model.bi.dto.ModulePagingDTO>
     * @author yl
     * @date 2022-12-12 11:37
     */
    @Override
    public PagingVO<ModulePagingDTO> paging(PagingDTO<BaseSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BaseSearchDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params);
        return new PagingVO(pageData);
    }


    /**
     * 修改模板状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-12-12 11:59
     */
    @Override
    public Boolean updateState(UpdateStateDTO dto) {
        BiModuleEntity entity = this.getById(dto.getId());
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_97004);
        }
        Boolean stateFlag = dto.getState();
        if (stateFlag) {
            entity.setState(IsDeleted.YES);
        } else {
            entity.setState(IsDeleted.NO);
        }
        return this.updateById(entity);
    }


    /**
     * 这个是获取到添加专题的时候分类用到的
     *
     * @param searchKeyword
     * @return java.util.List<com.erp.model.bi.dto.CategoryModuleDTO>
     * @author yl
     * @date 2022-12-12 18:43
     */
    @Override
    public List<CategoryModuleDTO> categoryList(String searchKeyword) {
        List<CategoryModuleDTO> resultList = new ArrayList<>(10);
        String userId = commonService.getUserInfo().getUid();
        /**
         * 根据用户id 查询到可见的模块id 集合
         */
        List<String> moduleIdList = baseMapper.getUserVisibleModuleIds(userId);
        List<Pair<String, String>> pairList = dictService.getCategory(DictEnum.MODULE.getType());
        List<ModuleDTO> moduleList = baseMapper.getByIds(moduleIdList, searchKeyword);
        for (Pair<String, String> pair : pairList) {
            CategoryModuleDTO result = new CategoryModuleDTO();
            String categoryId = pair.getKey();
            result.setCategoryId(categoryId);
            result.setCategoryName(pair.getValue());
            List<ModuleDTO> categoryModuleList = moduleList.stream().filter(m -> categoryId.equals(m.getCategoryId())).collect(Collectors.toList());
            result.setModuleList(categoryModuleList);
            resultList.add(result);
        }
        return resultList;
    }

    @Override
    public ModuleDTO details(String moduleId) {
        BiModuleEntity module = this.getById(moduleId);
        if (Objects.isNull(module)) {
            throw new ServiceException(ApiError.ERROR_97004);
        }
        ModuleDTO result = new ModuleDTO();
        BeanMapper.copy(module, result);
        List<String> permissionUserIdList = modulePermissionService.getByModuleId(moduleId);
        result.setPermissionUserIdList(permissionUserIdList);
        return result;
    }


    /**
     * 根据id 获取到模块
     *
     * @param moduleIdList
     * @return
     */
    @Override
    public List<BiModuleEntity> getByIds(List<String> moduleIdList) {
        if (CollectionUtils.isNotEmpty(moduleIdList)) {
            LambdaQueryWrapper<BiModuleEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(BiModuleEntity::getId, moduleIdList);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }

    /**
     * 新增数据
     *
     * @param biModule 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional
    public Boolean insert(ModuleDTO biModule) {
        BiModuleEntity module = new BiModuleEntity();
        String name = biModule.getName();
        checkName(null, name);
        String sysModuleId = biModule.getSysModuleId();
        Object imageObject = biModule.getImageFile();
        String fileUrl = "";
        if (imageObject != null && !imageObject.equals("null")) {
            MultipartFile imageFile = (MultipartFile) imageObject;
            File file = FileUtil.multiToFile(imageFile);
            String fileName = imageFile.getOriginalFilename().toLowerCase();
            fileUrl = FastDFSClientUtil.uploadFile(file, fileName);
            if (StringUtils.isBlank(fileUrl)) {
                throw new ServiceException(ApiError.ERROR_95018);
            }
        }

        module.setImageUrl(fileUrl);
        module.setName(name);
        module.setRemark(biModule.getRemark());
        module.setViewCode(biModule.getViewCode());
        module.setCategoryId(biModule.getCategoryId());
        module.setSysModuleId(sysModuleId);
        module.setCode(biModule.getCode());
        checkCode(null, biModule.getCode());
        List<String> permissionUserIdList = biModule.getPermissionUserIdList();
        boolean flag = this.save(module);
        if (flag) {
            //修改系统模块的状态
            sysModuleService.updateAddState(sysModuleId, IsDeleted.YES);
            if (CollectionUtils.isNotEmpty(permissionUserIdList)) {
                modulePermissionService.addModulePermission(module.getId(), permissionUserIdList);
            }
        }
        return flag;
    }


    /**
     * 检查模块名 是否重复
     * 只检查二级分类的
     *
     * @param id 表id  pid pid name 名字
     * @return void
     * @author yl
     * @date 2022-12-12 10:34
     */
    public void checkName(String id, String name) {
        LambdaQueryWrapper<BiModuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiModuleEntity::getName, name);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(BiModuleEntity::getId, id);
        }
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_97003);
        }

    }


    public void checkCode(String id, String code) {
        LambdaQueryWrapper<BiModuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiModuleEntity::getCode, code);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(BiModuleEntity::getId, id);
        }
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_97009);
        }

    }

    /**
     * 修改数据
     *
     * @param biModule 实例对象
     * @return 实例对象
     */
    @Override
    public Boolean update(ModuleDTO biModule) {
        BiModuleEntity module = this.getById(biModule.getId());
        if (Objects.isNull(module)) {
            throw new ServiceException(ApiError.ERROR_97004);
        }
        String name = biModule.getName();
        checkName(biModule.getId(), name);
        Boolean uploadFlag = biModule.getUploadFlag();
        Object imageObject = biModule.getImageFile();
        //当上传了文件 且文件不为空的时候
        if (imageObject != null && !imageObject.equals("null") && uploadFlag) {
            MultipartFile imageFile = (MultipartFile) imageObject;
            File file = FileUtil.multiToFile(imageFile);
            String fileName = imageFile.getOriginalFilename().toLowerCase();
            String fileUrl = FastDFSClientUtil.uploadFile(file, fileName);
            if (StringUtils.isBlank(fileUrl)) {
                throw new ServiceException(ApiError.ERROR_95018);
            }
            module.setImageUrl(fileUrl);
        }
        module.setName(name);
        module.setRemark(biModule.getRemark());
        module.setViewCode(biModule.getViewCode());
        String sysModuleId = biModule.getSysModuleId();
        String dbSysModuleId = module.getSysModuleId();
        module.setCategoryId(biModule.getCategoryId());
        module.setSysModuleId(sysModuleId);
        module.setCode(biModule.getCode());

        List<String> permissionUserIdList = biModule.getPermissionUserIdList();
        boolean flag = this.updateById(module);
        if (flag) {
            /*
             *当两个传来的不一样 说明更改了系统的模块
             * 那么原来的
             */
            if (!sysModuleId.equals(dbSysModuleId)) {
                sysModuleService.updateAddState(sysModuleId, IsDeleted.YES);
                sysModuleService.updateAddState(dbSysModuleId, IsDeleted.NO);
            }
            if (CollectionUtils.isNotEmpty(permissionUserIdList)) {
                modulePermissionService.addModulePermission(module.getId(), permissionUserIdList);
            }
        }
        return flag;
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public Boolean deleteById(String id) {
        Boolean flag = this.removeById(id);
        if (flag) {
            modulePermissionService.deleteByModuleId(id);
        }
        return flag;
    }


}
