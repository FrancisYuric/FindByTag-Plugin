package com.ciruy.francis;

import com.ciruy.francis.bean.ResIdBean;
import com.ciruy.francis.util.ClassDataWriter;
import com.ciruy.francis.util.PsiFileUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;

/**
 * Created by Ciruy on 2019/2/27.
 * Description:
 */
public class FindByTagAction extends AnAction {

    //当点击generate中的findViewById时出发对应的action
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        //触发action的编辑器
        Editor editor = anActionEvent.getData(DataKeys.EDITOR);
        //触发action的文件结构
        PsiFile psiFile = anActionEvent.getData(DataKeys.PSI_FILE);
        //获取选中元素
        PsiElement psiElement = PsiFileUtils.getPsiElementByEditor(editor, psiFile);
        if (editor != null && psiFile != null && psiElement != null) {
            String name = String.format("%s.xml", psiElement.getText());
//            NotifyUtils.showError(psiElement.getProject(),"psiElementName:"+name+"\n");
            //通过文件名获取文件
            PsiFile rootXmlFile = PsiFileUtils.getFileByName(psiElement, name);
            if (rootXmlFile != null) {
                ArrayList<ResIdBean> resIdBeans = new ArrayList<>();
                PsiFileUtils.getResIdBeans(rootXmlFile, resIdBeans);
                //获取当前类文档
                PsiClass psiClass = PsiFileUtils.getClassByClassFile(psiFile);

                //修改当前类文档信息
                new ClassDataWriter(psiFile, resIdBeans, PsiFileUtils.getParentClass(psiClass, psiElement)).execute();
            }
        }
    }

//    private PsiClass getParentPsiClass(PsiClass psiClass,PsiElement psiElement) {
//        if(psiClass.getAllInnerClasses().length <=0) return psiClass;
//        for(int i = 0; i < psiClass.getAllInnerClasses().length;i++) {
//            if(psiClass.getAllInnerClasses()[i].getParent().getTextOffset()<)
//        }
//    }
}