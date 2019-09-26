import com.synopsys.arc.jenkins.plugins.ownership.nodes.*
import hudson.EnvVars
import jenkins.model.*

EnvVars vars = build.getEnvironment(listener);
def registeredLabels=vars.get("REGISTERED_LABELS")

Jenkins.instance.nodes.each {

    if(it.displayName.contains("-custom-")&&it.labelString.contains("gp")&&!it.labelString.contains("gp-"))println("Custom node "+it.displayName+" contains label \"gp\"")
    if(it.displayName.contains("-gp-")&&it.labelString.contains("custom")&&!it.labelString.contains("custom-"))println("GP node "+it.displayName+" contains label \"custom\"")

    String violationLabels=""
    def nodeLabelsArray=it.labelString.split(" ")

    //Check
    for(nodeSingleLabel in nodeLabelsArray){if(!registeredLabels.contains(nodeSingleLabel))violationLabels=violationLabels+nodeSingleLabel+" "}
    //Report
    if(violationLabels!=""){

        OwnerNodeProperty prop = NodeOwnerHelper.getOwnerProperty(it)
        String nodeOwnerEmail=prop.ownership.ownerEmail
        println("")
        println("Node "+it.displayName+" has unregistered labels:\n"+violationLabels+"\nContact node owner:\n"+nodeOwnerEmail)
    }
}