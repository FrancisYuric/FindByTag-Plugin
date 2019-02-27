package com.ciruy.francis;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;

public class GenerateXmlTagAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        // TODO: insert action logic here
        Editor editor = anActionEvent.getData(DataKeys.EDITOR);
        PsiFile psiFile = anActionEvent.getData(DataKeys.PSI_FILE);

        if (editor != null && psiFile != null) {
        }
    }
}
