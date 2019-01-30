package com.dqdana.code.ioc_processor;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * 用于生成 java 文件的类,
 * 主要包含 FindViewById 代码
 * 所以它需要保存类的信息、包名、完整类名以及其中的变量列表
 */
public class ProxyInfo {

    // 生成文件的后缀
    private static final String SUFFIX = "ViewInjector";
    // 存储一个类中, 所有被注解修饰的元素, id 为 ket , view 为 value
    public Map<Integer, VariableElement> mInjectElements = new HashMap<>();

    // 临时中转的类信息
    private TypeElement mTypeElement;
    // 临时中转的包名
    private String mPackageName;
    // 临时中转的代理类名
    private String mProxyClassName;

    /**
     * 构造器
     *
     * @param pElementUtils : 用于基于元素进行操作的工具类
     * @param pTypeElement  : 是一个接口, 代表着一个包、类、方法或者元素
     */
    public ProxyInfo(final Elements pElementUtils, final TypeElement pTypeElement) {
        mTypeElement = pTypeElement;

        PackageElement packageElement = pElementUtils.getPackageOf(pTypeElement);
        mPackageName = packageElement.getQualifiedName().toString();
        System.out.println("mPackageName: " + mPackageName);

        String className = getClassName(mPackageName, pTypeElement);
        System.out.println("className: " + className);
        mProxyClassName = className + "$$" + SUFFIX;
        System.out.println("mProxyClassName: " + mProxyClassName);
    }

    /**
     * 返回 包名$元素名
     */
    private String getClassName(String mPackageName, TypeElement pTypeElement) {
        int packageLength = mPackageName.length() + 1;
        String subStr = pTypeElement.getQualifiedName().toString().substring(packageLength);
        System.out.println("subStr: " + subStr);
        return subStr.replace('.', '$');
    }

    /**
     * 根据上面获得的动态信息, 就可以生成不同的类了
     * 活生生的用 string 拼出了一个类的代码 nb
     */
    public String generateJavaCode() {
        StringBuilder sb = new StringBuilder();

        sb.append("// Generate code. Do not modify it !\n")
                .append("package ").append(mPackageName).append(";\n\n")
                .append("import com.dqdana.code.ioc.*;").append(";\n\n")
                .append("public class ").append(mProxyClassName)
                .append(" implements ").append(SUFFIX)
                .append("<").append(mTypeElement.getQualifiedName()).append(">")
                .append("{\n");
        generateMethod(sb);
        sb.append("\n}");
        return sb.toString();
    }

    private void generateMethod(final StringBuilder stringBuilder) {
        if (stringBuilder == null) {
            return;
        }
        stringBuilder.append("@Override\n")
                .append("public void inject(").append(mTypeElement.getQualifiedName()).append(" host, Object object )").append("{\n");

        for (Integer id : mInjectElements.keySet()) {
            VariableElement variableElement = mInjectElements.get(id);
            String name = variableElement.getSimpleName().toString();
            String type = variableElement.asType().toString();
            stringBuilder.append("if(object instanceof android.app.Activity)").append("{\n")
                    .append("host.").append(name).append(" = ")
                    .append("(").append(type).append(")((android.app.Activity)object).findViewById(").append(id).append(");")
                    .append("\n}\n")
                    .append("else").append("{\n")
                    .append("host.").append(name).append(" = ")
                    .append("(").append(type).append(")((android.view.View)object).findViewById(").append(id).append(");")
                    .append("\n}\n");
        }
        stringBuilder.append("\n}\n");
    }

    public String getProxyClassFullName() {
        return mPackageName + "." + mProxyClassName;
    }

    public TypeElement getTypeElement() {
        return mTypeElement;
    }
}
