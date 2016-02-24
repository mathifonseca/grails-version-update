Grails Plugin for Application or Plugin Version Update
=====================

[![Build Status](https://travis-ci.org/mathifonseca/grails-version-update.svg?branch=master)](https://travis-ci.org/mathifonseca/grails-version-update)

When you have a bunch of applications or plugins to maintain and each of them needs to update its version with every change, and you combine that with your useless memory... you end up following always the same process:

1. Make your changes
2. Start writing the `grails set-version` command
3. Forget which the current version was
4. Check the `application.properties` (or `*GrailsPlugin.groovy` for plugins) file to get the current version
5. Go back to your command and finish writing it
6. Execute the command

So, when you get tired of that, you create a plugin for doing a little less steps and your process becomes:

1. Make your changes
2. Execute the `grails version-update` command
3. Drink your coffee

[Magic](http://i.imgur.com/iZcUNxH.gif), right?

## Usage

`grails version-update $param $label`

The idea is that you write the least possible. So by convention, if you don't type any params, the plugin will increase the last level of your version by one.

In case you type a param, it can be the exact version you want or you can do some tricks using a simple notation. Lets imagine that your current version is 0.2.4.6 and you want to **keep** the 0, **increase** the 2, **decrease** the 4 and **change** the 6 to 1. The param you should type (for this extremely complicated version change) would be: `x.+.-.1`. Each char meaning, default values and configuration is explained below.

Lets think of some more common examples:

```
Old version:  0.2.4
Command:      grails version-update
New version:  0.2.5
```

```
Old version:  0.2.4
Command:      grails version-update x.+.x
New version:  0.3.4
```

```
Old version:  0.2.4
Command:      grails version-update +.+.0
New version:  1.3.0
```

```
Old version:  0.2.4
Command:      grails version-update 8.-.x
New version:  8.1.4
```

Also, starting from version 1.2.0 of this plugin, you can use shortcuts to increase major, minor or patch versions and reset or keep the other accordingly.

For example:

```
Old version:  1.2.4
Command:      grails version-update M
New version:  2.0.0
```

```
Old version:  1.2.4
Command:      grails version-update m
New version:  1.3.0
```

```
Old version:  1.2.4
Command:      grails version-update p
New version:  1.2.5
```

From version 1.5.0 of this plugin, if you specify a second param, it will be used as a label for the version. This is useful for tagging alpha, beta, snapshot, release candidates, etc. Anything you type will be appended to the version, except for `s` and `rc`, which are shortcuts for `SNAPSHOT` and `RC`.

```
Old version:  1.2.4
Command:      grails version-update p beta
New version:  1.2.5-beta
```

```
Old version:  1.2.4
Command:      grails version-update p s
New version:  1.2.5-SNAPSHOT
```

```
Old version:  1.2.4
Command:      grails version-update p rc
New version:  1.2.5-RC
```

## Configuration

The plugin is thought to be used with the standard version format taken from [Semantic Versioning 2.0.0](http://semver.org/) where, in summary, they define the format **X.Y.Z** as the standard. The plugin uses this as convention, but if you want to override it, you can do it by adding the following at the end of your `BuildConfig.groovy`

```groovy
grails.project.dependency.resolution = {
    repositories {
        ...
    }
    dependencies {
        ...
    }
    plugins {
        ...
    }
}

versionUpdate {

    depth = 3
    separator = '.'
    keep = 'x'
    increase = '+'
    decrease = '-'
    major = 'M'
    minor = 'm'
    patch = 'p'
    colored = true

}
```

Those values are also the defaults. That means that if you do not define any of them, the above values are going to be asumed.

Here is an explanation of each of them:

#### depth

This is the amount of levels that your version has. The default is 3, so the expected format is X.Y.Z but if you change it to 4, it will be W.X.Y.Z and so on.

#### separator

You also can change the separator for each level. The default is a simple dot, but if you want something like X-Y-Z, you should change this value.

#### keep

This char means that you want to maintain the version at that level as it is right now. For example, the command 'x.x.7' would only put a seven at the last level and keep the others unchanged.

#### increase

This is the char that means that you want to increase by one the version in the specified level. For example, the command 'x.x.+' will keep the first two levels as they are and increase the last one by one.

#### decrease

The opposite of the above, you use this char when you want to decrease your version at that level. Note that if it is already zero, it will stay in zero to avoid negative numbers.

#### major

This means that you want to increase your major version and reset minor and patch to zero. Only works when depth is 3.

#### minor

This means that you want to increase your minor version. It will keep major as it is and reset patch to zero. Only works when depth is 3.

#### patch

This means that you want to increase your patch version and keep major and minor untouched. Only works when depth is 3.

#### colored

By default, the output of the command will be colored to help you identify the new version. If you want to deactivate this, you can set this flag to false.

## Contact

If you have any questions or suggestions, you can contact me at <mathifonseca@gmail.com> or make any pull requests you want.

## Release Notes (excluding patches)

    0.0.1 > Initial version
    1.0.0 > It's official and published. Also upgraded to Grails 2.3.5 and improved performance.
    1.1.0 > Now working for updating plugin versions. Upgraded to Grails 2.3.8.
    1.2.0 > Added shortcuts for increasing major, minor and patch versions.
    1.3.0 > Added labels support (x.y.z-label).
    1.4.0 > Added colored output deactivation support.
    1.5.0 > Added shortcut for SNAPSHOT labels.
    1.6.0 > Added shortcut for RC labels.
