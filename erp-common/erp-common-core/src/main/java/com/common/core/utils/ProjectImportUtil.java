package com.common.core.utils;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.dto.ProjectImportDTO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.LocalDateUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.mpxj.*;
import net.sf.mpxj.reader.UniversalProjectReader;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: project解析Util
 * @date 2023/6/1 15:25
 */
@Slf4j
public class ProjectImportUtil {

    /**
     * @description: project导入
     * @author Will
     * @date: 2023/6/1 17:14
     * @param excelFile
     * @return List<Task>
     */
    public static List<Task> readMmpFile(MultipartFile excelFile) {
        //如果读取的是MultipartFile，那么直接使用获取InputStream即可
        try{
            String extension = FilenameUtils.getExtension(excelFile.getOriginalFilename());
            //非mpp结尾的文件返回报错
            if (!"mpp".equals(extension)) {
                throw new ServiceException(ApiError.ERROR_1032);
            }
            //获取文件路径
            InputStream inputStream = excelFile.getInputStream();
            //使用通用项目阅读器 自动识别文件类型,另外的读取组件：MPPReader mppRead = new MPPReader();
            UniversalProjectReader reader = new UniversalProjectReader();
            //根据文件路径查找文件并解析文件数据
            ProjectFile project = reader.read(inputStream);
            //获取project中所有的任务
            List<Task> taskList = project.getChildTasks();
            return taskList;
        } catch (MPXJException e) {
            log.error("mpp文件读取失败",e);
            throw new ServiceException(ApiError.ERROR_1033);
        } catch (Exception e) {
            log.error("mpp文件读取失败",e);
            throw new ServiceException(ApiError.ERROR_1033);
        }
    }

    /**
     * @description: 解析子集任务
     * @author Will
     * @date: 2023/6/1 19:40
     * @param task 任务
     * @param list 用于接收的集合
     * @param customFields 自定义字段Map
     */
    public static void getChildrenTask (Task task , List<ProjectImportDTO> list, Map<Integer,String> customFields) {
        // 继续获取子任务
        List<Task> tasks = task.getChildTasks();
        //该循环是遍历所有的子任务
        for (int i = 0; i < tasks.size(); i++) {
            // 说明还是在父任务层
            if (tasks.get(i).getResourceAssignments().size() == 0) {
                //生成projectDTO
                generateProjectDTO(tasks.get(i),list,customFields);
                // 继续进行递归，当前保存的只是父任务的信息
                getChildrenTask(tasks.get(i),list,customFields);
            } else {
                //生成projectDTO
                generateProjectDTO(tasks.get(i),list,customFields);
            }
        }
    }

    /**
     * @description: 生成projectDTO
     * @author Will
     * @date: 2023/6/1 18:18
     * @param task
     * @param proList
     * @param customFields
     */
    private static void generateProjectDTO (Task task, List <ProjectImportDTO> proList, Map<Integer,String> customFields){
        ProjectImportDTO pro = new ProjectImportDTO();

        //任务项ID
        Integer taskId = task.getID();
        //任务唯一ID
        Integer taskUniqueId = task.getUniqueID();
        //父级任务项ID
        Integer taskParentDefId = task.getParentTask().getID();
        //任务项等级
        Integer taskOutlineLevel = task.getOutlineLevel();
        //wbs
        String taskWbs = task.getWBS();
        //任务项名称
        String taskName = task.getName();
        //计划工作量
        double taskWork = task.getWork().getDuration();
        //计划工作量单位
        String taskWorkUnits = task.getWork().getUnits().getName();
        //计划工期
        double taskDuration = task.getDuration().getDuration();
        //计划工期单位
        String taskDurationUnits = task.getDuration().getUnits().getName();
        //计划开始日期
        Date taskStartDate = task.getStart();
        //计划结束日期
        Date taskFinishDate = task.getFinish();
        //完成百分比
        String taskPercentage = String.valueOf(task.getPercentageComplete().byteValue()) + "%";
        //获取前置任务（任务流）
        List<Relation> taskPredecessors = task.getPredecessors();
        StringBuffer sb = new StringBuffer();
        if (taskPredecessors != null) {
            if (taskPredecessors.size() > 0) {
                for (Relation relation : taskPredecessors) {
                    Integer targetTaskId = relation.getTargetTask().getID();
                    if (sb.length() == 0) {
                        sb.append(targetTaskId);
                    } else {
                        sb.append("," + targetTaskId);
                    }
                }
            }
        }
        //资源名称
        String resourceName = "";
        List<ResourceAssignment> resourceAssignments = task.getResourceAssignments();
        if (resourceAssignments != null && resourceAssignments.size() != 0) {
            Boolean flag = true;
            for (ResourceAssignment resourceAssignment : resourceAssignments) {
                if (resourceAssignment.getResource() == null) {
                    flag = false;
                    break;
                }
                resourceName = resourceName + "," + resourceAssignment.getResource().getName();
            }
            if (flag) {
                resourceName = resourceName.substring(1, resourceName.length());
            }
        }
        //自定义字段
        if (ObjectUtils.isNotEmpty(customFields)) {
            Map<String,String> customFieldValues = new LinkedHashMap<>();
            for (Map.Entry<Integer,String> entry : customFields.entrySet()) {
                Integer key = entry.getKey();
                if (MathUtil.compareTo(key,MathUtil.ZERO) <= MathUtil.ZERO) {
                    continue;
                }
                String text = task.getText(key.intValue());
                customFieldValues.put(entry.getValue(),text);
            }
            pro.setCustomFieldValues(customFieldValues);
        }

        String taskPredecessorsIds = sb.toString();
        //前置任务
        pro.setPreTask(taskPredecessorsIds);
        //任务ID
        pro.setTaskId(taskId.toString());
        //任务唯一ID
        pro.setTaskUniqueId(taskUniqueId.toString());
        //父任务ID
        pro.setTaskParentDefId(taskParentDefId.toString());
        //任务级别
        pro.setTaskOutlineLevel(taskOutlineLevel);
        //WBS码
        pro.setTaskWbs(taskWbs);
        //任务名称
        pro.setTaskName(taskName);
        //计划工作量
        pro.setTaskWork(String.valueOf(taskWork));
        //计划工作量单位
        pro.setTaskWorkUnits(taskWorkUnits);
        //实际工作量单位
        pro.setTaskActualWorkUnits(taskWorkUnits);
        //计划工期
        pro.setTaskDuration(String.valueOf(taskDuration));
        //计划工期单位
        pro.setTaskDurationUnits(taskDurationUnits);
        //计划开始时间
        pro.setTaskStartDate(ObjectUtils.isEmpty(taskStartDate) ? null : LocalDateUtil.date2LocalDate(taskStartDate) );
        //计划结束时间
        pro.setTaskFinishDate(ObjectUtils.isEmpty(taskFinishDate) ? null : LocalDateUtil.date2LocalDate(taskFinishDate) );
        //前置任务
        pro.setPreTask(taskPredecessorsIds);
        //资源名称
        pro.setResourceName(resourceName);
        //阶段百分比
        pro.setTaskPercentage(taskPercentage);
        proList.add(pro);
    }

}
