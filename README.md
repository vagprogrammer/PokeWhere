# Linio Android App

Here is most of the information that you need to know in order to develop the Linio app for Android. Please read through this page and adapt your practices accordingly. If you want to cooperate, please [let us know](mailto:eduardo.garcia@linio.com,victor.aceves@linio.com).

Linio Android Devs

## Getting Started
	
### Prerequisites

We strongly recommend use Android Studio, which is the Official IDE for Android, it provides the fastest tools for building apps on every type of Android device.

#### Install Android Studio
Setting up Android Studio takes just a few clicks. (You should have already downloaded [Android Studio](https://developer.android.com/studio/index.html).)

- `Windows` To install Android Studio on Windows, proceed as follows:
	- Launch the .exe file you downloaded.
	- Follow the setup wizard to install Android Studio and any necessary SDK tools.

- `Mac` To install Android Studio on your Mac, proceed as follows:
	- Launch the Android Studio DMG file.
	- Drag and drop Android Studio into the Applications folder, then launch Android Studio.
	- Select whether you want to import previous Android Studio settings, then click OK.
	- The Android Studio Setup Wizard guides you though the rest of the setup, which includes downloading Android SDK components that are required for development.
	
- `Linux` To install Android Studio on Linux, proceed as follows:

	- Unpack the .zip file you downloaded to an appropriate location for your applications, such as within /usr/local/ for your user profile, or /opt/ for shared users.
	- To launch Android Studio, open a terminal, navigate to the android-studio/bin/ directory, and execute studio.sh.
	- Select whether you want to import previous Android Studio settings or not, then click OK.
	- The Android Studio Setup Wizard guides you though the rest of the setup, which includes downloading Android SDK components that are required for development.
	
### Clone and run 
	
To get started, just clone this repository and find the `android-app` folder. Inside this folder, you will find the `Linio` folder, that is described below: 

- `Linio` folder -  contains the Android Studio project; inside this folder you will find the source code for the app and several items, the most important being:

	- `app` folder -  contains the source code for the app.
	- `build` folder - contains build outputs.
    - `build.gradle (project)` file - defines our build configuration that apply to all modules. This file is integral to the project, so you should maintain them in revision control with all other source code. 

In order to start developing, you need to import the `Linio` project, via `File -> New -> Import Project`. As you do it, accept the IDE's suggestion to sync the build files, so that Gradle can download all dependencies. After syncing, you should be able to build all targets and run the app everywhere =D

## Project description

### API Level

- The minimum system API Level that this application requires in order to run is `27 (Android 5.0 Lollipop)`
	```
	minSdkVersion 21
	```
- The API Level on which the application is designed to run is `21 (Android 8.1 Oreo)`
	```
	targetSdkVersion 27
	```
	
#### Build variants 
The Linio project has two schemes, detailed below. Use them to switch between our current environments: Staging and Production.

 - `staging` points to our staging version to the Mobile API. 
 - `production` points to the production (Live) environment. 

Additionally, the flags `DEBUG` and `RELEASE` are defined for each environment, so you can have, for example, a `DEBUG` version that points to `PROD`, or a `RELEASE` version that points to `STAGE`.

Checking these flags in the code is easy:

    if(BuildConfig.DEBUG)
        System.out.println("Debug mode set")
    else
        System.out.println("Release mode set")
    endif

> **Note:**
> In order to switch variants, click on the Build Variants section button bottom left, and change the build variant accordingly, selecting from `satagingDebug`, `stagingRelease`, `productionRelease`, etc.

Also very important are the per-release source folders:

- `app/src/dev/...` for Dev
- `app/src/staging/...` for Staging
- `app/src/production/...` for Production

If you have constants, values, source code, etc. that needs to vary across environments, place your source file in these three folders. For example, if you're adding a `Constants` class whose attributes vary with the environment, then add a `Constant.java` file to each of the folders above. Android Studio will then choose the appropriate .java file to compile against, depending on the build variant selected.

Additionally, our `build.gradle` is configured like this:

    flavorDimensions "unique"

    productFlavors {
        production {
            applicationId "com.linio.android"
            dimension "unique"
            multiDexEnabled true
            buildConfigField("String", "TestCountry", '"' + GradleCountryCode + '"')
            resValue "string", "A4SpartnerId", '"xxxxxxxxxxxxxxxxxxxxxxx"'
            resValue "string", "A4SprivateKey", '"xxxxxxxxxxxxxxxxxxxxxxx"'
            resValue "string", "A4Slogging", '"true"'
        }

        staging {
            applicationId "com.linio.android.staging"
            dimension "unique"
            multiDexEnabled true
            buildConfigField("String", "TestCountry", '"' + GradleCountryCode + '"')
            resValue "string", "A4SpartnerId", '"xxxxxxxxxxxxxxxxxxxxxxx"'
            resValue "string", "A4SprivateKey", '"xxxxxxxxxxxxxxxxxxxxxxx"'
            resValue "string", "A4Slogging", '"true"'
        }

        dev {
            applicationId "com.linio.android.dev"
            dimension "unique"
            multiDexEnabled true
            buildConfigField("String", "TestCountry", '"' + GradleCountryCode + '"')
            resValue "string", "A4SpartnerId", '"xxxxxxxxxxxxxxxxxxxxxxx"'
            resValue "string", "A4SprivateKey", '"xxxxxxxxxxxxxxxxxxxxxxx"'
            resValue "string", "A4Slogging", '"true"'

        }
    }

Note that every environment has its own package name, so you can run builds of the Linio app for different environments simultaneously in your emulator or phone.

#### <i class="icon-file"></i> Targets 
Our app has two targets:

- `app`, which contains the app product. 
- `src/main/androidTest`, which contains the instrumented tests for the business logic classes.

>Note
>Instrumented tests are those that depend on the Android framework to run. If your test does not depend on Android at all (pure Java test), you can use the `Unit Test` test artifact in `Build Variants`.

When you choose `app` and press the `Play` button, Android Studio will run the `app` target on the device or emulator. To run tests, click the `Project` view on the left side, drill down to `src/AndroidTest`, right-click then `Run tests in androidTest`. Additionally, you can run just one test suite, or even individual test cases (!) by just using the same procedure.

----------
Code & Architecture
-------------
#### <i class="icon-file"></i> Adding new source files 
Before you add a new source file, check this list out:

There are three simple rules for adding new source code files to the project:

- If it's a class that does not depend on the environment information, just add it to the correct package in the `main` folder.
- If it's a class that depends on the environment information, add one source file per environment (see `Constants` for example).
- If adding a new test suite, it necessarily needs to go to `src/AndroidTest/java/com.linio.android` or under.

#### <i class="icon-file"></i> Source code organization 
We have a few packages already, and here's a description of some of them:

- `fragments` contains UI fragments that make up the layout of the app.
- `model` contains a few things:
	- Realm managers
	- Packages related to Mobile API calls: `category, search, auth, etc.`
	- `entities` group, which contains our core models: `CategoryModel, ProductModel, etc.`
- `objects` contains adapters and other data objects (e.g. `banners`). Data objects will gradually be moved to `model`.
- `views` is where all activities are, strangely.
- `androidTest` is the home of all test suites.	

#### <i class="icon-file"></i> Source code formatting
First, go ahead and read this: [Google Java Style guide](https://google.github.io/styleguide/javaguide.html). Now some extra comments by me:

 1. When declaring a class, use exactly one white space between the class and its first instance variable or constants. Use exactly one white space between the last method and the end of the class.
 2. Exactly one whitespace between methods of a class.
 3. Order of declarations within a class: 
	 1. Class itself.
	 2. Any static or class variables or constants.
	 3. Normal instance variables.
	 5. Constructors
	 6. Public (business) methods
	 7. Listeners
	 8. Private methods
 4. Clearly mark methods that implement interfaces.
 5. Inside methods, no need to add whitespace between the method declaration and the first variables. No need to add white space to the end of a method. 
	 1. Separate the declaration of variables from other code (similar to declaring a class).
	 2. Group related functionality with comments.
	 3. You never need to add more than one vertical whitespace (line).
 6. All indentation is K&R style. No opening braces for ANYTHING in a new line, please.
 7. For lambdas and inner classes, typically Android can do a better job than you do. Use `Code -> Auto-indent lines`. 

#### <i class="icon-file"></i> How to add a new API call
If you are adding a new API call, follow these steps. **They are not optional.**

 3. Create a new package if necessary. If not, go to next step.
 4. Create an `API interface`,  which is the way that endpoints are exposed by Retrofit to our application. Name should be `<group>APIService` RequestModel` to represent the JSON request that will be sent to the Mobile API. 
 5. Create a `RequestModel` to represent the JSON request that we'll send to the Mobile API. 
 4. Create a `ResponseModel` to represent the JSON response that we will receive from the API. Please note that many APIs respond with a list of models that can be reused (`CategoryModel`, `ProductModel` , etc.) so don't reinvent the wheel and use what we have. If you need a new model, think about subclassing it or talk to me first. **Also, always use the `Model` suffix.**
 5. Create an entry in the `RetrofitClient` constructor to instantiate and cache your API service, which is and should be long-lived throughout the application.
 6. Create a new `<yourcall>APIServiceTest` test in the `src/androidTest` folder.
 7. Write a shitload of tests to ensure the API returns what you want and handles errors, bad inputs, etc. well.
 8. Once the tests pass (in all different environments), you are free to use the API in your code.
> **Note:**

> See the `auth and search` packages are good examples of how to add new APIs. Copy them.

#### <i class="icon-file"></i> How to add a new Realm model
If you are adding a new Realm model, follow these steps. **They are not optional.**

 1. Create a `<yourclass>Model`, maybe one that can be used for both API responses and Realm. Compatibility between Retrofit and Realm can be tricky, so read this: [Realm with Retrofit](https://realm.io/docs/java/latest/#retrofit). If you need to serialize **from** a RealmModel **to** JSON for use with Retrofit, then read this: [Custom serializer](https://gist.github.com/cmelchior/ddac8efd018123a1e53a)
 2. Create a `<yourclass>RealmManager' to act as a façade to all your CRUD operations. 
 3. Add the CRUD methods to `<yourclass>RealmManager`.
 4. Done! 
 5. No, not really, You will need to add all the tests your new Model. Create a `<yourclass>RealmManagerTest` class in `src/AndroidTest`, test all your methods.
 6. Once your methods pass, you're free to use the new model in your code.
 
#### <i class="icon-file"></i> Common advice often overlooked

1. Self-commenting code is always preferred, but do add comments when you are writing some hard-to-understand piece of code.
2. Speaking of self-commenting code, `manageLargeFeature()` is not it. `refreshScreenWhenNotifiedByRealm()` is it.
2. Keep your classes small. It's preferred to have 500 classes of 10 lines than 1 class with 5k lines of code.
3. Keep your methods small. Each method should do just one thing. If you have problems repeating part of an operation, for example refreshing a part of the screen, then your method is too big. 
4. KISS, Don't Repeat Yourself, You ain't gonna need it: http://code.tutsplus.com/tutorials/3-key-software-principles-you-must-understand--net-25161
5. Understand and apply **Separation of concerns**. Your ViewController is not a place to be doing business logic, but presentation and view logic. Similarly, do not mess with View code from your models. When in doubt, remember that ViewControllers should be very small, which means you will have to break them down if they become bloated, normally with business logic that doesn't belong there.
5. Ask for advice when in doubt.

#### <i class="icon-file"></i> Android - Memory management

It's very possible to have memory leaks on Android. Here's how to avoid them and track them down: [Google I/O Talk: Memory Management in Android Apps](https://www.youtube.com/watch?v=_CruQY55HOk)
 
#### <i class="icon-file"></i> Android - Realm
- Unlike in iOS, **all Realms need to be explicitly closed on Android.** Every call to `Realm.getInstance()` needs to have a corresponding `Realm.close()`. 
- Don't manage Realm directly from your fragment or activity. Use your `<class>RealmManager` to do refined management (for example, of closing) and only call methods from it.
- Limit async transitions to expensive operations, like writing a lot of models at the same time. They complicate the code and can get out of hand real quick. Realm is fast enough to work well synchronously in the UI thread, and most of our needs are for small data sets. If you do need an async transaction, create a high-level method in your RealmManager to sort out all the details, and only call those high-level methods from UI controller classes.
- For Realm, you only really need to try/catch when creating a Realm for the first time in a thread. The other times it is assumed it will succeed.
- Still with Realm: understand that Results<Class> is a LIVE collection, which means if you delete one of its objects from the Realm, the collection is automatically updated. **No need to refresh it again**.
- Take a look at the [best practices here](https://realm.io/docs/java/latest/#best-practices).

#### <i class="icon-folder-open"></i> Testing

- Engine/model/non-UI/business classes all need to be tested. If you are writing a class that will do a calculation, access data, perform a call, etc. **You need to write tests for it.**
- Learn how to run tests in Android Studio by going to `src/AndroidTest` tab where test cases are listed. 
- UI testing will not be done automatically for now, since it's very time consuming and we usually end up trying to catch up with changes. We can revisit this later.
- Make sure you test ALL possible cases in your code. The objective of the test is to break your code, not to make it work. 
- No need to use mock objects for the most part. But be sure to clean up after your tests so that actual product data remains untouched. For example, SearchHistoryModelTest uses a test model, not the one actively being used by the application.
- **We have a dedicated QA person now. USE HIM.**

#### <i class="icon-folder-open"></i> Version control

- Use feature branches. Don't work in the `sprint-n` branches if you can avoid it. The usual approach is to create your feature branch, and if changes are correct, then merge to `sprint-n`.
- **RUN THE TESTS EVERY TIME BEFORE AND AFTER YOU MERGE**. This will help you figure out if something you did broke the build. 
- Right now you have direct access to committing to the branches. This will change soon, so learn how to use Pull Requests.
- Make many small commits rather than a big one. Ideally you should commit every single day (even multiple times), and pull request/merge to `sprint-n` when your feature is done. Super large commits are impossible to revert, and will cause us problems when features are changed or canceled.

