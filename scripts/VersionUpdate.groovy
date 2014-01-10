import grails.util.GrailsUtil

includeTargets << grailsScript('_GrailsInit')
includeTargets << grailsScript('_GrailsCompile')

target(versionUpdate: "Update application version") {
	depends(compile)
	runVersionUpdate()
}

private void runVersionUpdate() {

	println 'Updating application version...'

	def configClassName = getBindingValueOrDefault('configClassname', 'BuildConfig')

	def config = loadConfig(configClassName)

	int depth = Integer.parseInt(getConfig(config, 'depth', 3))

	if (!depth || depth < 1) {
		depth = depth
	}

	String separator = getConfig(config, 'separator', '.')

	println "Using version format: " + expectedVersionFormat(depth, separator)

	println "Verifying parameters..."

	def param = argsMap.params[0]

	boolean paramsOk = verifyFormat(depth, param, separator)

	if (paramsOk) {
		println 'Parameter is correct!'
	} else {
		System.err.println "Parameter is NOT in the expected format!"
		return
	}

	def appVersion = getAppVersion()

	if (appVersion) {
		println "Old application version -> " + appVersion
	} else {
		System.err.println 'Could not get application version!'
		return
	}

	boolean currentFormatOk = verifyFormat(depth, appVersion, separator)

	if (!currentFormatOk) {
		System.err.println 'Current application version does not comply with expectations!'
		return
	}

	def newVersion = calculateNewVersion(appVersion, param, separator)

	boolean newFormatOk = verifyFormat(depth, newVersion, separator)

	if (!newVersion || !newFormatOk) {
		System.err.println 'Could not calculate new application version!'
		return
	} else {
		println "New application version -> " + newVersion
	}

	def versionChanged = setAppVersion(newVersion)

	if (versionChanged) {
		println "Application version set successfully!"
	} else {
		System.err.println 'Could not set new application version!'
	}

}

setDefaultTarget(versionUpdate)

private ConfigObject loadConfig(String className) {
    def classLoader = Thread.currentThread().contextClassLoader
	classLoader.addURL(new File(classesDirPath).toURL())
    try {
		def parser = getBindingValueOrDefault('configParser', { name -> return new ConfigSlurper(GrailsUtil.environment).parse(classLoader.loadClass(className)) })
		return parser(className).versionupdate
    } catch (ClassNotFoundException e) {
        return new ConfigObject()
    }
}

private getBindingValueOrDefault(String varName, Object defaultValue) {
    def variables = getBinding().getVariables()
    return variables.containsKey(varName) ? getProperty(varName) : defaultValue
}

private String getConfig(config, String name, Object defaultIfMissing) {
	return config[name] ?: defaultIfMissing
}

private String expectedVersionFormat(int depth, String separator) {
	String result = ""
	int ascii = 90
	depth.times { 
		char c = (char) ascii
		def dot = it == depth - 1 ? '' : separator
		result += c.toString() + dot
		ascii--
	}
	return result
}

private boolean verifyFormat(int depth, String v, String separator) {
	if (!v) return false
	if (v.endsWith(separator)) return false
	def regex = /^([0-9]+[\.]?){$depth}$/
	return v ==~ regex
}

private String getAppVersion() {

	def applicationPropertiesFile = "${basedir}/application.properties" as File
	def applicationProperties = new Properties()
	applicationProperties.load(applicationPropertiesFile.newReader())
	return applicationProperties['app.version']

}

private boolean setAppVersion(version) {

	boolean result = true

	try {

		def applicationPropertiesFile = "${basedir}/application.properties" as File
		def props = new Properties()
		props.load(new FileInputStream(applicationPropertiesFile))
		props['app.version']=version.toString()
		props.store(new FileOutputStream(applicationPropertiesFile), 'Grails Metadata file')

	} catch (Exception ex) {
		result = false
	}

	return result

}

private String calculateNewVersion(String current, String change, String separator) {

	def currentArray = current.tokenize(separator)
	def changeArray = change.tokenize(separator)

	def result = ""

	currentArray.eachWithIndex { v, index -> 

		int nv = Integer.parseInt(v) + Integer.parseInt(changeArray[index])

		result += nv.toString() + separator

	}

	result = result.substring(0, result.size()-1)

	return result

}