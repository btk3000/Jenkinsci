import jenkins.model.*;
import hudson.FilePath;
import org.jenkinsci.plugins.mailwatcher.WatcherNodeProperty;
import hudson.EnvVars;

EnvVars vars = build.getEnvironment(listener);
final boolean confirmed = Boolean.parseBoolean(vars.get("DISABLE_EMAIL_NOTIFICATIONS"));

def noEmailNotification = 0;
def nodesWithoutEmailNotifications = "";
def nodesEmailNotifications = "";

def allNodes=Jenkins.getInstance().getNodes();

/*
Collect email-addresses for all nodes.
 */

/*Creates empty files in job workspaces.
build is  predifined variable by Groovy plugin. Won`t work with Sript Console
 */
    if (build.workspace.isRemote()) {
        channel = build.workspace.channel;
        emailsProp = new FilePath(channel, build.workspace.toString() + "/node_emails.prop");
        noEmailsLog = new FilePath(channel, build.workspace.toString() + "/no_emails.log");
    } else {
        emailsProp = new FilePath(new File(build.workspace.toString() + "/node_emails.prop"));
        noEmailsLog = new FilePath(new File(build.workspace.toString() + "/no_emails.log"));
    }

/*Scan all nodes email properties and it into script variables.
 */
    allNodes.each {
        if (!it.getNodeProperties().toString().contains("WatcherNodeProperty")) {
            noEmailNotification++;
            nodesWithoutEmailNotifications <<= "\n" + it.getDisplayName();
        }
        def nodeName = it.getDisplayName();
        it.getNodeProperties().each {
            if (it instanceof WatcherNodeProperty) {
                nodesEmailNotifications <<= nodeName + "_OFFLINE_ADDRESS=" + it.getOfflineAddresses() + "\n";
                nodesEmailNotifications <<= nodeName + "_ONLINE_ADDRESS=" + it.getOnlineAddresses() + "\n";
            }
        };
    }
/*
Write into files and print results into console outpur
 */
    println nodesEmailNotifications;
    if (emailsProp != null) {
        boolean success = emailsProp.write(nodesEmailNotifications.toString(), "UTF8");
        println("Write to file result: " + success)
    }

    println("Number of nodes without e-mail notifications: " + noEmailNotification.toString() + nodesWithoutEmailNotifications.toString());
    if (noEmailsLog != null) {
        boolean success = noEmailsLog.write("Number of nodes without e-mail notifications: " + noEmailNotification + nodesWithoutEmailNotifications, "UTF8");
        println("Write to file result: " + success)
    }

/*
Disable email-addresses for all nodes.
 */
if(confirmed){
for(arcjenkinsNode in allNodes){
    for(arcjenkinsNodeProperty in arcjenkinsNode.getNodeProperties()){
        if (arcjenkinsNodeProperty instanceof WatcherNodeProperty){
            def wnpDescriptor = arcjenkinsNodeProperty.getDescriptor();
            arcjenkinsNode.getNodeProperties().remove(wnpDescriptor)
        }
    }
}
}
