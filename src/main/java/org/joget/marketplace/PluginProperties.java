package org.joget.marketplace;

import java.util.Collection;
import java.util.Map;

public class PluginProperties {

    private String wsdlUrl;
    private String operationName;
    private String username;
    private String password;
    private String xml;
    private Collection<String> params;
    private String soapAction;

    private String saveRequestResposeFormId;
    private String requestFieldId;
    private String responseFieldId;

    private String formDefId;
    private String multirowBaseObjectName;
    private Map<String, String> fieldMapping;
    private Map<String, String> wfVariableMapping;

    private String debug;

    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public Collection<String> getParams() {
        return params;
    }

    public void setParams(Collection<String> params) {
        this.params = params;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    public String getSaveRequestResposeFormId() {
        return saveRequestResposeFormId;
    }

    public void setSaveRequestResposeFormId(String saveRequestResposeFormId) {
        this.saveRequestResposeFormId = saveRequestResposeFormId;
    }


    public String getRequestFieldId() {
        return requestFieldId;
    }

    public void setRequestFieldId(String requestFieldId) {
        this.requestFieldId = requestFieldId;
    }

    public String getResponseFieldId() {
        return responseFieldId;
    }

    public void setResponseFieldId(String responseFieldId) {
        this.responseFieldId = responseFieldId;
    }

    public String getFormDefId() {
        return formDefId;
    }

    public void setFormDefId(String formDefId) {
        this.formDefId = formDefId;
    }

    public String getMultirowBaseObjectName() {
        return multirowBaseObjectName;
    }

    public void setMultirowBaseObjectName(String multirowBaseObjectName) {
        this.multirowBaseObjectName = multirowBaseObjectName;
    }

    public Map<String, String> getFieldMapping() {
        return fieldMapping;
    }

    public void setFieldMapping(Map<String, String> fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    public Map<String, String> getWfVariableMapping() {
        return wfVariableMapping;
    }

    public void setWfVariableMapping(Map<String, String> wfVariableMapping) {
        this.wfVariableMapping = wfVariableMapping;
    }

    public String getDebug() {
        return debug;
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }
}
