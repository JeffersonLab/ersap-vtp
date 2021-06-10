plugins {
    id 'java'
    id 'java-library'
}

group 'org.jlab.epsci.ersap.vtp'
version '1.0-SNAPSHOT'

sourceCompatibility = JavaVersion.VERSION_11
targetCompatibility = JavaVersion.VERSION_11

//create a single Jar with all dependencies
task fatJar(type: Jar) {
    baseName = project.name + '-all'
    from { configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
    with jar
}

repositories {
    maven {
        url 'https://clasweb.jlab.org/jhep/maven/'
    }
    maven {
        url 'https://clasweb.jlab.org/clas12maven/'
    }
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly 'org.jlab.coda:jclara:4.3-SNAPSHOT'
    compile 'org.json:json:20160810'
    compile fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'org.jetbrains:annotations:19.0.0'
    testCompile group: 'junit', name: 'junit', version: '4.12'
    implementation group: 'org.apache.commons', name: 'commons-pool2', version: '2.9.0'
    implementation group: 'com.google.protobuf', name: 'protobuf-java', version: '3.15.6'
//    implementation 'com.lmax:disruptor:3.4.0'
}


