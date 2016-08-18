class VersionUpdateGrailsPlugin extends grails.plugins.Plugin {
    def grailsVersion = "3.0.0 > *"
    def title = "Version Update"
    def author = "Mathias Fonseca"
    def authorEmail = "mathifonseca@gmail.com"
    def description = "Provides a more friendly way to update your application or plugin version."
    def documentation = "http://grails.org/plugin/version-update"
    def license = "APACHE"
    def issueManagement = [ system: "GITHUB", url: "https://github.com/mathifonseca/grails-version-update/issues" ]
    def scm = [ url: "https://github.com/mathifonseca/grails-version-update" ]
}
