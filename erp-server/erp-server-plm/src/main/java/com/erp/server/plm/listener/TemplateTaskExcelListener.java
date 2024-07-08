package com.erp.server.plm.listener;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.FindUserDTO;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.DocsDTO;
import com.erp.model.plm.dto.TaskChargeDistributionDTO;
import com.erp.model.plm.dto.TemplateTaskDTO;
import com.erp.model.plm.dto.TmeplateDocsNameDTO;
import com.erp.model.plm.dto.excel.TemplateTaskExcelDTO;
import com.erp.model.plm.entity.TemplatePhaseEntity;
import com.erp.model.plm.entity.TemplateTaskEntity;
import com.erp.model.plm.enums.RelatedSkuTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.service.TemplatePhaseService;
import com.erp.server.plm.service.TemplateTaskDocsNameService;
import com.erp.server.plm.service.TemplateTaskService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TemplateTaskExcelListener extends AnalysisEventListener<TemplateTaskExcelDTO> {
    private String templateId;

    private SysUserFeign sysUserFeign;

    private TemplatePhaseService templatePhaseService;

    private TemplateTaskService templateTaskService;

    private TemplateTaskDocsNameService templateTaskDocsNameService;

    private List<TemplateTaskExcelDTO> list;

    private List<TemplateTaskExcelDTO> dataList = new ArrayList<>();

    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<TemplateTaskExcelDTO> allList = new ArrayList<>();

    /**
     * 导入错误数据
     */
    private List<TemplateTaskExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private List<TemplateTaskExcelDTO> successList = new ArrayList<>();

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");

    public TemplateTaskExcelListener(String templateId, SysUserFeign sysUserFeign, TemplatePhaseService templatePhaseService, TemplateTaskService templateTaskService, TemplateTaskDocsNameService templateTaskDocsNameService) {
        this.templateId = templateId;
        this.sysUserFeign = sysUserFeign;
        this.templateTaskService = templateTaskService;
        this.templateTaskDocsNameService = templateTaskDocsNameService;
        this.templatePhaseService = templatePhaseService;
        this.list = new ArrayList<>();
    }

    @Override
    public void invoke(TemplateTaskExcelDTO templateTaskExcelDTO, AnalysisContext analysisContext) {

        List<String> errorMsgList = new ArrayList<>();
        TemplateTaskDTO templateTaskDTO = new TemplateTaskDTO();

        //添加数据用于判断是否为空
        dataList.add(templateTaskExcelDTO);

        //注解基础校验
        List<String> msgList = FieldValidUtil.fieldValid(templateTaskExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
            templateTaskExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            list.add(templateTaskExcelDTO);
            return;
        }

        if (StringUtils.isNotBlank(templateTaskExcelDTO.getType())) {
            if (!templateTaskExcelDTO.getType().equals("一般任务") && !templateTaskExcelDTO.getType().equals("评审任务")) {
                errorMsgList.add("[任务类型]请输入'一般任务'或'评审任务'");
            }

            if (templateTaskExcelDTO.getType().equals("一般任务")){
                templateTaskDTO.setType(0);
            } else {
                templateTaskDTO.setType(1);
            }
        }
        List<TaskChargeDistributionDTO> TaskChargeDistributionlist = new ArrayList<>();

        TemplateTaskEntity templateTaskEntity = templateTaskService.getTaskByName(templateId, templateTaskExcelDTO.getName());

        if (ObjectUtil.isNotEmpty(templateTaskEntity)) {
            templateTaskDTO.setId(templateTaskEntity.getId());
//              errorMsgList.add("[任务名称]在模板中已存在，不可重复");
        }

        List<String> chargeNameList = new ArrayList<>();
        String chargeName = templateTaskExcelDTO.getChargeName();
        String[] chargeNames = chargeName.split(",");
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        for (String name : chargeNames) {
            FindUserDTO findUserDTO = userList.stream().filter(u -> name.equals(u.getUserName())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(findUserDTO)) {
                errorMsgList.add("[任务负责人]在系统中未找到，多个负责人请用英文逗号','隔开");
            } else {
                chargeNameList.add(findUserDTO.getUserId());
            }
        }

        TemplatePhaseEntity templatePhaseEntity = templatePhaseService.getProductPhaseByName(templateId, templateTaskExcelDTO.getPhaseName());
        if (ObjectUtil.isEmpty(templatePhaseEntity)) {
            errorMsgList.add("[阶段名称]在这个[所属产品]下不存在");
        }

        String preTask = templateTaskExcelDTO.getPreTask();
        List<String> preTaskList = new ArrayList<>();
        if (StringUtils.isNotBlank(preTask)) {
            List<TemplateTaskEntity> templateTaskEntities = templateTaskService.listByTemplateId(templateId);
            String[] split = preTask.split(",");
            for (String task : split) {
                TemplateTaskEntity taskEntity = templateTaskEntities.stream().filter(t -> t.getName().equals(task)).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(taskEntity)) {
                    errorMsgList.add("[前置任务]在这个[所属产品]下不存在，多个前置任务请用英文逗号','隔开");
                } else {
                    preTaskList.add(taskEntity.getId());
                }
            }
        }

        String priority = templateTaskExcelDTO.getPriority();
        if (StringUtils.isNotBlank(priority)) {
            if (!priority.equals("高") && !priority.equals("中") && !priority.equals("低")) {
                errorMsgList.add("[任务优先级]请输入'高'或'中''低'");
            }
        }
        if (StringUtils.isNotBlank(templateTaskExcelDTO.getIsFixed())) {
            if (!templateTaskExcelDTO.getIsFixed().equals("是") && !templateTaskExcelDTO.getIsFixed().equals("否")) {
                errorMsgList.add("[是否是固定任务]请输入'是'或'否'");
            }
        }
        if (StringUtils.isNotBlank(templateTaskExcelDTO.getRefSku())) {
            if (!templateTaskExcelDTO.getRefSku().equals("关联") && !templateTaskExcelDTO.getRefSku().equals("不关联")) {
                errorMsgList.add("[SKU关联]请输入'关联'或'不关联'");
            }
        }

        //目标交付文档
        String docsName = templateTaskExcelDTO.getDocsName();
        List<DocsDTO> docsNameList = new ArrayList<>();
        if (StringUtils.isNotBlank(docsName)) {
            List<DocsDTO> docsList = templateTaskDocsNameService.getDocsNameList(templateId);
            String[] split = docsName.split(",");
            for (String docs : split) {
                DocsDTO docsDTO = docsList.stream().filter(t -> t.getName().equals(docs)).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(docsDTO)) {
                    TmeplateDocsNameDTO docsNameDTO = new TmeplateDocsNameDTO();
                    docsNameDTO.setName(docs);
                    docsNameDTO.setTemplateId(templateId);
                    String id = templateTaskDocsNameService.saveDocs(docsNameDTO);
                    DocsDTO dto = new DocsDTO();
                    dto.setId(id);
                    dto.setName(docs);
                    dto.setState(false);
                    docsNameList.add(dto);
                } else {
                    docsNameList.add(docsDTO);
                }
            }
        }

        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            templateTaskExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            list.add(templateTaskExcelDTO);
            return;
        }
        templateTaskDTO.setTemplateId(templateId);
        templateTaskDTO.setName(templateTaskExcelDTO.getName());

        templateTaskDTO.setChargeIds(chargeNameList);
        templateTaskDTO.setPreTaskIdList(preTaskList);

        if (StringUtils.isNotBlank(templateTaskExcelDTO.getPlanStartTime())) {
            templateTaskDTO.setPlanStartTime(LocalDate.parse(templateTaskExcelDTO.getPlanStartTime(), dateTimeFormatter));
        }

        if (StringUtils.isNotBlank(templateTaskExcelDTO.getPlanEndTime())) {
            templateTaskDTO.setPlanEndTime(LocalDate.parse(templateTaskExcelDTO.getPlanEndTime(), dateTimeFormatter));
        }

        if (StringUtils.isNotBlank(templateTaskExcelDTO.getPriority())) {
            if (templateTaskExcelDTO.getPriority().equals("高")) {
                templateTaskDTO.setPriority(3);
            } else if (templateTaskExcelDTO.getPriority().equals("中")) {
                templateTaskDTO.setPriority(2);
            } else {
                templateTaskDTO.setPriority(1);
            }
        }
        templateTaskDTO.setPhaseId(templatePhaseEntity.getId());
        templateTaskDTO.setPhaseName(templatePhaseEntity.getName());
        templateTaskDTO.setDescription(templateTaskExcelDTO.getDescription());
        templateTaskDTO.setApprovalList(TaskChargeDistributionlist);
        templateTaskDTO.setDeliveryDocsList(docsNameList);
        templateTaskDTO.setDistributionType(1);
        if (StringUtils.isNotBlank(templateTaskExcelDTO.getRefSku())) {
            if (templateTaskExcelDTO.getRefSku().equals("关联")) {
                templateTaskDTO.setRelatedSkuType(RelatedSkuTypeEnum.ALL_RELATED.getCode());
            } else {
                templateTaskDTO.setRelatedSkuType(RelatedSkuTypeEnum.NOT_RELATED.getCode());
            }
        } else {
            if (ObjectUtil.isNotEmpty(templateTaskEntity)) {
                templateTaskDTO.setRelatedSkuType(templateTaskEntity.getRelatedSkuType());
            } else {
                templateTaskDTO.setRelatedSkuType(RelatedSkuTypeEnum.NOT_RELATED.getCode());
            }
        }
        templateTaskDTO.setWorkPeriod(templateTaskExcelDTO.getWorkPeriod());
        if (StringUtils.isNotBlank(templateTaskExcelDTO.getIsFixed())) {
            if (templateTaskExcelDTO.getIsFixed().equals("是")) {
                templateTaskDTO.setIsFixed(1);
            } else {
                templateTaskDTO.setIsFixed(0);
            }
        }
        templateTaskService.saveOrUpdate(templateTaskDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    public List<TemplateTaskExcelDTO> getAllList() {
        return allList;
    }

    public List<TemplateTaskExcelDTO> getErrorList(){
        return errorList;
    }

    public List<TemplateTaskExcelDTO> getSuccessList() {
        return successList;
    }
}
