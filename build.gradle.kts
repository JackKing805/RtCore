import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    application
    `maven-publish`
}

val vv = "0.7.5"

group = "com.jerry"
version = vv

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    //协程
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")//todo remove
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}


publishing{
    publications{
        create("maven_public",MavenPublication::class){
            groupId = "com.jerry"
            artifactId = "RtCore"
            version = vv
            from(components.getByName("java"))
        }
    }
}
