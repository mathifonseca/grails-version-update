Grails Plugin for Application or Plugin Version Update
=====================

When you have a bunch of applications or plugins to maintain and each of them needs to update its version with every change, and you combine that with your useless memory... you end up following always the same process:

1. Make your changes
2. Start writing the `grails set-version` command
3. Forget which the current version was
4. Check the `application.properties` file to get the current version
5. Go back to your command and finish writing it
6. Execute the command

So, when you get tired of that, you create a plugin for doing a little less steps and your process becomes:

1. Make your changes
2. Execute the `grails version-update` command
3. Drink your coffee

Magic, right?

## Usage

`grails version-update $param`

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

## Configuration

The plugin is thought to be used with the standard version format taken from [Semantic Versioning 2.0.0](http://semver.org/) where, in summary, they define the format **X.Y.Z** as the standard. The plugin uses this as convention, but if you want to configure it, you can do it by adding the following at the end of your `BuildConfig.groovy`

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

versionupdate {

    depth = 3
    separator = '.'
    keep = 'x'
    increase = '+'
    decrease = '-'

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

## Contact

If you have any questions or suggestions, you can contact me at <mathifonseca@gmail.com> or make any pull requests you want.

## Status

[![Build Status](https://drone.io/github.com/mathifonseca/grails-version-update/status.png)](https://drone.io/github.com/mathifonseca/grails-version-update/latest)

## Release Notes

    0.0.1 > Initial version
    1.0.0 > It's official and published. Also upgraded to Grails 2.3.5 and improved performance.
    1.1.0 > Now working for updating plugin versions. Upgraded to Grails 2.3.8.
    1.1.1 > Fixed bug in plugin version updating.

