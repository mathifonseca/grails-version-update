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

	def defaults = 
		[
			depth : 3,
			separator : '.',
			increase : '+',
			decrease : '-',
			keep : 'x'
		]

	int depth = Integer.parseInt(getConfig(config, 'depth', defaults.depth))
	String separator = getConfig(config, 'separator', defaults.separator)
	String increase = getConfig(config, 'increase', defaults.increase)
	String decrease = getConfig(config, 'decrease', defaults.decrease)
	String keep = getConfig(config, 'keep', defaults.keep)

	if (!depth || depth < 1) {
		depth = depth
	}

	println "Using version format: " + expectedVersionFormat(depth, separator)

	println "Verifying parameters..."

	def param = argsMap.params[0]

	if (!param) {
		param = defaultCommand(depth, separator, keep, increase)
		println 'No parameters, using default: ' + param
	}

	boolean paramsOk = verifyFormat(depth, param, separator, increase, decrease, keep)

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

	boolean currentFormatOk = verifyFormat(depth, appVersion, separator, increase, decrease, keep)

	if (!currentFormatOk) {
		System.err.println 'Current application version does not comply with expectations!'
		return
	}

	def newVersion = calculateNewVersion(appVersion, param, separator, increase, decrease, keep)

	boolean newFormatOk = verifyFormat(depth, newVersion, separator, increase, decrease, keep)

	if (!newVersion || !newFormatOk) {
		System.err.println 'Could not calculate new application version!'
		return
	} else {
		println "New application version -> " + newVersion
	}

	def versionChanged = setAppVersion(newVersion)

	if (versionChanged) {
		println "| OK - Application version set successfully!"
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

private String defaultCommand(depth, separator, keep, increase) {
	String result = ""
	depth.times {
		def dot = it == depth - 1 ? '' : separator
		result += (it == depth - 1 ? increase : keep) + dot
	}
	return result
}

private String expectedVersionFormat(int depth, String separator) {
	String result = ""
	int ascii = 122
	depth.times { 
		char c = (char) ascii
		def dot = it == depth - 1 ? '' : separator
		result += c.toString() + dot
		ascii--
	}
	return result
}

private boolean verifyFormat(int depth, String v, String separator, String increase, String decrease, String keep) {
	if (!v) return false
	if (v.endsWith(separator)) return false
	def regex = /^([0-9A-Za-z$increase$decrease$keep]+[\$separator]?){$depth}$/
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

private String calculateNewVersion(String current, String change, String separator, String increase, String decrease, String keep) {

	def result = ""

	try {

		def currentArray = current.tokenize(separator)
		def changeArray = change.tokenize(separator)

		currentArray.eachWithIndex { v, index -> 

			Integer currentValue = Integer.parseInt(v) 

			def applyChange = changeArray[index]

			Integer changedValue = currentValue

			if (applyChange == increase) {

				changedValue = currentValue + 1

			} else if (applyChange == decrease) {

				changedValue = currentValue - 1 < 0 ? 0 : currentValue - 1

			} else if (applyChange == keep) {

				changedValue = currentValue

			} else {

				changedValue = Integer.parseInt(applyChange.toString())

			}
			
			result += changedValue.toString() + separator

		}

		result = result.substring(0, result.size()-1)

	} catch (Exception ex) {
		result = null
	}

	return result

}