package com.erp.model.dmp.entity;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;


/**
 * <p>
 * DMP飞书审批实例详情记录表
 * </p>
 *
 * @author Jim
 * @since 2025-10-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName(value = "dmp_feishu_instance_detail", autoResultMap = true)
public class DmpFeishuInstanceDetailEntity extends BaseEntity<DmpFeishuInstanceDetailEntity> {

    /**
    * 任务转换ID
    */
    @TableField("convert_id")
    private String convertId;
    /**
    * 店铺ID
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 任务来源唯一加密代号
    */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
    * 任务数据加密代号
    */
    @TableField("data_encrypt")
    private String dataEncrypt;
    /**
    * 输入任务id
    */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
    * 主键ID
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 审批名称
    */
    @TableField("approval_name")
    private String approvalName;
    /**
    * 开始时间
    */
    @TableField("start_time")
    private String startTime;
    /**
    * 结束时间
    */
    @TableField("end_time")
    private String endTime;
    /**
    * 用户ID
    */
    @TableField("user_id")
    private String userId;
    /**
    * 用户OpenID
    */
    @TableField("open_id")
    private String openId;
    /**
    * 审批流水号
    */
    @TableField("serial_number")
    private String serialNumber;
    /**
    * 部门ID
    */
    @TableField("department_id")
    private String departmentId;
    /**
    * 审批状态
    */
    @TableField("status")
    private String status;
    /**
    * 唯一标识UUID
    */
    @TableField("uuid")
    private String uuid;
    /**
    * 表单内容(JSON文本)
    */
    @TableField("form")
    private String form;
    /**
    * 任务列表(JSON文本)
    */
    @TableField(value = "task_list", typeHandler = JacksonTypeHandler.class)
    private JSONArray taskList;
    /**
    * 评论列表(JSON文本)
    */
    @TableField(value = "comment_list", typeHandler = JacksonTypeHandler.class)
    private JSONArray commentList;
    /**
    * 时间线(JSON文本)
    */
    @TableField(value = "timeline", typeHandler = JacksonTypeHandler.class)
    private JSONArray timeline;
    /**
    * 修改后实例编码
    */
    @TableField("modified_instance_code")
    private String modifiedInstanceCode;
    /**
    * 回退实例编码
    */
    @TableField("reverted_instance_code")
    private String revertedInstanceCode;
    /**
    * 审批定义编码
    */
    @TableField("approval_code")
    private String approvalCode;
    /**
    * 是否回退
    */
    @TableField("reverted")
    private Boolean reverted;
    /**
    * 审批实例编码
    */
    @TableField("instance_code")
    private String instanceCode;


    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String MAIN_ID = "main_id";

    public static final String APPROVAL_NAME = "approval_name";

    public static final String START_TIME = "start_time";

    public static final String END_TIME = "end_time";

    public static final String USER_ID = "user_id";

    public static final String OPEN_ID = "open_id";

    public static final String SERIAL_NUMBER = "serial_number";

    public static final String DEPARTMENT_ID = "department_id";

    public static final String STATUS = "status";

    public static final String UUID = "uuid";

    public static final String FORM = "form";

    public static final String TASK_LIST = "task_list";

    public static final String COMMENT_LIST = "comment_list";

    public static final String TIMELINE = "timeline";

    public static final String MODIFIED_INSTANCE_CODE = "modified_instance_code";

    public static final String REVERTED_INSTANCE_CODE = "reverted_instance_code";

    public static final String APPROVAL_CODE = "approval_code";

    public static final String REVERTED = "reverted";

    public static final String INSTANCE_CODE = "instance_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

    public static List<String> fromConvertFileInfoList(String form, String instanceCode) {
        if (StringUtils.isBlank(form)){
            return Collections.emptyList();
        }
        JSONArray formArray = JSONUtil.parseArray(form);
        return parseFormAndGetAllFileKey(formArray,instanceCode);
    }


    /**
     * 将飞书结构转为控件id和值的映射
     */
    public static List<String> parseFormAndGetAllFileKey(JSONArray formFields, String instanceCode) {
        List<String> resultList = new LinkedList<>();
        formFields.forEach(obj -> {
            JSONObject field = (JSONObject) obj;
            String fieldType = field.getStr("type");
            if ("fieldList".equals(fieldType)) {
                JSONArray valueArray = field.getJSONArray("value");
                valueArray.forEach(row -> {
                    JSONArray rowFields = (JSONArray) row;
                    rowFields.forEach(subObj -> {
                        JSONObject subField = (JSONObject) subObj;
                        List<String> objectList = extractFieldValue(subField, instanceCode);
                        if (CollectionUtils.isNotEmpty(objectList)) {
                            resultList.addAll(objectList);
                        }
                    });
                });
            } else {
                List<String> objectList = extractFieldValue(field, instanceCode);
                if (CollectionUtils.isNotEmpty(objectList)) {
                    resultList.addAll(objectList);
                }
            }
        });
        return resultList;
    }

    /**
     * 根据控件类型提取字段值
     */
    public static List<String> extractFieldValue(JSONObject field, String instanceCode) {
        String fieldType = field.getStr("type");
        if ("attachmentV2".equals(fieldType) || "imageV2".equals(fieldType) || "image".equals(fieldType)) {
            return convertFileKey(field, instanceCode);
        }
        return Collections.emptyList();
    }

    public static List<String> convertFileKey(JSONObject valueObj, String instanceCode) {
        // {"id": "widget17530790199490001","name": "证件附件1","type": "attachmentV2","ext": "replay_pid24236.log","value": ["飞书url"]}
        JSONArray fileArray = valueObj.getJSONArray("value");
        if (CollectionUtils.isEmpty(fileArray)) {
            return Collections.emptyList();
        }
        List<String> resultList = new LinkedList<>();
        String id = valueObj.getStr("id");
        String[] names = valueObj.getStr("ext").split(",");
        for (int i = 0; i < names.length; i++) {
            String fileName = names[i];
            // 以 “审批实例 ID + 控件 ID + 文件名” 作为复合键判断重复。
            String fileKey = CharSequenceUtil.format("{}|{}|{}", instanceCode, id, fileName);
            resultList.add(fileKey);
        }
        return resultList;
    }
}