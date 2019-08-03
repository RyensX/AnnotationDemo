package com.su.injectprocessor.Processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.su.injectapis.BindView;
import com.su.injectprocessor.Entity.VariableInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

//步骤：1.编写注解->2.编写注解处理类，获取注解信息，根信息用POET生成Java代码并生成文件->3.实例化生成的Java类
//参考https://www.jianshu.com/p/9e34defcb76f

@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.su.injectapis.BindView"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class BindViewProcessor extends AbstractProcessor {

    //同一个Class下的所有注解信息
    private Map<String, List<VariableInfo>> classMap = new HashMap<>();
    //Class对应的信息：TypeElement
    private Map<String, TypeElement> classTypeElement = new HashMap<>();

    private Filer filer;
    private Elements elementUtils;
    private String className = "$BindViewInjector";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        collectInfo(roundEnvironment);
        writeToFile();
        return true;
    }

    private void collectInfo(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);

        for (Element element : elements) {
            //获取注解的值
            int viewId = element.getAnnotation(BindView.class).value();

            //被注解的元素
            VariableElement variableElement = (VariableElement) element;

            //被注解元素所在的Class
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            //Class的完整路径
            String classFullName = typeElement.getQualifiedName().toString();

            //收集Class中所有被注解的元素
            List<VariableInfo> variableList = classMap.get(classFullName);
            if (variableList == null) {
                variableList = new ArrayList<>();
                classMap.put(classFullName, variableList);

                //保存Class对应要素（名称、完整路径等）
                classTypeElement.put(classFullName, typeElement);
            }
            VariableInfo variableInfo = new VariableInfo();
            variableInfo.setVariableElement(variableElement);
            variableInfo.setViewId(viewId);
            variableList.add(variableInfo);
        }
    }

    private void writeToFile() {
        try {
            for (String classFullName : classMap.keySet()) {
                TypeElement typeElement = classTypeElement.get(classFullName);

                //构建Java源码
                //构建方法
                MethodSpec.Builder constructor = MethodSpec.constructorBuilder()//构造函数，在此处直接find元素
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(TypeName.get(typeElement.asType()), "activity").build());
                List<VariableInfo> variableList = classMap.get(classFullName);
                //遍历元素find
                for (VariableInfo variableInfo : variableList) {
                    VariableElement variableElement = variableInfo.getVariableElement();
                    //变量名
                    String variableName = variableElement.getSimpleName().toString();
                    //变量类型的完整类路径
                    String variableFullName = variableElement.asType().toString();
                    //find
                    constructor.addStatement("activity.$L=($L)activity.findViewById($L)", variableName, variableFullName, variableInfo.getViewId());
                }

                //构建Class
                TypeSpec typeSpec = TypeSpec.classBuilder(typeElement.getSimpleName() + className)
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(constructor.build())
                        .build();

                //与目标Class放在同一个包下使属性可访问
                String packageFullName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
                JavaFile javaFile = JavaFile.builder(packageFullName, typeSpec).build();
                // 生成class文件
                javaFile.writeTo(filer);
            }
        } catch (Exception ex) {
            System.out.println("文件生成失败");
            ex.printStackTrace();
        }
    }
}