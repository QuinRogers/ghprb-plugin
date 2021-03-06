package org.jenkinsci.plugins.ghprb;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.ghprb.extensions.GhprbExtension;
import org.jenkinsci.plugins.ghprb.extensions.GhprbExtensionDescriptor;
import org.jenkinsci.plugins.ghprb.extensions.comments.GhprbBuildResultMessage;
import org.jenkinsci.plugins.ghprb.extensions.comments.GhprbBuildStatus;
import org.jenkinsci.plugins.ghprb.extensions.comments.GhprbCommentFile;
import org.jenkinsci.plugins.ghprb.extensions.status.GhprbSimpleStatus;
import org.kohsuke.github.GHCommitState;

import antlr.ANTLRException;
import hudson.model.AbstractProject;
import hudson.triggers.Trigger;
import hudson.util.DescribableList;


public abstract class GhprbTriggerBackwardsCompatible extends Trigger<AbstractProject<?, ?>> {
    
    public abstract DescribableList<GhprbExtension, GhprbExtensionDescriptor> getExtensions();
    

    protected Integer configVersion;

    public GhprbTriggerBackwardsCompatible(String cron) throws ANTLRException {
        super(cron);
    }
    

    @Deprecated
    protected transient String commentFilePath;
    @Deprecated
    protected transient String msgSuccess;
    @Deprecated
    protected transient String msgFailure;
    @Deprecated 
    protected transient String commitStatusContext;
    
    
    protected void convertPropertiesToExtensions() {
        if (configVersion == null) {
            configVersion = 0;
        }
        
        checkCommentsFile();
        checkBuildStatusMessages();
        checkCommitStatusContext();
        
        configVersion = 2;
    }
    
    private void checkBuildStatusMessages() {
        if (!StringUtils.isEmpty(msgFailure) || !StringUtils.isEmpty(msgSuccess)) {
            List<GhprbBuildResultMessage> messages = new ArrayList<GhprbBuildResultMessage>(2);
            if (!StringUtils.isEmpty(msgFailure)) {
                messages.add(new GhprbBuildResultMessage(GHCommitState.FAILURE, msgFailure));
                msgFailure = null;
            }
            if (!StringUtils.isEmpty(msgSuccess)) {
                messages.add(new GhprbBuildResultMessage(GHCommitState.SUCCESS, msgSuccess));
                msgSuccess = null;
            }
            addIfMissing(new GhprbBuildStatus(messages));
        }
    }

    private void checkCommentsFile() {
        if (!StringUtils.isEmpty(commentFilePath)) {
            GhprbCommentFile comments = new GhprbCommentFile(commentFilePath);
            addIfMissing(comments);
            commentFilePath = null;
        }
    }
    
    private void checkCommitStatusContext() {
        if (configVersion < 1) {
            GhprbSimpleStatus status = new GhprbSimpleStatus(commitStatusContext);
            addIfMissing(status);
        }
    }
    
    protected void addIfMissing(GhprbExtension ext) {
        if (getExtensions().get(ext.getClass()) == null) {
            getExtensions().add(ext);
        }
    }

    
}
