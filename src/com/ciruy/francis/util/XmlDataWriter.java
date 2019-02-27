package com.ciruy.francis.util;

import com.ciruy.francis.bean.ResIdBean;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by Ciruy on 2019/2/27.
 * Description:
 */
public class XmlDataWriter extends WriteCommandAction {

    private List<ResIdBean> resIdBeans;
    private PsiClass psiClass;
    private PsiFile psiFile;

    public XmlDataWriter(PsiFile psiFile, List<ResIdBean> resIdBeans, PsiClass psiClass) {
        this(psiFile.getProject(), psiFile);
        this.psiFile = psiFile;
        this.resIdBeans = resIdBeans;
        this.psiClass = psiClass;
    }

    private XmlDataWriter(@Nullable Project project, PsiFile... files) {
        super(project, files);
    }

    @Override
    protected void run(@NotNull Result result) {
        writeFindViews();
    }

    /**
     * 这里最大的问题就是如果出现重载现象就会出现很无语的问题，如果是java语言建议使用签名而并非方法名来区分每个方法，
     * 如果是其他基于JVM的语言，建议使用描述符进行方法之间的区分
     */
    private void writeFindViews() {
        StringBuilder method = new StringBuilder();
        String methodBegin = "";
        if (isActivity()) {
            methodBegin = "private void findViews(){";
        } else {
            methodBegin = "private void findViews(View view){";
        }
        String methodEnd = "}";
        PsiElementFactory psiElementFactory = PsiElementFactory.SERVICE.getInstance(psiFile.getProject());
        for (ResIdBean resIdBean : resIdBeans) {
            //添加BindByTag标签以及对应的编辑内容,这个内容仅仅是考虑在界面刚刚开始实现的时候进行使用
            if (psiClass.findFieldByName(resIdBean.getId(), false) == null) {
                String annotation = String.format("@BindByTag(\"%s\")\n", resIdBean.getTag());
                String field = annotation + resIdBean.getName() + " " + resIdBean.getTag() + ";";
                PsiField fieldElement = psiElementFactory.createFieldFromText(field, psiClass);
                psiClass.add(fieldElement);
            }
//            //添加findViews方法
//            //找到findViews方法
//            PsiMethod[] methods = psiClass.findMethodsByName("findViews", false);
//            PsiMethod findViewsMethod = methods.length > 0 ? methods[0] : null;
//            //如果当前类结构中并不存在findViews方法，则添加对应的类结构
//            if (findViewsMethod == null) {
//                psiClass.add(psiElementFactory.createMethodFromText((methodBegin + methodEnd), psiClass));
//            }
//            //此时确保方法已经存在，直接获取对应的结构句柄即可
//            methods = psiClass.findMethodsByName("findViews", false);
//            findViewsMethod = methods[0];
//            //对方法体内部进行操作
//            PsiCodeBlock body = findViewsMethod.getBody();
//            if (body != null && !body.getText().contains(resIdBean.getId())) {
//                //先构建类的内部填充内容到StringBuilder中
//                appendFindViewsMethodBody(method, resIdBean);
//            }
        }
//        //清空方法内容，并将新的内容填充到对应的方法体中
//        if (method.length() != 0) {
//            PsiMethod[] methods = psiClass.findMethodsByName("findViews", false);
//            PsiMethod findViewsMethod = methods.length > 0 ? methods[0] : null;
//            if (findViewsMethod != null) {
//                PsiCodeBlock body = findViewsMethod.getBody();
//                if (body != null) {
//                    StringBuilder codeBlock = new StringBuilder(body.getText());
//                    body.delete();
//                    codeBlock.insert(codeBlock.length() - 1, method.toString());
//                    findViewsMethod.add(psiElementFactory.createCodeBlockFromText(codeBlock.toString(), findViewsMethod));
//                }
//            }
//        }
        NotifyUtils.showInfo(psiFile.getProject(), "inject BindByTag code success");
    }

    /**
     * 在方法体重添加若干个findViewById操作
     *
     * @param method
     * @param resIdBean
     */
    private void appendFindViewsMethodBody(StringBuilder method, ResIdBean resIdBean) {
        String findViewById = "";
        if (isActivity()) {
            findViewById = "findViewById(";
        } else {
            findViewById = "view.findViewById(";
        }
        method.append(resIdBean.getId())
                .append(" = ")
                .append("(")
                .append(resIdBean.getName())
                .append(")")
                .append(findViewById)
                .append("R.id.")
                .append(resIdBean.getId())
                .append(");");
    }

    /**
     * 结构层级判断
     *
     * @return
     */
    private boolean isActivity() {
        GlobalSearchScope scope = GlobalSearchScope.allScope(psiFile.getProject());
        PsiClass activityClass = JavaPsiFacade.getInstance(psiFile.getProject()).findClass(
                "android.app.Activity", scope);
        return activityClass != null && psiClass.isInheritor(activityClass, false);
    }
}