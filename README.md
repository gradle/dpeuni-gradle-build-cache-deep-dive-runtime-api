# DPE University Training

<p align="left">
<img width="10%" height="10%" src="https://user-images.githubusercontent.com/120980/174325546-8558160b-7f16-42cb-af0f-511849f22ebc.png">
</p>

## Gradle Build Cache Deep Dive - Runtime API Exercise

This is a hands-on exercise to go along with the
[Gradle Build Cache Deep Dive](https://dpeuniversity.gradle.com/app/courses/54469478-55ba-416d-9cef-3b5aa33dd2a5)
training module. In this exercise you will go over the following:

* Practice troubleshooting cache issues due to missing input and output declaration
* Get familiar with using the build cache runtime API

---
## Prerequisites

* Finished going through the build cache runtime API section in [Gradle Build Cache Deep Dive](https://dpeuniversity.gradle.com/app/courses/54469478-55ba-416d-9cef-3b5aa33dd2a5)

---
## Develocity Authentication

If you haven't already done so, you can authenticate with the training Develocity service by running:

```shell
./gradlew provisionGradleEnterpriseAccessKey
```

The output of the task will indicate a browser window will come up from which you can complete the authentication:

<p align="center">
<img width="75%" height="75%" src="https://github.com/gradle/build-tool-training-exercises/assets/120980/ccafa270-dbab-4c66-ba12-caabcd10399c">
</p>

Once the browser window comes up you can enter a title for the access key that will be created or go with the suggested title:

<p align="center">
<img width="75%" height="75%" src="https://github.com/gradle/build-tool-training-exercises/assets/120980/1aeef46a-2fb6-472a-8d87-82af31b20799">
</p>

Once confirmed you will see the following message and you can close the browser window and return to the editor:

<p align="center">
<img width="75%" height="75%" src="https://github.com/gradle/build-tool-training-exercises/assets/120980/1711c9db-814c-4df1-9d18-42fe5d1b82f8">
</p>

---
## Scenario

In `app/build.gradle.kts` there is a task called `countSrc`, which counts the
number of source files and puts the number in `app/build/src-stats/num-files.txt`.

<p align="center">
<img width="75%" height="75%" src="https://github.com/gradle/build-tool-training-exercises/assets/120980/6c80f0b5-3f77-4637-bcf6-59fc1ebda667">
</p>

The task makes use of the `CountSrcFiles` task type which is in `buildSrc/src/main/kotlin/CountSrcFiles.kt`. For this exercise we will assume the task type is provided by a third-party plugin and we cannot modify the source.

Both incremental build and build caching does not work with the `countSrc` task. You will debug why and use the build cache runtime API to address this.

---
## Steps

1. Open the Gradle project in this repository in an editor of your choice
2. Run `./gradlew :app:countSrc` task. You will notice the file `app/build/src-stats/num-files.txt` gets created which contains the count:

<p align="center">
<img width="75%" height="75%" src="https://github.com/gradle/build-tool-training-exercises/assets/120980/06f0e972-c5fd-41c0-819b-912d3461cee6">
</p>

3. Run the task again. You will notice the task is not `UP-TO-DATE`:

<p align="center">
<img width="75%" height="75%" src="https://github.com/gradle/build-tool-training-exercises/assets/120980/9c570ddd-98a8-4c0c-8e26-d3da502c46e4">
</p>

4. Create a build scan, `./gradlew :app:countSrc --scan` , and look at the details in the timeline. Notice the message that indicates that no outputs were declared:

<p align="center">
<img width="75%" height="75%" src="https://github.com/gradle/build-tool-training-exercises/assets/120980/ec165a2a-a513-45f8-80c1-981685fd36e4">
</p>

5. Open the task type in `buildSrc/src/main/kotlin/CountSrcFiles.kt`. We can see from the task type implementation, as well as the output observed earlier, we need to declare `app/build/src-stats` as an output directory. We could also be more specific and declare `app/build/src-stats/num-files.txt`, however in the future the task type may create additional files, so it's more future-proof to specify the directory.

6. Use the [build cache runtime API documentation](https://docs.gradle.org/current/userguide/build_cache.html#using_the_runtime_api) to declare the output directory, call the property `outputDir`.

7. Run the task twice, it should now be `UP-TO-DATE`:

<p align="center">
<img width="75%" height="75%" src="https://github.com/gradle/build-tool-training-exercises/assets/120980/4b3369e7-fdd3-4f41-b83b-d01ff578a59e">
</p>

8. Now let's check if the task is working with the build cache. Do a clean, then run the task. We expect the `FROM-CACHE` outcome label, but instead there is no outcome label:

<p align="center">
<img width="75%" height="75%" src="https://github.com/gradle/build-tool-training-exercises/assets/120980/9c570ddd-98a8-4c0c-8e26-d3da502c46e4">
</p>

9. Create a build scan and look at the task details in the timeline. Notice the message that indicates that build caching is not enabled for the task:

<p align="center">
<img width="75%" height="75%" src="https://github.com/gradle/build-tool-training-exercises/assets/120980/fa38169c-275b-4cc9-a42e-3899745f5785">
</p>

10. Use the [build cache runtime API documentation](https://docs.gradle.org/current/userguide/build_cache.html#using_the_runtime_api) to enable build caching for the task.

11. Now do a clean, run the task. This should populate the cache. Do another clean and run the task again. You should see the `FROM-CACHE` outcome label:

<p align="center">
<img width="75%" height="75%" src="https://github.com/gradle/build-tool-training-exercises/assets/120980/fc192fd7-bdda-411f-a86b-cc3b0a42cd14">
</p>

12. Now let's check the caching behavior. Do a clean first.

13. Now add a new file under `app/src` anywhere. There are now 3 files under `app/src`, one `App.java`, one `AppTest.java`, and the new file.

14. Run the task. Check the `app/build/src-stats/num-files.txt` file, you will see the value `2` still. Also there should have been no outcome label as the action should have executed, however we see the `FROM-CACHE` outcome label.

<p align="center">
<img width="75%" height="75%" src="https://github.com/gradle/build-tool-training-exercises/assets/120980/fc192fd7-bdda-411f-a86b-cc3b0a42cd14">
</p>

15. We need to declare the `app/src` directory as an input directory. Refer to the documentation to do this. **Don't forget to specify the path sensitivity**.

16. Now do a clean and run the task. There should be no outcome label and the value in the output file should be correct.

---
## Solution Reference

If you get stuck you can refer to the `solution` branch of this repository.