# Aftermath

## Information 
[Aftermath](https://developer.nvidia.com/nsight-aftermath) is a GPU Debugging utility created by NVIDIA with NSight integration. It currently supports Vulkan and D3D12.
This library is a Java library intended to be used within a LWJGL project as a way to debug GPU crashes when using Vulkan. While this can be used outside a LWJGL project, it uses some LWJGL api, and the core module will need to be included.

## Use

### Gradle
Add ``implementation "com.oroarmor:aftermath:${aftermath_version}`` to the dependencies section of your ``build.gradle`` file.

If using the Kotlin DSL, ``implementation("com.oroarmor:aftermath:${aftermath_version})`` to the dependencies section of your ``build.gradle.kts`` file will work.


## Building
1. Clone the repository (https://github.com/Blaze4D-MC/Aftermath.git)
2. Run ``gradlew build`` in the project folder.

## Contributing
1. Clone the Repository
2. Add the features
3. Create a pull request with a detailed description

### IntelliJ Idea
1. Open IntelliJ IDEA
2. Press Open
3. Choose the ``build.gradle.kts`` file and open it as a project
