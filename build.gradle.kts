plugins {
    id("java")
    id("application")
    id("distribution")
    id("org.owasp.dependencycheck") version "13.0.0"
}

group = "tech.nmhillusion.jParrotDataSelectorApp"
version = "2025.3.1"

var appNameL = "jParrotDataSelectorApp"
var mainClassL = "$group.Main"


repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    // https://jitpack.io/#nmhillusion/neon-di
    implementation("com.github.nmhillusion:neon-di:2024.5.5")
    // https://jitpack.io/#nmhillusion/n2mix-java
    implementation("com.github.nmhillusion:n2mix-java:2025.5.12")
    // https://mvnrepository.com/artifact/org.yaml/snakeyaml
    implementation("org.yaml:snakeyaml:2.5")

    //// Mark: DATABASE SESSION FACTORY /////////////////////////

    // https://mvnrepository.com/artifact/org.hibernate.orm/hibernate-core
    implementation("org.hibernate.orm:hibernate-core:7.1.11.Final")

    // https://mvnrepository.com/artifact/org.hibernate.orm/hibernate-jcache
    implementation("org.hibernate.orm:hibernate-jcache:7.1.11.Final")

    // https://mvnrepository.com/artifact/com.zaxxer/HikariCP
    implementation("com.zaxxer:HikariCP:7.0.2")

    // https://mvnrepository.com/artifact/org.springframework/spring-jdbc
    implementation("org.springframework:spring-jdbc:6.2.14")

    // https://mvnrepository.com/artifact/org.springframework/spring-orm
    implementation("org.springframework:spring-orm:6.2.14")

    // https://mvnrepository.com/artifact/org.springframework/spring-context
    implementation("org.springframework:spring-context:7.1.0-M2")

    // https://mvnrepository.com/artifact/org.ehcache/ehcache
    implementation("org.ehcache:ehcache:3.11.1")

    //// Mark: DATABASE DRIVERS ///////////////////

    // https://mvnrepository.com/artifact/org.apache.calcite/calcite-core
    implementation("org.apache.calcite:calcite-core:1.42.0")

    // https://mvnrepository.com/artifact/com.mysql/mysql-connector-j
    implementation("com.mysql:mysql-connector-j:9.6.0")

    // https://mvnrepository.com/artifact/com.oracle.database.jdbc/ojdbc11
    implementation("com.oracle.database.jdbc:ojdbc11:23.26.0.0.0")

    constraints {
        implementation("net.minidev:json-smart:2.5.2") {
            because("CVE-2024-57699: High-severity Denial of Service vulnerability via stack exhaustion.")
        }
        implementation("io.airlift:aircompressor:2.0.3") {
            because("CVE-2025-67721: High-severity Information Leak vulnerability in Snappy/LZ4 buffers.")
        }
    }

    // https://mvnrepository.com/artifact/com.microsoft.sqlserver/mssql-jdbc
    implementation("com.microsoft.sqlserver:mssql-jdbc:13.2.1.jre11")

    // EXCEL EXPORT //////////////////////
    // https://mvnrepository.com/artifact/org.apache.poi/poi
    implementation("org.apache.poi:poi:5.5.1")

    // https://mvnrepository.com/artifact/org.apache.poi/poi-ooxml
    implementation("org.apache.poi:poi-ooxml:5.5.1")

    /// Mark: TEST /////////////////////

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

dependencyCheck {
    failBuildOnCVSS = 8f
    nvd.apiKey = "4fcade57-8220-41ef-81df-bbd737a596ab"
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from("src/main/resources").exclude(
        "output",
        "icon",
        "config"
    )

    manifest {
        attributes["Main-Class"] = mainClassL // Optional: if you need an executable jar
    }
    // You might configure the base archive name, version, etc. here if needed
    // archiveBaseName.set("my-app")
    // archiveVersion.set("1.0.0")
}

tasks.distZip {
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

val generateVersionProperties by tasks.registering {
    val propertiesFile = file("src/main/resources/config/app-info.yml")
    outputs.file(propertiesFile)

    doLast {
        propertiesFile.parentFile.mkdirs()
        propertiesFile.writeText("info:\n  name: ${appNameL}\n  version: ${project.version}\n")
    }
}

tasks.named("processResources") {
    dependsOn(generateVersionProperties)
}

application {
    mainClass = mainClassL
    applicationName = appNameL
}

distributions {
    main {
        distributionBaseName = appNameL
        contents {
            from("src/main/resources")
        }
    }
}