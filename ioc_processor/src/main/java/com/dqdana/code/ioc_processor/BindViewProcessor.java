package com.dqdana.code.ioc_processor;

import com.dqdana.code.ioc_annotation.BindView;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@AutoService(Processor.class) // 帮我们生成 META-INF 信息
//@SupportedAnnotationTypes("com.dqdana.code.ioc_annotation.BindView") // 要处理的注解类型
//@SupportedSourceVersion(SourceVersion.RELEASE_7) // 支持的源码版本
public class BindViewProcessor extends AbstractProcessor {

    /**
     * 不复写也可以, 但需要使用上面注释的注解
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new LinkedHashSet<>();
        annotationTypes.add(BindView.class.getCanonicalName());
        return annotationTypes;
    }

    /**
     * 不复写也可以, 但需要使用上面注释的注解
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private Elements mElementUtils;
    private Filer mFilerCreator;
    private Messager mMessager;
    // 保存 元素所在的类 的信息集合
    private Map<String, ProxyInfo> mProxyMap = new HashMap<>();

    /**
     * 参数 processingEnvironment 是注解处理环境，通过它可以获取很多功能类
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mElementUtils = processingEnvironment.getElementUtils(); // 返回用于基于元素进行操作的工具类
        mFilerCreator = processingEnvironment.getFiler(); // 返回用于创建 Java 文件、Class 文件或者其他辅助文件的文件创建者
        mMessager = processingEnvironment.getMessager(); // 返回信息传递者，用来报告错误、警告灯信息
    }

    /**
     * process() 主要做以下工作
     * 1. 收集信息
     * 2. 生成代码
     *
     * @param roundEnvironment : 是提供一个注解处理器，在编译时可以查询类的信息
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, "process...");

        // 避免生成重复的代理类
        mProxyMap.clear();

        // 拿到被 @BindView 注解修饰的所有元素 应该是 VariableElement
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        // 1. 收集信息
        for (Element element : elements) {
            // 去除不合格的元素
            if (!checkAnnotationValid(element)) {
                continue;
            }
            // 获取类中的成员变量
            VariableElement variableElement = (VariableElement) element;
            // 获取成员变量所在的,类或者接口的信息
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            // 获取类的完整名称
            String qualifiedName = typeElement.getQualifiedName().toString();

            ProxyInfo proxyInfo = mProxyMap.get(qualifiedName);
            if (proxyInfo == null) {
                // 将该类中被注解修饰的变量加入到 ProxyInfo 中
                proxyInfo = new ProxyInfo(mElementUtils, typeElement);
                mProxyMap.put(qualifiedName, proxyInfo);
            }

            // 根据注解, 获取 VIEW_ID
            BindView annotation = variableElement.getAnnotation(BindView.class);
            if (annotation != null) {
                int id = annotation.value();
                proxyInfo.mInjectElements.put(id, variableElement);
            }
        }

        // 2. 生成代理类
        for (String key : mProxyMap.keySet()) {
            ProxyInfo proxyInfo = mProxyMap.get(key);
            try {
                // 创建文件对象
                JavaFileObject sourceFile = mFilerCreator.createSourceFile(
                        proxyInfo.getProxyClassFullName(), proxyInfo.getTypeElement()
                );
                Writer writer = sourceFile.openWriter();
                writer.write(proxyInfo.generateJavaCode()); // 写入文件
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                error(proxyInfo.getTypeElement(), "Unable to write injector for type %s: %s",
                        proxyInfo.getTypeElement(), e.getMessage());
            }
        }

        return true;
    }

    /**
     * 检查 element 类型是否规范
     */
    private boolean checkAnnotationValid(final Element element) {
        if (element == null || element.getKind() != ElementKind.FIELD) {
            error(element, "%s must be declared on field !", BindView.class.getSimpleName());
            return false;
        }
        if (!element.getModifiers().contains(Modifier.PUBLIC)) {
            error(element, "%s must be public !", element.getSimpleName());
            return false;
        }
        return true;
    }

    /**
     * 打印错误信息
     */
    private void error(final Element element, String msg, final Object... args) {
        if (args != null && args.length > 0) {
            msg = String.format(msg, args);
            mMessager.printMessage(Diagnostic.Kind.ERROR, msg, element);
        }
    }
}