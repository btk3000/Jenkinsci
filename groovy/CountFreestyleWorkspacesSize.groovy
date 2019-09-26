import hudson.FilePath
import jenkins.model.*

def us01wsToChechk=""
def de02wsToChechk=""
def ru20wsToChechk=""
def in01wsToChechk=""

for(def item : Jenkins.getInstance().getAllItems()){

    if(item instanceof hudson.model.FreeStyleProject && item.getLastBuild() != null) {
        /*Need to check only last build node of Freestyle project
        Other ws must be deleted by DiskUsagePolicy
         */
        def lastBuildNode = item.getLastBuild().getBuiltOn()
        //skip if node doesn`t exist anymore
        if(lastBuildNode==null)continue
        //skip if node is off-line
        if(lastBuildNode.getWorkspaceFor(item)==null)continue
        //no need to processing jobs without workspaces
        if(!lastBuildNode.getWorkspaceFor(item).exists())continue

        println("Processing Freestyle Job "+item.fullDisplayName)
        def wsPath = lastBuildNode.getWorkspaceFor(item).toString()
        println("Job workspace path: "+wsPath)


        if(wsPath.startsWith("/slowfs/us01dwt2p323"))us01wsToChechk=us01wsToChechk+wsPath+'\n'
        if(wsPath.startsWith("/slowfs/de02dwt2p114"))de02wsToChechk=de02wsToChechk+wsPath+'\n'
        if(wsPath.startsWith("/slowfs/ru20arcjenkins"))ru20wsToChechk=ru20wsToChechk+wsPath+'\n'
        if(wsPath.startsWith("/slowfs/in01dwt2p079"))in01wsToChechk=in01wsToChechk+wsPath+'\n'
    }
}

/*Creates empty files in job workspaces.
build is  predifined variable by Groovy plugin. Won`t work with Sript Console
 */
if (build.workspace.isRemote()) {
    channel = build.workspace.channel;
    us01ws = new FilePath(channel, build.workspace.toString() + "/us01_ws.prop");
    de02ws = new FilePath(channel, build.workspace.toString() + "/de02_ws.prop");
    ru20ws = new FilePath(channel, build.workspace.toString() + "/ru20_ws.prop");
    in01ws = new FilePath(channel, build.workspace.toString() + "/in01_ws.prop");
} else {
    us01ws = new FilePath(new File(build.workspace.toString() + "/us01_ws.prop"));
    de02ws = new FilePath(new File(build.workspace.toString() + "/de02_ws.prop"));
    ru20ws = new FilePath(new File(build.workspace.toString() + "/ru20_ws.prop"));
    in01ws = new FilePath(new File(build.workspace.toString() + "/in01_ws.prop"));

}

/*
Write into files and print results into console outpur
 */
println("Workspaces to be checked at US01: \n"+us01wsToChechk)
if (us01wsToChechk != null) {
    boolean success = us01ws.write(us01wsToChechk.toString(), "UTF8");
    println("Write to file result: " + success)
}

println("Workspaces to be checked at DE02: \n"+de02wsToChechk)
if (de02wsToChechk != null) {de02ws.write(de02wsToChechk.toString(), "UTF8");}

println("Workspaces to be checked at RU20: \n"+ru20wsToChechk)
if (ru20wsToChechk != null) {ru20ws.write(ru20wsToChechk.toString(), "UTF8");}

println("Workspaces to be checked at IN01: \n"+in01wsToChechk)
if (in01wsToChechk != null) {in01ws.write(in01wsToChechk.toString(), "UTF8");}
