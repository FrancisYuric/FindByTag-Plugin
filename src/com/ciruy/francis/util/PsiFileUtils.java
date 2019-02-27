package com.ciruy.francis.util;

import com.ciruy.francis.bean.ResIdBean;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;

import java.util.ArrayList;

/**
 * Created by Ciruy on 2019/2/27.
 * Description:
 */
public class PsiFileUtils {
    //从代码编辑界面中获取选中的文字信息
    public static PsiElement getPsiElementByEditor(Editor editor, PsiFile psiFile) {
        //健壮性操作
        if (editor == null || psiFile == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        //选中文字信息
        CaretModel caret = editor.getCaretModel();
        stringBuilder.append(caret.getOffset() + "\n");
        PsiElement psiElement = psiFile.findElementAt(caret.getOffset());

        if (psiElement != null) {
            stringBuilder.append(psiElement.getParent().getText() + "\n");
            if (psiElement.getParent().getText().startsWith("R.layout.")) {
                stringBuilder.append(psiElement.getParent().getText() + "\n");
                stringBuilder.append(psiElement.getText() + "\n");
//                NotifyUtils.showError(psiFile.getProject(), stringBuilder.toString()+"\n"+"Layout Found!");
                return psiElement;
            }
        }
        //当未找到layout信息时，提出报错信息，并返回null值
        NotifyUtils.showError(psiFile.getProject(), stringBuilder.toString() + "\n" + "No Layout Found");
        return null;
    }

    /**
     * 根据名称获取对应的xml文件
     *
     * @param psiElement
     * @param fileName
     * @return
     */
    public static PsiFile getFileByName(PsiElement psiElement, String fileName) {
        StringBuilder stringBuilder = new StringBuilder().append("fileName:" + fileName + "\n");
        Module moduleForPsiElement = ModuleUtil.findModuleForPsiElement(psiElement);
        stringBuilder.append("moduleName:" + moduleForPsiElement.getName() + "\n");
        if (moduleForPsiElement != null) {
            GlobalSearchScope searchScope = GlobalSearchScope.moduleScope(moduleForPsiElement);
            Project project = psiElement.getProject();
            //递归剪枝操作
            stringBuilder.append("projectName:" + psiElement.getProject().getName() + "\n");
            PsiFile[] psiFilesForModule = FilenameIndex.getFilesByName(project, fileName, searchScope);
            if (psiFilesForModule.length != 0) {
                return psiFilesForModule[0];
            }

            PsiFile[] psiFilesForProject = FilenameIndex.getFilesByName(project, fileName, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(moduleForPsiElement));
            if (psiFilesForProject.length != 0) {
                return psiFilesForProject[0];
            }
        }
        NotifyUtils.showError(psiElement.getProject(), stringBuilder.append("No Layout Found").toString());
        return null;
    }

    public static final void autoGenXmlTag(PsiFile psiFile) {
        if (psiFile != null && psiFile.getName().endsWith(".xml")) {
            psiFile.accept(new XmlRecursiveElementVisitor(true) {
                @Override
                public void visitXmlTag(XmlTag tag) {
                    super.visitXmlTag(tag);
                    if (!tag.getName().equals("include")) {
                        XmlAttribute attribute = tag.getAttribute("android:id");
                        XmlAttribute attribute1 = tag.getAttribute("android:tag");
                    }
                }
            });
        }
    }

    /**
     * 递归获取id和name键值对信息
     *
     * @param psiFile
     * @param container
     */
    public static void getResIdBeans(PsiFile psiFile, ArrayList<ResIdBean> container) {
        //不希望当psiFile取消调用的时候取消操作
        if (psiFile == null) return;
        psiFile.accept(new XmlRecursiveElementVisitor(true) {
            @Override
            public void visitXmlTag(XmlTag tag) {
                super.visitXmlTag(tag);
                //当遇到include tag时，递归便利寻找对应的tag信息
                if (tag.getName().equals("include")) {
                    //获取到布局中对应的layout属性信息用于后续遍历操作,仅仅是作为后续遍历的桥梁而出现
                    XmlAttribute layout = tag.getAttribute("layout");
                    if (layout != null) {
                        String value = layout.getValue();
                        if (value != null && value.startsWith("@layout/")) {
                            String[] split = value.split("/");
                            //布局设计处于未完成状态
                            if (split.length <= 1) {
                                NotifyUtils.showError(psiFile.getProject(), "布局定义不完全！");
                                return;
                            }
                            //获取xml文件名称，并且根据名称获取到xml文件对应的psiFile结构文件
                            String xmlName = String.format("%s.xml", split[1]);
                            PsiFile fileByName = PsiFileUtils.getFileByName(psiFile, xmlName);
                            if (fileByName != null)
                                getResIdBeans(fileByName, container);
                            else {
                                NotifyUtils.showError(psiFile.getProject(), String.format("未找到名称为%s的布局文件", xmlName));
                            }
                        }
                    }
                } else {
                    //如果不是include类型的tag就是普通的布局类型tag，那么只需要进行结构的遍历即可
                    XmlAttribute attribute = tag.getAttribute("android:id");
                    XmlAttribute attributeTag = tag.getAttribute("android:tag");
                    if (attribute != null && attributeTag != null) {
                        //获取到整个id的字符串
                        String idValue = attribute.getValue();
                        String tagValue = attributeTag.getValue();
                        if (idValue != null && idValue.startsWith("@+id/") && tagValue != null) {
                            String[] split = idValue.split("/");
                            String id = split[1];
                            String className;
                            String canonicalName;
                            //如果是自定义视图，则获取对应的SimpleName和通用名
                            //如果是android原生的控件，系统会自动指定prefix前缀android.widget，所以在xml中表示的时候只需要指定simpleName就行了
                            if (tag.getName().contains(".")) {
                                canonicalName = tag.getName();
                                String[] custom = tag.getName().split("\\.");
                                className = custom[custom.length - 1];
                            } else {
                                className = tag.getName();
                                canonicalName = "android.widget." + className;
                            }
                            //将获取的结构储存到container中
                            container.add(new ResIdBean(className, canonicalName, id, tagValue));
                        }
                    }
                }
            }
        });
    }

    public static PsiClass getClassByClassFile(PsiFile classFile) {
        GlobalSearchScope globalSearchScope = GlobalSearchScope.fileScope(classFile);
        String fullName = classFile.getName();
        String className = fullName.split("\\.")[0];
        return PsiShortNamesCache.getInstance(classFile.getProject()).getClassesByName(className, globalSearchScope)[0];
    }


    public static PsiClass getClassByName(PsiFile classFile, String name) {
        GlobalSearchScope globalSearchScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(ModuleUtil.findModuleForFile(classFile));
        PsiClass[] psiClasses = PsiShortNamesCache.getInstance(classFile.getProject()).getClassesByName(name, globalSearchScope);
        if (psiClasses.length > 0)
            return PsiShortNamesCache.getInstance(classFile.getProject()).getClassesByName(name, globalSearchScope)[0];
        return null;
    }

    public static PsiClass getParentClass(PsiClass oriClass, PsiElement psiElement) {
        PsiElement curElement = psiElement;
        while (curElement != oriClass && !(curElement instanceof PsiClass)) {
            curElement = curElement.getParent();
        }
        return (PsiClass) curElement;
    }

}
