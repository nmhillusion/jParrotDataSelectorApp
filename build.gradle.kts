plugins {
    id("java")
    id("application")
    id("distribution")
}

group = "tech.nmhillusion.jParrotDataSelectorApp"
version = "2025.2.0"

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
    implementation("org.yaml:snakeyaml:2.4")

    //// DATABASE SESSION FACTORY /////////////////////////

    // https://mvnrepository.com/artifact/org.hibernate.orm/hibernate-core
    implementation("org.hibernate.orm:hibernate-core:6.6.13.Final")

    // https://mvnrepository.com/artifact/com.zaxxer/HikariCP
    implementation("com.zaxxer:HikariCP:6.3.0")

    // https://mvnrepository.com/artifact/org.springframework/spring-jdbc
    implementation("org.springframework:spring-jdbc:6.2.6")

    // https://mvnrepository.com/artifact/org.springframework/spring-orm
    implementation("org.springframework:spring-orm:6.2.6")

    // https://mvnrepository.com/artifact/org.springframework/spring-context
    implementation("org.springframework:spring-context:6.2.6")

    // https://mvnrepository.com/artifact/org.hibernate.orm/hibernate-jcache
    implementation("org.hibernate.orm:hibernate-jcache:6.6.13.Final")

    // https://mvnrepository.com/artifact/org.ehcache/ehcache
    implementation("org.ehcache:ehcache:3.10.8")

    //// DATABASE DRIVERS ///////////////////

    // https://mvnrepository.com/artifact/com.mysql/mysql-connector-j
    implementation("com.mysql:mysql-connector-j:9.3.0")

    // https://mvnrepository.com/artifact/com.oracle.database.jdbc/ojdbc11
    implementation("com.oracle.database.jdbc:ojdbc11:23.8.0.25.04")

    // https://mvnrepository.com/artifact/com.microsoft.sqlserver/mssql-jdbc
    implementation("com.microsoft.sqlserver:mssql-jdbc:12.10.0.jre11")

    // EXCEL EXPORT //////////////////////
    // https://mvnrepository.com/artifact/org.apache.poi/poi
    implementation("org.apache.poi:poi:5.4.1")

    // https://mvnrepository.com/artifact/org.apache.poi/poi-ooxml
    implementation("org.apache.poi:poi-ooxml:5.4.1")

    /// TEST /////////////////////

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
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