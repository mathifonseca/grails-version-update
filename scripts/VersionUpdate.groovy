import grails.util.GrailsUtil

target(versionUpdate: "Update application or plugin version") {
	runVersionUpdate()
}

setDefaultTarget(versionUpdate)

private void runVersionUpdate() {

	println 'Updating version...'

	def configClassName = getBindingValueOrDefault('configClassname', 'BuildConfig')

	def config = loadConfig(configClassName)

	def defaults = 
		[
			depth : 3,
			separator : '.',
            label : '-',
			increase : '+',
			decrease : '-',
			keep : 'x',
			major : 'M',
			minor : 'm',
			patch : 'p'
		]

	Integer depth = Integer.parseInt(getConfig(config, 'depth', defaults.depth))
	String separator = getConfig(config, 'separator', defaults.separator)
    String label = getConfig(config, 'label', defaults.label)
	String increase = getConfig(config, 'increase', defaults.increase)
	String decrease = getConfig(config, 'decrease', defaults.decrease)
	String keep = getConfig(config, 'keep', defaults.keep)
	String major = getConfig(config, 'major', defaults.major)
	String minor = getConfig(config, 'minor', defaults.minor)
	String patch = getConfig(config, 'patch', defaults.patch)
	Boolean colored = config.colored == false ? false : true

	if (depth < 1) depth = 1

	println "Using version format: " + expectedVersionFormat(depth, separator)

	def param = argsMap.params[0]

	if (!param) param = defaultCommand(depth, separator, keep, increase, label)

	if (depth == 3 && param in [major, minor, patch]) {

		switch (param) {
		    case major:
		        param = "${increase}${separator}0${separator}0"
		        break
		    case minor:
		        param = "${keep}${separator}${increase}${separator}0"
		        break
	        case patch:
		        param = "${keep}${separator}${keep}${separator}${increase}"
		        break
		}

		if (argsMap.params[1]) {

			param += "${label}${argsMap.params[1]}"

		}

	}

	boolean paramsOk = verifyParamFormat(depth, param, separator, increase, decrease, keep, label)

	if (!paramsOk) {
		System.err.println "Parameter is NOT in the expected format!"
		return
	}

	def appVersion = getAppVersion()

	if (appVersion) {
		println "Old version -> " + appVersion
	} else {
		System.err.println 'Could not get version!'
		return
	}

	boolean currentFormatOk = verifyVersionFormat(depth, appVersion, separator, increase, decrease, keep, label)

	if (!currentFormatOk) {
		System.err.println 'Current version does NOT comply with expectations!'
		return
	}

	def newVersion = calculateNewVersion(appVersion, param, separator, increase, decrease, keep, label)

	boolean newFormatOk = verifyVersionFormat(depth, newVersion, separator, increase, decrease, keep, label)

	if (!newVersion || !newFormatOk) {
		System.err.println 'Could not calculate new version!'
		return
	} else {
		if (colored) {
			println "New version -> ${(char)27}[32m" + newVersion + "${(char)27}[37;40m"
		} else {
			println "New version -> " + newVersion
		}
	}

	def versionChanged = setAppVersion(newVersion)

	if (versionChanged) {
		if (colored) {
			println "${(char)27}[32m| OK -${(char)27}[37m Version set successfully!"
		} else {
			println "| OK - Version set successfully!"
		}
	} else {
		System.err.println 'Could not set new version!'
	}

}

private ConfigObject loadConfig(String className) {
    def classLoader = Thread.currentThread().contextClassLoader
	classLoader.addURL(new File(classesDirPath).toURL())
    try {
		def parser = getBindingValueOrDefault('configParser', { name -> return new ConfigSlurper(GrailsUtil.environment).parse(classLoader.loadClass(className)) })
		return parser(className).versionUpdate
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

private String defaultCommand(depth, separator, keep, increase, label) {
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

private boolean verifyParamFormat(int depth, String v, String separator, String increase, String decrease, String keep, String label) {
	if (!v) return false
	if (v.endsWith(separator)) return false
	def part = ("[0-9A-Za-z$increase$decrease$keep]+$separator" * depth)[0..-2]
	def regex = /^$part($label[A-Za-z]+)?$/
	return v ==~ regex
}

private boolean verifyVersionFormat(int depth, String v, String separator, String increase, String decrease, String keep, String label) {
	if (!v) return false
	if (v.endsWith(separator)) return false
	def part = ("[0-9]+$separator" * depth)[0..-2]
	def regex = /^$part($label[A-Za-z0-9]+)?$/
	return v ==~ regex
}

private String getAppVersion() {

	def version

	try {

		def directory = new File("${basedir}/")

		if (directory?.isDirectory()) {

			def filePattern = ~/^.+GrailsPlugin.groovy$/

			def fileMatches = directory.list( [accept:{d, f-> f ==~ /^.+GrailsPlugin.groovy$/ }] as FilenameFilter)?.toList()

			if (fileMatches?.size() > 0) {

				def pluginDescriptor = "${basedir}/${fileMatches[0]}" as File

				def captured = ''

				pluginDescriptor.getText().find(/def version\s?=\s?[\"|\']?(.*)[\"|\']/) { match, capture -> captured = capture }

				if (captured) version = captured

			}

		}

		if (!version) {

			def applicationPropertiesFile = "${basedir}/application.properties" as File
			def applicationProperties = new Properties()
			applicationProperties.load(applicationPropertiesFile.newReader())
			version = applicationProperties['app.version']

		}

	} catch (Exception ex) {
		version = null
	}

	return version

}

private Boolean setAppVersion(version) {

	Boolean result

	try {

		def directory = new File("${basedir}/")

		if (directory?.isDirectory()) {

			def filePattern = ~/^.+GrailsPlugin.groovy$/

			def fileMatches = directory.list( [accept:{d, f-> f ==~ /^.+GrailsPlugin.groovy$/ }] as FilenameFilter)?.toList()

			if (fileMatches?.size() > 0) {

				def pluginDescriptor = "${basedir}/${fileMatches[0]}" as File

				String content = pluginDescriptor.getText()

				content = content.replaceFirst(~/def version\s?=\s?[\"|\']?(.*)[\"|\']/) { 
					"def version = \'${version}\'" 
				}

				pluginDescriptor.write(content)

				result = true

			}

		}

		if (!result) {

			def applicationPropertiesFile = "${basedir}/application.properties" as File
			def props = new Properties()
			props.load(new FileInputStream(applicationPropertiesFile))
			props['app.version'] = version.toString()
			props.store(new FileOutputStream(applicationPropertiesFile), 'Grails Metadata file')
			result = true

		}

	} catch (Exception ex) {
		result = false
	}

	return result

}

private String calculateNewVersion(String current, String change, String separator, String increase, String decrease, String keep, String label) {

	def result = ""

	try {

		def currentArray = current.tokenize(separator)
		def changeArray = change.tokenize(separator)

		def currentLabel

		def currentIdx = currentArray.last().lastIndexOf(label)

		if (currentIdx > -1) {

			currentLabel = currentArray.last()[(currentIdx + 1)..-1]

			currentArray = currentArray[0..-2] + currentArray.last()[0..(currentIdx - 1)]

		}

		def changeLabel

		def changedIdx = changeArray.last().lastIndexOf(label)

		if (changedIdx > -1) {

			changeLabel = changeArray.last()[(changedIdx + 1)..-1]

			changeArray = changeArray[0..-2] + changeArray.last()[0..(changedIdx - 1)]

		}

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

		if (changeLabel) result += label + changeLabel

	} catch (Exception ex) {

		result = null

	}

	return result

}