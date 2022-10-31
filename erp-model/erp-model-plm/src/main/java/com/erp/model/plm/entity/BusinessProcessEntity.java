package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 业务流程
 * @TableName business_process
 */
@TableName("business_process")
@Data
public class BusinessProcessEntity implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 业务流程的key
     */
    private String businessKey;

    /**
     * 流程定义的key
     */
    private String processDefinitionKey;

    /**
     * 在camunda 下画的流程图 在workflow resources 下的目录
     */
    private String bpmnName;

    /**
     * 业务名称
     */
    private String businessName;

    /**
     * 业务属性
     */
    private String businessType;

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public String getId() {
        return id;
    }

    /**
     * 
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 业务流程的key
     */
    public String getBusinessKey() {
        return businessKey;
    }

    /**
     * 业务流程的key
     */
    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    /**
     * 流程定义的key
     */
    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    /**
     * 流程定义的key
     */
    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    /**
     * 在camunda 下画的流程图 在workflow resources 下的目录
     */
    public String getBpmnName() {
        return bpmnName;
    }

    /**
     * 在camunda 下画的流程图 在workflow resources 下的目录
     */
    public void setBpmnName(String bpmnName) {
        this.bpmnName = bpmnName;
    }

    /**
     * 业务名称
     */
    public String getBusinessName() {
        return businessName;
    }

    /**
     * 业务名称
     */
    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    /**
     * 业务属性
     */
    public String getBusinessType() {
        return businessType;
    }

    /**
     * 业务属性
     */
    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        BusinessProcessEntity other = (BusinessProcessEntity) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getBusinessKey() == null ? other.getBusinessKey() == null : this.getBusinessKey().equals(other.getBusinessKey()))
            && (this.getProcessDefinitionKey() == null ? other.getProcessDefinitionKey() == null : this.getProcessDefinitionKey().equals(other.getProcessDefinitionKey()))
            && (this.getBpmnName() == null ? other.getBpmnName() == null : this.getBpmnName().equals(other.getBpmnName()))
            && (this.getBusinessName() == null ? other.getBusinessName() == null : this.getBusinessName().equals(other.getBusinessName()))
            && (this.getBusinessType() == null ? other.getBusinessType() == null : this.getBusinessType().equals(other.getBusinessType()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getBusinessKey() == null) ? 0 : getBusinessKey().hashCode());
        result = prime * result + ((getProcessDefinitionKey() == null) ? 0 : getProcessDefinitionKey().hashCode());
        result = prime * result + ((getBpmnName() == null) ? 0 : getBpmnName().hashCode());
        result = prime * result + ((getBusinessName() == null) ? 0 : getBusinessName().hashCode());
        result = prime * result + ((getBusinessType() == null) ? 0 : getBusinessType().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", businessKey=").append(businessKey);
        sb.append(", processDefinitionKey=").append(processDefinitionKey);
        sb.append(", bpmnName=").append(bpmnName);
        sb.append(", businessName=").append(businessName);
        sb.append(", businessType=").append(businessType);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}