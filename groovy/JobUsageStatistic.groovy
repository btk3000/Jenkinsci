import jenkins.model.*
import groovy.time.TimeCategory
import hudson.EnvVars

EnvVars vars = build.getEnvironment(listener);
def zabbixHostName=vars.get("JENKINS_MASTER_ZABBIX_HOST")
def zabbixSender=vars.get("ZABBIX_SENDER_PATH");
def zabbixServer='-z '+vars.get("ZABBIX_SERVER");

void sendToZabbix(def zabbixServer, def zabbixSender, def host, def item, def value){
    def zabbixHost='-s '+host
    def zabbixItem='-k '+item
    def zabbixItemValue='-o '+value
    def sendDataToZabbix=zabbixSender+' '+zabbixServer+' '+zabbixHost+' '+zabbixItem+' '+zabbixItemValue
    println(sendDataToZabbix.execute().text)
}

println("#BEGIN SECTION Common statistic#")
def allItems = Jenkins.getInstance().getAllItems();
println("Total Items count: "+allItems.size())


int foldersCount=0;
int multiBranchCount=0;
int freestyleCount=0;
int pipelineCount=0;
int matrixCount=0;
int matrixConfigCount=0;
for(def item : allItems){
    if(item instanceof com.cloudbees.hudson.plugins.folder.Folder)foldersCount++;
    if(item instanceof org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject)multiBranchCount++;
    if(item instanceof hudson.model.FreeStyleProject)freestyleCount++;
    if(item instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob)pipelineCount++;
    if(item instanceof hudson.matrix.MatrixProject)matrixCount++;
    if(item instanceof hudson.matrix.MatrixConfiguration)matrixConfigCount++;
}

println("Count of folders : "+foldersCount);
println("Count of Multibranch Pipelines : "+multiBranchCount);
println("Count of Freestyle projects : "+freestyleCount);
println("Count of Matrix projects : "+matrixCount);
println("Count of MatrixConfigurations : "+matrixConfigCount);
println("Count of Pipeline projects : "+pipelineCount);

int totalCount=foldersCount+multiBranchCount+freestyleCount+pipelineCount+matrixCount+matrixConfigCount
println("Debug : "+totalCount);
println("#END SECTION#")

def acceptedFormat = "yyyy-MM-dd"
def today = new Date() + 1

def oneMonthBefore = use(TimeCategory) {
    def monthBefore = today - 3.month
    return  monthBefore
}

println("#BEGIN SECTION Freestyle jobs cleanup section#")
int freestyleOlder = 0;
int freestyleYounger = 0;
int freestyleNoBuilds = 0;
for(def item : allItems) {
    if (item instanceof hudson.model.FreeStyleProject) {
        def checkItem = item.getLastBuild();
        if (checkItem == null) {
            freestyleNoBuilds++
//      allItems.get(i).disable()
        } else {
            jobsDate = Date.parse("yyyy-MM-dd hh:mm:ss", checkItem.getTime().format("YYYY-MM-dd HH:mm:ss"))
            if (jobsDate > oneMonthBefore) freestyleOlder++
            else {
                println("Processing builds for: " + item.getDisplayName());
                item.getBuilds().each {
                    if (it.equals(item.getLastSuccessfulBuild())) println("Last SUCCESS build is " + it.getNumber() + ". Keep it.");
                    else {
                        println("Deleting build #" + it.getNumber() + ".");
//                        it.delete()
                    }
                }
                freestyleYounger++
            }

        }
    }
}
println("#END SECTION#")

println("#BEGIN SECTION Freestyle jobs statistic section#")
println("Count of Freestyle jobs older than "+oneMonthBefore+"is : "+freestyleOlder);
println("Count of Freestyle jobs younger than "+oneMonthBefore+"is : "+freestyleYounger);
println("Count of Freestyle jobs without builds is : "+freestyleNoBuilds);
println("#END SECTION#")

println("#BEGIN SECTION Pipeline jobs cleanup section#")
int pipelineOlder = 0;
int pipelineYounger = 0;
int pipelineNoBuilds = 0;
for(def item : allItems) {
    if(item instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob){
        def checkItem=item.getLastBuild();
        if(checkItem==null){pipelineNoBuilds++
//      allItems.get(i).disable()
        }
        else {
            jobsDate = Date.parse("yyyy-MM-dd hh:mm:ss", checkItem.getTime().format("YYYY-MM-dd HH:mm:ss"))
            if (jobsDate > oneMonthBefore) pipelineOlder++
            else {
                println("Processing builds for: " + item.getDisplayName());
                item.getBuilds().each {
                    if (it.equals(item.getLastSuccessfulBuild())) println("Last SUCCESS build is " + it.getNumber() + ". Keep it.");
                    else {
                        println("Deleting build #" + it.getNumber() + ".");
//                        it.delete()
                    }
                }
                pipelineYounger++
            }
        }
    }
}
println("#END SECTION#")

println("#BEGIN SECTION Pipeline jobs statistc section#");
println("Count of Pipeline jobs older than "+oneMonthBefore+"is : "+pipelineOlder);
println("Count of Pipeline jobs younger than "+oneMonthBefore+"is : "+pipelineYounger);
println("Count of Pipeline jobs without builds is : "+pipelineNoBuilds);
println("#END SECTION#")

println("#BEGIN SECTION Matrix jobs cleanup section#");
int matrixOlder = 0;
int matrixYounger = 0;
int matrixNoBuilds = 0;
for(def item : allItems) {
    if(item instanceof hudson.matrix.MatrixProject){
        def checkItem=item.getLastBuild();
        if(checkItem==null){matrixNoBuilds++
//      allItems.get(i).disable()
        }
        else{jobsDate = Date.parse("yyyy-MM-dd hh:mm:ss", checkItem.getTime().format("YYYY-MM-dd HH:mm:ss"))
            if(jobsDate>oneMonthBefore)matrixOlder++
            else {
                println("Processing builds for: " + item.getDisplayName());
                item.getBuilds().each {
                    if(it.equals(item.getLastSuccessfulBuild())) println("Last SUCCESS build is "+it.getNumber()+". Keep it.");
                    else {
                        println('Deleting build #'+it.getNumber()+'.');
                        it.delete()
                    }
                }
                matrixYounger++
            }
        }
    }
}
println("#END SECTION#")

println("#BEGIN SECTION Matrix jobs statistic section#");
println("Count of Matrix jobs older than "+oneMonthBefore+"is : "+matrixOlder);
println("Count of Matrix jobs younger than "+oneMonthBefore+"is : "+matrixYounger);
println("Count of Matrix jobs without builds is : "+matrixNoBuilds);
println("#END SECTION#")

println("#BEGIN SECTION Send to Zabbix section#");
sendToZabbix(zabbixServer,zabbixSender,zabbixHostName,'items_count',allItems.size())
sendToZabbix(zabbixServer,zabbixSender,zabbixHostName,'folder_count',foldersCount)
sendToZabbix(zabbixServer,zabbixSender,zabbixHostName,'multibranch_count',multiBranchCount)
sendToZabbix(zabbixServer,zabbixSender,zabbixHostName,'freestyle_count',freestyleCount)
sendToZabbix(zabbixServer,zabbixSender,zabbixHostName,'matrix_count',matrixCount)
sendToZabbix(zabbixServer,zabbixSender,zabbixHostName,'matrixconfigurations_count',matrixConfigCount)
sendToZabbix(zabbixServer,zabbixSender,zabbixHostName,'pipeline_count',pipelineCount)

println("#END SECTION#")
