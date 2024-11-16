package com.erp.server.bi.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.constant.BaseStateConstants;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FileUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.bi.dto.CategoryModuleDTO;
import com.erp.model.bi.dto.ModuleDTO;
import com.erp.model.bi.dto.ModulePagingDTO;
import com.erp.model.bi.entity.BiDictEntity;
import com.erp.model.bi.entity.BiLayoutRefModuleEntity;
import com.erp.model.bi.entity.BiModuleEntity;
import com.erp.model.bi.entity.BiModulePermissionEntity;
import com.erp.model.bi.enums.BiShareIdentityTypeEnum;
import com.erp.model.bi.vo.LayoutVO;
import com.erp.rpc.sys.feign.SysUserFeign;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
    private BiDictService dictService;


    @Resource
    private BiSubjectRefLayoutService subjectRefLayoutService;

    @Resource
    private BiLayoutRefModuleService layoutRefModuleService;

    @Resource
    private SysUserFeign sysUserFeign;

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
        Page<Object> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        BaseSearchDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        IPage<ModulePagingDTO> pageData = baseMapper.paging(query, params);
        List<ModulePagingDTO> list = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(list)) {
            List<LayoutVO> layoutList = subjectRefLayoutService.getLayoutIds();
            List<String> layoutIdList = layoutList.stream().map(LayoutVO::getLayoutId).collect(Collectors.toList());
            List<BiLayoutRefModuleEntity> layoutRefModuleList = layoutRefModuleService.getByLayoutIds(layoutIdList);
            LocalDate localDate = LocalDate.now();
            LocalDateTime monthStart = LocalDateUtil.getThisMonthStart(localDate);
            LocalDateTime monthEnd = LocalDateUtil.getThisMonthEnd(localDate);
            List<String> monthLayoutIdList = layoutList.stream().filter(
                    l -> l.getSubjectCreateTime().compareTo(monthStart) >= 0 &&
                            l.getSubjectCreateTime().compareTo(monthEnd) <= 0
            ).map(LayoutVO::getLayoutId).collect(Collectors.toList());
            List<BiDictEntity> biDictEntities = dictService.listEntityByType(DictEnum.MODULE.getType());
            Map<String, String> dictMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(biDictEntities)){
                dictMap = biDictEntities.stream().collect(Collectors.toMap(BiDictEntity::getId, BiDictEntity::getName));
            }
            for (ModulePagingDTO item : list) {
                String moduleId = item.getId();
                long monthUsageCount = layoutRefModuleList.stream().filter(m -> monthLayoutIdList.contains(m.getLayoutId()) && m.getModuleId().equals(moduleId)).count();
                long usageCount = layoutRefModuleList.stream().filter(m -> layoutIdList.contains(m.getLayoutId()) && m.getModuleId().equals(moduleId)).count();
                item.setMonthUsageCount((int) monthUsageCount);
                item.setUsageCount((int) usageCount);
                item.setCategoryName(dictMap.get(item.getCategoryId()));
            }
            List<String> moduleIds = list.stream().map(ModulePagingDTO::getId).collect(Collectors.toList());
            Map<String, List<BiModulePermissionEntity>> permissionMap = modulePermissionService.mapByModuleIds(moduleIds);
            // 权限设置
            list.forEach( e -> e.checkAndSetShareFlagInfo(permissionMap.get(e.getId())));

        }


        return new PagingVO<>(pageData);
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
            entity.setState(BaseStateConstants.OPEN_STATE);
        } else {
            entity.setState(BaseStateConstants.CLOSE_STATE);
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
        String userId = UserContext.getDefaultLoginUser().getUid();
        List<String> roleIdList = sysUserFeign.getRoleIdList(userId);
        //根据用户id 查询到可见的模块id 集合
        List<String> moduleIdList = modulePermissionService.findModuleId(userId, roleIdList);
        List<Pair<String, String>> pairList = dictService.getCategory(DictEnum.MODULE.getType());
        List<ModuleDTO> moduleList = baseMapper.getByIds(moduleIdList, searchKeyword);
        // 模板IDS
        List<String> moduleIds = moduleList.stream().map(ModuleDTO::getId).distinct().collect(Collectors.toList());
        Map<String, List<BiModulePermissionEntity>> permissionMap = modulePermissionService.mapByModuleIds(moduleIds);
        // 设置权限信息
        moduleList.forEach(e -> e.checkAndSetFlagInfo(permissionMap.get(e.getId())));

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
        List<BiModulePermissionEntity> permissionEntityList = modulePermissionService.findByModuleId(moduleId);
        //  personal 私人 share 按多用户ID共享 role 按多角色ID
        String shareFlag = "personal";
        List<String> shareFlagIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(permissionEntityList)){
            shareFlag = BiShareIdentityTypeEnum.getShareFlag(permissionEntityList.get(0).getIdentityType());
            shareFlagIdList = permissionEntityList
                    .stream()
                    .map(BiModulePermissionEntity::getIdentityId)
                    .distinct()
                    .collect(Collectors.toList());
        }
        result.setShareFlagIdList(shareFlagIdList);
        result.setShareFlag(shareFlag);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateShare(List<String> shareFlagIdList, String mainId, String shareFlag) {
        BiModuleEntity entity = this.getById(mainId);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_97004);
        }
        modulePermissionService.checkAndAddModulePermission(shareFlagIdList, mainId, shareFlag);

        return BatchResultDTO.success(entity.getId(), "", OperationTypeEnum.PERMISSION);
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
        String moduleName = biModule.getName();
        if (moduleName.length() > 30) {
            throw new ServiceException(ApiError.ERROR_97026);
        }
        String remark = biModule.getRemark();
        if (remark.length() > 200) {
            throw new ServiceException(ApiError.ERROR_97027);
        }
        String fileUrl = "";
        if (imageObject != null && !"null".equals(imageObject)) {
            MultipartFile imageFile = (MultipartFile) imageObject;
            File file = FileUtil.multiToFile(imageFile);

            // 检查文件名是否为空或为null
            String originalFilename = imageFile.getOriginalFilename();
            if (CharSequenceUtil.isBlank(originalFilename)) {
                throw new ServiceException(ApiError.ERROR_95018);
            }

            String fileName = originalFilename.toLowerCase();
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
        List<String> permissionUserIdList = biModule.checkAndGetShareFlagIdList();
        boolean flag = this.save(module);
        if (flag) {
            //修改系统模块的状态
            sysModuleService.updateAddState(sysModuleId, BaseStateConstants.OPEN_STATE);
            if (CollectionUtils.isNotEmpty(permissionUserIdList)) {
                BiShareIdentityTypeEnum identityTypeEnum = BiShareIdentityTypeEnum.isRoleCheck(biModule.getShareFlag());
                modulePermissionService.addModulePermission(module.getId(), permissionUserIdList, identityTypeEnum);
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
        queryWrapper.eq(BiModuleEntity::getIsDeleted, false);
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
        queryWrapper.eq(BiModuleEntity::getIsDeleted, false);
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
        if (name.length() > 30) {
            throw new ServiceException(ApiError.ERROR_97026);
        }
        String remark = biModule.getRemark();
        if (remark.length() > 200) {
            throw new ServiceException(ApiError.ERROR_97027);
        }

        checkName(biModule.getId(), name);
        Boolean uploadFlag = biModule.getUploadFlag();
        Object imageObject = biModule.getImageFile();
        // 当上传了文件 且文件不为空的时候
        if (imageObject != null && !"null".equals(imageObject) && uploadFlag) {
            MultipartFile imageFile = (MultipartFile) imageObject;
            File file = FileUtil.multiToFile(imageFile);

            // 检查文件名是否为null
            String originalFilename = imageFile.getOriginalFilename();
            if (originalFilename == null) {
                throw new ServiceException(ApiError.ERROR_95018);
            }

            String fileName = originalFilename.toLowerCase();
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

        List<String> permissionUserIdList = biModule.getShareFlagIdList();
        boolean flag = this.updateById(module);
        if (flag) {
            /*
             *当两个传来的不一样 说明更改了系统的模块
             * 那么原来的
             */
            if (!sysModuleId.equals(dbSysModuleId)) {
                sysModuleService.updateAddState(sysModuleId, BaseStateConstants.OPEN_STATE);
                sysModuleService.updateAddState(dbSysModuleId, BaseStateConstants.CLOSE_STATE);
            }
            if (CollectionUtils.isNotEmpty(permissionUserIdList)) {
                BiShareIdentityTypeEnum identityTypeEnum = BiShareIdentityTypeEnum.isRoleCheck(biModule.getShareFlag());
                modulePermissionService.addModulePermission(module.getId(), permissionUserIdList, identityTypeEnum);
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
        //校验是否已使用
        List<BiLayoutRefModuleEntity> refModuleServiceByModuleIds = layoutRefModuleService.getByModuleIds(Collections.singletonList(id));
        if (CollectionUtils.isNotEmpty(refModuleServiceByModuleIds)){
            throw new ServiceException(ApiError.ERROR_97044);
        }
        Boolean flag = this.removeById(id);
        if (flag) {
            modulePermissionService.deleteByModuleId(id);
        }
        return flag;
    }


}
