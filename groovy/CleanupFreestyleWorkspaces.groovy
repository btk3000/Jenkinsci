import groovy.time.TimeCategory
import jenkins.model.*

for(def item : Jenkins.getInstance().getAllItems()){

    if(item instanceof hudson.model.FreeStyleProject && item.getLastBuild() != null) {

        def lastBuildNode = item.getLastBuild().getBuiltOn()
        def countOfWorkspaces = 0
        def today = new Date() + 1
        def twoDaysBefore = use(TimeCategory) {
            def dateBefore = today - 7.days
            return  dateBefore
        }

        for (def jenkinsNode : Jenkins.getInstance().getNodes()) {
            if (jenkinsNode.equals(lastBuildNode)) continue
            if (jenkinsNode.getWorkspaceFor(item) == null) continue
            if (!jenkinsNode.getWorkspaceFor(item).exists()) continue

            def wsDate = new Date(jenkinsNode.getWorkspaceFor(item).lastModified())
            if(twoDaysBefore<wsDate)continue

            countOfWorkspaces++
            jenkinsNode.getWorkspaceFor(item).deleteRecursive()
        }
        if (countOfWorkspaces > 0) println item.fullDisplayName
    }
}