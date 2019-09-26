import com.synopsys.arc.jenkins.plugins.ownership.*
import jenkins.model.*
import hudson.model.Cause
import com.synopsys.arc.jenkins.plugins.ownership.jobs.*

/*
Run this scrip as "System Groovy script of Freestyle project.
build is predefined parameter of Groovy plugin
For scrip console "import hudson.model.AbstractBuild"
*/

def startedUser = build.getCause(Cause.UserIdCause).getUserId();
OwnershipDescription jobOwner = new OwnershipDescription(true,startedUser);
JobOwnerHelper.setOwnership(Jenkins.getInstance().getItemByFullName(jobName),jobOwner);