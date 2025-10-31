package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.utils.RedisUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FileUtil;
import com.erp.model.dmp.entity.DmpRefPlatformFileEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.DmpRefPlatformFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputFeishuFileTransferInitHandler extends DmpInputInitHandler {

    @Resource
    private DmpRefPlatformFileService dmpRefPlatformFileService;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
        if (StringUtils.isBlank(parentStorageName)) {
            return Collections.emptyList();
        }
        List<ParamData> paramDataList = new ArrayList<>();
        // 查询父任务变更的信息
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
        //查询变更的实例ID
        List<Map<String, Object>> dmpInputMongoChildList = mongoService.findMongoData(paramDataList, parentStorageName);
        if (CollUtil.isEmpty(dmpInputMongoChildList)){
            return Collections.emptyList();
        }

        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        //收集done的id,
        JSONArray result = new JSONArray();
        if (CollUtil.isNotEmpty(dmpInputMongoChildList)) {
            List<JSONObject> allEntityList = new LinkedList<>();
            for (Map<String, Object> map : dmpInputMongoChildList) {
                List<JSONObject> sourceEntityList = convertFileInfoList(map);
                if (CollectionUtil.isNotEmpty(sourceEntityList)) {
                    allEntityList.addAll(sourceEntityList);
                }
            }
            if (CollUtil.isNotEmpty(allEntityList)) {
                // 批量校验已存在的文件，过滤掉已存在的文件
                List<String> fileKeyList = allEntityList.stream().map(e -> e.getStr("fileKey")).collect(Collectors.toList());
                Map<String, DmpRefPlatformFileEntity> fileEntityMap = dmpRefPlatformFileService.mapByFileKey("feishu", fileKeyList);
                // 上传
                for (JSONObject object : allEntityList) {
                    if (fileEntityMap.containsKey(object.getStr("fileKey"))) {
                        continue;
                    }
                    String redisKey = CharSequenceUtil.format(RedisCacheConstants.FEI_SHU_RESULT_PREFIX, "fileUrl", object.getStr("fileKey"));
                    // 优先从缓存获取，避免重复下载上传
                    Object redisObj = redisUtil.get(redisKey);
                    if (null != redisObj) {
                        JSONObject redisJson = (JSONObject) redisObj;
                        result.add(redisJson);
                        continue;
                    }
                    String sourceUrl = object.getStr("sourceUrl");
                    String fileName = object.getStr("fileName");
                    try {
                        // 1.下载第三方文件
                        byte[] fileByte = FileUtil.downloadFile(sourceUrl);
                        //fileByte转为file
                        File file = new File(fileName);
                        FileUtils.writeByteArrayToFile(file, fileByte);
                        // 2.将文件上传到文件服务器
                        String uploadUrl = FastDFSClientUtil.uploadFile(file, fileName);
                        // 3.更新url
                        object.set("fileUrl", uploadUrl);
                        result.add(object);
                        redisUtil.set(redisKey, object, 7200);
                    } catch (Exception e) {
                        log.warn("飞书文件下载处理失败{}", ExceptionUtils.getStackTrace(e));
                        throw new RuntimeException("飞书文件下载处理失败:" + ExceptionUtil.stacktraceToString(e, 1000));
                    }
                }
            }
        }
        //更新原表中的数据
        DmpInputTaskInitDTO dmpInputTaskInitDTO = DmpInputTaskInitDTO.initMsg(result.toJSONString(0));
        dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
        return dmpInputTaskInitDTOList;
    }

    private List<JSONObject> convertFileInfoList(Map<String, Object> map) {
        String form = (String) map.get("form");
        String instanceCode = (String) map.get("instanceCode");
        if (StringUtils.isBlank(form)){
            log.warn("实例{}的表单信息为空，无法提取文件信息", instanceCode);
            return Collections.emptyList();
        }
        JSONArray formArray = JSONUtil.parseArray(form);
        return parseFormAndGetFileInfoList(formArray, instanceCode);
    }


    /**
     * 将飞书结构转为控件id和值的映射
     */
    private List<JSONObject> parseFormAndGetFileInfoList(JSONArray formFields, String instanceCode) {
        List<JSONObject> resultList = new LinkedList<>();
        formFields.forEach(obj -> {
            JSONObject field = (JSONObject) obj;
            String fieldType = field.getStr("type");
            if ("fieldList".equals(fieldType)) {
                JSONArray valueArray = field.getJSONArray("value");
                valueArray.forEach(row -> {
                    JSONArray rowFields = (JSONArray) row;
                    rowFields.forEach(subObj -> {
                        JSONObject subField = (JSONObject) subObj;
                        List<JSONObject> objectList = extractFieldValue(subField, instanceCode);
                        if (CollectionUtils.isNotEmpty(objectList)) {
                            resultList.addAll(objectList);
                        }
                    });
                });
            } else {
                List<JSONObject> objectList = extractFieldValue(field, instanceCode);
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
    private List<JSONObject> extractFieldValue(JSONObject field, String instanceCode) {
        String fieldType = field.getStr("type");
        if ("attachmentV2".equals(fieldType) || "imageV2".equals(fieldType) || "image".equals(fieldType)) {
            return convertFileDmpEntity(field, instanceCode);
        }
        return null;
    }

    public List<JSONObject> convertFileDmpEntity(JSONObject valueObj, String instanceCode) {
        // {"id": "widget17530790199490001","name": "证件附件1","type": "attachmentV2","ext": "replay_pid24236.log","value": ["飞书url"]}
        JSONArray fileArray = valueObj.getJSONArray("value");
        if (CollectionUtils.isEmpty(fileArray)) {
            log.warn("控件{}的文件列表为空，无法提取文件信息", valueObj);
            return null;
        }
        List<JSONObject> resultList = new LinkedList<>();
        String fileDesc = valueObj.getStr("name");
        String id = valueObj.getStr("id");
        String[] names = valueObj.getStr("ext").split(",");
        for (int i = 0; i < names.length; i++) {
            String sourceUrl = fileArray.get(i).toString();
            String fileName = names[i];
            // 以 “审批实例 ID + 控件 ID + 文件名” 作为复合键判断重复。
            String fileKey = CharSequenceUtil.format("{}|{}|{}", instanceCode, id, fileName);
            String fileExt = valueObj.getStr("id");
            String fileExtension = FileUtil.getFileExtension(fileExt);
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("sourceSystem", "feishu");
            jsonObject.set("sourceUrl", sourceUrl);
            jsonObject.set("fileUrl", "");
            jsonObject.set("fileType", fileExtension);
            jsonObject.set("billTopic", "instance");
            jsonObject.set("billId", instanceCode);
            jsonObject.set("fileKey", fileKey);
            jsonObject.set("fileName", fileExt);
            jsonObject.set("remark", fileDesc);
            resultList.add(jsonObject);
        }
        return resultList;
    }
}
