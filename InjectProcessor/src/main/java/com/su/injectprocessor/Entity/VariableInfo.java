package com.su.injectprocessor.Entity;

import javax.lang.model.element.VariableElement;

public class VariableInfo {

    private int viewId;//ID
    private VariableElement variableElement;//变量名称、类型

    public void setViewId(int viewId) {
        this.viewId = viewId;
    }

    public int getViewId() {
        return viewId;
    }

    public void setVariableElement(VariableElement variableElement) {
        this.variableElement = variableElement;
    }

    public VariableElement getVariableElement() {
        return variableElement;
    }
}