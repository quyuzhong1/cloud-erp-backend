package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.FileUtil;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * dmp处理明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Service
@Scope("prototype")
public class DmpInputFeishuGetInstancesDetailDmpHandler extends DmpInputDoChildDmpHandler {

    @Override
    protected List<Map<String, Object>> getDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoEntityList, String childMongoStorageName) {
        List<ParamData> paramDataList = new ArrayList<>();
        List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, inputTaskId).list();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, list.get(0).getId()));
        List<Map<String, Object>> mongoData = mongoService.findMongoData(paramDataList, childMongoStorageName);
        for (Map<String, Object> mongoDatum : mongoData) {
            // TODO 处理文件url的路径问题

        }
//        JSONArray fileArray = valueObj.getJSONArray("value");
//        if (CollectionUtils.isEmpty(fileArray)) {
//            log.warn("控件{}的文件列表为空，无法提取文件信息", valueObj);
//            return null;
//        }
//        List<JSONObject> resultList = new LinkedList<>();
//        String fileDesc = valueObj.getStr("name");
//        String id = valueObj.getStr("id");
//        String[] names = valueObj.getStr("ext").split(",");
//        for (int i = 0; i < names.length; i++) {
//            String sourceUrl = fileArray.get(i).toString();
//            String fileName = names[i];
//            // 以 “审批实例 ID + 控件 ID + 文件名” 作为复合键判断重复。
//            String fileKey = CharSequenceUtil.format("{}|{}|{}", instanceCode, id, fileName);
//            String fileExt = valueObj.getStr("id");
//            String fileExtension = FileUtil.getFileExtension(fileExt);
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.set("sourceSystem", "feishu");
//            jsonObject.set("sourceUrl", sourceUrl);
//            jsonObject.set("fileUrl", "");
//            jsonObject.set("fileType", fileExtension);
//            jsonObject.set("billTopic", "instance");
//            jsonObject.set("billId", instanceCode);
//            jsonObject.set("fileKey", fileKey);
//            jsonObject.set("fileName", fileExt);
//            jsonObject.set("remark", fileDesc);
//            resultList.add(jsonObject);
//        }
        return mongoData;
    }

    @Override
    protected void putDmpId(List<Map<String, Object>> dmpInputMongoChildEntityList) {
        DmpCfgInputConvertEntity mainConvertId = this.getMainConvertId();
        String parentStorageName = mainConvertId.getStorageName();
        ServiceImpl parentServiceImpl = this.getServiceImpl(parentStorageName);
        QueryWrapper<?> wrapper = new QueryWrapper<>();
        wrapper.eq(INPUT_TASK_ID, inputTaskId);
        List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);
        Map<String, String> billNoIdMap = new HashMap<>();
        if (CollUtil.isNotEmpty(listMaps)) {
            for (Map<String, Object> listMap : listMaps) {
                billNoIdMap.put(listMap.get("instance_id").toString(), listMap.get(BaseEntity.FIELD_ID).toString());
            }
        }
        for (Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
            String billNo = dmpInputMongoChildEntity.get("instance_code").toString();
            String dmpId = billNoIdMap.get(billNo);
            dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
        }
    }
}
