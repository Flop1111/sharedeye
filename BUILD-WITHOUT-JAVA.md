# Getting the .jar without installing anything

Modrinth only accepts a `.jar`. A `.jar` does not exist until the source is compiled.
GitHub can compile it for you, on their machines, for free. You do not install Java, Gradle or
anything else.

You need a GitHub account (free) and about 10 minutes.

## 1. Make a repository

1. Go to https://github.com and sign in (or sign up).
2. Click the **+** in the top right -> **New repository**.
3. Name it `sharedeye`, leave everything else alone, click **Create repository**.

## 2. Upload the project

1. On the empty repository page click **uploading an existing file**.
2. Extract this zip somewhere first, then open the extracted folder.
3. Select everything inside it (`Ctrl+A`) - it must include the `src` folder, the `.github` folder,
   `build.gradle`, `settings.gradle` and `gradle.properties`.
4. Drag it all into the browser window, wait for the file list to appear, click **Commit changes**.

## 3. Wait for the build

1. Click the **Actions** tab at the top of the repository.
2. A run called **build** starts by itself. Click it.
3. Wait 3-5 minutes. A green check means it worked. A red X means it failed - open the failed step
   and copy the last 20 lines to me.

## 4. Download the jar

1. Still on that run's page, scroll to the bottom to **Artifacts**.
2. Click **sharedeye-jar**. Your browser downloads a ZIP.
3. Extract that ZIP. Inside is `sharedeye-0.2.0.jar` - that is the mod.

Yes, one more zip. GitHub always hands artifacts back as a zip; the jar is inside it.

## 5. Put it on Modrinth

1. https://modrinth.com -> dashboard -> your project -> **Create a version**.
2. Upload `sharedeye-0.2.0.jar`.
3. Version `0.2.0`, channel **Release**, loaders **Fabric**, game version **1.21.1**.
4. Add **Fabric API** as a required dependency, then submit.

## If you would rather build on your own PC

Install IntelliJ IDEA Community, open the extracted folder, wait for the Gradle sync, then use the
Gradle panel on the right: Tasks -> build -> build. The jar appears in `build/libs/`.
