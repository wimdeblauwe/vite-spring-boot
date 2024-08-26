# Spring Boot library for Vite integration

This library allows to use [Vite](https://vitejs.dev/) as the frontend build tool to have live
reloading (Called Hot Module Replacement or HMR).

It must be used together with
the [vite-plugin-spring-boot](https://www.npmjs.com/package/@wim.deblauwe/vite-plugin-spring-boot)
on the frontend.

## Maven configuration

When using Thymeleaf:

```xml

<dependency>
    <groupId>io.github.wimdeblauwe</groupId>
    <artifactId>vite-spring-boot-thymeleaf</artifactId>
    <version>LATEST_VERSION_HERE</version>
</dependency>
```

Other templating engines are currently not supported, but you can use the classes in `vite-spring-boot` as those do not
depend on Thymeleaf to build support.
Use the following dependency in that case:

```xml

<dependency>
    <groupId>io.github.wimdeblauwe</groupId>
    <artifactId>vite-spring-boot</artifactId>
    <version>LATEST_VERSION_HERE</version>
</dependency>
```

## Usage

### Hot Module Reload client

Vite needs the HMR client rendered when running in DEV mode.
This library exposes the `<vite:client>` tag to conditionally render the HMR depending on the value of the `vite.mode`
application configuration setting.

If the mode is `DEV`, then the HMR client is rendered. If it is `BUILD` then nothing is rendered.

### Vite entry points

Any CSS or JavaScript that is used in the Spring Boot application needs to be added to the list of entrypoints. The
`<vite:vite>` and `<vite:entry>` tags can be used for this.

For example:

```html

<vite:vite>
   <vite:entry value="/css/application.css"></vite:entry>
</vite:vite>
```

When running in DEV mode, this will point to the HMR server of Vite so live reloading works.
When running in BUILD mode, the manifest JSON that Vite generates is read and the links to the compiled assets are
generated.

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License

[Apache 2.0](https://choosealicense.com/licenses/apache-2.0/)

## Release

To release a new version of the project, follow these steps:

1. Update `pom.xml` with the new version (Use `mvn versions:set -DgenerateBackupPoms=false -DnewVersion=<VERSION>`)
2. Commit the changes locally.
3. Tag the commit with the version (e.g. `1.0.0`) and push the tag.
4. Create a new release in GitHub via https://github.com/wimdeblauwe/vite-spring-boot/releases/new
    - Select the newly pushed tag
    - Update the release notes. This should automatically start
      the [release action](https://github.com/wimdeblauwe/vite-spring-boot/actions).
5. Update `pom.xml` again with the next `SNAPSHOT` version.
6. Close the milestone in the GitHub issue tracker.
