package com.dstz.bpm.api.model.nodedef.impl;

import com.dstz.base.api.exception.BusinessException;
import com.dstz.bpm.api.constant.NodeType;
import com.dstz.bpm.api.engine.action.button.ButtonFactory;
import com.dstz.bpm.api.engine.plugin.context.BpmPluginContext;
import com.dstz.bpm.api.exception.BpmStatusCode;
import com.dstz.bpm.api.model.def.BpmProcessDef;
import com.dstz.bpm.api.model.def.NodeInit;
import com.dstz.bpm.api.model.def.NodeProperties;
import com.dstz.bpm.api.model.form.BpmForm;
import com.dstz.bpm.api.model.nodedef.BpmNodeDef;
import com.dstz.bpm.api.model.nodedef.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseBpmNodeDef implements BpmNodeDef {
    private static final long serialVersionUID = -2044846605763777657L;

    private String nodeId;
    private String name;
    private NodeType type;
    private BpmNodeDef parentBpmNodeDef;
    private List<BpmNodeDef> incomeNodes = new ArrayList<BpmNodeDef>();
    private List<BpmNodeDef> outcomeNodes = new ArrayList<BpmNodeDef>();

    private List<BpmPluginContext> bpmPluginContexts = new ArrayList<BpmPluginContext>();
    private Map<String, String> attrMap = new HashMap<String, String>();

    private BpmProcessDef bpmProcessDef;

    /**
     * 节点配置表单。
     */
    private BpmForm mobileForm;

    private BpmForm form;

    /**
     * 表单初始化项。
     */
    private List<NodeInit> nodeInits = new ArrayList<NodeInit>();

    /**
     * 节点属性。
     */
    private NodeProperties nodeProperties = new NodeProperties();

    private List<Button> buttons = null;

    private List<Button> buttonList = null;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public List<BpmNodeDef> getIncomeNodes() {
        return incomeNodes;
    }

    public void setIncomeNodes(List<BpmNodeDef> incomeNodes) {
        this.incomeNodes = incomeNodes;
    }

    public List<BpmNodeDef> getOutcomeNodes() {
        return outcomeNodes;
    }

    public void setOutcomeNodes(List<BpmNodeDef> outcomeNodes) {
        this.outcomeNodes = outcomeNodes;
    }

    public List<BpmPluginContext> getBpmPluginContexts() {
        return bpmPluginContexts;
    }

    public void setBpmPluginContexts(List<BpmPluginContext> bpmPluginContexts) {
    	Collections.sort(bpmPluginContexts);
        this.bpmPluginContexts = bpmPluginContexts;
    }

    public void setAttribute(String name, String value) {
        name = name.toLowerCase().trim();
        attrMap.put(name.toLowerCase(), value);

    }

    @Override
    public String getAttribute(String name) {
        name = name.toLowerCase().trim();
        return attrMap.get(name);

    }

    @Override
    public void addIncomeNode(BpmNodeDef bpmNodeDef) {
        this.incomeNodes.add(bpmNodeDef);
    }

    @Override
    public void addOutcomeNode(BpmNodeDef bpmNodeDef) {
        this.outcomeNodes.add(bpmNodeDef);

    }

    @Override
    public BpmNodeDef getParentBpmNodeDef() {
        return this.parentBpmNodeDef;
    }

    public void setParentBpmNodeDef(BpmNodeDef parentBpmNodeDef) {
        this.parentBpmNodeDef = parentBpmNodeDef;
    }

    @Override
    public String getRealPath() {
        String id = this.getNodeId();
        BpmNodeDef parent = getParentBpmNodeDef();
        while (parent != null) {
            id = parent.getNodeId() + "/" + id;
            parent = parent.getParentBpmNodeDef();
        }
        return id;
    }

    public BpmProcessDef getBpmProcessDef() {
        return bpmProcessDef;
    }

    public void setBpmProcessDef(BpmProcessDef bpmProcessDef) {
        this.bpmProcessDef = bpmProcessDef;
    }

    public BpmProcessDef getRootProcessDef() {
        BpmProcessDef processDef = this.bpmProcessDef;
        BpmProcessDef parent = processDef.getParentProcessDef();
        while (parent != null) {
            processDef = parent;
            parent = parent.getParentProcessDef();
        }
        return processDef;
    }


    @Override
    public List<BpmNodeDef> getOutcomeTaskNodes() {
        return getNodeDefList(outcomeNodes);
    }

    private List<BpmNodeDef> getNodeDefList(List<BpmNodeDef> bpmNodeDefs) {
        List<BpmNodeDef> bpmNodeList = new ArrayList<BpmNodeDef>();
        for (BpmNodeDef def : bpmNodeDefs) {
            if (NodeType.USERTASK.equals(def.getType()) || NodeType.SIGNTASK.equals(def.getType())) {
                bpmNodeList.add(def);
            } else if (NodeType.SUBPROCESS.equals(def.getType())) {
                SubProcessNodeDef subProcessNodeDef = (SubProcessNodeDef) def;
                // 取得子流程中的开始节点
                BpmNodeDef startNodeDef = subProcessNodeDef.getChildBpmProcessDef().getStartEvent();
                bpmNodeList.addAll(getNodeDefList(startNodeDef.getOutcomeNodes()));
            } else if (NodeType.END.equals(def.getType()) && def.getParentBpmNodeDef() != null && NodeType.SUBPROCESS.equals(def.getParentBpmNodeDef().getType())) {
                SubProcessNodeDef subProcessNodeDef = (SubProcessNodeDef) def.getParentBpmNodeDef();
                bpmNodeList.addAll(getNodeDefList(subProcessNodeDef.getOutcomeNodes()));
            } else {
                bpmNodeList.addAll(getNodeDefList(def.getOutcomeNodes()));
            }
        }
        return bpmNodeList;
    }

    @Override
    public List<BpmNodeDef> getInnerOutcomeTaskNodes(boolean includeSignTask) {
        List<BpmNodeDef> bpmNodeList = getInnerOutcomeTaskNodes(outcomeNodes, includeSignTask);
        return bpmNodeList;
    }

    private List<BpmNodeDef> getInnerOutcomeTaskNodes(List<BpmNodeDef> bpmNodeDefs, boolean includeSignTask) {
        List<BpmNodeDef> bpmNodeList = new ArrayList<BpmNodeDef>();
        for (BpmNodeDef def : bpmNodeDefs) {
            if (NodeType.USERTASK.equals(def.getType()) || (includeSignTask && NodeType.SIGNTASK.equals(def.getType()))) {
                bpmNodeList.add(def);
            } else if (NodeType.SUBPROCESS.equals(def.getType()) || NodeType.CALLACTIVITY.equals(def.getType()) || NodeType.END.equals(def.getType())) {
                continue;
            } else {
                bpmNodeList.addAll(getInnerOutcomeTaskNodes(def.getOutcomeNodes(), includeSignTask));
            }
        }
        return bpmNodeList;
    }

    @Override
    public <T> T getPluginContext(Class<T> cls) {
        BpmPluginContext ctx = null;
        List<BpmPluginContext> list = this.bpmPluginContexts;
        for (BpmPluginContext context : list) {
            if (context.getClass().isAssignableFrom(cls)) {
                ctx = context;
                break;
            }
        }
        return (T) ctx;
    }


    public NodeProperties getNodeProperties() {
        return nodeProperties;
    }

    public void setNodeProperties(NodeProperties nodeProperties) {
        this.nodeProperties = nodeProperties;
    }


    public void setButtons(List<Button> buttons) {
        this.buttons = buttons;
    }

    @Override
    public List<Button> getButtons() {
        if (this.buttons != null) return this.buttons;
        if (buttonList != null) return buttonList;

        try {
            buttonList = ButtonFactory.generateButtons(this, true);
        } catch (Exception e) {
            throw new BusinessException(BpmStatusCode.TASK_ACTION_BTN_ERROR, e);
        }
        return buttonList;
    }

    /**
     * 是否存在自定义按钮
     *
     * @return
     */
    public boolean isDefaultBtn() {
        if (this.buttons != null)
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getName()).append(":").append(this.getNodeId())
                .append(super.toString());
        return sb.toString();
    }

    public BpmForm getMobileForm() {
        return mobileForm;
    }

    public void setMobileForm(BpmForm mobileForm) {
        this.mobileForm = mobileForm;
    }

    public BpmForm getForm() {
        return form;
    }

    public void setForm(BpmForm form) {
        this.form = form;
    }
}
