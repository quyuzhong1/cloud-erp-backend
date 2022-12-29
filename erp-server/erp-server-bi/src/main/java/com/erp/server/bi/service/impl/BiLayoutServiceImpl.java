package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.bi.dto.*;
import com.erp.model.bi.entity.*;
import com.erp.server.bi.enums.DashboardEnum;
import com.erp.server.bi.enums.LayoutBlockEnum;
import com.erp.server.bi.mapper.BiLayoutMapper;
import com.erp.server.bi.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 布局表(BiLayout)表服务实现类
 *
 * @author yl
 * @since 2022-12-08 14:28:27
 */
@Service
public class BiLayoutServiceImpl extends ServiceImpl<BiLayoutMapper, BiLayoutEntity> implements BiLayoutService {


    @Resource
    private BiLayoutRefModuleService layoutRefModuleService;

    @Resource
    private BiSubjectRefLayoutService subjectRefLayoutService;

    @Resource
    private CommonService commonService;


    @Resource
    private BiSubjectService subjectService;

    @Resource
    private BiSubjectShareService subjectShareService;

    @Resource
    private BiDictService dictService;

    @Resource
    private BiModuleService moduleService;


    /**
     * 添加布局与专题
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-12-13 16:14
     */
    @Override
    @Transactional
    public Boolean addSubjectLayout(SubjectLayoutDTO dto) {
        String userId = commonService.getUserInfo().getUid();

        String subjectId = dto.getSubjectId();
        String name = dto.getName();
        //检查名字是否重复
        subjectService.checkName(subjectId, name);
        String categoryId = dto.getCategoryId();
        BiDictEntity dict = dictService.getById(categoryId);
        String categoryName = "";
        if (dict != null) {
            categoryName = dict.getName();
        }
        BiSubjectEntity subject = subjectService.getById(subjectId);
        if (Objects.isNull(subject)) {
            throw new ServiceException(ApiError.ERROR_97000);
        }
        //检查能否操作
        subjectService.checkCanHandle(subject, userId);
        subject.setName(name);
        subject.setCategoryId(categoryId);
        subject.setCategoryName(categoryName);
        Boolean result = subjectService.updateById(subject);
        String shareFlag = subject.getShareFlag();
        if (result) {
            //如果是分享
            if (DashboardEnum.SHARE.getFlag().equals(shareFlag)) {
                List<String> userList = dto.getShareUserIdList();
                //添加专题的分享用户
                subjectShareService.addSubjectShare(userList, subjectId);
            }
        }
        List<LayoutDTO> layoutList = dto.getLayoutList();
        List<String> LayoutIds = new ArrayList<>();
        for (LayoutDTO layout : layoutList) {
            BiLayoutEntity entity = new BiLayoutEntity();
            String layoutId = IdWorker.getIdStr();
            String blockNo = layout.getBlockNo();
            entity.setId(layoutId);
            entity.setBlockNo(blockNo);
            entity.setHeight(layout.getHeight());
            Integer columnCount = LayoutBlockEnum.getCount(blockNo);
            entity.setColumnCount(columnCount);
            Boolean flag = this.save(entity);
            if (flag) {
                LayoutIds.add(layoutId);
                //保存布局与 模块关系
                layoutRefModuleService.addLayoutRefModule(subjectId, layoutId, blockNo, layout.getModuleIdList());
            }
        }
        //保存专题与布局关系表
        subjectRefLayoutService.addSubjectRefLayout(subjectId, LayoutIds);
        return true;
    }

    /**
     * 根据专题id 获取专题详情信息
     *
     * @param subjectId
     * @return com.erp.model.bi.dto.SubjectLayoutDetailsDTO
     * @author yl
     * @date 2022-12-13 17:30
     */
    @Override
    public SubjectLayoutDetailsDTO subjectInfo(String subjectId) {
        String userId = commonService.getUserInfo().getUid();
        BiSubjectEntity subject = subjectService.getById(subjectId);
        if (Objects.isNull(subject)) {
            throw new ServiceException(ApiError.ERROR_97000);
        }
        subjectShareService.checkPermission(userId, subject);
        List<String> shareUserIdList = subjectShareService.getUserIdsBySubjectId(subjectId);
        SubjectLayoutDetailsDTO details = new SubjectLayoutDetailsDTO();
        details.setSubjectId(subjectId);
        details.setName(subject.getName());
        details.setCategoryId(subject.getCategoryId());
        details.setCategoryName(subject.getCategoryName());
        details.setShareUserIdList(shareUserIdList);
        List<LayoutDetailsDTO> layoutDetailsList = getBySubjectId(subjectId);
        details.setLayoutDetailsList(layoutDetailsList);
        return details;
    }


    /**
     * 修改专题布局
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-12-14 11:48
     */
    @Override
    @Transactional
    public Boolean updateSubjectLayout(SubjectLayoutDetailsDTO dto) {
        String userId = commonService.getUserInfo().getUid();
        String subjectId = dto.getSubjectId();
        String name = dto.getName();
        BiSubjectEntity subject = subjectService.getById(subjectId);
        if (Objects.isNull(subject)) {
            throw new ServiceException(ApiError.ERROR_97000);
        }
        //检查是否是自己创建的专题
        subjectService.checkCanHandle(subject, userId);

        boolean flag = true;

        //检查名字能否重复
        subjectService.checkName(subjectId, name);
        subject.setName(name);
        String categoryId = dto.getCategoryId();
        BiDictEntity dict = dictService.getById(categoryId);
        String categoryName = "";
        if (dict != null) {
            categoryName = dict.getName();
        }
        subject.setCategoryId(categoryId);
        subject.setCategoryName(categoryName);
        flag = subjectService.updateById(subject);
        List<String> userList = dto.getShareUserIdList();
        //添加专题的分享用户
        subjectShareService.addSubjectShare(userList, subjectId);
        //布局表
        List<LayoutDetailsDTO> layoutDetailsList = dto.getLayoutDetailsList();

        //删除布局主题关系
        subjectRefLayoutService.deleteBySubjectId(subjectId);
        //删除 模块与布局关系表
        layoutRefModuleService.deleteBySubjectId(subjectId);
        List<String> LayoutIds = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(layoutDetailsList)){
            for (LayoutDetailsDTO item : layoutDetailsList) {
                BiLayoutEntity entity = new BiLayoutEntity();
                String id = item.getId();
                if (StringUtils.isBlank(id)) {
                    id = IdWorker.getIdStr();
                }
                entity.setId(id);
                String blockNo = item.getBlockNo();
                entity.setBlockNo(blockNo);
                entity.setHeight(item.getHeight());
                Integer columnCount = LayoutBlockEnum.getCount(blockNo);
                entity.setColumnCount(columnCount);
                Boolean saveResult = this.saveOrUpdate(entity);
                if (saveResult) {
                    LayoutIds.add(id);
                    //保存布局与 模块关系
                    layoutRefModuleService.addLayoutRefModule(subjectId, id, blockNo, item.getModuleIdList());
                }
            }
            //保存专题与布局关系表
            subjectRefLayoutService.addSubjectRefLayout(subjectId, LayoutIds);
        }


        return flag;
    }


    /**
     * 删除布局模块
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-12-14 15:14
     */
    @Override
    public Boolean deleteLayoutModule(DeleteLayoutModuleDTO dto) {
        String moduleId = dto.getModuleId();
        //删除布局模块
        Boolean result = layoutRefModuleService.deleteLayoutModuleId(dto.getLayoutId(), moduleId);
        return result;
    }

    /**
     * 删除布局
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-12-14 15:14
     */
    @Override
    public Boolean deleteLayout(DeleteLayoutModuleDTO dto) {
        Boolean result = subjectRefLayoutService.delete(dto.getSubjectId(), dto.getLayoutId());
        this.removeById(dto.getLayoutId());
        //删除布局模块
        layoutRefModuleService.deleteLayout(dto.getLayoutId());
        return result;
    }


    /**
     * 复制专题 布局
     *
     * @param newSubjectId 新专题
     * @param subjectId
     * @return void
     * @author yl
     * @date 2022-12-15 10:00
     */
    @Override
    public void copySubjectLayout(String newSubjectId, String subjectId) {
        List<LayoutDetailsDTO> list = baseMapper.getLayoutBySubjectId(subjectId);
        List<String> layoutIdList = new ArrayList<>(list.size());
        for (LayoutDetailsDTO item : list) {
            BiLayoutEntity layout = new BiLayoutEntity();
            //需要复制的布局id
            String copyLayoutId = item.getId();
            String newLayoutId = IdWorker.getIdStr();
            layout.setId(newLayoutId);
            layout.setColumnCount(item.getColumnCount());
            layout.setHeight(item.getHeight());
            layout.setBlockNo(item.getBlockNo());
            Boolean flag = this.save(layout);
            //保存成功 就去看布局与模块关系
            if (flag) {
                layoutIdList.add(newLayoutId);
                //复制布局与模块 关系
                layoutRefModuleService.copyLayoutRefModule(newSubjectId, newLayoutId, copyLayoutId);
            }
        }
        //保存专题与布局关系表
        subjectRefLayoutService.addSubjectRefLayout(newSubjectId, layoutIdList);


    }


    /**
     * 添加整个专题
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-12-19 17:38
     */
    @Override
    public Boolean addSubject(AddTotalSubjectDTO dto) {
        SubjectDTO subject = new SubjectDTO();
        subject.setName(dto.getName());
        subject.setCategoryId(dto.getCategoryId());
        subject.setShareFlag(DashboardEnum.SHARE.getFlag());
        subject.setShareUserIdList(dto.getShareUserIdList());
        //专题id
        String subjectId = subjectService.addSubject(subject);
        if (StringUtils.isBlank(subjectId)) {
            return false;
        }

        List<LayoutDTO> layoutList = dto.getLayoutList();
        List<String> LayoutIds = new ArrayList<>();
        for (LayoutDTO layout : layoutList) {
            BiLayoutEntity entity = new BiLayoutEntity();
            String layoutId = IdWorker.getIdStr();
            String blockNo = layout.getBlockNo();
            entity.setId(layoutId);
            entity.setBlockNo(blockNo);
            entity.setHeight(layout.getHeight());
            Integer columnCount = LayoutBlockEnum.getCount(blockNo);
            entity.setColumnCount(columnCount);
            Boolean flag = this.save(entity);
            if (flag) {
                LayoutIds.add(layoutId);
                //保存布局与 模块关系
                layoutRefModuleService.addLayoutRefModule(subjectId, layoutId, blockNo, layout.getModuleIdList());
            }
        }
        //保存专题与布局关系表
        subjectRefLayoutService.addSubjectRefLayout(subjectId, LayoutIds);
        return true;
    }


    /**
     * 根据专题id 获取 专题与 布局的关系
     *
     * @param subjectId
     * @return java.util.List<com.erp.model.bi.dto.LayoutDetailsDTO>
     * @author yl
     * @date 2022-12-13 18:51
     */
    private List<LayoutDetailsDTO> getBySubjectId(String subjectId) {
        List<LayoutDetailsDTO> list = baseMapper.getLayoutBySubjectId(subjectId);
        //获取到布局id
        List<String> layoutIdList = list.stream().map(LayoutDetailsDTO::getId).collect(Collectors.toList());

        List<BiLayoutRefModuleEntity> layoutRefModuleList = layoutRefModuleService.getByLayoutIds(layoutIdList);
        List<String> moduleIdList = layoutRefModuleList.stream().map(BiLayoutRefModuleEntity::getModuleId).collect(Collectors.toList());
        List<BiModuleEntity> moduleList = moduleService.getByIds(moduleIdList);

        for (LayoutDetailsDTO item : list) {
            //布局id
            String layoutId = item.getId();
            List<BiLayoutRefModuleEntity> moduleIds = layoutRefModuleList.stream().
                    filter(l -> l.getLayoutId().equals(layoutId)).
                    collect(Collectors.toList());
            List<LayoutRefModuleDTO> layoutRefList = new ArrayList<>();
            for (BiLayoutRefModuleEntity ref : moduleIds) {
                String moduleId = ref.getModuleId();
                LayoutRefModuleDTO refModule = new LayoutRefModuleDTO();
                BiModuleEntity module = moduleList.stream().filter(m -> m.getId().equals(moduleId)).
                        findFirst().orElse(null);
                refModule.setId(moduleId);
                if (module != null) {
                    refModule.setSysModuleId(module.getSysModuleId());
                    refModule.setCode(module.getCode());
                    refModule.setName(module.getName());
                    refModule.setViewCode(module.getViewCode());
                    refModule.setImageUrl(module.getImageUrl());
                }
                layoutRefList.add(refModule);
            }
            item.setModuleIdList(layoutRefList);
        }
        return list;
    }
}
