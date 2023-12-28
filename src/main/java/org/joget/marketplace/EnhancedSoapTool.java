package org.joget.marketplace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.plugin.enterprise.SoapTool;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONObject;
import org.json.XML;

public class EnhancedSoapTool extends SoapTool {

    @Override
    public Object execute(Map properties) {
        WorkflowAssignment wfAssignment = (WorkflowAssignment) properties.get("workflowAssignment");

        try {
            String jsonResponse = null;
            String jsonRequest = null;
            Boolean debug = false;
            if (properties.get("debug") != null && "true".equals((String) properties.get("debug"))) {
                debug = true;
            }

            String wsdlUrl = WorkflowUtil.processVariable((String) properties.get("wsdlUrl"), "", wfAssignment);
            String operationName = WorkflowUtil.processVariable((String) properties.get("operationName"), "", wfAssignment);
            String username = WorkflowUtil.processVariable((String) properties.get("username"), "", wfAssignment);
            String password = WorkflowUtil.processVariable((String) properties.get("password"), "", wfAssignment);
            String xml = (String) properties.get("xml");

            String reqResformDefId = (String) properties.get("reqResformDefId");
            String requestFieldId = (String) properties.get("requestFieldId");
            String responseFieldId = (String) properties.get("responseFieldId");

            Object[] customNamespaces = (Object[]) properties.get("customNamespaces");

            Boolean useWSS = false;
            if (properties.get("useWSS") != null && "true".equals((String) properties.get("useWSS"))) {
                useWSS = true;
            }

            if (xml != null && !xml.isEmpty()) {
                xml = WorkflowUtil.processVariable(xml, "", wfAssignment);
                String soapAction = (String) properties.get("soapAction");
                jsonResponse = xmlCall(wsdlUrl, username, password, operationName, customNamespaces, xml, soapAction, debug, useWSS);
            } else {
                Collection<String> params = new ArrayList<>();
                Object[] paramsValues = (Object[]) properties.get("params");
                for (Object o : paramsValues) {
                    Map mapping = (HashMap) o;
                    String value = mapping.get("value").toString();
                    params.add(WorkflowUtil.processVariable(value, "", wfAssignment));
                }
                jsonResponse = call(wsdlUrl, username, password, operationName, params.toArray(new String[0]), debug);
            }

            
            // store the request and response
            jsonRequest = prepareRequest(properties);
            if (reqResformDefId != null && !reqResformDefId.isEmpty()) {
                save(jsonRequest, jsonResponse, reqResformDefId, requestFieldId, responseFieldId);
            }

            if (debug) {
                LogUtil.info(SoapTool.class.getName(), jsonResponse);
            }
            if (jsonResponse != null) {
                Map object = PropertyUtil.getPropertiesValueFromJson(jsonResponse);
                storeToForm(wfAssignment, properties, object);
                storeToWorkflowVariable(wfAssignment, properties, object);
            }
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        }

        return null;
    }

    private String prepareRequest(Map properties) {
        WorkflowAssignment wfAssignment = (WorkflowAssignment) properties.get("workflowAssignment");
        String requestJson = "";
        String wsdlUrl = WorkflowUtil.processVariable((String) properties.get("wsdlUrl"), "", wfAssignment);
        String operationName = WorkflowUtil.processVariable((String) properties.get("operationName"), "", wfAssignment);
        String username = WorkflowUtil.processVariable((String) properties.get("username"), "", wfAssignment);
        String password = WorkflowUtil.processVariable((String) properties.get("password"), "", wfAssignment);
        String xml = (String) properties.get("xml");
        Collection<String> params = new ArrayList<String>();
        Object[] paramsValues = (Object[]) properties.get("params");
        for (Object o : paramsValues) {
            Map mapping = (HashMap) o;
            String value = mapping.get("value").toString();
            params.add(WorkflowUtil.processVariable(value, "", wfAssignment));
        }
        String soapAction = (String) properties.get("soapAction");

        String reqResformDefId = (String) properties.get("reqResformDefId");
        String requestFieldId = (String) properties.get("requestFieldId");
        String responseFieldId = (String) properties.get("responseFieldId");

        String formDefId = (String) properties.get("formDefId");
        String multirowBaseObjectName = (String) properties.get("multirowBaseObject");
        Object[] fieldMapping = (Object[]) properties.get("fieldMapping");

        Map<String, String> fieldMap = new HashMap<>();
        for (Object o : fieldMapping) {
            Map mapping = (HashMap) o;
            String fieldName = mapping.get("field").toString();
            String jsonObjectName = WorkflowUtil.processVariable(mapping.get("jsonObjectName").toString(), null, wfAssignment, null, null);
            fieldMap.put(fieldName, jsonObjectName);
        }

        Map<String, String> wfVarMap = new HashMap<>();
        //Collection<String> wfVarParams = new ArrayList<String>();
        Object[] wfVariableMapping = (Object[]) properties.get("wfVariableMapping");
        for (Object o : wfVariableMapping) {
            Map mapping = (HashMap) o;
            String variable = mapping.get("variable").toString();
            String jsonObjectName = mapping.get("jsonObjectName").toString();
            wfVarMap.put(WorkflowUtil.processVariable(variable, "", wfAssignment), jsonObjectName);
        }

        String debug = (String) properties.get("debug");

        PluginProperties pp = new PluginProperties();
        pp.setWsdlUrl(wsdlUrl);
        pp.setOperationName(operationName);
        pp.setUsername(username);
        pp.setPassword(SecurityUtil.decrypt(password));
        pp.setXml(xml);
        pp.setParams(params);
        pp.setSoapAction(soapAction);
        pp.setSaveRequestResposeFormId(reqResformDefId);
        pp.setRequestFieldId(requestFieldId);
        pp.setResponseFieldId(responseFieldId);
        pp.setFormDefId(formDefId);
        pp.setMultirowBaseObjectName(multirowBaseObjectName);
        pp.setFieldMapping(fieldMap);
        pp.setWfVariableMapping(wfVarMap);
        pp.setDebug(debug);

        JSONObject jSONObject = new JSONObject(pp);
        requestJson = jSONObject.toString();
        return requestJson;
    }

    private void save(String request, String response, String formDefId, String requestFieldId, String responseFieldId) {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        AppService appService = (AppService) FormUtil.getApplicationContext().getBean("appService");
        FormRowSet rows = new FormRowSet();
        FormRow row = new FormRow();
        row.put(requestFieldId, request);
        row.put(responseFieldId, response);
        rows.add(row);
        String tableName = appService.getFormTableName(appDef, formDefId);
        appService.storeFormData(formDefId, tableName, rows, null);
    }

      protected String xmlCall(String wsdlURL, String username, String password, String operationName, Object[] customNamespaces, String xml, String soapAction, boolean debug, boolean useWSS) throws Exception {
        if (!(wsdlURL == null || wsdlURL.isEmpty() || xml == null || xml.isEmpty())) {
            CloseableHttpClient httpclient = null;
            try {
                HttpClientBuilder httpClientBuilder = HttpClients.custom();
                
                if(username != null && !username.isEmpty()){
                    CredentialsProvider credsProvider = new BasicCredentialsProvider();
                    credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
                    httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
                }
                
                httpclient = httpClientBuilder.build();
                wsdlURL = wsdlURL.replaceAll("(?i)\\?WSDL", "");
                HttpPost post = new HttpPost(wsdlURL);
                
                String customNs = "";
                
                if (ArrayUtils.isNotEmpty(customNamespaces)) {
                    for (Object ns : customNamespaces) {
                        Map mapping = (HashMap) ns;
                        String prefix = mapping.get("prefix").toString();
                        String uri = mapping.get("uri").toString();
                        customNs += "xmlns:" + prefix + "=\"" + uri + "\" ";
                    }
                }
                
                if (soapAction != null && !soapAction.isEmpty()) {
                    LogUtil.info(SoapTool.class.getName(), "Using SOAP 1.1. SOAP Action is " + soapAction);
                    post.addHeader("Content-Type", "text/xml; charset=utf-8");
                    post.addHeader("SOAPAction", soapAction);
                    String requestXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
                    requestXml += "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" ";
                    requestXml += customNs;
                    requestXml += ">\n";
                    if(useWSS){
                        requestXml += "<soapenv:Header>\n<wsse:Security soapenv:mustUnderstand='1' xmlns:wsse='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd' xmlns:wsu='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd'>\n";
                        requestXml += "<wsse:UsernameToken>\n<wsse:Username>" + username + "</wsse:Username>\n";
                        requestXml += " <wsse:Password>" + password + "</wsse:Password>\n</wsse:UsernameToken>\n";
                        requestXml += "</wsse:Security>\n</soapenv:Header>\n";
                    }
                    requestXml += "  <soap:Body>\n";
                    requestXml += xml;
                    requestXml += "  </soap:Body>\n";
                    requestXml += "</soap:Envelope>";
                    HttpEntity entity = new ByteArrayEntity(requestXml.getBytes("UTF-8"));
                    post.setEntity(entity);
                } else {
                    post.addHeader("Content-Type", "application/soap+xml; charset=utf-8");
                    String requestXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
                    requestXml += "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\" ";
                    requestXml += customNs;
                    requestXml += ">\n";
                    if(useWSS){
                        requestXml += "<soapenv:Header>\n<wsse:Security soapenv:mustUnderstand='1' xmlns:wsse='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd' xmlns:wsu='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd'>\n";
                        requestXml += "<wsse:UsernameToken>\n<wsse:Username>" + username + "</wsse:Username>\n";
                        requestXml += " <wsse:Password>" + password + "</wsse:Password>\n</wsse:UsernameToken>\n";
                        requestXml += "</wsse:Security>\n</soapenv:Header>\n";
                    }                    
                    requestXml += "  <soap12:Body>\n";
                    requestXml += xml;
                    requestXml += "  </soap12:Body>\n";
                    requestXml += "</soap12:Envelope>";
                    HttpEntity entity = new ByteArrayEntity(requestXml.getBytes("UTF-8"));
                    post.setEntity(entity);
                }
                
                HttpResponse httpResponse = httpclient.execute(post);
                String response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                response = convertStringResponse(response);
                
                JSONObject temp = XML.toJSONObject(response);
                JSONObject result = findRootElement(temp, operationName);
                result = beautifierResult(result);
                
                if (result != null) {
                    return result.toString();
                }
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "");
            } finally {
                if (httpclient != null) {
                    httpclient.close();
                }
            }
        }
        
        return null;
    }

    protected JSONObject findRootElement(JSONObject element, String operationName) throws Exception {
        JSONObject returnElement = element;
        
        Iterator<String> iterator = returnElement.keys();
	while (iterator.hasNext()) {
            String key = iterator.next();
            if (key.endsWith(":Envelope")) {
                returnElement = returnElement.getJSONObject(key);
                iterator = returnElement.keys();
            } else if (key.endsWith(":Body")) {
                returnElement = returnElement.getJSONObject(key);
                
                if (returnElement.has(operationName + "Response")) {
                    returnElement = returnElement.getJSONObject(operationName + "Response");
                }
                
                break;
            }
	}
        
        return returnElement;
    }
    

    @Override
    public String getName() {
        return "Enhanced SOAP Tool";
    }

    @Override
    public String getDescription() {
        return "Reads a WSDL URL, and inserts formatted data into form data table or workflow variable and stores the request and response";
    }

    @Override
    public String getVersion() {
        return "7.0.0";
    }

    @Override
    public String getLabel() {
        return "Enhanced SOAP Tool";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String appId = appDef.getId();
        String appVersion = appDef.getVersion().toString();
        Object[] arguments = new Object[]{appId, appVersion};
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/enhancedSoapTool.json", arguments, true, "messages/EnhancedSoapTool");
        return json;
    }

}
