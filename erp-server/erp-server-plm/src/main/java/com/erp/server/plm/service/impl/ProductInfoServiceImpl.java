package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.mapper.ProductInfoMapper;
import com.erp.server.plm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 产品信息表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Service
@Slf4j
public class ProductInfoServiceImpl extends ServiceImpl<ProductInfoMapper, ProductInfoEntity> implements ProductInfoService {


    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private ProjectTemplateService templateService;

    @Autowired
    private ProjectMembersService membersService;

    @Autowired
    private ProjectPhaseService phaseService;

    @Autowired
    private TemplateTaskService templateTaskService;

    /**
     * 查询 分类id 下有多少产品
     *
     * @param id
     * @return int
     * @author yl
     * @date 2022-09-16 15:45
     */
    @Override
    public int countByCategoryId(String id) {
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductInfoEntity::getCategoryId, id);
        return this.count(queryWrapper);
    }


    /**
     * 保存或许修改产品信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-16 17:06
     */
    @Override
    @Transactional
    public Boolean saveOrUpdateProduct(ProductDTO dto) {
        //检查名字是否重复
        checkName(dto.getName());
        ProductInfoEntity entity = new ProductInfoEntity();
        BeanMapper.copy(dto, entity);
        Boolean flag = this.saveOrUpdate(entity);
        //表示是新添加的 需要查询是否有系统任务 如果有就要添加对应任务
        if (flag && StringUtils.isBlank(dto.getId())) {
            projectTaskService.addSysTask(entity.getId());
        }
        return flag;
    }


    /**
     * 修改产品的分类
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-17 11:17
     */
    @Override
    public Boolean updateCategory(MoveCategoryDTO dto) {
        LambdaUpdateWrapper<ProductInfoEntity> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.set(ProductInfoEntity::getCategoryId, dto.getCategoryId());
        updateWrapper.in(ProductInfoEntity::getId, dto.getProductIds());
        return this.update(updateWrapper);
    }


    /**
     * 删除产品名称
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-17 11:35
     */
    @Override
    public Boolean removeProduct(RemoveProductDTO dto) {
        String productId = dto.getProductId();
        Boolean flag = false;
        ProductInfoEntity entity = this.getById(productId);
        if (!Objects.isNull(entity)) {
            if (!entity.getName().equals(dto.getProductName())) {
                throw new ServiceException(ApiError.ERROR_95009);
            }
            entity.setDeleteState(IsConstant.YES);
            flag = this.updateById(entity);
            //当保存成功 就要去删除对应的任务了
            if (flag) {
                projectTaskService.removeTaskByProductId(productId);
            }

        }
        return flag;

    }


    /**
     * 导出模板
     *
     * @param request
     * @param response
     * @return void
     * @author yl
     * @date 2022-09-17 14:14
     */
    @Override
    public void exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/project.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String("project.xlsx".getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error("exportTemplate ", e);
        }

    }

    @Override
    public PagingVO paging(PagingDTO<ProductSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        ProductSearchDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params);
        List<ProductShowDTO> list = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(list)) {
            //获取到所有出产品id
            List<String> productIds = list.stream().map(ProductShowDTO::getProductId).collect(Collectors.toList());
            List<ProjectTaskEntity> taskList = projectTaskService.getByProductIds(productIds);
            for (ProductShowDTO item : list) {
                //这是立项任务
                int approvalTaskCount = taskList.stream().filter(t -> TaskConstant.APPROVAL_TASK.equals(t.getProperty())).collect(Collectors.toList()).size();
                //这是项目任务
                int projectTaskCount = taskList.stream().filter(t -> TaskConstant.PROJECT_TASK.equals(t.getProperty())).collect(Collectors.toList()).size();
                //总的任务数
                int taskCount = approvalTaskCount + projectTaskCount;
                item.setTaskCount(taskCount);
                int approvalProgress = 0;
                int projectProgress = 0;
                if (taskCount != 0) {
                    approvalProgress = (approvalTaskCount / taskCount) * 100;
                    projectProgress = (projectTaskCount / taskCount) * 100;
                }
                item.setApprovalProgress(approvalProgress);
                item.setProjectProgress(projectProgress);
            }
        }
        return new PagingVO(pageData);
    }


    /**
     * 检查产品名 是否重复
     *
     * @param name
     * @return void
     * @author yl
     * @date 2022-09-16 17:16
     */
    private void checkName(String name) {
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductInfoEntity::getName, name);
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95007);
        }
    }

    /**
     * 保存产品模板
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-20 14:24
     */
    @Override
    public Boolean saveTemplate(SaveProductTemplateDTO dto) {
        //模板名
        String templateName = dto.getTemplateName();
        String productId = dto.getProductId();
        //保存模板
        String templateId = templateService.saveTemplate(templateName);
        if (StringUtils.isNotBlank(templateId)) {
            //保存团队成员
            membersService.saveMember(templateId, productId);

            //任务阶段
            phaseService.savePhase(templateId, productId);

            //保存模板任务
            templateTaskService.saveTemplateTask(templateId, productId);
        }

        return true;
    }


    /**
     * 更改产品的状态
     *
     * @param productId
     * @param state
     * @return void
     * @author yl
     * @date 2022-09-21 11:17
     */
    @Override
    public void updateProjectStatus(String productId, Integer state) {
        LambdaUpdateWrapper<ProductInfoEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductInfoEntity::getId, productId);
        updateWrapper.set(ProductInfoEntity::getProjectStatus, state);
        this.update(updateWrapper);
    }

}
