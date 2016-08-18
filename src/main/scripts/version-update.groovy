description "Update application or plugin version", "grails version-update"

runVersionUpdate()

private void runVersionUpdate() {

    println 'Updating version...'

    Integer depth = 3
    String separator = '.'
    String label = '-'
    String increase = '+'
    String decrease = '-'
    String keep = 'x'
    String major = 'M'
    String minor = 'm'
    String patch = 'p'
    Boolean colored = false

    println "Using version format: " + expectedVersionFormat(depth, separator)

    def param = args ? args[0] : null

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

        def labelParam = args ? args[1] : null

        if (labelParam) {

            if (labelParam == 's') {

                param += "${label}SNAPSHOT"

            } else if (labelParam == 'rc') {

                param += "${label}RC"

            } else {

                param += "${label}${args[1]}"

            }

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

        def file = fileSystemInteraction.file('gradle.properties')

        if (file) {
            version = (file.text =~ /(?m)appVersion\s?=\s?(.*)/)[0][1]
        }

    } catch (Exception ex) {
        version = null
    }

    return version

}

private Boolean setAppVersion(version) {

    Boolean result

    try {

        def file = fileSystemInteraction.file('gradle.properties')
        
        if (file) {

            String content = file.text

            content = content.replaceFirst(~/appVersion\s?=\s?(.*)/) { 
                "appVersion=$version" 
            }

            file.write(content)

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