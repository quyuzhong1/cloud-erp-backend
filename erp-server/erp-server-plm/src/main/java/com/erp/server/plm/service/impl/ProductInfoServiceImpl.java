package com.erp.server.plm.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.date.DateUtil;
import com.common.web.service.RedisService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.entity.ProjectInfoEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.enums.ApprovalStatusEnum;
import com.erp.server.plm.enums.ProjectStateEnum;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    private BasicCategoryService basicCategoryService;

    @Autowired
    private ProjectInfoService projectInfoService;

    @Autowired(required = false)
    private HttpServletResponse response;

    @Autowired
    private RedisService redisService;

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

    /**
     *@description
     *@parms
     *@return
     *@author yl
     *@date 2022-10-09
     */
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

            //保存模板任务 同时保存了对应交付文档
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
        //updateWrapper.set(ProductInfoEntity::getProjectStatus, state);
        this.update(updateWrapper);
    }

    /**
     * @param :产品基础信息请求参数
     * @return java.lang.Boolean
     * @Description 无规格sku修改产品信息
     * @Author Luo_WG
     * @Date 2022/9/21 18:44
     **/
    @Override
    public List<Map<String, Object>> getListObjs() {
        LambdaQueryWrapper<ProductInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(ProductInfoEntity::getId, ProductInfoEntity::getName);
        return this.listMaps(queryWrapper);
    }


    /**
     * 获取产品信息
     *
     * @param id
     * @return com.erp.model.plm.dto.ProductDTO
     * @author yl
     * @date 2022-09-28 10:21
     */
    @Override
    public ProductDTO info(String id) {
        ProductInfoEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_95010);
        }
        ProductDTO result = new ProductDTO();
        BeanMapper.copy(entity, result);
        String categoryId = result.getCategoryId();
        List<String> categoryIdList = basicCategoryService.getPidList(categoryId);
        result.setCategoryIdList(categoryIdList);
        return result;
    }

    /**
     * @param :产品基础信息请求参数
     * @return java.lang.Boolean
     * @Description 无规格sku修改产品信息
     * @Author Luo_WG
     * @Date 2022/9/21 18:44
     **/
    @Override
    public String updateSpec(ProductInfoDTO dto) {
        ProductInfoEntity productInfoEntity = new ProductInfoEntity();
        BeanMapper.copy(dto, productInfoEntity);
        this.saveOrUpdate(productInfoEntity);
        return productInfoEntity.getId();
    }
    /**
     * 产品列表编辑数据
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-09-28 17:27
     */
    @Override
    public void updateProduct(UpdateProductDTO dto) {
        ProductInfoEntity product = this.getById(dto.getProductId());
        if (!Objects.isNull(product)) {
            String grade = dto.getGrade();
            String productChargeId = dto.getProductChargeId();
            String productChargeName = dto.getProductChargeId();
            Integer approvalStatus = dto.getApprovalStatus();
            if (StringUtils.isNotBlank(grade)) {
                product.setGrade(grade);
            }
            if (StringUtils.isNotBlank(productChargeId)) {
                product.setChargeId(productChargeId);
            }
            if (StringUtils.isNotBlank(productChargeName)) {
                product.setChargeName(productChargeName);
            }
            if (approvalStatus != null) {
                product.setApprovalStatus(approvalStatus);
            }

            this.updateById(product);
        }

        //项目信息
        if (StringUtils.isNotBlank(dto.getProjectId())) {
            ProjectInfoEntity project = projectInfoService.getById(dto.getProjectId());
            if (!Objects.isNull(project)) {
                Integer projectStatus = dto.getProjectStatus();
                String projectChargeId = dto.getProjectChargeId();
                String projectChargeName = dto.getProjectChargeName();
                if (StringUtils.isNotBlank(projectChargeId)) {
                    project.setChargeId(projectChargeId);
                }
                if (StringUtils.isNotBlank(projectChargeName)) {
                    project.setChargeName(projectChargeName);
                }
                if (projectStatus != null) {
                    project.setProjectStatus(projectStatus);
                }
                projectInfoService.updateById(project);
            }
        }

    }


    /**
     * 导出数据
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-09-28 18:18
     */
    @Override
    public void exportProductData(ExportProductDataDTO dto) {
        //产品id集合
        List<String> productIds = dto.getProductIds();
        String exportData = dto.getExportData();
        String fileName = getFileName(exportData);
        //获取所有的
        if (exportData.equals("all")) {
            List<TaskExcelDTO> taskExcelList = projectTaskService.getExportTask(productIds);
            List<ProductExcelDTO> productList = getProductExcelList(productIds);
            exportExcel(fileName, taskExcelList, productList);

        }
        //获取任务
        if (exportData.equals("task")) {
            List<TaskExcelDTO> taskExcelList = projectTaskService.getExportTask(productIds);
            ExcelUtil.export(fileName, "任务列表", taskExcelList, TaskExcelDTO.class, response);
        }

        //获取产品
        if (exportData.equals("product")) {
            List<ProductExcelDTO> productList = getProductExcelList(productIds);
            ExcelUtil.export(fileName, "产品列表", productList, ProductExcelDTO.class, response);
        }
        
    }

    /**
     * 获取产品迭代数量
     * @author yl
     * @date 2022-10-09 12:16
     * @param
     * @return java.util.List<com.erp.model.plm.dto.CountDTO>
     */
    @Override
    public List<CountDTO> getProductRelevanceList() {
        return baseMapper.getProductRelevanceList();
    }


    /**
     * 获取文件名
     *
     * @param exportData
     * @return java.lang.String
     * @author yl
     * @date 2022-09-29 16:36
     */
    private String getFileName(String exportData) {
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        if ("all".equals(exportData)) {
            sb.append("产品管理");
        }
        if ("task".equals(exportData)) {
            sb.append("任务管理");
        }
        if ("product".equals(exportData)) {
            sb.append("产品管理");
        }
        sb.append(date);
        String redisKey="file:name:"+date;
        String last=redisService.getCacheObject(redisKey);
        String lastNo="1";
        if(StringUtils.isBlank(last)){
        }
        // redisService.setCacheObject(redisKey,lastNo,1, TimeUnit.DAYS);

        return sb.toString();


    }

    /**
     * 导出数据
     *
     * @param taskExcelList
     * @param productList
     * @return void
     * @author yl
     * @date 2022-09-29 14:55
     */
    private void exportExcel(String fileName, List<TaskExcelDTO> taskExcelList, List<ProductExcelDTO> productList) {
        response.setStatus(200);
        response.setCharacterEncoding("utf-8");

        OutputStream outputStream = null;
        ExcelWriter excelWriter = null;
        fileName = fileName.concat(".xlsx");
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            outputStream = response.getOutputStream();
            excelWriter = EasyExcel.write(outputStream)
                    .build();
            WriteSheet productSheet = EasyExcel.writerSheet("产品列表").head(ProductExcelDTO.class).build();
            excelWriter.write(productList, productSheet);
            WriteSheet taskSheet = EasyExcel.writerSheet("任务列表").head(TaskExcelDTO.class).build();
            excelWriter.write(taskExcelList, taskSheet);
        } catch (Exception e) {
            log.error(" exportExcel", e);
            e.printStackTrace();
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    log.error("导出数据关闭流异常", e);
                }
            }
        }

    }


    /**
     * 获取到产品导出的信息
     *
     * @param productIds
     * @return java.util.List<com.erp.model.plm.dto.ProductExcelDTO>
     * @author yl
     * @date 2022-09-29 12:27
     */
    public List<ProductExcelDTO> getProductExcelList(List<String> productIds) {
        List<ProductExcelDTO> productExcelList = baseMapper.getExportProduct(productIds);
        for (ProductExcelDTO item : productExcelList) {
            String productStatus = item.getProductStatus();
            Integer status = Integer.parseInt(productStatus);
            String productStatusNmae = ApprovalStatusEnum.getName(status);
            item.setProductStatus(productStatusNmae);
            //项目状态
            String projectStatus = item.getProjectStatus();

            Integer projectState = Integer.parseInt(projectStatus);
            String projectStateName = ProjectStateEnum.getName(projectState);
            item.setProjectStatus(projectStateName);
            TaskConductDTO conduct = projectTaskService.getTaskConduct(item.getProductId());
            item.setTaskCount(conduct.getTotalTaskCount());
            item.setTaskFinishCount(conduct.getFinishTaskCount());

        }
        return productExcelList;
    }


}
